package com.libra.activitys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.libra.R
import com.libra.jobs.LogOutJob
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {

    private val mCurrentCafeId: String? by lazy { intent?.getStringExtra(CAFE_ID) }
    private val mCurrentCafeName: String? by lazy { intent?.getStringExtra(CAFE_NAME) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.libra.R.layout.activity_login)
        if (mCurrentCafeId.isNullOrBlank()) finish()
        tvCafeName.text = mCurrentCafeName
        imgBack.setOnClickListener { finish() }
        btnEnter.setOnClickListener { login() }
        btnTerms.setOnClickListener { TermsActivity.launch(this) }
    }

    private fun validateName(): Boolean {
        val name = edtName.text?.toString() ?: ""
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, com.libra.R.string.enter_you_name, Toast.LENGTH_SHORT).show()
            return false
        }

        if (name.length > 10) {
            Toast.makeText(this, com.libra.R.string.nikename_is_limited, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getCheckedSex(): Int {
        return when (rgSex.checkedRadioButtonId) {
            rbMale.id -> MALE_STATUS
            rbFemale.id -> FEMALE_STATUS
            else -> -1
        }
    }

    private fun login() {
        if (!agree.isChecked) {
            Toast.makeText(this, getString(R.string.agree_message_login), Toast.LENGTH_SHORT).show()
            return
        }
        if (!validateName()) return
        val checkedSex = getCheckedSex()
        val name = edtName.text?.toString() ?: ""
        if (checkedSex == -1) {
            Toast.makeText(this, R.string.select_you_gender, Toast.LENGTH_SHORT).show()
            return
        }
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edtName.windowToken, 0)

        LogOutJob.reschedule(applicationContext)
//        startActivity(PhotoActivity.launchIntent(this, mCurrentCafeId,mCurrentCafeName))
    }

    companion object {

        private val MALE_STATUS = 0
        private val FEMALE_STATUS = 1

        private val TYPE_GENDER = "gender"
        private val CAFE_ID = "cafeId"
        private val CAFE_NAME = "cafeName"

        fun launchIntent(context: Context, cafeId: String, cafeName: String): Intent {
            return Intent(context, LoginActivity::class.java).putExtra(CAFE_ID, cafeId)
                    .putExtra(CAFE_NAME, cafeName)
        }
    }
}
