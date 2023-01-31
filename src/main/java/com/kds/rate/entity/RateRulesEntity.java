package com.kds.rate.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "rate_rules")
public class RateRulesEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
	
	@Column(name="priority")
	private int priority;
	
	@Column(name="rule_name")
	private String ruleName;

	@ManyToOne
	@JoinColumn(name = "condition_measurement", referencedColumnName = "id")
	private MeasurementEntity conditionMeasurement;
	
	@ManyToOne
	@JoinColumn(name = "comparator_id", referencedColumnName = "id")
	private ComparatorEntity comparator;
	
	@Column(name="condition_unit",nullable=true)
	private int conditionUnit;
	
	@Column(name="cost")
	private BigDecimal cost;
	
	@ManyToOne
	@JoinColumn(name="multiplier_measurement", referencedColumnName = "id")
	private MeasurementEntity multiplierMeasurement;
	
}
