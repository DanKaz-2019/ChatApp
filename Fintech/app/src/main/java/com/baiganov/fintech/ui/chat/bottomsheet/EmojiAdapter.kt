package com.baiganov.fintech.ui.chat.bottomsheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baiganov.fintech.R
import com.baiganov.fintech.model.Emoji

class EmojiAdapter(private val emojiClickListener: EmojiClickListener) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    private var emojis = emptyList<Emoji>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        return EmojiViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.emoji_grid_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(emojis[position], emojiClickListener)
    }

    override fun getItemCount(): Int {
        return emojis.size
    }

    fun setData(newData: List<Emoji>) {
        emojis = newData
        notifyDataSetChanged()
    }

    class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvEmoji: TextView = itemView.findViewById(R.id.emoji_item)

        fun bind(emoji: Emoji, emojiClickListener: EmojiClickListener) {
            tvEmoji.text = String(Character.toChars(emoji.code))
            tvEmoji.setOnClickListener {
                emojiClickListener.emojiClick(emoji.name)
            }
        }
    }
}