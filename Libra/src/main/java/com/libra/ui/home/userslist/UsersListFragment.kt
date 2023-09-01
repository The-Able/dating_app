package com.libra.ui.home.userslist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.libra.R
import com.libra.entity.User
import com.libra.fragments.BaseFragment
import com.libra.fragments.BaseHostedFragment
import com.libra.ui.home.cafeslist.CafeFragment

class UsersListFragment : BaseHostedFragment() {

    companion object {

        private const val USERS = "users"

        fun newInstance(users: ArrayList<User>): BaseFragment {
            val args = Bundle()
            args.putParcelableArrayList(USERS,users)
            val fragment = UsersListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.fragment_users_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = UserListAdapter(requireArguments().getParcelableArrayList<User>(USERS)?.toList()?: emptyList()){ user ->
            DialogLargeImage.newInstance(user.imageUrl,childFragmentManager)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvUsers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun getTitleToolbar(): Int {
        return R.string.who_s_online
    }

    override fun getLeftImageButton(): ImageButton {
        val imageButton = ImageButton(requireContext())
        imageButton.setImageResource(R.drawable.ic_action_back)
        return imageButton
    }
}