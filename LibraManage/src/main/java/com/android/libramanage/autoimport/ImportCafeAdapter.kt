package com.android.libramanage.autoimport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.libramanage.R
import com.android.libramanage.autoimport.ImportCafeAdapter.Holder
import com.android.libramanage.data.entities.PlaceModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_import_cafe.view.*

/**
 * @author Alex on 30.01.2019.
 */
class ImportCafeAdapter : RecyclerView.Adapter<Holder>() {

    private val items = mutableListOf<PlaceModel>()
    var clickListener: ((PlaceModel) -> Unit)? = null

    fun updateData(items: List<PlaceModel>) {
        val callback = DiffCallback(this.items, items)
        val diffResult = DiffUtil.calculateDiff(callback)
        this.items.clear()
        this.items.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_import_cafe, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position], clickListener)
    }

    inner class Holder(contentView: View) : RecyclerView.ViewHolder(contentView) {

        private var imgCafe: ImageView = contentView.imgCafe
        private var tvTitle: TextView = contentView.tvTitle
        private var tvAddress: TextView = contentView.tvAddress
        private var cbxSelcted: CheckBox = contentView.cbxSelected

        fun bind(item: PlaceModel, clickListener: ((PlaceModel) -> Unit)?) {
            itemView.setOnClickListener { clickListener?.invoke(item) }
            tvTitle.text = item.name
            tvAddress.text = item.vicinity
            cbxSelcted.isChecked = item.isSelected
            Picasso.get()
                    .load(item.getImageUrl(imgCafe.context.getString(R.string.places_key)))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_restaurant_menu_24dp)
                    .error(R.drawable.ic_restaurant_menu_24dp)
                    .into(imgCafe)
        }
    }

    private class DiffCallback(val oldList: List<PlaceModel>, val newList: List<PlaceModel>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].place_id == newList[newItemPosition].place_id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].isSelected == newList[newItemPosition].isSelected
        }
    }
}