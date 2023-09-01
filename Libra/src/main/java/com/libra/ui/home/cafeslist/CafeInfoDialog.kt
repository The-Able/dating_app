package com.libra.ui.home.cafeslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.libra.R
import com.libra.R.drawable
import com.libra.entity.Cafe
import com.libra.fragments.BaseDialogFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_cafe_info.btnWaze
import kotlinx.android.synthetic.main.dialog_cafe_info.imgCafeLogo
import kotlinx.android.synthetic.main.dialog_cafe_info.imgClose
import kotlinx.android.synthetic.main.dialog_cafe_info.tvAddress
import kotlinx.android.synthetic.main.dialog_cafe_info.tvDistance
import kotlinx.android.synthetic.main.dialog_cafe_info.tvOpenHours
import kotlinx.android.synthetic.main.dialog_cafe_info.tvTitle

/**
 * @author Alex on 20.01.2019.
 */
class CafeInfoDialog : BaseDialogFragment() {

    companion object {
        private const val EXTRA_CAFE = "key_cafe"

        fun instance(cafe: Cafe): CafeInfoDialog {
            return CafeInfoDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CAFE, cafe)
                }
            }
        }
    }

    var wazeClickListener: (Cafe) -> Unit = {}
    private val cafe: Cafe by lazy { arguments!!.getSerializable(EXTRA_CAFE) as Cafe }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_cafe_info, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        tvTitle.text = cafe.name
        tvAddress.text = cafe.address
        tvDistance.text = cafe.getDistanceLabel(context)
        tvDistance.visibility = if (cafe.distance > 0) View.VISIBLE else View.GONE
        tvOpenHours.text = cafe.openHours
        tvOpenHours.visibility = if (cafe.openHours != null && cafe.openHours.isNotEmpty()) View.VISIBLE else View.GONE
        imgClose.setOnClickListener { dismiss() }
        btnWaze.setOnClickListener {
            wazeClickListener.invoke(cafe)
            dismiss()
        }
        Picasso.get()
                .load(cafe.imageUrl)
                .fit()
                .centerCrop()
                .placeholder(drawable.ic_restaurant_menu_24dp)
                .error(drawable.ic_restaurant_menu_24dp)
                .into(imgCafeLogo)
    }
}