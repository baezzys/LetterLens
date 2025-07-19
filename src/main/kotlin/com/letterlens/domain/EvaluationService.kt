package com.letterlens.domain

import org.springframework.stereotype.Service

@Service
class EvaluationService {
    
    fun evaluateAllCriteria(resume: Resume): List<Evaluation> {
        return EvaluationCriteria.getAllCriteria().map { criteria ->
            evaluateSingleCriteria(resume, criteria)
        }
    }
    
    fun evaluateSingleCriteria(resume: Resume, criteria: EvaluationCriteria): Evaluation {
        // 실제로는 AI 서비스를 통해 평가하겠지만, 
        // 도메인 레벨에서는 비즈니스 로직만 정의
        
        val combinedAnswers = resume.questions.joinToString("\n\n") { 
            "Q: ${it.questionText}\nA: ${it.answer}" 
        }
        
        // AI 서비스 호출은 Application Service에서 처리
        // 여기서는 평가 결과를 받아서 도메인 객체로 변환하는 로직
        
        return Evaluation(
            id = EvaluationId(0), // Repository에서 실제 ID 할당
            resumeId = resume.id,
            criteria = criteria,
            score = Score(0), // AI 서비스에서 받은 점수로 대체
            feedback = "", // AI 서비스에서 받은 피드백으로 대체
            evaluatedAt = java.time.LocalDateTime.now()
        )
    }
    
    fun calculateOverallPerformance(evaluations: List<Evaluation>): OverallPerformance {
        require(evaluations.isNotEmpty()) { "Evaluations cannot be empty" }
        
        val averageScore = evaluations.map { it.score.value }.average()
        val strengths = evaluations.filter { it.score.value >= 70 }
            .map { it.criteria.displayName }
        val weaknesses = evaluations.filter { it.score.value < 60 }
            .map { it.criteria.displayName }
            
        return OverallPerformance(
            averageScore = averageScore,
            grade = Score(averageScore.toInt()).getGrade(),
            strengths = strengths,
            weaknesses = weaknesses,
            evaluationCount = evaluations.size
        )
    }
}

data class OverallPerformance(
    val averageScore: Double,
    val grade: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val evaluationCount: Int
) {
    fun hasStrengths(): Boolean = strengths.isNotEmpty()
    fun hasWeaknesses(): Boolean = weaknesses.isNotEmpty()
    fun isWellBalanced(): Boolean = weaknesses.isEmpty() && strengths.size >= 3
}
