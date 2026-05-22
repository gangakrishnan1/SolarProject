package com.lumenor.solariq.repository;

import com.lumenor.solariq.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findAllByOrderByLeadScoreDesc();

    List<Lead> findByStatusOrderByLeadScoreDesc(String status);

    List<Lead> findByLeadScoreGreaterThanEqualOrderByLeadScoreDesc(Integer minScore);

    List<Lead> findByStateContainingIgnoreCaseOrderByLeadScoreDesc(String state);
}
