package com.letterlens.domain

import java.time.LocalDateTime

data class Comment(
    val id: CommentId,
    val resumeId: ResumeId,
    val questionId: QuestionId,
    val authorId: UserId,
    val content: String,
    val selectedText: TextSelection,
    val createdAt: LocalDateTime
) {
    init {
        require(content.isNotBlank()) { "Comment content cannot be blank" }
    }
    
    fun canBeEditedBy(userId: UserId): Boolean {
        return authorId == userId
    }
    
    fun canBeDeletedBy(userId: UserId): Boolean {
        return authorId == userId
    }
}

@JvmInline
value class CommentId(val value: Long) {
    init {
        require(value > 0) { "Comment ID must be positive" }
    }
}

data class TextSelection(
    val questionId: QuestionId,
    val originalStartIndex: Int,        // 원본 위치 (참고용)
    val originalEndIndex: Int,          // 원본 위치 (참고용)
    val selectedText: String,           // 선택된 원본 텍스트
    val anchorPoints: List<AnchorPoint>, // 고정점들
    val semanticContext: SemanticContext, // 의미적 문맥
    val fingerprint: TextFingerprint    // 텍스트 지문
) {
    init {
        require(originalStartIndex >= 0) { "Start index must be non-negative" }
        require(originalEndIndex >= originalStartIndex) { "End index must be greater than or equal to start index" }
        require(selectedText.isNotBlank()) { "Selected text cannot be blank" }
        require(anchorPoints.isNotEmpty()) { "At least one anchor point is required" }
    }
    
    fun findInText(currentText: String): TextLocation? {
        // 1단계: 핑거프린트 기반 정확한 매칭 시도
        fingerprint.findExactMatch(currentText)?.let { return it }
        
        // 2단계: 앵커 포인트 기반 위치 추정
        val anchorBasedLocation = findByAnchorPoints(currentText)
        anchorBasedLocation?.let { return it }
        
        // 3단계: 의미적 문맥 기반 유사한 위치 찾기
        return findBySimilarity(currentText)
    }
    
    private fun findByAnchorPoints(currentText: String): TextLocation? {
        val validAnchors = anchorPoints.mapNotNull { anchor ->
            anchor.findInText(currentText)
        }
        
        if (validAnchors.isEmpty()) return null
        
        // 앵커들의 상대적 위치를 기반으로 선택 영역 추정
        return estimateLocationFromAnchors(validAnchors, currentText)
    }
    
    private fun findBySimilarity(currentText: String): TextLocation? {
        return semanticContext.findMostSimilarLocation(currentText, selectedText)
    }
    
    private fun estimateLocationFromAnchors(
        anchors: List<FoundAnchor>, 
        currentText: String
    ): TextLocation? {
        // 앵커들 사이의 상대적 거리를 계산해서 선택 영역 위치 추정
        // 복잡한 로직이므로 구현 생략
        return null
    }
}

// 고정점 - 텍스트 변경에도 찾기 쉬운 특징적인 부분들
data class AnchorPoint(
    val text: String,                    // 앵커 텍스트
    val type: AnchorType,               // 앵커 타입
    val relativePosition: RelativePosition, // 선택 영역과의 상대적 위치
    val distance: Int                   // 선택 영역으로부터의 거리
) {
    fun findInText(fullText: String): FoundAnchor? {
        val indices = findAllOccurrences(fullText, text)
        return when {
            indices.isEmpty() -> null
            indices.size == 1 -> FoundAnchor(this, indices[0])
            else -> {
                // 여러 개 발견된 경우, 타입과 문맥을 고려해서 가장 적절한 것 선택
                val bestIndex = selectBestMatch(indices, fullText)
                FoundAnchor(this, bestIndex)
            }
        }
    }
    
    private fun findAllOccurrences(text: String, pattern: String): List<Int> {
        val indices = mutableListOf<Int>()
        var index = text.indexOf(pattern)
        while (index != -1) {
            indices.add(index)
            index = text.indexOf(pattern, index + 1)
        }
        return indices
    }
    
    private fun selectBestMatch(indices: List<Int>, fullText: String): Int {
        // 타입별로 다른 전략 적용
        return when (type) {
            AnchorType.PUNCTUATION -> indices.first() // 첫 번째 구두점
            AnchorType.KEYWORD -> selectByKeywordContext(indices, fullText)
            AnchorType.SENTENCE_START -> indices.first()
            AnchorType.SENTENCE_END -> indices.last()
            AnchorType.UNIQUE_PHRASE -> indices.first()
        }
    }
    
    private fun selectByKeywordContext(indices: List<Int>, fullText: String): Int {
        // 키워드 주변 문맥을 분석해서 가장 적절한 위치 선택
        return indices.first() // 단순화
    }
}

enum class AnchorType {
    PUNCTUATION,    // 구두점 (. ! ? , 등)
    KEYWORD,        // 키워드 (Spring, Java, 개발 등)
    SENTENCE_START, // 문장 시작
    SENTENCE_END,   // 문장 끝
    UNIQUE_PHRASE   // 고유한 구문
}

enum class RelativePosition {
    BEFORE,         // 선택 영역 앞
    AFTER,          // 선택 영역 뒤
    CONTAINING,     // 선택 영역을 포함
    WITHIN          // 선택 영역 내부
}

data class FoundAnchor(
    val anchor: AnchorPoint,
    val currentIndex: Int
)

// 의미적 문맥 - 선택된 텍스트의 의미적 특성
data class SemanticContext(
    val precedingWords: List<String>,    // 앞에 나오는 주요 단어들
    val followingWords: List<String>,    // 뒤에 나오는 주요 단어들
    val sentencePosition: SentencePosition, // 문장 내 위치
    val semanticKeywords: List<String>,  // 의미적 키워드들
    val syntacticPattern: String         // 구문 패턴 (명사구, 동사구 등)
) {
    fun findMostSimilarLocation(currentText: String, targetText: String): TextLocation? {
        // TF-IDF나 임베딩을 활용한 의미적 유사도 계산
        // 현재는 단순한 키워드 매칭으로 구현
        
        val sentences = currentText.split("[.!?]".toRegex())
        val similarSentences = sentences.mapIndexedNotNull { index, sentence ->
            val similarity = calculateSimilarity(sentence)
            if (similarity > 0.7) { // 임계값
                SimilarSentence(index, sentence, similarity)
            } else null
        }.sortedByDescending { it.similarity }
        
        return similarSentences.firstOrNull()?.let { similar ->
            findTargetInSentence(similar.sentence, targetText)
        }
    }
    
    private fun calculateSimilarity(sentence: String): Double {
        val sentenceWords = sentence.toLowerCase().split("\\s+".toRegex())
        val matchingKeywords = semanticKeywords.count { keyword ->
            sentenceWords.any { it.contains(keyword.toLowerCase()) }
        }
        return matchingKeywords.toDouble() / semanticKeywords.size
    }
    
    private fun findTargetInSentence(sentence: String, targetText: String): TextLocation? {
        val index = sentence.indexOf(targetText)
        return if (index != -1) {
            TextLocation(
                startIndex = index,
                endIndex = index + targetText.length,
                confidence = 0.8
            )
        } else null
    }
}

data class SimilarSentence(
    val index: Int,
    val sentence: String,
    val similarity: Double
)

enum class SentencePosition {
    BEGINNING,  // 문장 시작 부분
    MIDDLE,     // 문장 중간 부분
    END         // 문장 끝 부분
}

// 텍스트 지문 - 정확한 매칭을 위한 해시값들
data class TextFingerprint(
    val contentHash: String,            // 내용 해시
    val structureHash: String,          // 구조 해시 (공백, 구두점 제거)
    val keywordHash: String,            // 키워드만의 해시
    val contextWindow: List<String>     // 주변 문맥 윈도우
) {
    fun findExactMatch(currentText: String): TextLocation? {
        // 정확한 해시 매칭 시도
        if (currentText.contains(contentHash)) {
            // 실제로는 해시가 아닌 원본 텍스트로 찾아야 함
            // 여기서는 단순화
        }
        return null
    }
}

data class TextLocation(
    val startIndex: Int,
    val endIndex: Int,
    val confidence: Double  // 매칭 신뢰도 (0.0 ~ 1.0)
) {
    fun isHighConfidence(): Boolean = confidence >= 0.8
    fun isMediumConfidence(): Boolean = confidence >= 0.5
    fun isLowConfidence(): Boolean = confidence < 0.5
}
