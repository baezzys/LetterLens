package com.letterlens.domain

import java.time.LocalDateTime

data class Evaluation(
    val id: EvaluationId,
    val resumeId: ResumeId,
    val criteria: EvaluationCriteria,
    val score: Score,
    val feedback: String,
    val evaluatedAt: LocalDateTime
) {
    init {
        require(feedback.isNotBlank()) { "Feedback cannot be blank" }
    }
    
    fun isExcellent(): Boolean = score.value >= 80
    fun isGood(): Boolean = score.value >= 60
    fun needsImprovement(): Boolean = score.value < 60
}

@JvmInline
value class EvaluationId(val value: Long) {
    init {
        require(value > 0) { "Evaluation ID must be positive" }
    }
}

@JvmInline
value class Score(val value: Int) {
    init {
        require(value in 0..100) { "Score must be between 0 and 100" }
    }
    
    fun getGrade(): String {
        return when {
            value >= 90 -> "A+"
            value >= 80 -> "A"
            value >= 70 -> "B+"
            value >= 60 -> "B"
            value >= 50 -> "C+"
            value >= 40 -> "C"
            else -> "D"
        }
    }
}
