package com.example.whisprnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class ChatActivity : AppCompatActivity(), FirebaseMessageManager.MessageListener {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnLeaveRoom: ImageButton
    private lateinit var tvRoomCode: TextView
    private lateinit var tvStatus: TextView

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var firebaseManager: FirebaseMessageManager

    private var username: String = ""
    private var roomCode: String = ""
    private val messages = mutableListOf<Message>()
    private var isConnected = false

    private lateinit var btnShareRoom: ImageButton

    companion object {
        private const val TAG = "ChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getIntentData()
        initViews()
        setupRecyclerView()
        setupClickListeners()
        debugFirebaseConnection()
        initializeFirebase()
    }

    private fun getIntentData() {
        username = intent.getStringExtra("username") ?: "Anonymous"
        roomCode = intent.getStringExtra("roomCode") ?: "UNKNOWN"
        Log.d(TAG, "Intent data - Username: $username, Room: $roomCode")
    }

    private fun initViews() {
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnShareRoom = findViewById(R.id.btnShareRoom)
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom)
        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvStatus = findViewById(R.id.tvStatus)

        tvRoomCode.text = "Room: $roomCode"
        tvStatus.text = "Connecting..."
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, username)
        rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }

    private fun setupClickListeners() {
        btnSend.setOnClickListener {
            sendMessage()
        }

        btnShareRoom.setOnClickListener {
            shareRoomCode()
        }

        btnLeaveRoom.setOnClickListener {
            showLeaveConfirmation()
        }

        etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun shareRoomCode() {
        val shareMessage = "Join my WhisprNet Room using Room Code: $roomCode"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Room Code via"))

        Log.d(TAG, "Room code shared: $roomCode")
    }


    private fun debugFirebaseConnection() {
        Log.d(TAG, "Starting Firebase debug for room: $roomCode, user: $username")

        // Test basic Firebase connectivity
        val testRef = Firebase.database.getReference("debug_test")
        testRef.setValue("test_${System.currentTimeMillis()}")
            .addOnSuccessListener {
                Log.d(TAG, "Firebase write test successful")
                Toast.makeText(this, "Firebase connected successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Firebase write test failed: ${exception.message}")
                Toast.makeText(this, "Firebase connection failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }

        // Test reading from the room
        val roomRef = Firebase.database.getReference("rooms").child(roomCode)
        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                Log.d(TAG, "Room data: ${snapshot.value}")
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(TAG, "Room read cancelled: ${error.message}")
            }
        })
    }

    private fun initializeFirebase() {
        Log.d(TAG, "Initializing Firebase for room: $roomCode, user: $username")
        firebaseManager = FirebaseMessageManager(this)
        firebaseManager.joinRoom(roomCode, username)
        addSystemMessage("Connecting to room $roomCode...")
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isEmpty()) return

        Log.d(TAG, "Attempting to send message: '$messageText' to room: $roomCode")

        // Send to Firebase
        firebaseManager.sendMessage(roomCode, username, messageText)

        // Clear input field
        etMessage.setText("")

        Log.d(TAG, "Message sent to Firebase")
    }

    private fun addSystemMessage(content: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            sender = "System",
            timestamp = System.currentTimeMillis(),
            type = MessageType.SYSTEM
        )
        messageAdapter.addMessage(message)
        rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
    }

    private fun showLeaveConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Leave Room")
            .setMessage("Are you sure you want to leave? All messages will be permanently deleted.")
            .setPositiveButton("Leave") { _, _ ->
                leaveRoom()
            }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun leaveRoom() {
        Log.d(TAG, "Leaving room: $roomCode")

        // Clear all messages (privacy feature)
        messageAdapter.clearMessages()

        // Disconnect Firebase
        firebaseManager.leaveRoom(roomCode, username)
        firebaseManager.disconnect()

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity destroyed, disconnecting Firebase")
        if (::firebaseManager.isInitialized) {
            firebaseManager.disconnect()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showLeaveConfirmation()
    }

    // Firebase MessageListener Methods
    override fun onMessageReceived(message: Message) {
        runOnUiThread {
            Log.d(TAG, "Message received from ${message.sender}: ${message.content}")

            // Add message to UI
            messageAdapter.addMessage(message)
            rvMessages.scrollToPosition(messageAdapter.itemCount - 1)

            Log.d(TAG, "Message added to UI, total messages: ${messageAdapter.itemCount}")
        }
    }

    override fun onUserJoined(username: String) {
        runOnUiThread {
            Log.d(TAG, "User joined: $username")
            addSystemMessage("$username joined the room")
        }
    }

    override fun onUserLeft(username: String) {
        runOnUiThread {
            Log.d(TAG, "User left: $username")
            addSystemMessage("$username left the room")
        }
    }

    override fun onConnectionStatusChanged(isConnected: Boolean) {
        runOnUiThread {
            this.isConnected = isConnected
            Log.d(TAG, "Connection status changed: $isConnected")

            tvStatus.text = if (isConnected) "Connected" else "Disconnected"
            tvStatus.setTextColor(
                if (isConnected)
                    getColor(R.color.green)
                else
                    getColor(android.R.color.holo_red_light)
            )

            if (isConnected) {
                addSystemMessage("Successfully connected to room")
            } else {
                addSystemMessage("Disconnected from room")
            }
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Log.e(TAG, "Firebase error: $error")
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()

            // Add error as system message for debugging
            addSystemMessage("Error: $error")
        }
    }
}
