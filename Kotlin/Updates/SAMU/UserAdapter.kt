package com.exemplo.natabase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.exemplo.natabase.utils.AuthManager

class UserAdapter(
    private val users: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.tvUsername.text = user.username
        holder.tvRole.text = if (user.isAdmin) "Administrador" else "Utilizador"

        holder.btnEdit.setOnClickListener { onEditClick(user) }
        holder.btnDelete.setOnClickListener { onDeleteClick(user) }

        val currentUser = AuthManager.getCurrentUser()
        val isCurrentUser = currentUser?.username == user.username
        val isDefaultAdmin = user.username == "admin"

        holder.btnDelete.isEnabled = !isCurrentUser && !isDefaultAdmin
        holder.btnDelete.alpha = if (holder.btnDelete.isEnabled) 1f else 0.5f
    }

    override fun getItemCount() = users.size
}
