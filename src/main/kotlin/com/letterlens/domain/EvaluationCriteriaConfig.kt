package com.letterlens.domain

data class EvaluationCriteriaConfig(
    val criteria: EvaluationCriteria,
    val weight: Double,
    val isActive: Boolean,
    val description: String
)
