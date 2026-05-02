# QueryBuilderUtil: Deep Dive Documentation

This document explains how `QueryBuilderUtil` works, focusing on both **Sorting (`buildSort`)** and **Filtering (`buildSpecification` / `buildPredicates`)**, as well as how `JpaSpecificationExecutor` integrates with Spring for GraphQL. This pattern can be easily adapted for other services (e.g., `ProductService`, `ProcessOrderService`).

---

## 1. Why does this look so complex? (You are not alone!)

If you are looking at `CriteriaBuilder`, `Root`, and `Predicate` and thinking, *"Wow, this is really hard to remember and looks overly complex,"* **you are 100% correct, and you are not missing any knowledge.**

The **JPA Criteria API** (which Spring Data `Specification` uses under the hood) is notorious in the Java community for being incredibly verbose and difficult to read. 

*   **Why does it exist?** It was created to provide a **type-safe** way to build SQL queries. Instead of writing a string like `"SELECT * FROM User WHERE age > " + age`, which can cause runtime errors or SQL injection, Criteria API forces you to use Java objects (`cb.greaterThan(root.get("age"), age)`). If you misspell a column name, it fails safely.
*   **Why use it here?** GraphQL is entirely dynamic. The user can ask for any combination of filters (`AND`, `OR`, nested objects). The Criteria API is the only built-in Java standard that allows us to construct a `WHERE` clause piece-by-piece at runtime based on an unpredictable JSON/Map structure.

**The Good News:** You don't need to memorize the Criteria API. This `QueryBuilderUtil` is a "Write Once, Use Everywhere" utility. The pattern written here handles 99% of all GraphQL filtering needs. You just copy this file to your new service and change the word `Activity` to `Product`.

---

## 2. JpaSpecificationExecutor & The Fluent Query API

In standard Spring Data JPA, your repository extends `JpaRepository<Activity, Long>`. This gives you basic methods like `findById`, `findAll`, and `save`.

To run the dynamic `Specification` queries we build, your repository must also extend **`JpaSpecificationExecutor<Activity>`**.

```java
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> { }
```

### What does JpaSpecificationExecutor do?
It adds methods to your repository that accept a `Specification` object. This tells Spring Data: *"Hey, instead of looking at my method name to guess the query, use this dynamic Criteria Query I built."*

### The Controller Execution (The Fluent API)
In the controller, we execute the query like this:

```java
return activityRepository.findBy(spec, q -> q.limit(limit).sortBy(sort).scroll(scrollPosition));
```

This is called the **Fluent Query API** (introduced in Spring Data 3.0). It solves a massive problem: How do you combine a dynamic `Specification` (the `WHERE` clause) with GraphQL Relay Pagination (the `LIMIT`, `ORDER BY`, and `CURSOR`)?

*   **`spec`**: The first argument is our `Specification` containing all the dynamic `WHERE` logic built by `QueryBuilderUtil`.
*   **`q -> ...`**: The second argument is a Lambda function providing a `FluentQuery` builder. 
    *   **`q.limit(limit)`**: Tells the database to only return a certain number of rows (e.g., `LIMIT 10`).
    *   **`q.sortBy(sort)`**: Applies the `Sort` object we built, creating the SQL `ORDER BY` clause. (Crucial for Keyset/Relay pagination).
    *   **`q.scroll(scrollPosition)`**: Applies the Relay cursor. If we are using `.offset()`, it creates the SQL `OFFSET` clause.

Spring Data takes all these pieces—the dynamic `WHERE`, the `LIMIT`, the `ORDER BY`, and the `OFFSET`—and safely combines them into one highly optimized SQL query!

---

## 3. Sorting: How `buildSort` works

Sorting in Spring Data is much simpler than filtering. We just need to convert the GraphQL `order: { ... }` map into a Spring Data `Sort` object.

```java
public static Sort buildSort(Map<String, Object> order, String defaultField, Sort.Direction defaultDirection) {
    // 1. Fallback: If the user didn't provide an 'order' argument, sort by the default (e.g., activityId ASC)
    if (order == null || order.isEmpty()) {
        return Sort.by(defaultDirection, defaultField);
    }

    List<Sort.Order> orders = new ArrayList<>();
    
    // 2. Loop through the requested sorts (e.g., { title: "DESC", status: { activityStatusDesc: "ASC" } })
    for (Map.Entry<String, Object> entry : order.entrySet()) {
        String field = entry.getKey();
        Object value = entry.getValue();
        
        // 3. Handle Nested Sorting (e.g., sorting by the status description)
        if(value instanceof Map) {
            Map<String, Object> nestedOrder = (Map<String, Object>) value;
            for(Map.Entry<String, Object> nestedEntry : nestedOrder.entrySet()) {
                 // Spring Data allows sorting joined tables using dot notation: "status.activityStatusDesc"
                 String nestedField = field + "." + nestedEntry.getKey();
                 String directionStr = String.valueOf(nestedEntry.getValue());
                 
                 Sort.Direction direction = "DESC".equalsIgnoreCase(directionStr) ? Sort.Direction.DESC : Sort.Direction.ASC;
                 orders.add(new Sort.Order(direction, nestedField));
            }
        } 
        // 4. Handle Simple Sorting (e.g., sorting by title)
        else {
            String directionStr = String.valueOf(value);
            Sort.Direction direction = "DESC".equalsIgnoreCase(directionStr) ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, field));
        }
    }

    // 5. Combine all the Sort.Order objects into one final Sort object and return it.
    return Sort.by(orders);
}
```

---

## 4. Filtering: The Goal of a Specification

In Spring Data JPA, `Specification<T>` is an interface that allows you to construct the `WHERE` clause dynamically.

The `Specification<T>` interface has exactly **one** method that you must implement:
```java
Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
```

Because it only has one method, it is a **Functional Interface**. This means instead of writing a whole class, you just write a **Lambda function**:
```java
return (root, query, criteriaBuilder) -> { /* build predicate */ return null; };
```

### Understanding the Lambda Parameters

*   **`Root<T> root`**: Represents the `FROM` clause (the `activity` table). Use `root.get("title")` to point to the `title` column. Use `root.join("status")` to join the `activity_status` table.
*   **`CriteriaBuilder cb`**: The factory to build SQL operators (`=`, `>`, `<`, `AND`, `OR`). Use `cb.equal(column, value)` to create `column = value`.
*   **`CriteriaQuery<?> query`**: Represents the whole query (useful for `GROUP BY` or subqueries, but we ignore it here).

---

## 5. Step-by-Step Flow of `buildSpecification`

```java
public static Specification<Activity> buildSpecification(Map<String, Object> filter) {
    // 1. Safety Check: If no filters, return null. Spring Data will run a basic SELECT *.
    if (filter == null || filter.isEmpty()) {
        return null;
    }
    
    // 2. The Lambda: Implementing the toPredicate method.
    return (root, query, criteriaBuilder) -> {
        
        // 3. Delegate to buildPredicates to loop through the Map and build the conditions.
        List<Predicate> predicates = buildPredicates(filter, root, criteriaBuilder);
        
        if (predicates.isEmpty()) {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
        }

        // 4. Final Assembly: Wrap all conditions in a giant SQL `AND`.
        // Example: (status = 'OPEN') AND (title = 'Test')
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
}
```

---

## 6. Step-by-Step Flow of `buildPredicates`

This method loops through the `Map<String, Object>` from GraphQL.

Imagine the client sends:
```json
{
  "title": "Inspection",
  "status": { "activityStatusDesc": "OPEN" },
  "or": [ { "activityTypeId": 1 }, { "activityTypeId": 2 } ]
}
```

### Case 1: Simple Fields (`title: "Inspection"`)
It falls to the final `else` block:
```java
// predicates.add(cb.equal(root.get(key), value));
```
* **SQL:** `title = 'Inspection'`

### Case 2: Nested Objects (`status: { activityStatusDesc: "OPEN" }`)
It hits `else if (value instanceof Map)`:
```java
// Map<String, Object> nestedMap = (Map<String, Object>) value;
// for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
//    predicates.add(cb.equal(root.join(key).get(nestedEntry.getKey()), nestedEntry.getValue()));
// }
```
* **SQL:** `INNER JOIN activity_status s ... WHERE s.activityStatusDesc = 'OPEN'`

### Case 3: Logical Operators (`or: [...]`)
It hits `else if (key.equals("or"))`:
```java
// List<Predicate> orPredicates = new ArrayList<>();
// for (Map<String, Object> orFilter : orList) {
    // RECURSION! Call this method again to process the inner object.
    // List<Predicate> innerOrPredicates = buildPredicates(orFilter, root, cb);
    // orPredicates.add(cb.and(innerOrPredicates.toArray(new Predicate[0])));
// }
// Wrap results in an OR operator
// predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
```
* **SQL:** `(activity_type_id = 1) OR (activity_type_id = 2)`

---

## 7. How to adapt this for `ProductService`

1. Copy `QueryBuilderUtil.java` to `product-service`.
2. Change `Specification<Activity>` to `Specification<Product>`.
3. Change `Root<Activity>` to `Root<Product>`.
4. Update the `id` block to match the Product entity's ID logic.