package com.example.kotlinmap

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinmap.Common.Common
import com.example.kotlinmap.Model.MyPlaces
import com.example.kotlinmap.Remote.IGoogleAPIService
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Response
import java.lang.StringBuilder
import javax.security.auth.callback.Callback

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var latitude:Double= 0.toDouble()
    private var longitude:Double= 0.toDouble()

    private lateinit var mLastLocation:Location
    private var mMarker:Marker?=null

    //location

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object{
        private const val MY_PERMISSTION_CODE: Int = 1000
    }

    lateinit var mServices:IGoogleAPIService
    internal lateinit var currentPlace:MyPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //Init Service
        mServices = Common.googleApiService


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

        bottom_navigation_view.setOnNavigationItemSelectedListener() {item->
            when(item.itemId)
            {
                R.id.action_hospital->nearByPlace("hospital")
                R.id.action_market->nearByPlace("market")
                R.id.action_restaurant->nearByPlace("restaurant")
                R.id.action_school->nearByPlace("school")

            }
            true

        }
    }

    private fun nearByPlace(typePlace: String) {
        Log.d("URL_DEBUG2222",typePlace)
        //Clear all on Map
        mMap.clear()
        var url = getUrl(latitude,longitude,typePlace)
        mServices.getNearbyPlaces(url)
            .enqueue(object : retrofit2.Callback<MyPlaces>{
                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                   Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {
                    currentPlace = response!!.body()!!
                    Log.d("URL_DEBUG55","aaaaa")
                    Log.d("URL_DEBUG55",response.message())

                    if(response!!.isSuccessful)
                    {
                        Log.d("URL_DEBUG55","bbbb")
                        for (i  in 0 until response!!.body()!!.results!!.size)
                        {
                            Log.d("URL_DEBUG55","xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                            val markerOptions=MarkerOptions()
                            val googlePlace = response.body()!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat,lng)
                            markerOptions.position(latLng)
                            markerOptions.title(placeName)
                            if(typePlace.equals("hospital"))
                               //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
                                markerOptions.icon(bitMapFromVector(R.drawable.ic_hospital))
                            else  if(typePlace.equals("market"))
                                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))
                                markerOptions.icon(bitMapFromVector(R.drawable.ic_market))
                            else  if(typePlace.equals("restaurant"))
                               // markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                                markerOptions.icon(bitMapFromVector(R.drawable.ic_restaurant))
                            else  if(typePlace.equals("school"))
                               // markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                                markerOptions.icon(bitMapFromVector(R.drawable.ic_school))
                            else
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                            markerOptions.snippet(i.toString()) //Assign index For market

                            //Add market to map
                            mMap!!.addMarker(markerOptions)
                            //move camera
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))

                        }


                    }
                    else
                    {
                        Log.d("URL_DEBUG55","yyyyyyyyyyyyyyyyyyyy")
                    }
                }

            })
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=10000")
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyCl82msmMD9xR2ib5vtxDD7bWBoIvpiqTk")
        Log.d("URL_DEBUG",googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    private fun bitMapFromVector(vectorResID:Int):BitmapDescriptor {
        val vectorDrawable=ContextCompat.getDrawable(baseContext,vectorResID)
        vectorDrawable!!.setBounds(0,0,vectorDrawable!!.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap=
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun builLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
               // mLastLocation = p0!!.locations.get(p0!!.locations.size -1) // get lastLocation
                mLastLocation = p0!!.lastLocation
                if(mMarker != null)
                {
                    mMarker!!.remove()
                }
                latitude = mLastLocation.latitude
                longitude = mLastLocation.latitude
                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                mMarker = mMap!!.addMarker(markerOptions)

                //Move camera
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(5f))
                Log.d("thoxxxxxx your1",latLng.toString())

            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun builLocaltionRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermisstion():Boolean{
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSTION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSTION_CODE)
            return false
        }
        else
            return true
    }

//Override

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
       when(requestCode)
       {
           MY_PERMISSTION_CODE->{
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

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Init Google Service
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                mMap!!.isMyLocationEnabled=true
            }
        }else
            mMap!!.isMyLocationEnabled=true
        //Enable Zoom Control
        mMap.uiSettings.isZoomControlsEnabled=true

        //Make event click on market
        mMap!!.setOnMarkerClickListener { marker ->
            if(marker.snippet != null) {
                Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]
                startActivity(Intent(this, ViewPlace::class.java))
            }
            true
        }

    }
}
