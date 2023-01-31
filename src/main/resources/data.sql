INSERT INTO measurement  (name) VALUES ('Weight');
INSERT INTO measurement  (name) VALUES ('Volume');

INSERT INTO comparator (name, description) VALUES ('GT', 'Greater than');
INSERT INTO comparator (name, description) VALUES ('LT', 'Less than');

INSERT INTO rate_rules 
  (priority, rule_name, condition_measurement, comparator_id, condition_unit, cost, multiplier_measurement)
   VALUES ('1', 'Reject', 1, 1, '50', '0', NULL);

INSERT INTO rate_rules 
  (priority, rule_name, condition_measurement, comparator_id, condition_unit, cost, multiplier_measurement)
   VALUES ('2', 'Heavy Parcel', '1', '1', '10', '20', '1');

INSERT INTO rate_rules 
  (priority, rule_name, condition_measurement, comparator_id, condition_unit, cost, multiplier_measurement)
   VALUES ('3', 'Small Parcel', '2', '2', '1500', '0.03', '2');

INSERT INTO rate_rules 
  (priority, rule_name, condition_measurement, comparator_id, condition_unit, cost, multiplier_measurement)
   VALUES ('4', 'Medium Parcel', '2', '2', '2500', '0.04', '2');

INSERT INTO rate_rules 
  (priority, rule_name, condition_measurement, comparator_id, condition_unit, cost, multiplier_measurement)
   VALUES ('5', 'Large Parcel', NULL, NULL, '0', '0.05', '2');