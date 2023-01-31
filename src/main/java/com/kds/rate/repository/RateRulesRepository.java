package com.kds.rate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kds.rate.entity.RateRulesEntity;

@Repository
public interface RateRulesRepository extends JpaRepository<RateRulesEntity, Long> {

}
