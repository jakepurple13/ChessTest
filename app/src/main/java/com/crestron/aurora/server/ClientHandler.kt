package com.crestron.aurora.server

import android.annotation.SuppressLint
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class ClientHandler(clientUISetup: ClientUISetup) {

    companion object {
        var host = "192.168.1.128"
    }

    private val client = HttpClient {
        install(WebSockets)
    }

    init {
        GlobalScope.launch {
            client.ws(
                    method = HttpMethod.Get,
                    host = host,
                    port = 8080,
                    path = "/chat/ws"
            ) {
                // this: DefaultClientWebSocketSession
                clientUISetup.uiSetup(this)
            }
        }
    }

    fun close() {
        client.close()
    }

}

interface ClientUISetup {
    suspend fun uiSetup(socket: DefaultClientWebSocketSession)
}

suspend fun DefaultClientWebSocketSession.newMessage(message: (Frame.Text) -> Unit) {
    // Receive frame.
    incoming.consumeEach {
        if (it is Frame.Text) {
            message(it)
        }
    }
}

data class ChatUser(
        var name: String,
        var image: String = "https://www.w3schools.com/w3images/bandmember.jpg"
)

enum class MessageType {
    MESSAGE, EPISODE, SERVER, INFO, TYPING_INDICATOR, DOWNLOADING
}

data class SendMessage(
        val user: ChatUser,
        val message: String,
        val type: MessageType?,
        val data: Any? = null
) {
    @SuppressLint("SimpleDateFormat")
    val time = SimpleDateFormat("MM/dd hh:mm a").format(System.currentTimeMillis())!!

    fun toJson(): String = Gson().toJson(this)
}

data class Action(val type: String, val json: String)
data class TypingIndicator(val isTyping: Boolean)
data class Download(val download: Boolean)
data class Profile(val username: String?, val image: String?)

fun TypingIndicator.toAction() = Action("Typing", this.toJson())
fun Profile.toAction() = Action("Profile", this.toJson())
fun Download.toAction() = Action("Download", this.toJson())

suspend fun DefaultClientWebSocketSession.sendAction(action: Action) = send(action.toJson())

fun Any.toJson(): String = Gson().toJson(this)
