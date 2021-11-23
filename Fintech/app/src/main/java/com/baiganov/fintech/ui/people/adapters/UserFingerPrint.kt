package com.baiganov.fintech.ui.people.adapters

import com.baiganov.fintech.R
import com.baiganov.fintech.model.response.User
import com.baiganov.fintech.ui.channels.streams.recyclerview.fingerprints.ItemFingerPrint

class UserFingerPrint(
    val user: User
) : ItemFingerPrint {
    override val viewType: Int =  R.layout.item_user
    override val id: Int = user.userId
}