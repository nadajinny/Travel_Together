package com.example.myapplication.ui.message

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.SocketManager
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var chatInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: Button
    private lateinit var chatRecyclerView: RecyclerView
    private val chatMessages = ArrayList<ChatModel>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var email: String
    private lateinit var roomId: String
    private lateinit var roomName: String

    private var socketId: String? = null
    private var hasConnection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_main)

        // SharedPreferences 초기화
        preferences = getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        email = preferences.getString("currentUser", "") ?: ""
        val userInfoJson = preferences.getString(email, null)
        userId = if (!userInfoJson.isNullOrEmpty()) {
            JSONObject(userInfoJson).getString("userId")
        } else {
            "defaultUser"
        }

        // Intent에서 roomId 가져오기
        roomId = intent.getStringExtra("roomId") ?: "defaultRoomId"

        roomName = intent.getStringExtra("roomName") ?: "defaultRoomId"
        val chatRoomNameTextView: TextView = findViewById(R.id.chatRoom_Name)
        chatRoomNameTextView.text = roomName

        // RecyclerView 초기화
        chatRecyclerView = findViewById(R.id.chat_recyclerview)
        chatAdapter = ChatAdapter(this, chatMessages, userId)
        chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatRoomActivity)
            setHasFixedSize(true)
        }

        // 버튼 초기화
        backButton = findViewById(R.id.back_button)
        sendButton = findViewById(R.id.chat_Send_Button)
        chatInput = findViewById(R.id.chating_Text)

        backButton.setOnClickListener { onBackPressed() }
        sendButton.setOnClickListener { sendMessage() }

        // 소켓 연결 및 기존 메시지 불러오기
        if (!hasConnection) {
            setupSocketConnection()
            loadPreviousMessages()
            hasConnection = true
        }
    }

    private fun setupSocketConnection() {
        SocketManager.on("socket_id", onSocketIdReceived)
        SocketManager.on("chat_message", onNewMessage)
        Log.d("onNewMessage", "chat_message_fun: ")

        val userInfo = JSONObject().apply {
            put("username", userId)
            put("roomId", roomId)
        }
        SocketManager.emit("connect user", userInfo)
    }

    private val onSocketIdReceived = Emitter.Listener { args ->
        runOnUiThread {
            try {
                val data = args[0] as JSONObject
                socketId = data.getString("socketId")
                Log.d("Socket", "Received Socket ID: $socketId")
            } catch (e: JSONException) {
                Log.e("Socket", "Error parsing socket ID", e)
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            try {
                val data = args[0] as JSONObject
                val newMessage = ChatModel(
                    name = data.getString("name"),
                    script = data.getString("script"),
                    profile_image = data.getString("profile_image"),
                    date_time = data.getString("date_time")
                )
                Log.d("onNewMessage", "onNewMessage: $newMessage")
                chatMessages.add(newMessage)
                chatAdapter.notifyItemInserted(chatMessages.size - 1)
                chatRecyclerView.scrollToPosition(chatMessages.size - 1)
            } catch (e: JSONException) {
                Log.e("Socket", "Error parsing message", e)
            }
        }
    }

    private fun sendMessage() {
        val messageText = chatInput.text.toString().trim()
        if (TextUtils.isEmpty(messageText)) return

        chatInput.text.clear()

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val messageData = JSONObject().apply {
            put("name", userId)
            put("script", messageText)
            put("profile_image", "example")
            put("date_time", currentTime)
            put("socket_id", socketId)
            put("roomId", roomId)
        }

        SocketManager.emit("chat_message", messageData)
        Log.d("Socket", "Sent Message: $messageText, Room: $roomId, Socket ID: $socketId")
    }

    private fun loadPreviousMessages() {
        SocketManager.emit("join_room", roomId)

        SocketManager.on("room_messages", Emitter.Listener { args ->
            runOnUiThread {
                try {
                    val data = args[0] as JSONObject
                    val messagesArray = data.getJSONArray("messages")

                    for (i in 0 until messagesArray.length()) {
                        val messageObj = messagesArray.getJSONObject(i)
                        val message = ChatModel(
                            name = messageObj.getString("name"),
                            script = messageObj.getString("script"),
                            profile_image = "example",
                            date_time = messageObj.getString("timestamp")
                        )
                        chatMessages.add(message)
                    }
                    chatAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                } catch (e: JSONException) {
                    Log.e("Socket", "Error loading previous messages", e)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (hasConnection) {
            SocketManager.off("socket_id", onSocketIdReceived)
            SocketManager.off("chat_message", onNewMessage)
            hasConnection = false
        }
        finish()
    }
}
