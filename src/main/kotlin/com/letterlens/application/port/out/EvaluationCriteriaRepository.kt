package com.letterlens.application.port.out

import com.letterlens.domain.EvaluationCriteria
import com.letterlens.domain.EvaluationCriteriaConfig

interface EvaluationCriteriaRepository {
    suspend fun findAll(): List<EvaluationCriteriaConfig>
    suspend fun findByCriteria(criteria: EvaluationCriteria): EvaluationCriteriaConfig?
    suspend fun save(config: EvaluationCriteriaConfig): EvaluationCriteriaConfig
}
