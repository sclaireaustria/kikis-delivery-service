package com.kds.rate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.rate.dto.RateRequestDTO;
import com.kds.rate.dto.RateResponseDTO;
import com.kds.rate.entity.RateRulesEntity;
import com.kds.rate.service.RateService;

@RestController
@RequestMapping("/kikis-delivery-service")
public class RateController {
	
	@Autowired
	RateService rateService;
	
	@GetMapping("/rates")
	public List<RateRulesEntity> getRates() {
		return rateService.getAllRateRules();
		
	}
	
	@PostMapping("/compute-rate")
	public RateResponseDTO computeRate(@RequestBody RateRequestDTO rateDTO) {
		
		return rateService.computeRate(rateDTO);
	}
	
	@GetMapping(value = "/voucher")
	public void getCountries () {
		
	}
	
	

}
