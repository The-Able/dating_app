package com.libra.ui.home.cafeslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.libra.R
import com.libra.entity.Cafe
import com.libra.fragments.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_cafe_login_error.*

class CafeLoginErrorDialog : BaseDialogFragment() {
    companion object {
        private const val EXTRA_CAFE = "key_cafe"
        private const val EXTRA_MSG = "key_msg"
        fun instance(cafe: Cafe, msg: String): CafeLoginErrorDialog {
            return CafeLoginErrorDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CAFE, cafe)
                    putString(EXTRA_MSG, msg)
                }
            }
        }
    }

    private val cafe: Cafe by lazy { arguments!!.getSerializable(EXTRA_CAFE) as Cafe }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_cafe_login_error, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        tvAddress.text = arguments!!.getString(EXTRA_MSG)
        tvAddress.visibility = View.VISIBLE
        imgClose.setOnClickListener { dismiss() }
    }
}