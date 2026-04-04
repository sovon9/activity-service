CREATE TABLE activity_status (
    activity_status_id INT PRIMARY KEY,
    activity_status_desc VARCHAR(255)
);

CREATE TABLE activity_type (
    activity_type_id INT PRIMARY KEY,
    activity_desc VARCHAR(255)
);

CREATE TABLE activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_status_id INT,
    activity_type_id INT,
    production_unit_id BIGINT,
    process_order_id BIGINT,
    title VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_activity_status FOREIGN KEY (activity_status_id) REFERENCES activity_status(activity_status_id),
    CONSTRAINT fk_activity_type FOREIGN KEY (activity_type_id) REFERENCES activity_type(activity_type_id)
);

--CREATE TABLE variable (
--    id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    name VARCHAR(255),
--    unit VARCHAR(255),
--    spec_min DOUBLE,
--    spec_max DOUBLE,
--    activity_id BIGINT,
--    CONSTRAINT fk_variable_activity FOREIGN KEY (activity_id) REFERENCES activity(id)
--);
--
--CREATE TABLE test_value (
--    id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    value VARCHAR(255),
--    recorded_at TIMESTAMP,
--    recorded_by VARCHAR(255),
--    variable_id BIGINT,
--    CONSTRAINT fk_test_value_variable FOREIGN KEY (variable_id) REFERENCES variable(id)
--);