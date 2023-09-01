package com.libra.ui.home.cafeslist

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.libra.R
import com.libra.adapters.BaseRecyclerAdapter
import com.libra.entity.Cafe
import com.libra.support.TypefaceCache
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

/**
 * @author Alex on 20.01.2019.
 */
class CafeListAdapter : BaseRecyclerAdapter<Cafe>() {

    companion object {
        private const val TYPE_FULL = 1
        private const val TYPE_EMPTY = 2
    }

    var isLoginEnabled: Boolean = false
    var cafeInfoClickListener: (Cafe) -> Unit = {}
    var loginClickListener: (Cafe) -> Unit = {}
    var usersListClickListener: (Cafe) -> Unit = {}
//  var loginErrorListener: (Cafe) -> Unit = {}

    override fun getItemViewType(position: Int): Int = if (getItem(position).isEmpty) {
        TYPE_EMPTY
    } else {
        TYPE_FULL
    }

    override fun setData(data: List<Cafe>) {
        val newData = mutableListOf<Cafe>()
        newData.addAll(data)
        if (newData.isEmpty()) {
            newData.add(Cafe())
        } else {
            newData.sortBy { it.distance }
        }

        val diffResult = DiffUtil.calculateDiff(DiffCallback(mData, newData))
        mData.clear()
        mData.addAll(newData)
        Log.e("TAGS","data is "+mData.size)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        val id = if (viewType == TYPE_EMPTY) R.layout.item_cafe_empty else R.layout.item_cafe_temp
        val v = LayoutInflater.from(parent.context).inflate(id, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        if (holder.itemViewType == TYPE_EMPTY) return
        (holder as? Holder)?.bind(getItem(position), isLoginEnabled)
    }

    class DiffCallback(val oldList: List<Cafe>, val newList: List<Cafe>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.name == newItem.name &&
                    oldItem.distance == newItem.distance &&
                    oldItem.usersCount == newItem.usersCount &&
                    oldItem.maleCount == newItem.maleCount &&
                    oldItem.femaleCount == newItem.femaleCount &&
                    oldItem.canLogin() == newItem.canLogin()
        }
    }

    inner class Holder(contentView: View) : BaseHolder(contentView) {

        val imgCafeLogo: ImageView? = contentView.findViewById(R.id.imgCafeLogo)
        val tvTitle: TextView? = contentView.findViewById(R.id.tvTitle)
        val tvDistance: TextView? = contentView.findViewById(R.id.tvDistance)
        val tvAddressa: TextView? = contentView.findViewById(R.id.location)
        val placeTypeName: TextView? = contentView.findViewById(R.id.service_name)
        val serviceIcon: ImageView? = contentView.findViewById(R.id.service_im)
        val serviceBg: LinearLayout? = contentView.findViewById(R.id.servce_bg)
        val progressContainer: LinearLayout? = contentView.findViewById(R.id.progressContainer)
        val ladiesProgress: FrameLayout? = contentView.findViewById(R.id.progressLadies)
        val mensProgress: FrameLayout? = contentView.findViewById(R.id.progressMans)
        val ladiesValue: TextView? = contentView.findViewById(R.id.ladiesValue)
        val mansValue: TextView? = contentView.findViewById(R.id.mansValue)
        val emptyProgress: FrameLayout? = contentView.findViewById(R.id.emptyView)
        val btnLoginNow: LinearLayout? = contentView.findViewById(R.id.btnLoginNow)
        val tvWhosOnline = contentView.findViewById<TextView>(R.id.tvWhosOnline)

        private val decimalFormat = DecimalFormat("##.#")

        init {
            if (ladiesValue != null) setAdaptiveIcon(ladiesValue, R.drawable.ic_ladies)
            if (mansValue != null) setAdaptiveIcon(mansValue, R.drawable.ic_men)
        }

        private val iconScale = 0.65

        fun bind(cafe: Cafe, isLoginEnabled: Boolean) {
            itemView.tag = cafe
            tvTitle?.text = cafe.name
            tvAddressa?.text = cafe.address
            setupDistance(cafe)
            setupProgress(cafe)
//      loadIcon(cafe.imageUrl)
            btnLoginNow?.visibility =
                View.VISIBLE//if (cafe.canLogin() && isLoginEnabled) View.VISIBLE else View.GONE
//      imgCafeLogo?.setOnClickListener {
//        cafeInfoClickListener.invoke(cafe)
//      }
            val typeface = TypefaceCache.getTypeface(context, TypefaceCache.GEO_MEDIUM)
            placeTypeName?.typeface = typeface

            marqueeText(tvAddressa)
            marqueeText(tvTitle)
            btnLoginNow?.setOnClickListener {
                // this canLogin condition is handled on loginClickListener
                loginClickListener.invoke(cafe)
//        if(cafe.canLogin()) {
//          loginClickListener.invoke(cafe)
//        } else {
//          loginErrorListener.invoke(cafe)
//        }
            }
            tvWhosOnline.setOnClickListener {
                usersListClickListener.invoke(cafe)
            }


            val placeType: String =
                if (cafe.getPlacesType() != null) cafe.getPlacesType() else "Bar"

            var bgColor = ContextCompat.getColor(
                context,
                R.color.purple_color
            );

            when (placeType) {
                "Bar" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_bar)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.purple_color
                    )

                }
                "Restaurant" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_restaurant_menu_24dp)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.restau_color
                    )
                }
                "Parks" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_park_black_24dp_1)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.park_color
                    )
                }
                "Gym" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_fitness_center_black_24dp_1)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.gym_color
                    )
                }
                "Office" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_apartment_black_24dp_1)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.off_color
                    )
                }
                "Shopping" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_shopping_bag_black_24dp_1)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.shopp_color
                    )
                }
                "Coffee" -> {
                    serviceIcon?.setBackgroundResource(R.drawable.ic_group)
                    bgColor = ContextCompat.getColor(
                        context,
                        R.color.cup_color
                    )
                }

            }
            serviceBg?.background?.apply {
                setTint(bgColor)
                alpha=30
            }
            placeTypeName?.setTextColor(bgColor)
            if(placeType.equals("Parks")){
                placeTypeName?.text="OUTDOOR"
            }else {
                placeTypeName?.text = placeType.toString().toUpperCase()
            }

        }

        private fun setupDistance(cafe: Cafe) {
            if (cafe.distance < 0) {
                tvDistance?.visibility = View.GONE
                return
            }
            tvDistance?.visibility = View.VISIBLE

            (context.getString(R.string.distance)+": "+cafe.getDistanceLabel(context)).also { tvDistance?.text = it }
        }

        private fun setupProgress(cafe: Cafe) {
            if (cafe.usersCount == 0) {
                emptyProgress?.visibility = View.VISIBLE
            } else {
                emptyProgress?.visibility = View.GONE
                progressContainer?.weightSum = cafe.usersCount.toFloat()
            }
        }

        private fun setWeight(view: View, weight: Int) {
            view.layoutParams = (view.layoutParams as? LinearLayout.LayoutParams)?.apply {
                this.weight = weight.toFloat()
            }
        }

        private fun loadIcon(imageUrl: String?) {
            Picasso.get()
                .load(imageUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_restaurant_menu_24dp)
                .error(R.drawable.ic_restaurant_menu_24dp)
                .into(imgCafeLogo)
        }

        private fun setAdaptiveIcon(tv: TextView, iconId: Int) {
            tv.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    val img = context.resources.getDrawable(iconId)
                    val newWidth =
                        (img.intrinsicWidth * tv.measuredHeight / img.intrinsicHeight * iconScale).toInt()
                    val newHeight = (tv.measuredHeight * iconScale).toInt()
                    img.setBounds(0, 0, newWidth, newHeight)
                    tv.setCompoundDrawables(null, null, img, null)
                    tv.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            })
        }

    }

    private fun marqueeText(textView: TextView?) {
        textView!!.ellipsize = TextUtils.TruncateAt.MARQUEE
        textView!!.marqueeRepeatLimit = -1
        textView!!.isSingleLine = true
        textView!!.isSelected = true
    }
}