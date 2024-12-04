package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.errors.ApiException
import ru.netology.nmedia.errors.AppError
import ru.netology.nmedia.errors.Errors
import ru.netology.nmedia.errors.UnknownException
import java.io.IOException

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            dao.getAll()
            val response = Api.service.getAll()
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
            val body = response.body() ?: throw ApiException(response.message())
            dao.insert(body.toEntity().map {
                it.copy(viewed = true)
            })
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun getNewPosts() {
        try {
            dao.viewedPosts()
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override fun getNewerCount(firstId: Long): Flow<Int> = flow {
        while (true) {
            val response = Api.service.getNewer(firstId)
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
            val body =
                response.body() ?: throw ApiException(response.message())
            dao.insert(body.toEntity().map {
                it.copy(viewed = false)
            })
            emit(body.size)
            delay(100_000L)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun save(post: Post) {
        try {
            val response = Api.service.save(post)
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
            val body = response.body() ?: throw ApiException(response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = uploadWithContent(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.id, TypeAttachment.IMAGE))
            save(postWithAttachment)
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun uploadWithContent(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData("file",
                upload.file.name,
                upload.file.asRequestBody())

            val content = MultipartBody.Part.createFormData("content", "text")

            val response = Api.service.uploadPhoto(media, content)
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
            return response.body() ?: throw ApiException(response.message())
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            dao.likeById(id)
            val response = Api.service.likeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
            val body = response.body() ?: throw ApiException(response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            dao.likeById(id)
            val response = Api.service.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
            val body = response.body() ?: throw ApiException(response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = Api.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.message())
            }
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}