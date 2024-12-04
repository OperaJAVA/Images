package ru.netology.nmedia.errors

import java.io.IOException
import java.sql.SQLException

sealed class AppError() : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbException
            is IOException -> Errors
            else -> UnknownException
        }
    }
}

class ApiException(code: String) : AppError()
object Errors : AppError() {
    private fun readResolve(): Any = Errors
}

object DbException : AppError() {
    private fun readResolve(): Any = DbException
}

object UnknownException : AppError() {
    private fun readResolve(): Any = UnknownException
}