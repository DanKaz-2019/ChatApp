package com.baiganov.fintech.network

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder().addHeader(AUTH_HEADER, credential)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val API_KEY = "AgYl57TrQkneXFxwntvewfgoGvrPTAHL"

        private val credential: String = Credentials.basic("daniyar.baiganov@gmail.com", API_KEY)
    }
}