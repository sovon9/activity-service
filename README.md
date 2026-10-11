# activity-service

Owns `Activity` data for the MES platform: what happened on the shop floor, its status/type, and which
production unit and process order it belongs to. Exposes that data over **GraphQL** (the primary API, consumed
by `fusion-gateway-service` as a federated subgraph) and a smaller, older **REST** API.

## Stack

- Java 17, Spring Boot 3.5.7, Maven (`mvnw`)
- `spring-boot-starter-graphql` (GraphQL over HTTP, embedded GraphiQL)
- `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server` (JWT validation)
- Spring Data JPA + MySQL
- Spring Cloud Config (`spring.config.import=optional:configserver:...`) + Eureka client — both optional/best-effort at startup
- Spring Kafka (dependency present; no producer/consumer code in this service yet)
- JaCoCo (70% instruction-coverage gate on `verify`) + PMD (`verify`) — `mvn verify` enforces both

## Running locally

| Dependency | Default | Required? |
|---|---|---|
| MySQL | `localhost:3308/dev` | Yes — JPA needs it at startup |
| Config server | `localhost:8763` | No — `fail-fast=false`, falls back to local `application.properties` |
| Eureka | `localhost:8761` | No — `spring.cloud.discovery.enabled=false` |
| Auth server (JWKS) | `localhost:9000` | Yes, for any authenticated request — see **Security** |

```bash
./mvnw spring-boot:run     # runs on :8080
```

## API surface

Two separate, overlapping APIs exist on this service — know which one you're looking at:

| | Path | Controller | Status |
|---|---|---|---|
| GraphQL | `POST /graphql`, GraphiQL IDE at `GET /graphiql` | `ActivitiesGraphqlController` | **Primary** — this is what the gateway federates |
| REST | `/activity/v1/activities/**` | `ActivityController` → `ActivityService` | Older, not used by the gateway; kept for direct/legacy callers |

This README focuses on the GraphQL side, since that's where the interesting query-building and pagination code lives.

## Security

- This service is an OAuth2 **resource server**: `SecurityConfig` requires a valid JWT on everything except `GET /graphql` and `GET /graphiql` (`SecurityConfig.java:31-34`) — note only `GET` is exempted; the actual query-executing `POST /graphql` requires authentication.
- JWT signature is checked via `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9000/oauth2/jwks` (`application.properties:37`). This validates the **signature only** — it does not check the `iss` claim. If cross-service issuer validation matters here, switch to `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000` instead, which also does OIDC discovery instead of a hand-typed path.
- **`@PreAuthorize` on the `activities` resolver is currently commented out and empty** (`ActivitiesGraphqlController.java:43`: `//@PreAuthorize("")`). Today this service only checks "is there a valid JWT" — it does **not** check any scope/authority (e.g. `SCOPE_activity.read`). Don't assume scope enforcement is live here until that line is uncommented and filled in, and `@EnableMethodSecurity` is added (it isn't present anywhere in `config/` yet — without it, `@PreAuthorize` is silently ignored even if uncommented).
- The caller's bearer token is expected to arrive via `fusion-gateway-service`'s header propagation (`Authorization` forwarded from the original MCP/client request) — this service doesn't itself read or validate who the gateway is forwarding on behalf of beyond the JWT itself.

## Data model

`Activity` (table `activity`) is the root entity:

| Field | Type | Notes |
|---|---|---|
| `activityId` | `Long` (PK, identity) | Mapped from column `id` |
| `status` | `ActivityStatus` | `@ManyToOne(LAZY)`, FK `activity_status_id` |
| `type` | `ActivityType` | `@ManyToOne(LAZY)`, FK `activity_type_id` |
| `productionUnit` | `ProductionUnit` | `@ManyToOne(LAZY)`, FK `production_unit_id` — but see note below |
| `title`, `createdAt` | `String`, `LocalDateTime` | plain columns |
| `processOrderId` | `Long` | a raw FK column with **no** `@ManyToOne`/join — just an id, not a navigable relationship |

`Activity implements Node` (`entities/Node.java`) — a GraphQL Relay convention (a type with a global `id: ID!`).
`@PostLoad` computes that id via `GlobalUtil.toGlobalId("Activity", activityId)` — **this is plain
`Base64("Activity:" + activityId)`, not encryption or an opaque/secure token** — anyone can decode it
(`GlobalUtil.fromGlobalId`) and get the raw numeric id back. It exists purely so Relay-style GraphQL clients have
a stable, type-tagged cursor/id string, not for access control.

`ProductionUnit` here is a local JPA-mapped stub (just `productionUnitId`) used for the SQL join, separate from
the richer `ProductionUnit` type the GraphQL schema exposes via the `@lookup` directive (see schema section) —
the gateway resolves the rest of that type's fields from `product-service`, not from here.

## GraphQL schema shape (`resources/graphql/schema.graphqls`)

```graphql
activities(where: ActivityFilter, first: Int, after: String, last: Int, before: String, order: ActivityOrder)
  : ActivityConnection
```

This is a **Relay-style Connection**: `ActivityConnection { edges { node, cursor }, pageInfo }`. The
`first`/`after`/`last`/`before` arguments are the standard Relay cursor-pagination contract — Spring for GraphQL
turns these into the `ScrollSubrange` parameter the resolver actually receives (see below). `productionUnit` is
marked `@lookup` — a federation directive meaning the gateway fetches that field from a different subgraph
(`product-service`) and stitches it onto this service's `Activity` node; this service only needs to return
`productionUnitId` for the gateway to make that connection.

## The `activities` resolver, in detail

`ActivitiesGraphqlController.activities(...)` (`:44-69`) is the one method worth understanding carefully — it
combines three different Spring Data JPA/GraphQL mechanisms that don't commonly all appear together. If you
haven't used `Specification`, `ScrollPosition`, or the fluent `findBy`, read this section before touching this
method.

### 1. Cursor pagination: `ScrollSubrange` → `ScrollPosition` → `Window<T>`

Spring for GraphQL auto-populates the `ScrollSubrange subrange` parameter from whichever Relay pagination
arguments the client sent (`first`/`after` or `last`/`before`). From it:

- `subrange.position()` — the cursor to resume from. Decoded into a `ScrollPosition`, which is either a plain
  numeric offset (`ScrollPosition.offset()`, the default used when the client sent no cursor — `:51`) or a
  keyset cursor built from the actual sort-key values of the last row the client saw (more stable than offset
  pagination under concurrent writes, and what a real `after:` cursor from a Relay client decodes into).
- `subrange.count()` — page size, defaulted to `10` (`:52`).
- `subrange.forward()` — `false` when the client paged backwards (`last`/`before`). When that happens, the sort
  direction is flipped (`:56-59`) so the query fetches from the *end* of the result set, then `QueryBuilderUtil.buildSort`
  inverts each per-field direction a second time (see below) to compensate.

The resolver returns `Window<Activity>` rather than `List<Activity>` or `Page<Activity>` — `Window` carries the
rows *plus* scrolling metadata (`hasNext()`, a cursor for each row). Spring for GraphQL specifically recognizes
a `@QueryMapping` method that takes `ScrollSubrange` and returns `Window<T>`, and automatically converts it into
the `ActivityConnection`/`edges`/`pageInfo` shape the schema declares — none of that mapping is written by hand
here.

### 2. Dynamic filtering: `Specification`

`QueryBuilderUtil.buildSpecification(where)` turns the GraphQL `where: ActivityFilter` argument (an arbitrary
`Map<String,Object>`) into a JPA `Specification<Activity>` — a composable `WHERE` clause built in Java via the
Criteria API, used here because the filter shape (arbitrary fields, nested `and`/`or`, nested object filters like
`status: { activityStatusDesc: "OPEN" }`) can't be expressed as a single derived-query method name, and isn't
worth hand-writing JPQL for. See `buildPredicates` (`QueryBuilderUtil.java:76-141`) for the actual field-by-field
logic — notably:
- `and`/`or` keys recurse into nested filter groups.
- `id`/`activityId` accepts either a raw numeric id or a Relay global id string (decoded via `GlobalUtil.fromGlobalId`).
- Any other scalar key is matched as a plain `root.get(key) = value` — so a new scalar field on `Activity` is
  automatically filterable with no code change, but a typo'd filter key also silently does nothing (it isn't in
  `Activity`'s properties, so this would actually throw at query-build time — not silently ignored — if the key
  doesn't match a real JPA attribute).
- `buildSpecification` returns `null` when `where` is empty/absent — this `null` is exactly what the resolver
  branches on next.

### 3. Two different `findBy` overloads, picked based on whether a filter was given

```java
if (spec == null) {
    return activityRepository.findBy(scrollPosition, Limit.of(limit), sort);           // (A) no filter
}
return activityRepository.findBy(spec, q -> q.limit(limit).sortBy(sort).scroll(scrollPosition)); // (B) filtered
```

**(A) `Window<Activity> findBy(ScrollPosition, Limit, Sort)`** — declared explicitly in `ActivityRepository.java:12`.
This is *not* the familiar `findBy<PropertyName>` derived-query pattern. The part of the method name after `find`
is just `By` with **no property tokens at all** — in Spring Data's method-name grammar that means "no `WHERE`
clause, return everything," and the parameter types are read specially regardless of name/order: `Sort` controls
ordering, `Limit` (a value type, `Limit.of(n)`/`Limit.unlimited()`) caps row count, and `ScrollPosition` is what
makes Spring Data hand back a `Window<T>` instead of a plain `List<T>`. Spring Data generates the actual query at
startup — there's no method body to write.

**(B) `<S extends T, R> R findBy(Specification<T>, Function<FetchableFluentQuery<S>, R>)`** — not declared
anywhere in this codebase; it's inherited from `JpaSpecificationExecutor<Activity>` in `ActivityRepository`'s
`extends` clause. This is the **fluent query API**: `spec` supplies the dynamic `WHERE` clause from step 2, and
the lambda receives a builder (`q`) already filtered by it. Inside the lambda you chain configuration
(`.limit(...)`, `.sortBy(...)`) and end with exactly one **terminal** call that both executes the query and
decides the return type — `.all()` → `List<T>`, `.first()` → `Optional<T>`, `.stream()` → `Stream<T>`,
`.count()` → `long`, or here `.scroll(ScrollPosition)` → `Window<T>`, matching (A)'s return shape.

**Why branch on this at all**, rather than always going through (B): `Specification` is the mechanism for a
dynamic filter; when there's no filter, going straight to (A) skips the Criteria-API machinery entirely for the
common "just give me a page" case. Both converge on the same `Window<Activity>`, so the caller (Spring for
GraphQL's Connection-conversion logic) doesn't need to know which path was taken.

**This pattern isn't GraphQL-specific.** `Specification` + fluent `findBy` is plain Spring Data JPA — usable in
any Spring Boot app (a REST controller, a batch job) for building a dynamic "search with optional filters" query.
`Window`/`ScrollPosition`/`Limit`/`Sort` are also generic Spring Data types, usable outside GraphQL too. The only
piece that's actually tied to GraphQL here is `ScrollSubrange` (the argument type) and the automatic
`Window<T>` → Relay `Connection` JSON conversion — a REST endpoint returning `Window<T>` would need to unpack
`.content()`/`.hasNext()` by hand, which is why REST APIs more commonly reach for `Pageable`/`Page<T>` instead.

### Sort direction inversion (`QueryBuilderUtil.buildSort`)

Worth calling out separately since it's easy to misread: `buildSort` inverts the requested direction a **second**
time whenever `defaultDirection == DESC` (`:34-38`, `:46-50`) — i.e. whenever the resolver is paging backwards
(`last`/`before`). That's deliberate, not a bug: to fetch the *last* N rows in ascending order, the DB query has
to run in descending order and then the results get handed back in the Connection's natural order. If you touch
this method, keep the two inversions (direction flip at the resolver, re-inversion per field here) in sync — it's
easy to "fix" one side and silently break backward pagination.

## N+1 avoidance

`status` and `type` are resolved via `@BatchMapping` (`:71-113`), not a per-row lazy-load — Spring for GraphQL
collects every `Activity` in the current page first, then these methods batch-fetch all needed `ActivityStatus`/
`ActivityType` rows in one `findAllById` call each, avoiding one query per row. **`productionUnit` has no
equivalent `@BatchMapping`** in this service — it's resolved entirely by the gateway via the `@lookup` directive
instead, so there's no N+1 risk to fix here for that field; just don't be surprised not to find a batch method
for it alongside `status`/`type`.

## Known gaps / gotchas

- `@PreAuthorize` on `activities` is commented out and empty — see **Security** above.
- `GlobalUtil`'s "global id" is Base64, not a secret — don't rely on it hiding or protecting the numeric id.
- `processOrderId` has no `@ManyToOne` — if you need the related entity, you're querying `process-order-service`
  separately (or via the gateway), not through a JPA association here.
- `jwk-set-uri` (not `issuer-uri`) means no `iss` validation on incoming tokens — see **Security**.
- The REST API (`ActivityController`) and the GraphQL API maintain **separate** read paths against the same
  table (`ActivityService` for REST vs. the repository calls above for GraphQL) — a change to filtering/paging
  behavior on one side doesn't automatically apply to the other.
