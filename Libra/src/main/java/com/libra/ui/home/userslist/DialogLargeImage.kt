package com.libra.ui.home.userslist

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.libra.R
import com.squareup.picasso.Picasso

class DialogLargeImage: DialogFragment(R.layout.dialog_large_image) {

    companion object{
        private const val IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String, manager: FragmentManager){
            val dialog = DialogLargeImage()
            val args = Bundle()
            args.putString(IMAGE_URL,imageUrl)
            dialog.arguments = args
            dialog.show(manager,"dialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.get().load(requireArguments().getString(IMAGE_URL))
            .into(view.findViewById<ImageView>(R.id.ivAvatar))
        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dismiss()
        }
    }
}