package com.android.libramanage.autoimport

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.libramanage.R
import com.android.libramanage.autoimport.AutoImportViewModel.Companion.TYPE_CAFE
import com.android.libramanage.autoimport.AutoImportViewModel.Companion.TYPE_RESTAURANT
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_auto_import.*
import java.util.*
import kotlin.collections.ArrayList

class AutoImportActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 3434
        private const val PLACES_IDS = "places_ids"
        @JvmStatic
        fun launch4Result(fragment: Fragment, placesIds: ArrayList<String>) {
            val launcher = Intent(fragment.context, AutoImportActivity::class.java)
                    .putStringArrayListExtra(PLACES_IDS, placesIds)
            fragment.startActivityForResult(launcher, REQUEST_CODE)
        }
    }

    var placeTypeValue =
        Arrays.asList("Bar", "Restaurant", "Parks", "Gym", "Office", "Shopping", "Coffee")

    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private lateinit var viewModel: AutoImportViewModel
    private val adapter = ImportCafeAdapter()
    private val alreadyRegisteredPlacesIds by lazy {
        intent?.getStringArrayListExtra(PLACES_IDS) ?: arrayListOf()
    }
    private val progressDialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Importing. It could take a lot of time. Please wait...")
            setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_import)
        tvPickedLocation.text = getString(R.string.picked_location, "")
        imgMapMarker.visibility = View.GONE

        val myAdapter: ArrayAdapter<*> = ArrayAdapter<String>(
            this,
            R.layout.simple_dropdown_item_custom, placeTypeValue
        )

        place_val.adapter=myAdapter

        setupImportButton()
        setupTypeChooser()
        setupToolbar()
        setupViewModel()
        setupRecycler()
        (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync {
            imgMapMarker.visibility = View.VISIBLE
            googleMap = it
            checkIfCanEnableLocation()
            it.setOnCameraIdleListener { updateLocation() }
        }
        viewModel.setPlType(placeTypeValue.get(place_val.selectedItemPosition))
        place_val.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if(viewModel!=null){
                    viewModel.setPlType(placeTypeValue.get(position))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })
    }

    private fun updateLocation() {
        val map = googleMap ?: return
        val location = map.cameraPosition.target
        currentLocation = location
        tvPickedLocation.text = getString(R.string.picked_location, "${location.latitude} | ${location.longitude}")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkIfCanEnableLocation()
            }
        }
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.menu_auto_import)
        toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.select_all -> {
                    viewModel.selectAll(true)
                    true
                }
                R.id.unselect_all -> {
                    viewModel.selectAll(false)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(AutoImportViewModel::class.java)
        viewModel.init(getString(R.string.places_key), alreadyRegisteredPlacesIds)
        viewModel.finishData.observe(this, Observer {
            progressDialog.dismiss()
            Toast.makeText(this, "Complete", Toast.LENGTH_LONG).show()
            tvPickedLocation.postDelayed({ setResult(Activity.RESULT_OK); finish() }, 300L)
        })

        viewModel.placesData.observe(this, Observer {
            it ?: return@Observer
            progressDialog.hide()
            btnConfirmImport.visibility = if (it.any { it.isSelected }) View.VISIBLE else View.GONE
            tvEmpty.visibility = if (it.isNotEmpty()) View.GONE else View.VISIBLE
            svMap.visibility = View.GONE
            flCafesList.visibility = View.VISIBLE
            adapter.updateData(it)
        })
    }

    private fun setupRecycler() {
        rvCafes.layoutManager = LinearLayoutManager(this)
        rvCafes.itemAnimator = null
        rvCafes.adapter = adapter
        adapter.clickListener = {
            viewModel.placeSelected(it)
        }
    }

    private fun setupImportButton() {
        btnImport.setOnClickListener {
            if (currentLocation == null) {
                Toast.makeText(this, "You need to select location", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (edtRadius.text?.toString().isNullOrBlank() || edtRadius.text?.toString()?.toFloatOrNull() == null) {
                tilRadius.error = "You need to set valid radius"
                return@setOnClickListener
            }

            progressDialog.show()
            val radius = edtRadius.text?.toString()?.toFloatOrNull()!!
            viewModel.importPlaces(currentLocation!!, radius)
        }

        btnConfirmImport.setOnClickListener {
            progressDialog.show()
            viewModel.importConfirmed()
        }
    }

    private fun setupTypeChooser() {
        rgType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCafe -> viewModel.onTypeUpdated(TYPE_CAFE)
                R.id.rbRestaurant -> viewModel.onTypeUpdated(TYPE_RESTAURANT)
            }
        }
    }

    private fun checkIfCanEnableLocation() {
        val map = googleMap ?: return
        if (ActivityCompat.checkSelfPermission(this@AutoImportActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@AutoImportActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@AutoImportActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            if (!map.isMyLocationEnabled)
                map.isMyLocationEnabled = true

            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (myLocation == null) {
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_COARSE
                val provider = lm.getBestProvider(criteria, true)
                myLocation = lm.getLastKnownLocation(provider.toString())
            }

            if (myLocation != null) {
                val userLocation = LatLng(myLocation.latitude, myLocation.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f), 1500, null)
            }
        }
    }
}
