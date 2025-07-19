package com.letterlens.application.service

import com.letterlens.application.port.`in`.*
import com.letterlens.application.port.out.*
import com.letterlens.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional
class CommentApplicationService(
    private val commentRepository: CommentRepository,
    // private val resumeRepository: ResumeRepository,  // 주석처리 - ResumeRepository 구현 후 활성화
    private val userRepository: UserRepository,
    private val commentService: CommentService,
    private val textSelectionBuilder: TextSelectionBuilder
) : CommentUseCase {
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    override suspend fun createComment(command: CreateCommentCommand): CommentResult {
        // TODO: ResumeRepository 구현 후 활성화
        throw NotImplementedError("Comment functionality will be implemented later")
        
        /* 주석처리 - ResumeRepository 구현 후 활성화
        // 1. 공유 자소서 조회
        val resume = resumeRepository.findByShareToken(command.shareToken)
            ?: throw IllegalArgumentException("Shared resume not found")
        
        // 2. 접근 권한 확인
        if (!resume.canBeAccessedBy(command.userId)) {
            throw IllegalArgumentException("Access denied to shared resume")
        }
        
        // 3. 질문 존재 확인
        val question = resume.getQuestionById(command.questionId)
            ?: throw IllegalArgumentException("Question not found in resume")
        
        // 4. 사용자 정보 조회
        val user = userRepository.findById(command.userId)
            ?: throw IllegalArgumentException("User not found")
        
        // 5. 텍스트 선택 영역 생성
        val textSelection = textSelectionBuilder.createTextSelection(
            questionId = command.questionId,
            fullText = question.answer,
            startIndex = command.startIndex,
            endIndex = command.endIndex
        )
        
        // 6. 코멘트 생성
        val comment = commentService.createComment(
            id = CommentId(generateId()),
            resumeId = resume.id,
            questionId = command.questionId,
            authorId = command.userId,
            content = command.content,
            textSelection = textSelection
        )
        
        // 7. 저장
        val savedComment = commentRepository.save(comment)
        
        return CommentResult(
            commentId = savedComment.id,
            authorName = user.username,
            content = savedComment.content,
            startIndex = command.startIndex,
            endIndex = command.endIndex,
            confidence = 1.0, // 새로 생성된 코멘트는 항상 정확한 위치
            isValid = true,
            canEdit = true, // 작성자는 편집 가능
            canDelete = true, // 작성자는 삭제 가능
            createdAt = savedComment.createdAt.format(dateTimeFormatter)
        )
        */
    }
    
    override suspend fun updateComment(command: UpdateCommentCommand): CommentResult {
        // TODO: ResumeRepository 구현 후 활성화
        throw NotImplementedError("Comment functionality will be implemented later")
    }
    
    override suspend fun deleteComment(command: DeleteCommentCommand) {
        // TODO: ResumeRepository 구현 후 활성화
        throw NotImplementedError("Comment functionality will be implemented later")
    }
    
    override suspend fun getCommentsByQuestion(query: GetCommentsByQuestionQuery): List<CommentResult> {
        // TODO: ResumeRepository 구현 후 활성화
        return emptyList()
    }
    
    private fun generateId(): Long {
        return System.currentTimeMillis() // 실제로는 ID 생성 전략 사용
    }
}
