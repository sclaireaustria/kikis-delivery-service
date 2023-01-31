package com.kds.rate.service;

import java.util.List;

import com.kds.rate.dto.RateRequestDTO;
import com.kds.rate.dto.RateResponseDTO;
import com.kds.rate.entity.RateRulesEntity;

public interface RateService {

	public abstract List<RateRulesEntity> getAllRateRules();
	public abstract RateResponseDTO computeRate(RateRequestDTO rateDto);
}
