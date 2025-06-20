package com.example.whisprnet


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: MutableList<Message>,
    private val currentUsername: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val isOwnMessage = message.sender == currentUsername

        holder.tvSender.text = message.sender
        holder.tvMessage.text = message.content
        holder.tvTimestamp.text = timeFormat.format(Date(message.timestamp))

        // Style message based on sender
        if (isOwnMessage) {
            holder.messageContainer.background = ContextCompat.getDrawable(
                holder.itemView.context, R.drawable.message_bubble_sent
            )
            (holder.messageContainer.layoutParams as LinearLayout.LayoutParams).gravity =
                android.view.Gravity.END
            holder.tvSender.visibility = View.GONE
        } else {
            holder.messageContainer.background = ContextCompat.getDrawable(
                holder.itemView.context, R.drawable.message_bubble_received
            )
            (holder.messageContainer.layoutParams as LinearLayout.LayoutParams).gravity =
                android.view.Gravity.START
            holder.tvSender.visibility = View.VISIBLE
        }

        // Handle system messages
        if (message.type == MessageType.SYSTEM ||
            message.type == MessageType.JOIN ||
            message.type == MessageType.LEAVE) {
            holder.tvSender.visibility = View.GONE
            holder.messageContainer.background = ContextCompat.getDrawable(
                holder.itemView.context, R.drawable.message_bubble_received
            )
            (holder.messageContainer.layoutParams as LinearLayout.LayoutParams).gravity =
                android.view.Gravity.CENTER
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }
}
