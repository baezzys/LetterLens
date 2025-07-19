package com.letterlens.domain

enum class EvaluationCriteria(
    val displayName: String,
    val description: String
) {
    COHERENCE("글의 개연성", "논리적 흐름과 일관성을 평가합니다"),
    TECHNICAL_NOVELTY("기술적 참신성", "기술적 내용의 독창성과 참신함을 평가합니다"),
    CORE_COMPETENCY("핵심 역량", "백엔드 개발자로서의 핵심 역량이 잘 드러나는지 평가합니다"),
    EVIDENCE_SUPPORT("뒷받침 근거", "주장에 대한 구체적이고 적절한 근거가 제시되었는지 평가합니다");
    
    companion object {
        fun getAllCriteria(): List<EvaluationCriteria> = values().toList()
    }
}
