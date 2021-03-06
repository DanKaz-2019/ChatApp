package com.baiganov.fintech.domain.usecase.chat

import com.baiganov.fintech.domain.repository.MessageRepository
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class LoadMessageUseCase @Inject constructor(private val messageRepository: MessageRepository) {

    fun execute(
        streamTitle: String,
        topicTitle: String?,
        anchor: Long,
        streamId: Int,
        numBefore: Int,
        numAfter: Int
    ): Completable {
        return messageRepository.loadMessages(  streamTitle,
            topicTitle,
            anchor,
            streamId,
            numBefore,
            numAfter)
    }
}