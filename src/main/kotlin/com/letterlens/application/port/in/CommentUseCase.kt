package com.letterlens.application.port.`in`

import com.letterlens.domain.*

interface CommentUseCase {
    
    suspend fun createComment(command: CreateCommentCommand): CommentResult
    
    suspend fun updateComment(command: UpdateCommentCommand): CommentResult
    
    suspend fun deleteComment(command: DeleteCommentCommand)
    
    suspend fun getCommentsByQuestion(query: GetCommentsByQuestionQuery): List<CommentResult>
}

// Commands
data class CreateCommentCommand(
    val userId: UserId,
    val shareToken: ShareToken,
    val questionId: QuestionId,
    val content: String,
    val selectedText: String,
    val startIndex: Int,
    val endIndex: Int
)

data class UpdateCommentCommand(
    val userId: UserId,
    val commentId: CommentId,
    val content: String
)

data class DeleteCommentCommand(
    val userId: UserId,
    val commentId: CommentId
)

// Queries
data class GetCommentsByQuestionQuery(
    val questionId: QuestionId,
    val userId: UserId? = null
)
