package com.baiganov.fintech.presentation.ui.chat.recyclerview

import androidx.recyclerview.widget.DiffUtil
import com.baiganov.fintech.presentation.model.MessageFingerPrint
import com.baiganov.fintech.presentation.model.ItemFingerPrint

class MessageDiffUtil : DiffUtil.ItemCallback<ItemFingerPrint>() {

    override fun areItemsTheSame(oldItem: ItemFingerPrint, newItem: ItemFingerPrint): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ItemFingerPrint, newItem: ItemFingerPrint): Boolean {
        return when {
            oldItem is MessageFingerPrint && newItem is MessageFingerPrint -> {
                oldItem.message == newItem.message
            }
            oldItem is DateDividerFingerPrint && newItem is DateDividerFingerPrint -> {
                oldItem.date == newItem.date
            }
            else -> true
        }
    }
}