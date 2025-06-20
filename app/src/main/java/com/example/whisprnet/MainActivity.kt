package com.example.whisprnet


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etRoomCode: EditText
    private lateinit var btnJoinRoom: Button
    private lateinit var btnCreateRoom: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etRoomCode = findViewById(R.id.etRoomCode)
        btnJoinRoom = findViewById(R.id.btnJoinRoom)
        btnCreateRoom = findViewById(R.id.btnCreateRoom)
    }

    private fun setupClickListeners() {
        btnJoinRoom.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val roomCode = etRoomCode.text.toString().trim()

            if (validateInput(username, roomCode)) {
                joinChatRoom(username, roomCode)
            }
        }

        btnCreateRoom.setOnClickListener {
            val username = etUsername.text.toString().trim()

            if (validateUsername(username)) {
                val newRoomCode = generateRoomCode()
                etRoomCode.setText(newRoomCode)
                joinChatRoom(username, newRoomCode)
            }
        }
    }

    private fun validateInput(username: String, roomCode: String): Boolean {
        if (username.isEmpty()) {
            etUsername.error = "Please enter a nickname"
            return false
        }

        if (username.length < 2) {
            etUsername.error = "Nickname must be at least 2 characters"
            return false
        }

        if (roomCode.isEmpty()) {
            etRoomCode.error = "Please enter a room code"
            return false
        }

        if (roomCode.length < 4) {
            etRoomCode.error = "Room code must be at least 4 characters"
            return false
        }

        return true
    }

    private fun validateUsername(username: String): Boolean {
        if (username.isEmpty()) {
            etUsername.error = "Please enter a nickname"
            return false
        }

        if (username.length < 2) {
            etUsername.error = "Nickname must be at least 2 characters"
            return false
        }

        return true
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

    private fun joinChatRoom(username: String, roomCode: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("username", username)
            putExtra("roomCode", roomCode.uppercase())
        }
        startActivity(intent)
    }
}
