package co.kaizenpro.mainapp.mainappvalidator

import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment


import android.location.Criteria
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.util.Log
import android.os.Looper
import com.beust.klaxon.*
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.maps.model.*


import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import java.net.URL


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var mylocation = LatLng(-34.603748,-58.381631)
    var destination = LatLng(-0.0 , -0.0)

    private lateinit var md : GMapV2Direction

    //Minimo tiempo para updates en Milisegundos
    private val MIN_TIEMPO_ENTRE_UPDATES = (1000 * 60 * 1).toLong() // 1 minuto
    //Minima distancia para updates en metros.
    private val MIN_CAMBIO_DISTANCIA_PARA_UPDATES: Float = 1.5f // 1.5 metros

    //var URL = "https://mainapp.kaizenpro.co.uk/consulta_marcador.php?id=";
    val URL = "https://mainapp.kaizenpro.co.uk/consulta_marcadores_all.php";
    var Traders = ArrayList<Trader>()
    private var UserId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

         UserId = intent.getIntExtra("UserId",0)
        //URL += UserId




        val mLocMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.C2D_MESSAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.C2D_MESSAGE) != PackageManager.PERMISSION_GRANTED) {
            //Requiere permisos para Android 6.0
            Log.e("MapsActivity", "No se tienen permisos necesarios!, se requieren.")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.C2D_MESSAGE, Manifest.permission.C2D_MESSAGE), 225)
            return
        } else {
            Log.i("MapsActivity", "Permisos necesarios OK!.")
            mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locListener, Looper.getMainLooper())
        }


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.C2D_MESSAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Debes aceptar para disfrutar de todos los servicios de la aplicación!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.C2D_MESSAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }

        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true)
        mMap.uiSettings.isZoomControlsEnabled = true

        //Obtengo la ultima localizacion conocida
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()


        var location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false))



        if (location == null) {
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider = locationManager.getBestProvider(criteria, false)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locListener, Looper.getMainLooper())

            location = locationManager.getLastKnownLocation(provider)


        }


            // consulta Ws para ultima posicion del trader
            URL.httpGet().responseObject(Trader.Deserializer()) { request, response, result ->
                val (traders, err) = result


                //Add to ArrayList
                traders?.forEach { trader ->
                    Traders.add(trader)
                    val lat = trader.LatLng.split(',')
                    //val ubicacion = LatLng(lat[0].toDouble(), lat[1].toDouble())
                    //mylocation = ubicacion
                    val otro = LatLng(lat[0].toDouble(), lat[1].toDouble())
                    drawMarker(otro, trader.nombre, trader.sexo, trader)
                    //drawMarker(mylocation,"Yo",trader.sexo)
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 16F))
                }

            }






        //val latitude = location.latitude
        //val longitude = location.longitude


        // Add a marker in Sydney and move the camera
      //  val sydney = LatLng(-34.603112, -58.384678)
      //  mMap.addMarker(MarkerOptions().position(sydney).title("Trader Aurora").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_dama)))

      //  val otro = LatLng(-34.602405, -58.379936)
      //  drawMarker(otro, "Trader Roger", "M")





    }



    private fun drawMarker(point: LatLng,title: String, sexo: String, datos: Trader) {
        // Creating an instance of MarkerOptions
        val markerOptions = MarkerOptions()

        // Setting latitude and longitude for the marker
        markerOptions.position(point)

        // Set title
        markerOptions.title(title)


        // Icon
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        if (sexo=="M") {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_caballero))
        }else{
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_dama))
        }

        // Adding marker on the Google Map
        val marcador = mMap.addMarker(markerOptions)
        marcador.tag = datos

        // Set a listener for info window events.
        mMap.setOnMarkerClickListener { marker ->
            val position = marker.position
            //Using position get Value from arraylist
            false
        }

        mMap.setOnInfoWindowClickListener {  marker ->

            val position = marker.position
            val n = marker.tag


            //INICIO RUTA
            val LatLongB = LatLngBounds.Builder()

            // Declare polyline object and set up color and width
            val options = PolylineOptions()
            options.color(Color.RED)
            options.width(5f)

            // build URL to call API

           // val url = getURL(mylocation, position)
           // if (mylocation != LatLng(-34.603748,-58.381631)) {
/*
                doAsync {
                    // Connect to URL, download content and convert into string asynchronously
                    val result = URL(url).readText()
                    uiThread {
                        // When API call is done, create parser and convert into JsonObjec
                        val parser: Parser = Parser()
                        val stringBuilder: StringBuilder = StringBuilder(result)
                        val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                        // get to the correct element in JsonObject
                        val routes = json.array<JsonObject>("routes")
                        val duration = routes!!["legs"]["duration"] as JsonArray<JsonObject>
                        //duracion_recorrido = duration[0]["value"].toString()
                        //Toast.makeText(this@MapsActivity, "duración:" +duracion_recorrido  , Toast.LENGTH_LONG).show();

                        val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                        // For every element in the JsonArray, decode the polyline string and pass all points to a List
                        val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)  }
                        // Add  points to polyline and bounds
                        options.add(mylocation)
                        LatLongB.include(mylocation)
                        for (point in polypts)  {
                            options.add(point)
                            LatLongB.include(point)
                        }
                        options.add(position)
                        LatLongB.include(position)
                        // build bounds
                        val bounds = LatLongB.build()
                        // add polyline to the map
                        mMap!!.addPolyline(options)
                        // show map with route centered
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

                    */

                        val args = Bundle()
                        args.putParcelable("from_position", mylocation)
                        args.putParcelable("to_position", position)
                        destination = position



                        //   val intent = Intent(this@MapsActivity, TabLayoutDemoActivity::class.java)
                        val intent = Intent(this@MapsActivity, Popup::class.java)
                        intent.putExtra("bundle", args)
                        intent.putExtra("duracion", 0)
                        intent.putExtra("Id", (n as Trader).id)


                        intent.putExtra("nombre", (n as Trader).nombre)
                        intent.putExtra("especialidad", (n as Trader).especialidad)
                        intent.putExtra("enable", (n as Trader).activo)
                        intent.putExtra("level", (n as Trader).level)
                        intent.putExtra("limite", (n as Trader).tespera)
                        intent.putExtra("pago", (n as Trader).pago)

                        if ((n as Trader).imagen==""){
                            intent.putExtra("img","avatar.png")
                        }else{
                            intent.putExtra("img",(n as Trader).imagen)
                        }

                        if ((n as Trader).direccion==""){
                            intent.putExtra("dir","NA")
                        }else{
                            intent.putExtra("dir",(n as Trader).direccion)
                        }
                        //startActivity(intent)
                        //
                        startActivityForResult(intent, 100)




               //     }



             //   }

                //FIN RUTA

           // }else{
             //   Toast.makeText(this@MapsActivity,"Por favor espere unos instantes para obtener su ubicación",Toast.LENGTH_LONG).show()
           // }


        }
       /* mMap.setOnMapLongClickListener { marker ->
            val latitude = marker.latitude

            false

        }*/
/*
        mMap.setOnMapLongClickListener { marker ->
            // Removing the marker and circle from the Google Map
            mMap.clear()
            drawMarker(marker,"Yo",Traders[0].sexo)
            val intent = Intent(this, Popup::class.java)
            val lat = marker.latitude
            val lng = marker.longitude

            intent.putExtra("id",UserId)
            intent.putExtra("nombre", Traders[0].nombre)
            intent.putExtra("especialidad", Traders[0].especialidad)
            intent.putExtra("enable", Traders[0].enable)
            intent.putExtra("lat",lat)
            intent.putExtra("lng",lng)
            intent.putExtra("tiempo",Traders[0].tespera)


            startActivityForResult(intent,100)


           // Toast.makeText(this, "Nuevo punto de ubicación asignado", Toast.LENGTH_LONG).show();


        }*/


        /*mMap.setOnInfoWindowClickListener {  marker ->
            val position = marker.position

            val args = Bundle()
            args.putParcelable("from_position", mylocation)
            args.putParcelable("to_position", position)
            destination = position

            val intent = Intent(this, Popup::class.java)
            intent.putExtra("bundle", args)
            //startActivity(intent)
            //
            startActivityForResult(intent,100)

        } */

    }

    var locListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i("MapsActivity", "Lat " + location.getLatitude() + " Long " + location.getLongitude())
           // mylocation = LatLng(location.getLatitude(),location.getLongitude())

        }

        override fun onProviderDisabled(provider: String) {
            Log.i("MapsActivity", "onProviderDisabled()")
        }

        override fun onProviderEnabled(provider: String) {
            Log.i("MapsActivity", "onProviderEnabled()")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.i("MapsActivity", "onStatusChanged()")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100){
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "Trader Modificado", Toast.LENGTH_LONG).show();
                   } else {

                Toast.makeText(this, "Acción Cancelada, intente nuevamente", Toast.LENGTH_LONG).show();


                }
            }
        }



    private fun getURL(from : LatLng, to : LatLng) : String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }















}

