package com.kds.rate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kds.rate.entity.ComparatorEntity;

@Repository
public interface ComparatorRepository extends JpaRepository<ComparatorEntity, Long> {

}
