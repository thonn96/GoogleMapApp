package com.example.kotlinmap

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinmap.Common.Common
import com.example.kotlinmap.Helper.DirectionJSONParser
import com.example.kotlinmap.Remote.IGoogleAPIService
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dmax.dialog.SpotsDialog
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.StringBuilder
import javax.security.auth.callback.Callback

class ViewDirections : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var mServices: IGoogleAPIService

    lateinit var mCurrentMarker: Marker

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var mLastLocation:Location
    var polyLine:Polyline?=null

    companion object{
        private const val MY_PERMISSTION_CODE: Int = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_directions)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Init Service
        mServices = Common.googleApiServiceScalars

        //Request runtime permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermisstion()) {
                builLocaltionRequest()
                builLocationCallBack()
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                )
            }
        }
        else {
            builLocaltionRequest()
            builLocationCallBack()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            mLastLocation = location

            //add your location to map
            val markerOptions : MarkerOptions = MarkerOptions()
                .position(LatLng(mLastLocation.latitude,mLastLocation.longitude))

                .title("Your Position111")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            mCurrentMarker = mMap!!.addMarker(markerOptions)

            //move camera
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mLastLocation.latitude,mLastLocation.longitude)))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12.0f))


            //Create marker for destination location
            val destinationLatlng = LatLng(Common.currentResult!!.geometry!!.location!!.lat.toDouble(),
                Common.currentResult!!.geometry!!.location!!.lng.toDouble())


            mMap!!.addMarker(MarkerOptions().position(destinationLatlng)
                .title(Common.currentResult!!.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            //get direction
            drawPath(mLastLocation,Common.currentResult!!.geometry!!.location!!)


        }
    }

    private fun checkLocationPermisstion():Boolean{
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), ViewDirections.MY_PERMISSTION_CODE
                )
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), ViewDirections.MY_PERMISSTION_CODE
                )
            return false
        }
        else
            return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode)
        {
            ViewDirections.MY_PERMISSTION_CODE ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        if(checkLocationPermisstion()){
                            builLocaltionRequest()
                            builLocationCallBack()
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )
                            mMap!!.isMyLocationEnabled=true
                        }

                }
                else{
                    Toast.makeText(this,"Permission",Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    private fun builLocaltionRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun builLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.lastLocation
                //add your location to map
                val markerOptions : MarkerOptions = MarkerOptions()
                    .position(LatLng(mLastLocation.latitude,mLastLocation.longitude))
                    .title("Your Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mCurrentMarker = mMap!!.addMarker(markerOptions)

                //move camera
               // mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mLastLocation.latitude,mLastLocation.longitude)))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(37.0,37.0)))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12.0f))

                //Create marker for destination location
                val destinationLatlng = LatLng(Common.currentResult!!.geometry!!.location!!.lat.toDouble(),
                    Common.currentResult!!.geometry!!.location!!.lng.toDouble())

                mMap!!.addMarker(MarkerOptions().position(destinationLatlng)
                    .title(Common.currentResult!!.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

                //get direction
                drawPath(mLastLocation,Common.currentResult!!.geometry!!.location!!)



            }
        }
    }
    inner class ParseTask:AsyncTask<String,Int,List<List<HashMap<String,String>>>>(){

        internal val waitingDialog:android.app.AlertDialog = SpotsDialog(this@ViewDirections)

        override fun onPreExecute() {
            super.onPreExecute()
            waitingDialog.show()
            waitingDialog.setMessage("Please wait")
        }

        override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>> {
            val jsonObject:JSONObject
            var routes:List<List<HashMap<String,String>>>?=null
            try {
                jsonObject = JSONObject(params[0])
                val parser = DirectionJSONParser()
                routes = parser.parse(jsonObject)
            }catch (e:JSONException)
            {
                e.printStackTrace()
            }
            return routes!!
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            super.onPostExecute(result)
            var points:ArrayList<LatLng>?= null
//            var polylineOptions:PolylineOptions?=null
            var polylineOptions = PolylineOptions()
            for(i in result!!.indices)
            {
                points = ArrayList()
//                polylineOptions = PolylineOptions()
                val path = result[i]

                for(j in path.indices)
                {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat,lng)
                    points.add(position)
                }
                polylineOptions.addAll(points)
                polylineOptions.width(12f)
                polylineOptions.color(Color.RED)
                polylineOptions.geodesic(true)
            }
            polyLine = mMap.addPolyline(polylineOptions)
            waitingDialog.dismiss()
        }
    }

    private fun drawPath(mLastLocation: Location?, location: com.example.kotlinmap.Model.Location) {
        if(polyLine!=null)
            polyLine!!.remove()
        val origin:String =StringBuilder(mLastLocation!!.latitude.toString())
            .append(",")
            .append(mLastLocation.longitude.toString())
            .toString()
        val destination = StringBuilder(location.lat.toString())
            .append(",")
            .append(location.lng)
            .toString()
        mServices.getDirections(origin,destination, "AIzaSyA_PAGvZ7xsmrK9JeFkPJ3DOkHASZdUH-M")
            .enqueue(object :retrofit2.Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {

                }
                override fun onResponse(call: Call<String>, response: Response<String>) {
                  ParseTask().execute(response!!.body()!!.toString())
                }
            })
    }




    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }



}

