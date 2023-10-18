package com.txurdinaga.rednit_app.views

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.txurdinaga.rednit_app.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.osmdroid.api.IMapController
import java.io.IOException
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapsActivity : FragmentActivity() {
    private var mapView: MapView? = null
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var myLocationOverlay: MyLocationNewOverlay? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        Log.d("MiTag", firestore.toString())
        // Configura la biblioteca osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicializa el MapView
        mapView = findViewById(R.id.mapView)
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mapView!!.setBuiltInZoomControls(true)
        mapView!!.setMultiTouchControls(true)
        mapView?.setMinZoomLevel(10.0)
        mapView?.setMaxZoomLevel(19.0)

        // Establece una ubicación inicial (por ejemplo, el centro de la ciudad)
        val geocoder = Geocoder(this)
        val marker = Marker(mapView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // Se tienen los permisos, registra el LocationListener y solicita la ubicación
            // Esto debería mostrar el diálogo de solicitud de permiso
            val locationProvider = GpsMyLocationProvider(this)
            val mapController: IMapController = mapView!!.controller
            mapController.setZoom(17.0)

            myLocationOverlay = MyLocationNewOverlay(locationProvider, mapView)

            mapView!!.overlays.add(myLocationOverlay)
            myLocationOverlay!!.enableMyLocation()
            myLocationOverlay!!.enableFollowLocation()

            myLocationOverlay!!.runOnFirstFix(Runnable {
                // Esto se ejecutará cuando se obtenga la ubicación
                val location = myLocationOverlay?.myLocation
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    Log.d("MiTag", geoPoint.toString())

                    runOnUiThread {
                        // Actualiza la vista en el hilo principal
                        val mapController: IMapController? = mapView?.controller
                        mapController?.setCenter(geoPoint)
                        // Puedes hacer algo con las coordenadas aquí
                    }
                }
            })
        }

        // Recupera los documentos de la colección "actividades" en Firestore
        firestore.collection("actividades")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    Log.d("MiTag", task.toString())
                    for (document in task.result!!) {
                        // Obtiene la dirección (localización) del documento
                        val direccion = document.getString("localizacion")
                        Log.d("MiTag", direccion.toString())
                        // Convierte la dirección en coordenadas de latitud y longitud
                        val latLng = direccion?.let { getAddressLatLng(geocoder, it) }

                        if (latLng != null) {
                            val latitude = latLng.first
                            val longitude = latLng.second
                            Log.d("MiTag", latLng.toString())
                            // Crea un nuevo marcador para esta ubicación
                            val marker = Marker(mapView)
                            val geoPoint = GeoPoint(latitude, longitude)
                            marker.position = geoPoint
                            marker.title = "Marker Title"
                            marker.snippet = "Marker Description"

                            mapView?.overlays?.add(marker)
                        }
                    }
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.exception)
                }

            })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso de ubicación fue otorgado, ahora puedes obtener la ubicación actual
                obtenerUbicacionActual()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerUbicacionActual() {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                val geoPoint = GeoPoint(latitude, longitude)

                // Centra el mapa en la ubicación actual
                val mapController: IMapController = mapView!!.controller
                mapController.setCenter(geoPoint)

                // Puedes hacer algo con las coordenadas aquí
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }}
    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    fun getAddressLatLng(geocoder: Geocoder, address: String): Pair<Double, Double>? {
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val latitude = addressList[0].latitude
                val longitude = addressList[0].longitude
                return Pair(latitude, longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


}



