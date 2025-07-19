package com.letterlens.application.port.out

import com.letterlens.domain.EvaluationCriteria

interface AiEvaluationPort {
    fun evaluateResume(request: EvaluationRequest): EvaluationResponse
}

data class EvaluationRequest(
    val questions: List<QuestionForEvaluation>,
    val criteria: List<EvaluationCriteria>
)

data class QuestionForEvaluation(
    val questionText: String,
    val answer: String
)

data class EvaluationResponse(
    val evaluations: List<AiEvaluationResult>
)

data class AiEvaluationResult(
    val criteria: EvaluationCriteria,
    val score: Int,
    val feedback: String
)
