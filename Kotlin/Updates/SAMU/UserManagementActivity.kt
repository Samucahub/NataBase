package com.exemplo.natabase

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exemplo.natabase.utils.AuthManager

class UserManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCreateUser: Button
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        
        if (!AuthManager.isAdmin()) {
            Toast.makeText(this, "Acesso negado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupUserList()
    }

    private fun initViews() {
        // Agora usando RecyclerView com o ID correto
        recyclerView = findViewById(R.id.recyclerViewUsers)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        tvEmpty = findViewById(R.id.tvEmpty)

        btnCreateUser.setOnClickListener {
            showCreateUserDialog()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupUserList() {
        val users = AuthManager.getAllUsers()

        if (users.isEmpty()) {
            tvEmpty.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            tvEmpty.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE

            adapter = UserAdapter(
                users = users,
                onEditClick = { user -> showEditUserDialog(user) },
                onDeleteClick = { user -> showDeleteConfirmationDialog(user) }
            )
            recyclerView.adapter = adapter
        }
    }

    private fun showCreateUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val cbIsAdmin = dialogView.findViewById<CheckBox>(R.id.cbIsAdmin)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Criar Utilizador")
            .setView(dialogView)
            .setPositiveButton("Criar") { dialogInterface, _ ->
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString()
                val isAdmin = cbIsAdmin.isChecked

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                try {
                    val success = AuthManager.createUser(username, password, isAdmin)
                    if (success) {
                        Toast.makeText(this, "Utilizador criado com sucesso", Toast.LENGTH_SHORT).show()
                        setupUserList()
                    } else {
                        Toast.makeText(this, "Utilizador já existe", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val cbIsAdmin = dialogView.findViewById<CheckBox>(R.id.cbIsAdmin)

        etUsername.setText(user.username)
        cbIsAdmin.isChecked = user.isAdmin

        // Se for o próprio utilizador, não permitir alterar o role de admin
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser?.username == user.username) {
            cbIsAdmin.isEnabled = false
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Utilizador")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialogInterface, _ ->
                val newUsername = etUsername.text.toString().trim()
                val newPassword = etPassword.text.toString().takeIf { it.isNotEmpty() }
                val isAdmin = cbIsAdmin.isChecked

                if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Nome de utilizador é obrigatório", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                try {
                    val success = AuthManager.updateUser(user.username, newUsername, newPassword, isAdmin)
                    if (success) {
                        Toast.makeText(this, "Utilizador atualizado com sucesso", Toast.LENGTH_SHORT).show()
                        setupUserList()
                    } else {
                        Toast.makeText(this, "Erro ao atualizar utilizador", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Utilizador")
            .setMessage("Tem a certeza que deseja eliminar o utilizador ${user.username}?")
            .setPositiveButton("Eliminar") { dialog, which ->
                try {
                    val success = AuthManager.deleteUser(user.username)
                    if (success) {
                        Toast.makeText(this, "Utilizador eliminado com sucesso", Toast.LENGTH_SHORT).show()
                        setupUserList()
                    } else {
                        Toast.makeText(this, "Erro ao eliminar utilizador", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupUserList()
    }
}
