package com.masksearchapp

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.masksearchapp.api.MaskAPI
import com.masksearchapp.data.MaskData
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.ZoomControlView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val TAG = "MainActivity"
    }


    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var mapFragment: MapFragment
    private var markerList: MutableList<Marker>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG, "onCreate: ")

        val fm = supportFragmentManager
        mapFragment = fm.findFragmentById(R.id.map) as MapFragment
        //getMapAsync를 해줘야 onMapReadyCallback이 호출됨
        mapFragment.getMapAsync(this)




    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource


        val uiSettings = naverMap.uiSettings
        uiSettings.isCompassEnabled = false
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLogoClickEnabled = false

        naverMap.locationTrackingMode = LocationTrackingMode.Follow
//        naverMap.addOnLocationChangeListener { location ->
//            Toast.makeText(this,
//                "${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
//        }

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance(NaverMapOptions().zoomControlEnabled(false))
                .also {
                    fm.beginTransaction().add(R.id.map, it).commit()
                }

        mapFragment.getMapAsync {
            val zoomControlView = findViewById<ZoomControlView>(R.id.zoom)
            zoomControlView.map = naverMap
        }

        val mapCenter = naverMap.cameraPosition.target
        searchStores(mapCenter.latitude, mapCenter.longitude, 5000)
        
    }

    private fun searchStores(lat: Double, lng: Double, m: Int) {
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(MaskAPI.base_Url)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val maskAPI = retrofit.create(MaskAPI::class.java)
        val callGetSearchNews = maskAPI.searchMask(lat, lng, m)
        Log.e(TAG, "searchStores: ${retrofit.baseUrl()}" )
        Log.e(TAG, "searchStores: $lat, $lng, $m", )
        Log.e(TAG, "searchStores: callGetSearchNews : $callGetSearchNews" )
        callGetSearchNews.enqueue(object : Callback<MaskData> {
            override fun onResponse(call: Call<MaskData>, response: Response<MaskData>) {

                Log.e(TAG, "onResponse: ${response.code()}")
                if (response.code() == 200) {
                    Log.e(TAG, "onResponse: 성공")
                    val maskData: MaskData = response.body() as MaskData
                    updateMapMarkers(result = maskData)
                }
                Log.e(TAG, "onResponse: ${response.body()}")
            }

            override fun onFailure(call: Call<MaskData>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.cause}")
            }

        })
    }

    fun updateMapMarkers(result: MaskData) {
        resetMarker()

        if (result.count > 0) {
            for (store in result.stores) {
                val marker = Marker()
                marker.position = LatLng(store.lat, store.lng)
                if (store.remain_stat == "plenty") {
                    marker.icon = OverlayImage.fromResource(R.drawable.marker_green)
                } else if (store.remain_stat == "some") {
                    marker.icon = OverlayImage.fromResource(R.drawable.marker_yellow)
                } else if (store.remain_stat == "few") {
                    marker.icon = OverlayImage.fromResource(R.drawable.marker_red)
                } else {
                    marker.icon = OverlayImage.fromResource(R.drawable.marker_gray)
                }

                marker.anchor = PointF(1f, 1f)
                marker.map = naverMap
                markerList?.add(marker)
            }
        }

    }

    private fun resetMarker() {
        if (markerList != null && markerList!!.size > 0) {

            for (marker in markerList!!) {
                marker.map = null
            }
            markerList!!.clear()
        }
    }


}