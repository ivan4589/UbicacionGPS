package com.example.ubicaciongps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var taxiList: List<Taxi> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_maps)

        // Ajustar para que no se sobreponga a la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener la lista de taxis enviada desde MainActivity
        taxiList = intent.getSerializableExtra("TAXI_LIST") as? List<Taxi> ?: emptyList()
        
        if (taxiList.isEmpty()) {
            Toast.makeText(this, "No hay datos de taxis para mostrar", Toast.LENGTH_LONG).show()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }
        
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                showNearestTaxis(location)
            } else {
                Toast.makeText(this, "No se pudo obtener tu ubicación. Verifica tu GPS.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNearestTaxis(userLocation: Location) {
        val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
        
        // Mover cámara a mi ubicación
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))

        if (taxiList.isEmpty()) return

        // Calcular distancias y ordenar
        val taxisWithDistance = taxiList.mapNotNull { taxi ->
            val lat = taxi.latitud.toDoubleOrNull()
            val lon = taxi.longitud.toDoubleOrNull()
            if (lat != null && lon != null) {
                val taxiLocation = Location("").apply {
                    latitude = lat
                    longitude = lon
                }
                val distance = userLocation.distanceTo(taxiLocation)
                Pair(taxi, distance)
            } else null
        }.sortedBy { it.second }

        // Tomar los 3 más cercanos
        val nearestTaxis = taxisWithDistance.take(3)

        // Mostrar en el mapa
        for (item in nearestTaxis) {
            val taxi = item.first
            val distanceKm = String.format("%.2f", item.second / 1000)
            val taxiLatLng = LatLng(taxi.latitud.toDouble(), taxi.longitud.toDouble())
            
            mMap.addMarker(MarkerOptions()
                .position(taxiLatLng)
                .title("Móvil: ${taxi.movil}")
                .snippet("Distancia: $distanceKm km")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission()
        }
    }
}