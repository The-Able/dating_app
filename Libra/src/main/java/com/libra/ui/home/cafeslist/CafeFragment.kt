package com.libra.ui.home.cafeslist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.libra.Constants
import com.libra.R
import com.libra.R.color
import com.libra.activitys.PhotoActivity
import com.libra.entity.Cafe
import com.libra.firebase.FirebaseEvents
import com.libra.fragments.BaseFragment
import com.libra.fragments.GeoLocationFragment
import com.libra.tools.bold
import com.libra.ui.home.cafeslist.viewmodels.CafeListViewModel
import com.libra.ui.home.cafeslist.viewmodels.LoginTimeViewModel
import com.libra.ui.home.userslist.UsersListFragment
import com.patloew.rxlocation.RxLocation
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_cafe.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

class CafeFragment : GeoLocationFragment(), SwipeRefreshLayout.OnRefreshListener {

    private var mAdapter: CafeListAdapter? = null
    private lateinit var mSwipeRefresh: SwipeRefreshLayout
    private lateinit var cafeListViewModel: CafeListViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_cafe
    }

    override fun isLogged(): Boolean {
        return true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupCafeListViewModel()
        subscribeToCafeListUpdates()
        subscribeToTimerViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llTimer.visibility = View.GONE

        setupLoginTimesTitle()
        setupSwipeRefreshLayout(view)
        setupRecycler(view)
    }

    private fun setupLoginTimesTitle() {
        val titles = Constants.openHoursList.joinToString(", ") {
            "$it:00"
        }
        tvLoginTimesTitle.text =
            getString(R.string.login_times_title, titles).bold(titles, requireContext())
    }

    private fun setupRecycler(view: View) {
        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recycler)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = SlideInUpAnimator()
        mRecyclerView.setHasFixedSize(true)

        mAdapter = CafeListAdapter()
        mAdapter?.cafeInfoClickListener = ::showInfoScreen
        mAdapter?.loginClickListener = ::toLoginScreen
        mAdapter?.usersListClickListener = ::showUsersList
//        mAdapter?.loginErrorListener = ::showLoginErrorScreen
        mRecyclerView.adapter = mAdapter
    }

    private fun setupSwipeRefreshLayout(view: View) {
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh)
        mSwipeRefresh.setColorSchemeResources(color.color_primary, color.color_accent)
        mSwipeRefresh.setOnRefreshListener(this)
    }

    private fun setupCafeListViewModel() {
        val rxLocation = RxLocation(requireActivity())
        cafeListViewModel = ViewModelProvider(this).get(CafeListViewModel::class.java)
        cafeListViewModel.init(rxLocation)
    }

    private fun subscribeToCafeListUpdates() {
        if(cafeListViewModel.cafeList.value == null)  mSwipeRefresh.post { mSwipeRefresh.isRefreshing = true }
        cafeListViewModel.cafeList.observe(viewLifecycleOwner, Observer { cafeList ->
            mSwipeRefresh.isRefreshing = false
            mAdapter?.data = cafeList
        })
    }

    private fun subscribeToTimerViewModel() {
        val timerVM = ViewModelProvider(this).get(LoginTimeViewModel::class.java)
        timerVM.isLoginEnabled.observe(viewLifecycleOwner, Observer {
            mAdapter?.isLoginEnabled = true//isLoginEnabled
            mAdapter?.notifyItemRangeChanged(0, mAdapter?.itemCount!!)
        })
        timerVM.timeLeft.observe(viewLifecycleOwner, Observer { timeLeft ->
            if (timeLeft <= 0L) {
                tvTimer.visibility = View.GONE
                tvLoginTimesTitle.gravity = Gravity.CENTER
            } else {
                tvTimer.visibility = View.VISIBLE
                tvLoginTimesTitle.gravity = Gravity.START

                val hours = TimeUnit.HOURS.convert(timeLeft, MILLISECONDS)
                val minutes = TimeUnit.MINUTES.convert(timeLeft, MILLISECONDS) % 60
                val seconds = TimeUnit.SECONDS.convert(timeLeft, MILLISECONDS) % 60
                val sb = StringBuilder()

                if (hours > 0) sb.append(getString(R.string.left_hour, hours)).append(" ")
                if (minutes > 0) sb.append(getString(R.string.left_minutes, minutes)).append(" ")
                sb.append(getString(R.string.left_sec, seconds)).append(" ")

                if (sb.isEmpty()) tvTimer.visibility = View.GONE
                tvTimer.text = getString(R.string.time_left, sb.toString())
            }
        })
    }

    override fun locationRequestingSuccess() {
        cafeListViewModel.requestCafesList()
    }

    override fun locationDenied() {
        mAdapter?.data = emptyList()
        mSwipeRefresh.isRefreshing = false
    }

    override fun onRefresh() {
        clearAdapter()
        mSwipeRefresh.isRefreshing = true
        if (isLocationGranted) {
            cafeListViewModel.requestCafesList()
        } else {
            requestLocationAgain()
        }
    }

    private fun clearAdapter() {
        mAdapter?.clearData()
    }

    override fun getTitleToolbar(): Int {
        return R.string.place
    }


    private fun showInfoScreen(cafe: Cafe) {
        val dialog = CafeInfoDialog.instance(cafe)
        dialog.wazeClickListener = {
            openWaze(it.location.latitude, it.location.longitude)
        }
        dialog.show(parentFragmentManager, "INFO")
    }

    private fun openWaze(latitude: Double, longitude: Double) {
        activity?.packageManager?.let {
            val url = "waze://?ll=$latitude,$longitude&navigate=yes"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.resolveActivity(it)?.let {
                startActivity(intent)
            } ?: run {
                Toast.makeText(context, R.string.noWazeAppFound, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toLoginScreen(cafe: Cafe) {
        FirebaseAnalytics.getInstance(requireContext()).logEvent(FirebaseEvents.CLICKED_ON_LOGIN, null)

//        val current = Calendar.getInstance()
//        val currentDay = current.get(Calendar.DAY_OF_WEEK);

//        val time20 = Calendar.getInstance()
//        time20.set(Calendar.HOUR_OF_DAY, 20)
//        time20.set(Calendar.MINUTE, 0)
//        time20.set(Calendar.SECOND, 0)
//        time20.set(Calendar.MILLISECOND, 0)

//        val time22 = Calendar.getInstance()
//        time22.set(Calendar.HOUR_OF_DAY, 22)
//        time22.set(Calendar.MINUTE, 0)
//        time22.set(Calendar.SECOND, 0)
//        time22.set(Calendar.MILLISECOND, 0)

//        if (currentDay == Calendar.MONDAY || currentDay == Calendar.THURSDAY) {
        // user can login only between 20:00 - 22:00
//            if (current.timeInMillis >= time20.timeInMillis && current.timeInMillis <= time22.timeInMillis) {
        // time is 20:00 - 22:00
//        if (cafe.canLogin())
        startActivity(
            PhotoActivity.launchIntent(
                requireContext(),
                cafe.id,
                cafe.name,
                cafe.location.latitude,
                cafe.location.longitude
            )
        )
//        else
//            showLoginErrorScreen(cafe, getString(R.string.you_are_not_in_cafe))
//            } else {
        // time is not 20:00 - 22:00
//                if (cafe.canLogin()) {
//                    showLoginErrorScreen(cafe, "\u202B" + getString(R.string.login_error_1))
//                } else {
//                    showLoginErrorScreen(cafe, "\u202B" + getString(R.string.login_error_2))
//                }
//            }
//        } else {
//            showLoginErrorScreen(cafe, "\u202B" + getString(R.string.login_error_3))
//        }
    }

    private fun showUsersList(cafe: Cafe){
        mFragments.changeFragment(UsersListFragment.newInstance(ArrayList(cafe.users)), true)
    }

    private fun showLoginErrorScreen(cafe: Cafe, message: String) {
        val dialog = CafeLoginErrorDialog.instance(cafe, message)
        dialog.show(parentFragmentManager, "INFO")
    }

    companion object {

        fun newInstance(): BaseFragment {
            val args = Bundle()
            val fragment = CafeFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
