package com.baiganov.fintech.data

import com.baiganov.fintech.data.db.MessagesDao
import com.baiganov.fintech.data.db.entity.MessageEntity
import com.baiganov.fintech.data.network.ChatApi
import com.baiganov.fintech.model.response.Message
import com.baiganov.fintech.model.response.Narrow
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MessageRepository(private val service: ChatApi, private val messagesDao: MessagesDao) {

    fun loadMessages(stream: String, topicName: String, anchor: Long, streamId: Int): Completable {
        val narrow = getNarrow(stream, topicName)

        return service.getMessages(narrow = narrow, anchor = anchor)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                val messages = mapToEntity(it.messages)

                messagesDao.deleteTopicMessages(topicName, streamId)
                    .andThen(messagesDao.saveMessages(messages))
            }
    }

    fun updateMessage(
        stream: String,
        topic: String,
        anchor: Long,
        numBefore: Int = 1
    ): Completable {
        val narrow = getNarrow(stream, topic)

        return service.getMessages(narrow = narrow, anchor = anchor, numBefore = numBefore)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                val messages = mapToEntity(it.messages)

                messagesDao.saveMessages(messages)
            }
    }

    fun loadNextMessages(stream: String, topic: String, anchor: Long): Completable {
        val narrow = getNarrow(stream, topic)

        return service.getMessages(narrow = narrow, anchor = anchor)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                val messages = mapToEntity(it.messages)

                messagesDao.saveMessages(messages)
            }
    }

    fun sendMessage(streamId: Int, message: String, topicTitle: String): Completable {
        return service.sendMessage(streamId = streamId, text = message, topicTitle = topicTitle)
    }

    fun addReaction(messageId: Int, emojiName: String): Completable {
        return service.addReaction(messageId, emojiName)
    }

    fun deleteReaction(messageId: Int, emojiName: String): Completable {
        return service.deleteReaction(messageId, emojiName)
    }

    fun getMessagesFromDb(topicName: String, streamId: Int): Flowable<List<MessageEntity>> =
        messagesDao.getTopicMessages(topicName, streamId)

    private fun getNarrow(stream: String, topic: String): String {
        return Json.encodeToString(
            listOf(
                Narrow(OPERATOR_STREAM, stream),
                Narrow(OPERATOR_TOPIC, topic)
            )
        )
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
        private const val OPERATOR_STREAM = "stream"
        private const val OPERATOR_TOPIC = "topic"
    }
}