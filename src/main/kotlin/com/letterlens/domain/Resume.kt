package com.letterlens.domain

import java.time.LocalDateTime

data class Resume(
    val id: ResumeId,
    val userId: UserId,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val questions: List<Question>,
    val evaluations: List<Evaluation>,
    val shareToken: ShareToken,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(originalFileName.isNotBlank()) { "Original file name cannot be blank" }
        require(fileUrl.isNotBlank()) { "File URL cannot be blank" }
        require(questions.isNotEmpty()) { "Resume must have at least one question" }
    }
    
    fun addEvaluation(evaluation: Evaluation): Resume {
        require(evaluation.resumeId == this.id) { "Evaluation must belong to this resume" }
        
        return this.copy(
            evaluations = evaluations + evaluation,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun isEvaluationComplete(): Boolean {
        val evaluatedCriteria = evaluations.map { it.criteria }.toSet()
        return evaluatedCriteria.size == EvaluationCriteria.getAllCriteria().size
    }
    
    fun getEvaluationByCriteria(criteria: EvaluationCriteria): Evaluation? {
        return evaluations.find { it.criteria == criteria }
    }
    
    fun getOverallScore(): Double {
        if (evaluations.isEmpty()) return 0.0
        return evaluations.map { it.score.value }.average()
    }
    
    fun getOverallGrade(): String {
        val overallScore = getOverallScore().toInt()
        return Score(overallScore).getGrade()
    }
    
    fun getQuestionById(questionId: QuestionId): Question? {
        return questions.find { it.id == questionId }
    }
    
    fun getTotalWordCount(): Int {
        return questions.sumOf { it.getAnswerWordCount() }
    }
    
    fun isOwnedBy(userId: UserId): Boolean {
        return this.userId == userId
    }
    
    fun canBeAccessedBy(userId: UserId?): Boolean {
        // 소유자는 항상 접근 가능
        if (userId != null && isOwnedBy(userId)) return true
        
        // 평가가 완료된 자소서는 모든 로그인 사용자가 접근 가능
        return userId != null && isEvaluationComplete()
    }
    
    companion object {
        fun create(
            id: ResumeId,
            userId: UserId,
            title: String,
            originalFileName: String,
            fileUrl: String,
            questions: List<Question>
        ): Resume {
            val now = LocalDateTime.now()
            return Resume(
                id = id,
                userId = userId,
                title = title,
                originalFileName = originalFileName,
                fileUrl = fileUrl,
                questions = questions,
                evaluations = emptyList(),
                shareToken = ShareToken.generate(),
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
