package com.letterlens.application.port.out

import org.springframework.web.multipart.MultipartFile

interface FileStoragePort {
    fun store(file: MultipartFile, directory: String): FileStorageResult
    fun delete(fileUrl: String)
    fun getUrl(fileName: String): String
}

data class FileStorageResult(
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long
)
