package co.kaizenpro.mainapp.mainappvalidator

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class User(
        val id: Int,
        val nombre: String,
        val correo: String,
        val rol: String,
        val especialidad: String,
        val telefono: String,
        val sexo: String,
        val level: String,
        val enable: String

){
    class Deserializer: ResponseDeserializable<Array<User>> {
        override fun deserialize(content: String): Array<User>? = Gson().fromJson(content, Array<User>::class.java)
    }
}