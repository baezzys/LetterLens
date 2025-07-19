package com.letterlens.domain

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ResumeService {
    
    fun createResume(
        id: ResumeId,
        userId: UserId,
        title: String,
        originalFileName: String,
        fileUrl: String,
        questions: List<Question>
    ): Resume {
        validateQuestions(questions)
        
        return Resume.create(
            id = id,
            userId = userId,
            title = title,
            originalFileName = originalFileName,
            fileUrl = fileUrl,
            questions = questions
        )
    }
    
    fun addEvaluationToResume(
        resume: Resume,
        criteria: EvaluationCriteria,
        score: Score,
        feedback: String
    ): Resume {
        // 이미 해당 기준으로 평가된 경우 중복 방지
        if (resume.getEvaluationByCriteria(criteria) != null) {
            throw IllegalStateException("Resume already evaluated for criteria: ${criteria.displayName}")
        }
        
        val evaluation = Evaluation(
            id = EvaluationId(0), // 실제로는 Repository에서 생성
            resumeId = resume.id,
            criteria = criteria,
            score = score,
            feedback = feedback,
            evaluatedAt = LocalDateTime.now()
        )
        
        return resume.addEvaluation(evaluation)
    }
    
    fun canShareResume(resume: Resume): Boolean {
        return resume.isEvaluationComplete()
    }
    
    private fun validateQuestions(questions: List<Question>) {
        require(questions.isNotEmpty()) { "Resume must have at least one question" }
        
        // 모든 질문이 최소 요구사항을 만족하는지 확인
        questions.forEach { question ->
            if (!question.hasMinimumContent()) {
                throw IllegalArgumentException("Question ${question.id.value} does not meet minimum content requirements")
            }
        }
        
        // orderIndex 중복 확인
        val orderIndices = questions.map { it.orderIndex }
        if (orderIndices.size != orderIndices.toSet().size) {
            throw IllegalArgumentException("Duplicate order indices found in questions")
        }
    }
}
