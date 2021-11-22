package com.baiganov.fintech.ui.chat.recyclerview.viewholders

import android.view.View
import com.baiganov.fintech.R
import com.baiganov.fintech.ui.chat.bottomsheet.TypeClick
import com.baiganov.fintech.ui.chat.recyclerview.BaseViewHolder
import com.baiganov.fintech.ui.chat.recyclerview.MessageFingerPrint
import com.baiganov.fintech.сustomview.MessageViewGroup
import com.baiganov.fintech.сustomview.OnClickMessage
import org.jsoup.Jsoup

class InComingMessageViewHolder(itemView: View, private val clickListener: OnClickMessage) : BaseViewHolder<MessageFingerPrint>(itemView) {

    private val viewGroup: MessageViewGroup = itemView.findViewById(R.id.incoming_message)

    override fun bind(item: MessageFingerPrint) {
        val message = item.message
        viewGroup.apply {
            setReactions(
                message.reactions,
                clickListener,
                message.id
            )
            text = Jsoup.parse(message.content).text()
            author = message.senderFullName
            addReactionByButton(clickListener, message.id, item)
        }

        itemView.setOnLongClickListener {
            clickListener.onItemClick(TypeClick.OpenActionDialog(message.id))
            return@setOnLongClickListener true
        }
    }
}