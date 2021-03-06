package com.baiganov.fintech.data.repository

import android.net.Uri
import com.baiganov.fintech.data.UriReader
import com.baiganov.fintech.data.datasource.MessageLocalDataSource
import com.baiganov.fintech.data.datasource.MessageRemoteDataSource
import com.baiganov.fintech.data.db.entity.MessageEntity
import com.baiganov.fintech.data.model.FileResponse
import com.baiganov.fintech.data.model.Message
import com.baiganov.fintech.domain.repository.MessageRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val remoteDataSource: MessageRemoteDataSource,
    private val localDataSource: MessageLocalDataSource,
    private val uriReader: UriReader
) : MessageRepository {

    override fun loadMessages(
        streamTitle: String,
        topicTitle: String?,
        anchor: Long,
        streamId: Int,
        numBefore: Int,
        numAfter: Int
    ): Completable {

        return remoteDataSource.loadMessages(streamTitle, topicTitle, anchor, numBefore, numAfter)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                topicTitle?.let {
                    remoteDataSource.markTopicAsRead(streamId, topicTitle)
                } ?: remoteDataSource.markStreamAsRead(streamId)
                //Stream

                val messages = mapToEntity(it.messages)
                //Topic else stream
                topicTitle?.let {
                    localDataSource.deleteTopicMessages(topicTitle, streamId)
                        .andThen(localDataSource.saveMessages(messages))
                } ?: localDataSource.deleteStreamMessages(streamId)
                    .andThen(localDataSource.saveMessages(messages))
            }
    }

    override fun editTopic(messageId: Int, newTopic: String): Completable {
        return remoteDataSource.editTopic(messageId, newTopic)
    }

    override fun updateMessage(
        streamTitle: String,
        topicTitle: String?,
        anchor: Long,
        numBefore: Int,
        numAfter: Int
    ): Completable {

        return remoteDataSource.loadMessages(streamTitle, topicTitle, anchor, numBefore, numAfter)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                val messages = mapToEntity(it.messages)

                localDataSource.saveMessages(messages)
            }
    }

    override fun loadNextMessages(
        streamTitle: String,
        topicTitle: String?,
        anchor: Long,
        numBefore: Int,
        numAfter: Int
    ): Completable {

        return remoteDataSource.loadMessages(streamTitle, topicTitle, anchor, numBefore, numAfter)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                val messages = mapToEntity(it.messages)

                localDataSource.saveMessages(messages)
            }
    }

    override fun sendMessage(streamId: Int, message: String, topicTitle: String): Completable {
        return remoteDataSource.sendMessage(
            streamId = streamId,
            message = message,
            topicTitle = topicTitle
        )
    }

    override fun addReaction(messageId: Int, emojiName: String): Completable {
        return remoteDataSource.addReaction(messageId, emojiName)
    }

    override fun deleteReaction(messageId: Int, emojiName: String): Completable {
        return remoteDataSource.deleteReaction(messageId, emojiName)
    }

    override fun deleteMessage(messageId: Int): Completable {
        return remoteDataSource.deleteMessage(messageId)
    }

    override fun getTopicMessages(
        topicTitle: String,
        streamId: Int
    ): Flowable<List<MessageEntity>> =
        localDataSource.getTopicMessages(topicTitle, streamId)

    override fun getStreamMessages(
        streamId: Int
    ): Flowable<List<MessageEntity>> =
        localDataSource.getStreamMessages(streamId)

    override fun uploadFile(uri: Uri, type: String, name: String): Single<FileResponse> {
        val bytes = uriReader.readBytes(uri)

        bytes?.let {
            val body = bytes.toRequestBody(type.toMediaType())
            val part = MultipartBody.Part.createFormData(NAME_FILE, name, body)


            return remoteDataSource.uploadFile(part)
        }
        return Single.just(null)
    }

    override fun editMessage(messageId: Int, content: String): Completable {
        return remoteDataSource.editMessage(messageId, content)
    }

    private fun mapToEntity(list: List<Message>): List<MessageEntity> {
        return list.map { message ->
            MessageEntity(
                id = message.id,
                avatarUrl = message.avatarUrl,
                content = message.content,
                reactions = message.reactions,
                senderEmail = message.senderEmail,
                senderFullName = message.senderFullName,
                senderId = message.senderId,
                timestamp = message.timestamp,
                streamId = message.streamId,
                topicName = message.topicName,
            )
        }
    }

    companion object {
        private const val NAME_FILE = "file"
    }
}