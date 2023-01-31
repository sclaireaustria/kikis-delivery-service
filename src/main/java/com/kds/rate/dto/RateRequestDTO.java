package com.kds.rate.dto;

import lombok.Data;

@Data
public class RateRequestDTO {

	private double weight;
	
	private double height;
	
	private double width;
	
	private double length;
	
	private String voucherCode;
}