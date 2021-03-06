package com.baiganov.fintech.domain.usecase.chat

import com.baiganov.fintech.domain.repository.MessageRepository
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class UpdateMessageUseCase @Inject constructor(private val messageRepository: MessageRepository) {

    fun execute(
        streamTitle: String,
        topicTitle: String?,
        anchor: Long,
        numBefore: Int,
        numAfter: Int
    ): Completable {
        return messageRepository.updateMessage(
            streamTitle = streamTitle,
            topicTitle = topicTitle,
            anchor = anchor,
            numBefore = numBefore,
            numAfter = numAfter
        )
    }
}