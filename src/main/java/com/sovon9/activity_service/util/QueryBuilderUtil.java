package com.sovon9.activity_service.util;

import com.sovon9.activity_service.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBuilderUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilderUtil.class);

    public static Sort buildSort(Map<String, Object> order, String defaultField, Sort.Direction defaultDirection) {
        if (order == null || order.isEmpty()) {
            return Sort.by(defaultDirection, defaultField);
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (Map.Entry<String, Object> entry : order.entrySet()) {
            String field = entry.getKey();
            String directionStr = String.valueOf(entry.getValue());
            
            Sort.Direction direction = Sort.Direction.ASC;
            if ("DESC".equalsIgnoreCase(directionStr)) {
                direction = Sort.Direction.DESC;
            }
            
            orders.add(new Sort.Order(direction, field));
        }

        return Sort.by(orders);
    }

    public static Specification<Activity> buildSpecification(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null; // Return null when no filter is provided
        }
        
        return (root, query, criteriaBuilder) -> {
            LOGGER.error("===> buildSpecification");
            List<Predicate> predicates = buildPredicates(filter, root, criteriaBuilder);
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Predicate> buildPredicates(Map<String, Object> filter, jakarta.persistence.criteria.Root<Activity> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            if (key.equals("and")) {
                List<Map<String, Object>> andList = (List<Map<String, Object>>) value;
                List<Predicate> andPredicates = new ArrayList<>();
                for (Map<String, Object> andFilter : andList) {
                    andPredicates.addAll(buildPredicates(andFilter, root, cb));
                }
                if (!andPredicates.isEmpty()) {
                    predicates.add(cb.and(andPredicates.toArray(new Predicate[0])));
                }
            } else if (key.equals("or")) {
                List<Map<String, Object>> orList = (List<Map<String, Object>>) value;
                List<Predicate> orPredicates = new ArrayList<>();
                for (Map<String, Object> orFilter : orList) {
                    List<Predicate> innerOrPredicates = buildPredicates(orFilter, root, cb);
                    if (!innerOrPredicates.isEmpty()) {
                        orPredicates.add(cb.and(innerOrPredicates.toArray(new Predicate[0])));
                    }
                }
                if (!orPredicates.isEmpty()) {
                    predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
                }
            } else {
                // Handle simple field filters
                if (key.equals("activityStatusDesc")) {
                    predicates.add(cb.equal(root.join("status").get("activityStatusDesc"), value));
                } else if (key.equals("id") || key.equals("activityId")) {
                    try {
                        Long parsedId = Long.parseLong(value.toString());
                        predicates.add(cb.equal(root.get("activityId"), parsedId));
                    } catch (NumberFormatException e) {
                        try {
                           String[] globalId = GlobalUtil.fromGlobalId(value.toString());
                           if(globalId.length == 2) {
                               predicates.add(cb.equal(root.get("activityId"), Long.parseLong(globalId[1])));
                           }
                        }catch (Exception ex) {
                            // ignore 
                        }
                    }
                } else {
                    predicates.add(cb.equal(root.get(key), value));
                }
            }
        }

        return predicates;
    }
}