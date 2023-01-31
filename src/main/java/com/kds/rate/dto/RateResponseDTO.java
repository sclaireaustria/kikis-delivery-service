package com.kds.rate.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RateResponseDTO {
	
	public String rateRule;
	
	public BigDecimal cost;
	
	public String description;
}
