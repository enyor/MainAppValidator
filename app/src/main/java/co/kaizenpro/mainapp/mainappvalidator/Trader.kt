package co.kaizenpro.mainapp.mainappvalidator

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class Trader(
        val id: Int,
        val nombre: String,
        val LatLng: String,
        val especialidad: String,
        val telefono: String,
        val sexo: String,
        val enable: String,
        val imagen: String,
        val direccion: String,
        val tespera: Int,
        val activo: String,
        val level: String,
        var pago: Int

){
    class Deserializer: ResponseDeserializable<Array<Trader>> {
        override fun deserialize(content: String): Array<Trader>? = Gson().fromJson(content, Array<Trader>::class.java)
    }
}
