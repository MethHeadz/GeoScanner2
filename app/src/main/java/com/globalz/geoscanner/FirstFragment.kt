package com.globalz.geoscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_first.*

private const val PERMISSION_REQUEST = 10
// private var COORDS_INITIAL_SET = false
private var globalCoordinateModus: CoordinateMode = CoordinateMode.UNKNOWN

public enum class CoordinateMode {
    GET_INITIAL_COORDS,
    GET_ACTUAL_COORDS,
    UNKNOWN
}

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGetCoordinateButtons()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkPermission(permissions))
            {
                // toggleCoordinateButtons()
            }
            else
            {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        }
    }

    private fun UpdateCoordField(actualText: String) {

        when (globalCoordinateModus) {
            CoordinateMode.GET_INITIAL_COORDS -> {
                tv_start_location.text = ""
                tv_start_location.append(actualText)
                globalCoordinateModus = CoordinateMode.UNKNOWN
            }

            CoordinateMode.GET_ACTUAL_COORDS -> {
                tv_actual_location.text = ""
                tv_actual_location.append(actualText)
                globalCoordinateModus = CoordinateMode.UNKNOWN
            }

            CoordinateMode.UNKNOWN -> return
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocations() {
        ((activity as MainActivity).getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager).also { locationManager = it }
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            // GPS
            if (hasGps) {
                Log.d("CoseAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object:
                    LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationGps = location

                        val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                        UpdateCoordField(txtCoord)

                        Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                        Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)
                    }
                })
            }

            var localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (localGpsLocation != null)
                locationGps = localGpsLocation

            // NETWORK
            if (hasNetwork) {
                Log.d("CoseAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object:
                    LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationNetwork  = location

                        val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                        UpdateCoordField(txtCoord)

                        Log.d("CodeAndroidLocation", " Network Latitude: " + locationNetwork!!.latitude)
                        Log.d("CodeAndroidLocation", " Network Longitude: " + locationNetwork!!.longitude)
                    }
                })
            }

            var localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (localNetworkLocation  != null)
                locationNetwork = localNetworkLocation

            // GET one of both!
            if (locationGps != null && locationNetwork != null)
            {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {

                    val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                    UpdateCoordField(txtCoord)

                    Log.d("CodeAndroidLocation", " Network Latitude: " + locationNetwork!!.latitude)
                    Log.d("CodeAndroidLocation", " Network Longitude: " + locationNetwork!!.longitude)
                }
                else
                {
                    val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                    UpdateCoordField(txtCoord)

                    Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)
                }
            }
        }
        else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if ((activity as MainActivity).checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    private fun initGetCoordinateButtons() {
        // Initial Coords Button
        (activity as MainActivity).btnGetMyInitialCoords.isEnabled = true
        (activity as MainActivity).btnGetMyInitialCoords?.setOnClickListener {
            globalCoordinateModus = CoordinateMode.GET_INITIAL_COORDS

            // Buttons initialisieren
            (activity as MainActivity).btnGetMyInitialCoords.isEnabled = false
            (activity as MainActivity).btnGetActualCoords.isEnabled = true

            getLocations()
        }

        // Actual Coords Button
        (activity as MainActivity).btnGetActualCoords.isEnabled = false
        (activity as MainActivity).btnGetActualCoords.alpha = 0.5F
        (activity as MainActivity).btnGetActualCoords?.setOnClickListener {
            globalCoordinateModus = CoordinateMode.GET_ACTUAL_COORDS
            getLocations()
        }
    }

    private fun handleCoordinateButtons() {
        when (globalCoordinateModus) {
            CoordinateMode.GET_INITIAL_COORDS -> {
                (activity as MainActivity).btnGetMyInitialCoords.isEnabled = false
                (activity as MainActivity).btnGetActualCoords.isEnabled = true
            }
        }
        // Toast.makeText(this, "Terminado", Toast.LENGTH_SHORT).show()
    }
}