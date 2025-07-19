package com.letterlens.application.port.`in`

import com.letterlens.domain.EvaluationCriteria

interface AdminUseCase {
    
    suspend fun getEvaluationCriteria(): List<EvaluationCriteriaResult>
    
    suspend fun updateEvaluationPrompt(command: UpdateEvaluationPromptCommand): EvaluationCriteriaResult
}

// Commands
data class UpdateEvaluationPromptCommand(
    val criteria: EvaluationCriteria,
    val prompt: String
)

// Results
data class EvaluationCriteriaResult(
    val criteria: EvaluationCriteria,
    val displayName: String,
    val description: String,
    val prompt: String,
    val isActive: Boolean,
    val updatedAt: String
)
