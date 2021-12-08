package com.baiganov.fintech.data.network

import com.baiganov.fintech.data.model.response.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*

interface ChatApi {

    @GET("streams")
    fun getStreams(): Single<AllStreamsResponse>

    @GET("users/me/subscriptions")
    fun getSubscribedStreams(): Single<SubscribedStreamsResponse>

    @GET("users/me/{stream_id}/topics")
    fun getTopics(@Path("stream_id") streamId: Int): Single<TopicsResponse>

    @GET("messages")
    fun getMessages(
        @Query("anchor") anchor: Long,
        @Query("num_before") numBefore: Int,
        @Query("num_after") numAfter: Int = 0,
        @Query("narrow") narrow: String,
        @Query("apply_markdown") applyMarkdown: Boolean = false
    ): Single<MessagesResponse>

    @GET("users")
    fun getUsers(): Single<UsersResponse>

    @GET("users/me")
    fun getOwnUser(): Single<User>

    @POST("messages")
    fun sendMessage(
        @Query("type") type: String = "stream",
        @Query("to") streamId: Int,
        @Query("content") text: String,
        @Query("topic") topicTitle: String
    ): Completable

    @POST("users/me/subscriptions")
    fun subscribeOnStreams(
        @Query("subscriptions") subscriptions: String,
    ): Completable

    @Multipart
    @POST("user_uploads")
    fun uploadFile(
        @Part file: MultipartBody.Part
    ): Single<FileResponse>

    @POST("messages/{message_id}/reactions")
    fun addReaction(
        @Path("message_id") messageId: Int,
        @Query("emoji_name") emojiName: String,
        @Query("reaction_type") reactionType: String = "unicode_emoji",
    ): Completable

    @DELETE("messages/{message_id}/reactions")
    fun deleteReaction(
        @Path("message_id") messageId: Int,
        @Query("emoji_name") emojiName: String
    ): Completable

    @DELETE("messages/{msg_id}")
    fun deleteMessage(
        @Path("msg_id") id: Int
    ): Completable
}