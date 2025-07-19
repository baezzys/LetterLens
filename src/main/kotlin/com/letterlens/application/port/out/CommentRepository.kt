package com.letterlens.application.port.out

import com.letterlens.domain.*

interface CommentRepository {
    
    suspend fun save(comment: Comment): Comment
    
    suspend fun findById(commentId: CommentId): Comment?
    
    suspend fun findByQuestionId(questionId: QuestionId): List<Comment>
    
    suspend fun findByResumeId(resumeId: ResumeId): List<Comment>
    
    suspend fun deleteById(commentId: CommentId)
    
    suspend fun existsByIdAndAuthorId(commentId: CommentId, authorId: UserId): Boolean
}
