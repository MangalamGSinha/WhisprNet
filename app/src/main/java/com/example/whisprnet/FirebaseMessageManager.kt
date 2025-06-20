package com.example.whisprnet

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit

class FirebaseMessageManager(private val listener: MessageListener) {
    private val database = Firebase.database
    private var currentRoomRef: DatabaseReference? = null
    private var participantsRef: DatabaseReference? = null
    private var messageListener: ChildEventListener? = null

    companion object {
        private const val TAG = "FirebaseMessageManager"
    }

    interface MessageListener {
        fun onMessageReceived(message: Message)
        fun onUserJoined(username: String)
        fun onUserLeft(username: String)
        fun onConnectionStatusChanged(isConnected: Boolean)
        fun onError(error: String)
    }

    fun joinRoom(roomCode: String, username: String) {
        try {
            Log.d(TAG, "Attempting to join room: $roomCode with user: $username")

            currentRoomRef = database.getReference("rooms").child(roomCode).child("messages")
            participantsRef = database.getReference("rooms").child(roomCode).child("participants")

            // Add user to participants list
            participantsRef?.child(username)?.setValue(true)
                ?.addOnSuccessListener {
                    Log.d(TAG, "Successfully added user to participants")
                    listener.onConnectionStatusChanged(true)
                }
                ?.addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to add user to participants: ${exception.message}")
                    listener.onError("Failed to join room: ${exception.message}")
                }

            // Listen for new messages
            messageListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "New message received: ${snapshot.value}")
                    try {
                        val messageData = snapshot.value as? Map<String, Any>
                        if (messageData != null) {
                            Log.d(TAG, "Parsing message data: $messageData")
                            val message = Message(
                                id = snapshot.key ?: UUID.randomUUID().toString(),
                                content = messageData["content"] as? String ?: "",
                                sender = messageData["sender"] as? String ?: "Unknown",
                                timestamp = messageData["timestamp"] as? Long ?: System.currentTimeMillis()
                            )
                            listener.onMessageReceived(message)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse message: ${e.message}")
                        listener.onError("Failed to parse message: ${e.message}")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "Message changed: ${snapshot.key}")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d(TAG, "Message removed: ${snapshot.key}")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "Message moved: ${snapshot.key}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}")
                    listener.onError("Database error: ${error.message}")
                }
            }

            currentRoomRef?.addChildEventListener(messageListener!!)

        } catch (e: Exception) {
            Log.e(TAG, "Exception in joinRoom: ${e.message}")
            listener.onError("Failed to join room: ${e.message}")
        }
    }

    fun sendMessage(roomCode: String, username: String, messageText: String) {
        try {
            Log.d(TAG, "Sending message: '$messageText' from $username to room $roomCode")

            val message = mapOf(
                "content" to messageText,
                "sender" to username,
                "timestamp" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24))
            )

            database.getReference("rooms")
                .child(roomCode)
                .child("messages")
                .push()
                .setValue(message)
                .addOnSuccessListener {
                    Log.d(TAG, "Message sent successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to send message: ${exception.message}")
                    listener.onError("Failed to send message: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendMessage: ${e.message}")
            listener.onError("Error sending message: ${e.message}")
        }
    }

    fun leaveRoom(roomCode: String, username: String) {
        try {
            Log.d(TAG, "Leaving room: $roomCode")

            val roomRef = database.getReference("rooms").child(roomCode)

            // Remove user from participants
            roomRef.child("participants").child(username).removeValue()

            // Remove message listener
            messageListener?.let { listener ->
                currentRoomRef?.removeEventListener(listener)
            }

            // Check if room is empty and delete
            roomRef.child("participants").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() || !snapshot.hasChildren()) {
                        Log.d(TAG, "Room is empty, deleting room: $roomCode")
                        roomRef.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error checking room status: ${error.message}")
                    listener.onError("Error checking room status: ${error.message}")
                }
            })

            listener.onConnectionStatusChanged(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving room: ${e.message}")
            listener.onError("Error leaving room: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            Log.d(TAG, "Disconnecting Firebase manager")
            messageListener?.let { listener ->
                currentRoomRef?.removeEventListener(listener)
            }
            currentRoomRef = null
            participantsRef = null
            messageListener = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect: ${e.message}")
        }
    }
}
