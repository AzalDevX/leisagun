package com.txurdinaga.rednit_app.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.txurdinaga.rednit_app.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.txurdinaga.rednit_app.MainActivity
import org.osmdroid.api.IMapController
import java.io.IOException
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.Locale

class MapsActivity : FragmentActivity() {
    private var mapView: MapView? = null
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var predefinedLocation: GeoPoint? = null

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


        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                    predefinedLocation = GeoPoint(location.latitude, location.longitude)
                    Log.d("MiTag", predefinedLocation.toString())

                    runOnUiThread {
                        // Actualiza la vista en el hilo principal
                        val mapController: IMapController? = mapView?.controller
                        mapController?.setCenter(predefinedLocation)
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
                            val title = document.getString("actividad")
                            val describe = document.getTimestamp("hora")
                            val fecha = describe?.toDate()
                            val simple = SimpleDateFormat("dd/MM/yyyy ' - ' HH:mm", Locale.getDefault())
                            val formattedDate = simple.format(fecha)
                            Log.d("MiTag", latLng.toString())
                            // Crea un nuevo marcador para esta ubicación
                            val marker = Marker(mapView)
                            val geoPoint = GeoPoint(latitude, longitude)
                            marker.position = geoPoint
                            marker.title = title?.capitalize()
                            marker.snippet = formattedDate

                            mapView?.overlays?.add(marker)
                        }
                    }
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.exception)
                }

            })
        // Dentro de la función onCreate o donde tengas el botón "Encontrar actividades cercanas"
        val btnEncontrarActividadesCercanas = findViewById<Button>(R.id.findActivitiesButton)
        val centerMapButton = findViewById<ImageButton>(R.id.centerMapButton)
        centerMapButton.setOnClickListener {
            // Centra el mapa en la ubicación predefinida (reemplaza con tus coordenadas)

            val mapController: IMapController = mapView!!.controller
            mapController.setZoom(17.0)
            mapController.setCenter(predefinedLocation)
        }

        btnEncontrarActividadesCercanas.setOnClickListener {
            // Obtén tu ubicación actual
            val myLocation = myLocationOverlay?.myLocation
            if (myLocation != null) {
                val myLatLng = Pair(myLocation.latitude, myLocation.longitude)
                var closestMarker: Marker? = null
                var minDistance: Double = Double.MAX_VALUE
                var marklatlang: Pair<Double, Double>? = null

                // Itera a través de todas las marcas en el mapa
                for (overlay in mapView?.overlays!!) {
                    if (overlay is Marker) {
                        val marker = overlay as Marker
                        val markerLatLng = Pair(marker.position.latitude, marker.position.longitude)

                        // Calcula la distancia entre tu ubicación y la marca
                        val distance = calculateDistance(myLatLng, markerLatLng)

                        if (distance < minDistance) {
                            // Actualiza la actividad más cercana encontrada hasta ahora
                            minDistance = distance
                            closestMarker = marker
                            marklatlang = markerLatLng
                        }
                    }
                }

                // Comprueba si se encontró la actividad más cercana
                if (closestMarker != null) {
                    // Muestra la distancia, nombre y fecha en un cuadro de diálogo o de otra manera
                    val distanciaEnKilometros = minDistance
                    val nombreActividad = closestMarker.title
                    val fechaActividad = closestMarker.snippet

                    val mensaje = "La actividad más cercana es \"$nombreActividad\" el $fechaActividad, a una distancia de ${distanciaEnKilometros.toInt()} km de tu ubicación."

                    // Muestra el mensaje en un cuadro de diálogo o en un TextView, como prefieras
                    // Por ejemplo, usando un AlertDialog
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Actividad más cercana")
                    alertDialog.setMessage(mensaje)
                    alertDialog.setPositiveButton("Aceptar", null)
                    alertDialog.show()
                    val geoPoint = marklatlang?.let { it1 -> GeoPoint(it1.first, marklatlang.second) }
                    val mapController: IMapController = mapView!!.controller
                    mapController.setZoom(17.0)
                    mapController.setCenter(geoPoint)

                } else {
                    // No se encontraron actividades en el mapa
                    // Puedes mostrar un mensaje indicando que no hay actividades cercanas
                    Toast.makeText(this, "No se encontraron actividades cercanas.", Toast.LENGTH_SHORT).show()
                }
            }
        }



    }

    fun calculateDistance(latLng1: Pair<Double, Double>, latLng2: Pair<Double, Double>): Double {
        // Fórmula de la distancia haversine
        val radius = 6371 // Radio de la Tierra en kilómetros
        val dLat = Math.toRadians(latLng2.first - latLng1.first)
        val dLon = Math.toRadians(latLng2.second - latLng1.second)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latLng1.first)) * Math.cos(Math.toRadians(latLng2.first)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radius * c
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso de ubicación fue otorgado, ahora puedes obtener la ubicación actual
                Toast.makeText(this, "Permiso de ubicación aceptado, recargando!", Toast.LENGTH_SHORT).show()
                obtenerUbicacionActual()
                recreate()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
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



