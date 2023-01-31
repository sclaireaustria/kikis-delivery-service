DROP TABLE IF EXISTS rate_rules;
DROP TABLE IF EXISTS measurement;
DROP TABLE IF EXISTS comparator;

CREATE TABLE rate_rules ( 
   id IDENTITY PRIMARY KEY, 
   priority INT NOT NULL,
   rule_name VARCHAR(20) NOT NULL,
   condition_measurement INT,
   comparator_id INT,
   condition_unit INT, 
   cost NUMERIC(20, 2),
   multiplier_measurement INT 
);

CREATE TABLE measurement (
   id IDENTITY PRIMARY KEY, 
   name VARCHAR(20) NOT NULL
);

CREATE TABLE comparator (
   id IDENTITY PRIMARY KEY, 
   name VARCHAR(20) NOT NULL, 
   description VARCHAR(20) NOT NULL
);

ALTER TABLE rate_rules
   ADD FOREIGN KEY (condition_measurement) 
   REFERENCES measurement(id);
    
ALTER TABLE rate_rules
	ADD FOREIGN KEY (multiplier_measurement) 
	REFERENCES measurement(id);

ALTER TABLE rate_rules
    ADD FOREIGN KEY (comparator_id) 
    REFERENCES comparator(id);