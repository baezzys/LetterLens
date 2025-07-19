package com.letterlens.application.service

import com.letterlens.application.port.`in`.*
import com.letterlens.application.port.out.*
import com.letterlens.domain.EvaluationCriteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class AdminApplicationService(
    private val evaluationCriteriaRepository: EvaluationCriteriaRepository
) : AdminUseCase {
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    override suspend fun getEvaluationCriteria(): List<EvaluationCriteriaResult> {
        // TODO: 평가 기준 관리 기능은 추후 구현
        return emptyList()
        
        /* 주석처리 - 추후 구현
        val configs = evaluationCriteriaRepository.findAll()
        
        return EvaluationCriteria.getAllCriteria().map { criteria ->
            val config = configs.find { it.criteria == criteria }
            
            EvaluationCriteriaResult(
                criteria = criteria,
                displayName = criteria.displayName,
                description = criteria.description,
                prompt = config?.description ?: getDefaultPrompt(criteria),
                isActive = config?.isActive ?: true,
                updatedAt = LocalDateTime.now().format(dateTimeFormatter)
            )
        }
        */
    }
    
    override suspend fun updateEvaluationPrompt(command: UpdateEvaluationPromptCommand): EvaluationCriteriaResult {
        // TODO: 평가 프롬프트 업데이트 기능은 추후 구현
        throw NotImplementedError("Admin functionality will be implemented later")
    }
    
    private fun getDefaultPrompt(criteria: EvaluationCriteria): String {
        return when (criteria) {
            EvaluationCriteria.COHERENCE -> "논리적 흐름과 개연성 평가"
            EvaluationCriteria.TECHNICAL_NOVELTY -> "기술적 참신성과 독창성 평가"
            EvaluationCriteria.CORE_COMPETENCY -> "핵심 역량 평가"
            EvaluationCriteria.EVIDENCE_SUPPORT -> "근거 지원 평가"
        }
    }
}
