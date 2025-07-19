package com.letterlens.domain

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService {
    
    fun createComment(
        id: CommentId,
        resumeId: ResumeId,
        questionId: QuestionId,
        authorId: UserId,
        content: String,
        textSelection: TextSelection
    ): Comment {
        return Comment(
            id = id,
            resumeId = resumeId,
            questionId = questionId,
            authorId = authorId,
            content = content,
            selectedText = textSelection,
            createdAt = LocalDateTime.now()
        )
    }
    
    fun validateCommentAccess(comment: Comment, userId: UserId, resume: Resume): Boolean {
        // 1. 자소서가 공유 가능한 상태인지 확인
        if (!resume.canBeAccessedBy(userId)) {
            return false
        }
        
        // 2. 코멘트가 해당 자소서에 속하는지 확인
        if (comment.resumeId != resume.id) {
            return false
        }
        
        return true
    }
    
    fun canUserDeleteComment(comment: Comment, userId: UserId, resume: Resume): Boolean {
        // 1. 자소서 소유자는 모든 코멘트 삭제 가능
        if (resume.isOwnedBy(userId)) {
            return true
        }
        
        // 2. 코멘트 작성자는 본인 코멘트만 삭제 가능
        return comment.canBeDeletedBy(userId)
    }
    
    fun getCommentsWithCurrentPositions(
        comments: List<Comment>,
        currentQuestionText: String
    ): List<CommentWithPosition> {
        return comments.mapNotNull { comment ->
            val location = comment.selectedText.findInText(currentQuestionText)
            
            if (location != null && location.isHighConfidence()) {
                CommentWithPosition(
                    comment = comment,
                    startIndex = location.startIndex,
                    endIndex = location.endIndex,
                    confidence = location.confidence,
                    isValid = true
                )
            } else if (location != null && location.isMediumConfidence()) {
                // 중간 신뢰도의 경우 경고와 함께 표시
                CommentWithPosition(
                    comment = comment,
                    startIndex = location.startIndex,
                    endIndex = location.endIndex,
                    confidence = location.confidence,
                    isValid = true,
                    hasPositionWarning = true
                )
            } else {
                // 위치를 찾을 수 없는 경우
                CommentWithPosition(
                    comment = comment,
                    startIndex = -1,
                    endIndex = -1,
                    confidence = 0.0,
                    isValid = false,
                    originalText = comment.selectedText.selectedText
                )
            }
        }
    }
}

data class CommentWithPosition(
    val comment: Comment,
    val startIndex: Int,
    val endIndex: Int,
    val confidence: Double,
    val isValid: Boolean,
    val hasPositionWarning: Boolean = false,
    val originalText: String? = null  // 위치를 찾을 수 없을 때 원본 텍스트 표시용
) {
    fun isHighConfidence(): Boolean = confidence >= 0.8
    fun isMediumConfidence(): Boolean = confidence >= 0.5 && confidence < 0.8
    fun isLowConfidence(): Boolean = confidence < 0.5
}
