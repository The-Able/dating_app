package com.android.libramanage.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.libramanage.R
import com.android.libramanage.firebase.FBHelper
import com.firebase.client.DataSnapshot
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.dialog_edit_radius.view.btnCancel
import kotlinx.android.synthetic.main.dialog_edit_radius.view.btnConfirm
import kotlinx.android.synthetic.main.dialog_edit_radius.view.edtNweRadius
import kotlinx.android.synthetic.main.dialog_edit_radius.view.tilNewRadius
import kotlinx.android.synthetic.main.dialog_edit_radius.view.tvCurrentRadius
import java.text.DecimalFormat

/**
 * @author Alex on 29.01.2019.
 */
class EditRadiusDialog : DialogFragment() {

  companion object {
    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      EditRadiusDialog().show(fragmentManager, "EditRadius")
    }
  }

  private val radiusRef = FBHelper.getHomeScreenRadius()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val inflater = LayoutInflater.from(context!!)
    val v = inflater.inflate(R.layout.dialog_edit_radius, null)
    setupView(v)
    return Builder(context!!)
        .setView(v)
        .setCancelable(false)
        .create()
  }

  private fun setupView(v: View) {
    v.apply {
      readCurrentValue(tvCurrentRadius)
      btnCancel.setOnClickListener { dismiss() }
      btnConfirm.setOnClickListener {
        val newRadius = edtNweRadius.text?.toString()?.toFloatOrNull()
        if (newRadius == null) {
          tilNewRadius.error = "Invalid radius format. Please check your input."
        }else {
          radiusRef.setValue(newRadius)
          dismiss()
        }
      }
    }
  }

  private fun readCurrentValue(tv: TextView) {

    radiusRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener{
      override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
        snapshot?.getValue(Float::class.java)?.let {
          tv.text = "Current radius: ${DecimalFormat("0.##").format(it)}"
        }
      }

      override fun onCancelled(error: DatabaseError) {

      }

    })
  }

}