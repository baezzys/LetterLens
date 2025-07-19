package com.letterlens.domain

data class Question(
    val id: QuestionId,
    val resumeId: ResumeId,
    val questionText: String,
    val answer: String,
    val orderIndex: Int
) {
    init {
        require(questionText.isNotBlank()) { "Question text cannot be blank" }
        require(answer.isNotBlank()) { "Answer cannot be blank" }
        require(orderIndex >= 0) { "Order index must be non-negative" }
    }
    
    fun getAnswerWordCount(): Int {
        return answer.split("\\s+".toRegex()).size
    }
    
    fun hasMinimumContent(): Boolean {
        return answer.length >= 50 // 최소 50자 이상
    }
}

@JvmInline
value class QuestionId(val value: Long) {
    init {
        require(value > 0) { "Question ID must be positive" }
    }
}
