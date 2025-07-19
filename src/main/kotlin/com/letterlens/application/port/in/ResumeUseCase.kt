package com.letterlens.application.port.`in`

import com.letterlens.domain.*
import org.springframework.web.multipart.MultipartFile

interface ResumeUseCase {
    
    suspend fun uploadAndParseResume(command: UploadResumeCommand): UploadResumeResult
    
    suspend fun getResumeById(query: GetResumeQuery): ResumeDetailResult
    
    suspend fun getMyResumes(query: GetMyResumesQuery): List<ResumeListResult>
    
    suspend fun requestEvaluation(command: RequestEvaluationCommand): EvaluationResult
    
    suspend fun getSharedResume(query: GetSharedResumeQuery): SharedResumeResult
    
    suspend fun deleteResume(command: DeleteResumeCommand)
}

// Commands
data class UploadResumeCommand(
    val userId: UserId,
    val title: String,
    val file: MultipartFile
)

data class RequestEvaluationCommand(
    val userId: UserId,
    val resumeId: ResumeId
)

data class DeleteResumeCommand(
    val userId: UserId,
    val resumeId: ResumeId
)

// Queries
data class GetResumeQuery(
    val userId: UserId,
    val resumeId: ResumeId
)

data class GetMyResumesQuery(
    val userId: UserId,
    val page: Int = 0,
    val size: Int = 20
)

data class GetSharedResumeQuery(
    val shareToken: ShareToken,
    val userId: UserId? = null
)

// Results
data class UploadResumeResult(
    val resumeId: ResumeId,
    val title: String,
    val questionsCount: Int,
    val shareToken: ShareToken
)

data class ResumeDetailResult(
    val resumeId: ResumeId,
    val title: String,
    val originalFileName: String,
    val questions: List<QuestionResult>,
    val evaluations: List<EvaluationResult>,
    val shareToken: ShareToken,
    val isEvaluationComplete: Boolean,
    val overallScore: Double,
    val overallGrade: String,
    val createdAt: String,
    val updatedAt: String
)

data class ResumeListResult(
    val resumeId: ResumeId,
    val title: String,
    val questionsCount: Int,
    val evaluationsCount: Int,
    val isEvaluationComplete: Boolean,
    val overallScore: Double?,
    val createdAt: String
)

data class SharedResumeResult(
    val resumeId: ResumeId,
    val title: String,
    val questions: List<QuestionWithCommentsResult>,
    val evaluations: List<EvaluationResult>,
    val overallScore: Double,
    val overallGrade: String,
    val canComment: Boolean // 로그인 사용자인지 여부
)

data class QuestionResult(
    val questionId: QuestionId,
    val questionText: String,
    val answer: String,
    val orderIndex: Int,
    val wordCount: Int
)

data class QuestionWithCommentsResult(
    val questionId: QuestionId,
    val questionText: String,
    val answer: String,
    val orderIndex: Int,
    val comments: List<CommentResult>
)

data class EvaluationResult(
    val evaluationId: EvaluationId,
    val criteria: EvaluationCriteria,
    val score: Int,
    val grade: String,
    val feedback: String,
    val evaluatedAt: String
)

data class CommentResult(
    val commentId: CommentId,
    val authorName: String,
    val content: String,
    val startIndex: Int,
    val endIndex: Int,
    val confidence: Double,
    val isValid: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val createdAt: String
)
