package co.kaizenpro.mainapp.mainappvalidator

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class Response(
        val code: Int,
        val text: String

){
    class Deserializer: ResponseDeserializable<Array<Response>> {
        override fun deserialize(content: String): Array<Response>? = Gson().fromJson(content, Array<Response>::class.java)
    }
}