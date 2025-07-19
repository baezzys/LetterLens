package com.letterlens.application.service

import com.letterlens.application.port.`in`.*
import com.letterlens.application.port.out.*
import com.letterlens.domain.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional
class ResumeApplicationService(
    private val resumeRepository: ResumeRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val fileStoragePort: FileStoragePort,
    private val documentParsingPort: DocumentParsingPort,
    private val aiEvaluationPort: AiEvaluationPort,
    private val evaluationCriteriaRepository: EvaluationCriteriaRepository,
    private val resumeService: ResumeService,
    private val evaluationService: EvaluationService,
    private val commentService: CommentService,
    private val textSelectionBuilder: TextSelectionBuilder
) : ResumeUseCase {
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    override suspend fun uploadAndParseResume(command: UploadResumeCommand): UploadResumeResult {
        // 1. 사용자 존재 확인
        val user = userRepository.findById(command.userId)
            ?: throw IllegalArgumentException("User not found: ${command.userId}")
        
        // 2. 파일 저장
        val fileResult = fileStoragePort.store(command.file, "resumes")
        
        // 3. 문서 텍스트 추출
        val extractedText = documentParsingPort.extractText(command.file)
        
        // 4. AI를 통한 문항 추출
        val questionAnswerPairs = documentParsingPort.extractQuestionsAndAnswers(extractedText)
        
        if (questionAnswerPairs.isEmpty()) {
            throw IllegalArgumentException("No questions found in the document")
        }
        
        // 5. 도메인 객체 생성
        val resumeId = ResumeId(generateId())
        val questions = questionAnswerPairs.mapIndexed { index, pair ->
            Question(
                id = QuestionId(generateId()),
                resumeId = resumeId,
                questionText = pair.questionText,
                answer = pair.answer,
                orderIndex = index
            )
        }
        
        val resume = resumeService.createResume(
            id = resumeId,
            userId = command.userId,
            title = command.title,
            originalFileName = command.file.originalFilename ?: "unknown",
            fileUrl = fileResult.fileUrl,
            questions = questions
        )
        
        // 6. 저장
        val savedResume = resumeRepository.save(resume)
        
        return UploadResumeResult(
            resumeId = savedResume.id,
            title = savedResume.title,
            questionsCount = savedResume.questions.size,
            shareToken = savedResume.shareToken
        )
    }
    
    override suspend fun getResumeById(query: GetResumeQuery): ResumeDetailResult {
        val resume = resumeRepository.findByIdAndUserId(query.resumeId, query.userId)
            ?: throw IllegalArgumentException("Resume not found or access denied")
        
        return mapToResumeDetailResult(resume)
    }
    
    override suspend fun getMyResumes(query: GetMyResumesQuery): List<ResumeListResult> {
        val pageable = PageRequest.of(query.page, query.size)
        val resumesPage = resumeRepository.findByUserId(query.userId, pageable)
        
        return resumesPage.content.map { resume ->
            ResumeListResult(
                resumeId = resume.id,
                title = resume.title,
                questionsCount = resume.questions.size,
                evaluationsCount = resume.evaluations.size,
                isEvaluationComplete = resume.isEvaluationComplete(),
                overallScore = if (resume.evaluations.isNotEmpty()) resume.getOverallScore() else null,
                createdAt = resume.createdAt.format(dateTimeFormatter)
            )
        }
    }
    
    override suspend fun requestEvaluation(command: RequestEvaluationCommand): EvaluationResult {
        // 1. 자소서 조회 및 권한 확인
        val resume = resumeRepository.findByIdAndUserId(command.resumeId, command.userId)
            ?: throw IllegalArgumentException("Resume not found or access denied")
        
        // 2. 평가 기준 조회
        val criteriaConfigs = evaluationCriteriaRepository.findAll()
            .filter { it.isActive }
        
        if (criteriaConfigs.isEmpty()) {
            throw IllegalStateException("No active evaluation criteria found")
        }
        
        // 3. AI 평가 요청
        val evaluationRequest = EvaluationRequest(
            questions = resume.questions.map { question ->
                QuestionForEvaluation(question.questionText, question.answer) 
            },
            criteria = criteriaConfigs.map { it.criteria }
        )
        
        val evaluationResponse = aiEvaluationPort.evaluateResume(evaluationRequest)
        
        // 4. 평가 결과를 도메인 객체로 변환 및 저장
        var updatedResume = resume
        evaluationResponse.evaluations.forEach { aiEvaluation ->
            val evaluation = Evaluation(
                id = EvaluationId(generateId()),
                resumeId = resume.id,
                criteria = aiEvaluation.criteria,
                score = Score(aiEvaluation.score),
                feedback = aiEvaluation.feedback,
                evaluatedAt = java.time.LocalDateTime.now()
            )
            
            updatedResume = resumeService.addEvaluationToResume(
                updatedResume,
                aiEvaluation.criteria,
                Score(aiEvaluation.score),
                aiEvaluation.feedback
            )
        }
        
        // 5. 저장
        resumeRepository.save(updatedResume)
        
        // 6. 결과 반환 (첫 번째 평가 기준의 결과)
        val firstEvaluation = evaluationResponse.evaluations.first()
        return EvaluationResult(
            evaluationId = EvaluationId(generateId()),
            criteria = firstEvaluation.criteria,
            score = firstEvaluation.score,
            grade = Score(firstEvaluation.score).getGrade(),
            feedback = firstEvaluation.feedback,
            evaluatedAt = java.time.LocalDateTime.now().format(dateTimeFormatter)
        )
    }
    
    override suspend fun getSharedResume(query: GetSharedResumeQuery): SharedResumeResult {
        val resume = resumeRepository.findByShareToken(query.shareToken)
            ?: throw IllegalArgumentException("Shared resume not found")
        
        // 접근 권한 확인
        if (!resume.canBeAccessedBy(query.userId)) {
            throw IllegalArgumentException("Access denied to shared resume")
        }
        
        // 코멘트 정보 포함한 질문 목록 생성
        val questionsWithComments = resume.questions.map { question ->
            val comments = commentRepository.findByQuestionId(question.id)
            val commentsWithPosition = commentService.getCommentsWithCurrentPositions(
                comments, question.answer
            )
            
            val commentResults = commentsWithPosition.map { commentWithPos ->
                val user = userRepository.findById(commentWithPos.comment.authorId)
                CommentResult(
                    commentId = commentWithPos.comment.id,
                    authorName = user?.username ?: "Unknown",
                    content = commentWithPos.comment.content,
                    startIndex = commentWithPos.startIndex,
                    endIndex = commentWithPos.endIndex,
                    confidence = commentWithPos.confidence,
                    isValid = commentWithPos.isValid,
                    canEdit = query.userId?.let { commentWithPos.comment.canBeEditedBy(it) } ?: false,
                    canDelete = query.userId?.let { 
                        commentService.canUserDeleteComment(commentWithPos.comment, it, resume) 
                    } ?: false,
                    createdAt = commentWithPos.comment.createdAt.format(dateTimeFormatter)
                )
            }
            
            QuestionWithCommentsResult(
                questionId = question.id,
                questionText = question.questionText,
                answer = question.answer,
                orderIndex = question.orderIndex,
                comments = commentResults
            )
        }
        
        return SharedResumeResult(
            resumeId = resume.id,
            title = resume.title,
            questions = questionsWithComments,
            evaluations = resume.evaluations.map { mapToEvaluationResult(it) },
            overallScore = resume.getOverallScore(),
            overallGrade = resume.getOverallGrade(),
            canComment = query.userId != null
        )
    }
    
    override suspend fun deleteResume(command: DeleteResumeCommand) {
        // 1. 권한 확인
        if (!resumeRepository.existsByIdAndUserId(command.resumeId, command.userId)) {
            throw IllegalArgumentException("Resume not found or access denied")
        }
        
        // 2. 관련 코멘트들 삭제
        val comments = commentRepository.findByResumeId(command.resumeId)
        comments.forEach { comment ->
            commentRepository.deleteById(comment.id)
        }
        
        // 3. 자소서 삭제
        resumeRepository.deleteById(command.resumeId)
    }
    
    private fun mapToResumeDetailResult(resume: Resume): ResumeDetailResult {
        return ResumeDetailResult(
            resumeId = resume.id,
            title = resume.title,
            originalFileName = resume.originalFileName,
            questions = resume.questions.map { mapToQuestionResult(it) },
            evaluations = resume.evaluations.map { mapToEvaluationResult(it) },
            shareToken = resume.shareToken,
            isEvaluationComplete = resume.isEvaluationComplete(),
            overallScore = resume.getOverallScore(),
            overallGrade = resume.getOverallGrade(),
            createdAt = resume.createdAt.format(dateTimeFormatter),
            updatedAt = resume.updatedAt.format(dateTimeFormatter)
        )
    }
    
    private fun mapToQuestionResult(question: Question): QuestionResult {
        return QuestionResult(
            questionId = question.id,
            questionText = question.questionText,
            answer = question.answer,
            orderIndex = question.orderIndex,
            wordCount = question.getAnswerWordCount()
        )
    }
    
    private fun mapToEvaluationResult(evaluation: Evaluation): EvaluationResult {
        return EvaluationResult(
            evaluationId = evaluation.id,
            criteria = evaluation.criteria,
            score = evaluation.score.value,
            grade = evaluation.score.getGrade(),
            feedback = evaluation.feedback,
            evaluatedAt = evaluation.evaluatedAt.format(dateTimeFormatter)
        )
    }
    
    private fun generateId(): Long {
        return System.currentTimeMillis() // 실제로는 ID 생성 전략 사용
    }
}
