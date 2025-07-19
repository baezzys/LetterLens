package com.letterlens.infrastructure.persistence.adapter

import com.letterlens.application.port.out.EvaluationCriteriaRepository
import com.letterlens.domain.EvaluationCriteria
import com.letterlens.domain.EvaluationCriteriaConfig
import org.springframework.stereotype.Repository

@Repository
class EvaluationCriteriaRepositoryAdapter : EvaluationCriteriaRepository {
    
    // In-memory storage for now
    private val criteriaConfigs = mutableListOf<EvaluationCriteriaConfig>()
    
    override suspend fun findAll(): List<EvaluationCriteriaConfig> {
        return criteriaConfigs.toList()
    }
    
    override suspend fun findByCriteria(criteria: EvaluationCriteria): EvaluationCriteriaConfig? {
        return criteriaConfigs.find { it.criteria == criteria }
    }
    
    override suspend fun save(config: EvaluationCriteriaConfig): EvaluationCriteriaConfig {
        val existing = criteriaConfigs.find { it.criteria == config.criteria }
        if (existing != null) {
            criteriaConfigs.remove(existing)
        }
        criteriaConfigs.add(config)
        return config
    }
}
