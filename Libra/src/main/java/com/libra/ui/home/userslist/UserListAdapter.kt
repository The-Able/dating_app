package com.libra.ui.home.userslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.libra.R
import com.libra.adapters.BaseRecyclerAdapter
import com.libra.entity.User
import com.squareup.picasso.Picasso

class UserListAdapter(users: List<User>, val onClick: (User) -> Unit): BaseRecyclerAdapter<User>(users) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserHolder(inflater.inflate(R.layout.item_users_list, parent, false))
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        val userHolder = holder as UserHolder
        val user = getItem(position)
        Picasso.get().load(user.imageUrl).into(userHolder.ivAvatar)
        userHolder.tvName.text = user.name
        userHolder.tvLastSeen.text = user.getLastSeenString(holder.context)

        userHolder.itemView.setOnClickListener {
            onClick.invoke(user)
        }
    }

    inner class UserHolder(view: View): BaseHolder(view){

        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvLastSeen = view.findViewById<TextView>(R.id.tvLastSeen)

    }
}