package com.letterlens.application.port.out

import com.letterlens.domain.Resume
import com.letterlens.domain.ResumeId
import com.letterlens.domain.ShareToken
import com.letterlens.domain.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResumeRepository {
    suspend fun save(resume: Resume): Resume
    suspend fun findById(id: ResumeId): Resume?
    suspend fun findByIdAndUserId(id: ResumeId, userId: UserId): Resume?
    suspend fun findByUserId(userId: UserId, pageable: Pageable): Page<Resume>
    suspend fun findByShareToken(shareToken: ShareToken): Resume?
    suspend fun delete(resume: Resume)
    suspend fun deleteById(id: ResumeId)
    suspend fun existsByUserIdAndTitle(userId: UserId, title: String): Boolean
    suspend fun existsByIdAndUserId(id: ResumeId, userId: UserId): Boolean
}
