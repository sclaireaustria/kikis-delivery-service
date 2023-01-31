package com.kds.rate.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.kds.rate.constants.MessageConstants;
import com.kds.rate.constants.RateConstants;
import com.kds.rate.dto.RateRequestDTO;
import com.kds.rate.dto.RateResponseDTO;
import com.kds.rate.entity.RateRulesEntity;
import com.kds.rate.repository.ComparatorRepository;
import com.kds.rate.repository.MeasurementRepository;
import com.kds.rate.repository.RateRulesRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class RateServiceImpl implements RateService {
	
	@Autowired
	RateRulesRepository rateRuleRepository;
	
	@Autowired
	MeasurementRepository measurementRepository;
	
	@Autowired
	ComparatorRepository comparatorRepository;	
	
	@Autowired
	private Environment applicationProperties;
	
	@Override
	public List<RateRulesEntity> getAllRateRules() {
		return rateRuleRepository.findAll();
	}
			
	@Override
	public RateResponseDTO computeRate(RateRequestDTO requestDto) {
		
		RateResponseDTO responseDto =  new RateResponseDTO();
		
		// Get rate rules 
		List<RateRulesEntity> rateRulesList = getAllRateRules();
		
		// Place rate rules in a tree map to sort according to priority
		TreeMap<Integer, RateRulesEntity> rateRulesMap = new TreeMap<Integer, RateRulesEntity>();
		
		for (RateRulesEntity rateRulesEntity: rateRulesList) {
			
			rateRulesMap.put(rateRulesEntity.getPriority(), rateRulesEntity);
		}	
		
		// Apply rate rule
		responseDto = applyRateRule(requestDto, rateRulesMap);
		
		// If cost > 0 and a voucher code was provided, apply discount
		if ((responseDto.getCost().compareTo(BigDecimal.ZERO) != 0) && StringUtils.isNotEmpty(requestDto.getVoucherCode())) {
			responseDto = applyDiscountVoucher(requestDto.getVoucherCode(), responseDto);
		}
		
		return responseDto;
	}
	
	private RateResponseDTO applyRateRule(RateRequestDTO requestDto, TreeMap<Integer, RateRulesEntity> rateRulesMap) {
		
		RateResponseDTO responseDto =  new RateResponseDTO();
		RateRulesEntity currentRule = null;

		// Compute for the volume if the dimensions are provided
		double volume = computeVolume(requestDto);	
		
		// Loop through the rules to check which is applicable
		for (var rateRule : rateRulesMap.entrySet()) {
			
		    currentRule = rateRule.getValue();
		    
		    if (currentRule.getConditionMeasurement() != null && currentRule.getComparator() != null) {

			    String measurementName = currentRule.getConditionMeasurement().getName();
			    String comparator = currentRule.getComparator().getName();
			    
			    if (RateConstants.WEIGHT.equalsIgnoreCase(measurementName) &&
			    		requestDto.getWeight() != 0) {
			    	
			    	// If rule is based on weight
			    	boolean isWeightWithinRange = isParameterWithinRange(currentRule.getConditionUnit(), 
			    			requestDto.getWeight(), comparator);
			    	
			    	if (isWeightWithinRange) {
			    		
			    		responseDto = setCostAndRateRule(currentRule, requestDto.getWeight(), volume, responseDto);
			    		
			    		break;
			    	}
			    } else if (RateConstants.VOLUME.equalsIgnoreCase(measurementName) &&
			    		volume != 0) {
			    	
			    	// If rule is based on volume
			    	boolean isVolumeWithinRange = isParameterWithinRange(currentRule.getConditionUnit(), 
			    			volume, comparator);
			    	
			    	if (isVolumeWithinRange) {
			    		
			    		responseDto = setCostAndRateRule(currentRule, requestDto.getWeight(), volume, responseDto);
			    		
			    		break;
			    	}
			    	
			    } 
		    } else if (currentRule.getConditionMeasurement() == null && currentRule.getComparator() == null) {
		    	
		    	responseDto = setCostAndRateRule(currentRule, requestDto.getWeight(), volume, responseDto);
	    		
	    		break;
		    }
		    
		}
		
		return responseDto;
	}
	
	private double computeVolume(RateRequestDTO requestDto) {
		
		double volume = 0;
		double height = requestDto.getHeight();
		double width = requestDto.getWidth();
		double length = requestDto.getLength();
		
		if (height != 0 && width != 0 && length !=0) {
			volume = height * width * length;
		}
		 
		return volume;
	}
	
	private boolean isParameterWithinRange(double ruleUnit, double requestUnit, String comparator) {
		
		if (RateConstants.GREATER_THAN.equals(comparator)) {
			
			return requestUnit > ruleUnit;
		} else if (RateConstants.LESS_THAN.equals(comparator)) {
			
			return requestUnit < ruleUnit;
		} else {
			
			return false;
		}
	}
	
	private RateResponseDTO setCostAndRateRule (RateRulesEntity currentRule, double weight,
			double volume, RateResponseDTO response) {

		BigDecimal cost = currentRule.getCost();
		
		BigDecimal actualCost = BigDecimal.ZERO;
		double costMultiplier = 0;
		String message = "";
		
		if (cost.compareTo(BigDecimal.ZERO) != 0 && currentRule.getMultiplierMeasurement() != null) {
			
			String multiplierMeasurement = currentRule.getMultiplierMeasurement().getName();
			
			if (RateConstants.WEIGHT.equals(multiplierMeasurement)) {
			
				costMultiplier = weight;
			} else if (RateConstants.VOLUME.equals(multiplierMeasurement)) {
				
				costMultiplier = volume;
			}
			
			actualCost = cost.multiply(new BigDecimal(costMultiplier));
			message = MessageConstants.VALID_ORDER;
			
		}  else {
			
			message = MessageConstants.INVALID_ORDER;
		}
		
		response.setCost(actualCost);
		response.setRateRule(currentRule.getRuleName());
		response.setDescription(message);
		
		return response;
	}
	
	private RateResponseDTO applyDiscountVoucher(String voucherCode, RateResponseDTO rateResponseDto) {
		
		BigDecimal discount = makeVoucherApiCall(voucherCode);
		
		// If discount is not zero, subtract it from the total
		if (discount.compareTo(BigDecimal.ZERO) != 0) {
			
			BigDecimal newCost = rateResponseDto.getCost().subtract(discount);
			rateResponseDto.setCost(newCost);
			rateResponseDto.setDescription(rateResponseDto.getDescription() 
					+ " "
					+ MessageConstants.VALID_VOUCHER
					+ " "
					+ String.valueOf(discount));
		} else {
			rateResponseDto.setDescription(rateResponseDto.getDescription() 
					+ " "
					+ MessageConstants.INVALID_VOUCHER);
		}
		
		return rateResponseDto;
	}
	
	private BigDecimal makeVoucherApiCall(String voucherCode) { 
		 
		BigDecimal discount = BigDecimal.ZERO;
		
		String voucherUrl = applicationProperties.getProperty("voucher.api.url");
	    String voucherApiKey = applicationProperties.getProperty("voucher.api.key");
	    
	    Map<String, String> uriVariableMap = new HashMap<>();
	    uriVariableMap.put(RateConstants.VOUCHER_CODE, voucherCode);
	    uriVariableMap.put(RateConstants.API_KEY, voucherApiKey);

	    RestTemplate restTemplate = new RestTemplate();
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity <String> entity = new HttpEntity<String>(headers);
	    
	    try {
	    	
		    ResponseEntity<String> responseEntity = restTemplate.exchange(
				voucherUrl,
				HttpMethod.GET,
				entity,
				String.class,
				uriVariableMap
	        );
		    
		    JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
			String discountValue = jsonResponse.getString("discount");  
			discount = new BigDecimal(discountValue);
		    
	    } catch (HttpStatusCodeException ex) {

	    	HttpHeaders responseHeader = ex.getResponseHeaders();
	    	System.out.println(ex.getStatusCode().toString());
	        System.out.println(responseHeader.get("Matched-Stub-Name"));
	    } catch (JSONException e) {
	    	
			e.printStackTrace();
		}
	    
	    return discount;
	}

}
