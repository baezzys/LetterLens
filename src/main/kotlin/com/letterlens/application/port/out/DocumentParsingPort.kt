package com.letterlens.application.port.out

import org.springframework.web.multipart.MultipartFile

interface DocumentParsingPort {
    fun extractText(file: MultipartFile): String
    fun extractQuestionsAndAnswers(text: String): List<QuestionAnswerPair>
}

data class QuestionAnswerPair(
    val questionText: String,
    val answer: String
)
