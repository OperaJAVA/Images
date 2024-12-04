package ru.netology.nmedia.repository

import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.errors.ApiException
import ru.netology.nmedia.errors.Errors
import ru.netology.nmedia.errors.UnknownException
import java.io.IOException

class AuthRepository {

    suspend fun authUser(login: String, password: String): User {
        try {
            val response = Api.service.updateUser(login, password)
//            println(1)
            if (!response.isSuccessful) {
//                println("authorized")
                throw ApiException(response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    suspend fun registrationUser(login: String, password: String, name: String): User {
        try {
            val response = Api.service.registrationUser(login, password, name)
//            println(2)
            if (!response.isSuccessful) {
//                println("register")
                throw ApiException(response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw Errors
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}