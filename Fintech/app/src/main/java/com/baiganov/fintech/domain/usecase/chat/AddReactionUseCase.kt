package com.baiganov.fintech.domain.usecase.chat

import com.baiganov.fintech.domain.repository.MessageRepository
import io.reactivex.Completable
import javax.inject.Inject

class AddReactionUseCase @Inject constructor(private val messageRepository: MessageRepository) {

    fun execute(messageId: Int, emojiName: String): Completable {
        return messageRepository.addReaction(messageId, emojiName)
    }
}