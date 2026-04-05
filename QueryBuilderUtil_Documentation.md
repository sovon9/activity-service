# QueryBuilderUtil: Deep Dive Documentation

This document explains how `QueryBuilderUtil` works, focusing on the `buildSpecification` and `buildPredicates` methods, so you can easily adapt this pattern for other services (e.g., `ProductService`, `ProcessOrderService`).

## 1. The Goal of a Specification

In Spring Data JPA, `Specification<T>` is an interface that allows you to programmatically construct SQL `WHERE` clauses (Criteria Queries) without writing raw SQL strings.

The `Specification<T>` interface has exactly **one** method that you must implement:
```java
Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
```

Because it only has one method, it is a **Functional Interface**. This means instead of writing a whole class that implements `Specification`, you can just write a **Lambda function**:
```java
return (root, query, criteriaBuilder) -> { ... };
```

---

## 2. Understanding the Lambda Parameters

When Spring Data executes your query, it calls your lambda function and passes in three objects:

### A. `Root<T> root` (The "FROM" clause)
This represents the entity table you are querying.
* If `T` is `Activity`, then `root` represents the `activity` table.
* **How to use it:** You use `root.get("fieldName")` to select a specific column.
  * Example: `root.get("title")` translates to the `title` column.
* **Handling Joins:** You use `root.join("relationshipName")` to join another table.
  * Example: `root.join("status").get("activityStatusDesc")` translates to `INNER JOIN activity_status s ON s.id = activity.activity_status_id WHERE s.activityStatusDesc = ...`

### B. `CriteriaBuilder cb` (The SQL Operator Factory)
This is a factory object used to build SQL operators (like `=`, `>`, `<`, `AND`, `OR`, `LIKE`).
* **How to use it:** You call methods on the builder and pass the column (from `root`) and the value.
  * Example: `cb.equal(root.get("title"), "Startup Check")` translates to `title = 'Startup Check'`.

### C. `CriteriaQuery<?> query` (The Overall Query)
This represents the whole query. You usually don't need to use this unless you are doing complex things like `GROUP BY`, `ORDER BY` (if not handled elsewhere), or `SUBQUERIES`. We ignore it in our implementation.

---

## 3. Step-by-Step Flow of `buildSpecification`

```java
public static Specification<Activity> buildSpecification(Map<String, Object> filter) {
    // 1. Safety Check: If there are no filters, return null. Spring Data will just run a basic SELECT *.
    if (filter == null || filter.isEmpty()) {
        return null;
    }
    
    // 2. The Lambda: We are implementing the `toPredicate` method of the Specification interface.
    return (root, query, criteriaBuilder) -> {
        
        // 3. Delegate: We pass the GraphQL map, the Root, and the Builder to a helper method to do the heavy lifting.
        List<Predicate> predicates = buildPredicates(filter, root, criteriaBuilder);
        
        // 4. Empty Check: If the helper method couldn't find any valid filters, return a generic "TRUE" condition.
        if (predicates.isEmpty()) {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
        }

        // 5. Final Assembly: We take the list of all conditions and wrap them in a giant SQL `AND`.
        // Example: (status = 'OPEN') AND (title = 'Test')
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
}
```

---

## 4. Step-by-Step Flow of `buildPredicates`

The `buildPredicates` method is recursive. It loops through the `Map<String, Object>` provided by the GraphQL request.

Imagine the client sends this filter:
```json
{
  "title": "Inspection",
  "status": { "activityStatusDesc": "OPEN" },
  "or": [
    { "activityTypeId": 1 },
    { "activityTypeId": 2 }
  ]
}
```

The method loops through each key:

### Case 1: Simple Fields (`title: "Inspection"`)
It falls down to the final `else` block:
```java
predicates.add(cb.equal(root.get(key), value));
```
* **What happens:** `root.get("title")` gets the column. `cb.equal()` creates the condition `title = 'Inspection'`.

### Case 2: Nested Objects (`status: { activityStatusDesc: "OPEN" }`)
It hits the `else if (value instanceof Map)` block:
```java
Map<String, Object> nestedMap = (Map<String, Object>) value;
for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
    String nestedKey = nestedEntry.getKey(); // "activityStatusDesc"
    Object nestedValue = nestedEntry.getValue(); // "OPEN"
    predicates.add(cb.equal(root.join(key).get(nestedKey), nestedValue));
}
```
* **What happens:** `root.join("status")` tells Hibernate to join the `activity_status` table. Then it applies the condition `activity_status_desc = 'OPEN'`.

### Case 3: Logical Operators (`or: [...]`)
It hits the `else if (key.equals("or"))` block.
```java
List<Map<String, Object>> orList = (List<Map<String, Object>>) value;
List<Predicate> orPredicates = new ArrayList<>();

// Loop through each item in the array
for (Map<String, Object> orFilter : orList) {
    // RECURSION! Call this exact method again to process the inner object.
    List<Predicate> innerOrPredicates = buildPredicates(orFilter, root, cb);
    orPredicates.add(cb.and(innerOrPredicates.toArray(new Predicate[0])));
}

// Wrap all the results in an OR operator
predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
```
* **What happens:** It processes `{ activityTypeId: 1 }` and `{ activityTypeId: 2 }` separately. Then it combines them: `(activity_type_id = 1) OR (activity_type_id = 2)`.

---

## 5. How to adapt this for `ProductService`

To reuse this in another service, you simply change the Generic Type!

1. Copy `QueryBuilderUtil.java` to your `product-service`.
2. Change `Specification<Activity>` to `Specification<Product>`.
3. Change `Root<Activity>` to `Root<Product>`.
4. Update the Primary Key logic (the `id` block) to match the Product entity's ID field (e.g., `productId`).

Everything else (the AND/OR logic, the nested object logic, the simple field logic) is completely generic and will work automatically for any entity!