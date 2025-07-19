package com.letterlens.domain

import org.springframework.stereotype.Component

@Component
class TextSelectionBuilder {
    
    fun createTextSelection(
        questionId: QuestionId,
        fullText: String,
        startIndex: Int,
        endIndex: Int
    ): TextSelection {
        val selectedText = fullText.substring(startIndex, endIndex)
        
        return TextSelection(
            questionId = questionId,
            originalStartIndex = startIndex,
            originalEndIndex = endIndex,
            selectedText = selectedText,
            anchorPoints = extractAnchorPoints(fullText, startIndex, endIndex),
            semanticContext = buildSemanticContext(fullText, startIndex, endIndex),
            fingerprint = createTextFingerprint(fullText, selectedText, startIndex)
        )
    }
    
    private fun extractAnchorPoints(
        fullText: String, 
        startIndex: Int, 
        endIndex: Int
    ): List<AnchorPoint> {
        val anchors = mutableListOf<AnchorPoint>()
        
        // 1. 앞쪽 고정점들 찾기 (가까운 순서대로)
        anchors.addAll(findNearbyAnchors(fullText, startIndex, RelativePosition.BEFORE))
        
        // 2. 뒤쪽 고정점들 찾기
        anchors.addAll(findNearbyAnchors(fullText, endIndex, RelativePosition.AFTER))
        
        // 3. 선택 영역을 포함하는 문장의 시작/끝
        anchors.addAll(findSentenceBoundaries(fullText, startIndex, endIndex))
        
        // 4. 고유한 키워드나 구문들
        anchors.addAll(findUniqueKeywords(fullText, startIndex, endIndex))
        
        return anchors.take(10) // 최대 10개 앵커만 유지
    }
    
    private fun findNearbyAnchors(
        fullText: String, 
        position: Int, 
        relativePosition: RelativePosition
    ): List<AnchorPoint> {
        val anchors = mutableListOf<AnchorPoint>()
        val searchRange = 50 // 앞뒤 50자 범위에서 찾기
        
        val searchStart = if (relativePosition == RelativePosition.BEFORE) {
            maxOf(0, position - searchRange)
        } else {
            position
        }
        
        val searchEnd = if (relativePosition == RelativePosition.BEFORE) {
            position
        } else {
            minOf(fullText.length, position + searchRange)
        }
        
        val searchText = fullText.substring(searchStart, searchEnd)
        
        // 구두점 찾기
        val punctuations = listOf(".", "!", "?", ",", ";", ":")
        punctuations.forEach { punct ->
            val index = if (relativePosition == RelativePosition.BEFORE) {
                searchText.lastIndexOf(punct)
            } else {
                searchText.indexOf(punct)
            }
            
            if (index != -1) {
                val distance = if (relativePosition == RelativePosition.BEFORE) {
                    position - (searchStart + index)
                } else {
                    (searchStart + index) - position
                }
                
                anchors.add(AnchorPoint(
                    text = punct,
                    type = AnchorType.PUNCTUATION,
                    relativePosition = relativePosition,
                    distance = distance
                ))
            }
        }
        
        return anchors.sortedBy { it.distance }.take(3)
    }
    
    private fun findSentenceBoundaries(
        fullText: String, 
        startIndex: Int, 
        endIndex: Int
    ): List<AnchorPoint> {
        val anchors = mutableListOf<AnchorPoint>()
        
        // 현재 문장의 시작점 찾기
        val sentenceStart = findSentenceStart(fullText, startIndex)
        if (sentenceStart != -1) {
            val sentenceStartText = fullText.substring(sentenceStart, minOf(sentenceStart + 20, fullText.length))
            anchors.add(AnchorPoint(
                text = sentenceStartText.trim(),
                type = AnchorType.SENTENCE_START,
                relativePosition = RelativePosition.BEFORE,
                distance = startIndex - sentenceStart
            ))
        }
        
        // 현재 문장의 끝점 찾기
        val sentenceEnd = findSentenceEnd(fullText, endIndex)
        if (sentenceEnd != -1) {
            val sentenceEndText = fullText.substring(maxOf(sentenceEnd - 20, 0), sentenceEnd + 1)
            anchors.add(AnchorPoint(
                text = sentenceEndText.trim(),
                type = AnchorType.SENTENCE_END,
                relativePosition = RelativePosition.AFTER,
                distance = sentenceEnd - endIndex
            ))
        }
        
        return anchors
    }
    
    private fun findUniqueKeywords(
        fullText: String, 
        startIndex: Int, 
        endIndex: Int
    ): List<AnchorPoint> {
        val anchors = mutableListOf<AnchorPoint>()
        val contextRange = 100 // 앞뒤 100자 범위
        
        val contextStart = maxOf(0, startIndex - contextRange)
        val contextEnd = minOf(fullText.length, endIndex + contextRange)
        val contextText = fullText.substring(contextStart, contextEnd)
        
        // 기술 키워드들 찾기
        val techKeywords = listOf(
            "Spring", "Boot", "Java", "Kotlin", "Docker", "Kubernetes",
            "AWS", "React", "Vue", "Node.js", "MySQL", "PostgreSQL",
            "Redis", "MongoDB", "Git", "Jenkins", "API", "REST",
            "GraphQL", "Microservice", "Architecture", "Design Pattern"
        )
        
        techKeywords.forEach { keyword ->
            val index = contextText.indexOf(keyword, ignoreCase = true)
            if (index != -1) {
                val actualIndex = contextStart + index
                val relativePos = when {
                    actualIndex < startIndex -> RelativePosition.BEFORE
                    actualIndex > endIndex -> RelativePosition.AFTER
                    else -> RelativePosition.WITHIN
                }
                
                val distance = when (relativePos) {
                    RelativePosition.BEFORE -> startIndex - actualIndex
                    RelativePosition.AFTER -> actualIndex - endIndex
                    else -> 0
                }
                
                anchors.add(AnchorPoint(
                    text = keyword,
                    type = AnchorType.KEYWORD,
                    relativePosition = relativePos,
                    distance = distance
                ))
            }
        }
        
        return anchors.distinctBy { it.text }.take(5)
    }
    
    private fun buildSemanticContext(
        fullText: String, 
        startIndex: Int, 
        endIndex: Int
    ): SemanticContext {
        val selectedText = fullText.substring(startIndex, endIndex)
        val words = selectedText.split("\\s+".toRegex())
        
        // 앞 문맥에서 주요 단어들 추출
        val precedingText = fullText.substring(maxOf(0, startIndex - 100), startIndex)
        val precedingWords = extractKeywords(precedingText).take(5)
        
        // 뒤 문맥에서 주요 단어들 추출
        val followingText = fullText.substring(endIndex, minOf(fullText.length, endIndex + 100))
        val followingWords = extractKeywords(followingText).take(5)
        
        // 문장 내 위치 파악
        val sentenceStart = findSentenceStart(fullText, startIndex)
        val sentenceEnd = findSentenceEnd(fullText, endIndex)
        val sentencePosition = calculateSentencePosition(sentenceStart, sentenceEnd, startIndex, endIndex)
        
        // 의미적 키워드 추출
        val semanticKeywords = extractSemanticKeywords(selectedText)
        
        return SemanticContext(
            precedingWords = precedingWords,
            followingWords = followingWords,
            sentencePosition = sentencePosition,
            semanticKeywords = semanticKeywords,
            syntacticPattern = analyzeSyntacticPattern(selectedText)
        )
    }
    
    private fun createTextFingerprint(
        fullText: String, 
        selectedText: String, 
        startIndex: Int
    ): TextFingerprint {
        val contentHash = selectedText.hashCode().toString()
        val structureHash = selectedText.replace("\\s+".toRegex(), "").hashCode().toString()
        val keywordHash = extractKeywords(selectedText).joinToString("").hashCode().toString()
        
        // 주변 문맥 윈도우 (앞뒤 각각 3단어씩)
        val words = fullText.split("\\s+".toRegex())
        val selectedWordStart = fullText.substring(0, startIndex).split("\\s+".toRegex()).size
        val contextWindow = mutableListOf<String>()
        
        // 앞 3단어
        for (i in maxOf(0, selectedWordStart - 3) until selectedWordStart) {
            if (i < words.size) contextWindow.add(words[i])
        }
        
        // 뒤 3단어
        val selectedWordEnd = selectedWordStart + selectedText.split("\\s+".toRegex()).size
        for (i in selectedWordEnd until minOf(words.size, selectedWordEnd + 3)) {
            contextWindow.add(words[i])
        }
        
        return TextFingerprint(
            contentHash = contentHash,
            structureHash = structureHash,
            keywordHash = keywordHash,
            contextWindow = contextWindow
        )
    }
    
    // 헬퍼 메서드들
    private fun findSentenceStart(text: String, position: Int): Int {
        val sentenceEnders = listOf(".", "!", "?")
        for (i in position - 1 downTo 0) {
            if (sentenceEnders.contains(text[i].toString())) {
                return i + 1
            }
        }
        return 0
    }
    
    private fun findSentenceEnd(text: String, position: Int): Int {
        val sentenceEnders = listOf(".", "!", "?")
        for (i in position until text.length) {
            if (sentenceEnders.contains(text[i].toString())) {
                return i
            }
        }
        return text.length - 1
    }
    
    private fun calculateSentencePosition(
        sentenceStart: Int, 
        sentenceEnd: Int, 
        selectionStart: Int, 
        selectionEnd: Int
    ): SentencePosition {
        val sentenceLength = sentenceEnd - sentenceStart
        val selectionPosition = selectionStart - sentenceStart
        
        return when {
            selectionPosition < sentenceLength * 0.3 -> SentencePosition.BEGINNING
            selectionPosition > sentenceLength * 0.7 -> SentencePosition.END
            else -> SentencePosition.MIDDLE
        }
    }
    
    private fun extractKeywords(text: String): List<String> {
        // 간단한 키워드 추출 (실제로는 더 정교한 NLP 적용 가능)
        val stopWords = setOf("은", "는", "이", "가", "을", "를", "에", "에서", "으로", "로", "와", "과", "의", "도")
        return text.split("\\s+".toRegex())
            .filter { it.length > 1 && !stopWords.contains(it) }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }
    }
    
    private fun extractSemanticKeywords(text: String): List<String> {
        // 의미적으로 중요한 키워드들 추출
        val keywords = extractKeywords(text)
        return keywords.filter { keyword ->
            // 기술 용어, 고유명사, 동작 동사 등을 우선적으로 선택
            keyword.length > 2 && (
                keyword.matches("[A-Z].*".toRegex()) || // 대문자로 시작
                keyword.contains("개발") ||
                keyword.contains("구현") ||
                keyword.contains("설계") ||
                keyword.contains("경험")
            )
        }.take(10)
    }
    
    private fun analyzeSyntacticPattern(text: String): String {
        // 간단한 구문 패턴 분석
        return when {
            text.contains("했습니다") || text.contains("했다") -> "과거_행동"
            text.contains("합니다") || text.contains("한다") -> "현재_행동"
            text.contains("할 것입니다") || text.contains("할 예정") -> "미래_계획"
            text.matches(".*[을를] .*".toRegex()) -> "목적어구"
            text.matches(".*에서 .*".toRegex()) -> "장소구"
            else -> "일반구"
        }
    }
}
