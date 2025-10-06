package com.exemplo.natabase.utils

import android.content.Context
import android.content.SharedPreferences
import com.exemplo.natabase.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import com.exemplo.natabase.App

object AuthManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_USERS = "users"
    private const val KEY_CURRENT_USER = "current_user"
    private const val ADMIN_USERNAME = "admin"
    private const val ADMIN_PASSWORD = "admin123"

    init {
        ensureAdminExists()
    }

    private fun ensureAdminExists() {
        val users = loadUsers()
        val adminHash = hashPassword(ADMIN_PASSWORD)

        if (!users.any { it.username == ADMIN_USERNAME }) {
            users.add(User(ADMIN_USERNAME, adminHash, true))
            saveUsers(users)
        }
    }

    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            throw RuntimeException("Erro ao encriptar password", e)
        }
    }

    private fun loadUsers(): MutableList<User> {
        val prefs = getSharedPreferences()
        val json = prefs.getString(KEY_USERS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<User>>() {}.type
            Gson().fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
    }

    private fun saveUsers(users: List<User>) {
        val prefs = getSharedPreferences()
        val editor = prefs.edit()
        val json = Gson().toJson(users)
        editor.putString(KEY_USERS, json)
        editor.apply()
    }

    fun login(username: String, password: String): Boolean {
        val users = loadUsers()
        val passwordHash = hashPassword(password)

        val user = users.find {
            it.username == username && it.passwordHash == passwordHash
        }

        if (user != null) {
            setCurrentUser(user)
            return true
        }
        return false
    }

    fun logout() {
        val prefs = getSharedPreferences()
        prefs.edit().remove(KEY_CURRENT_USER).apply()
    }

    fun getCurrentUser(): User? {
        val prefs = getSharedPreferences()
        val json = prefs.getString(KEY_CURRENT_USER, null)
        return if (json != null) {
            Gson().fromJson(json, User::class.java)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    fun isAdmin(): Boolean {
        return getCurrentUser()?.isAdmin == true
    }

    fun createUser(username: String, password: String, isAdmin: Boolean = false): Boolean {
        if (!isAdmin()) {
            throw SecurityException("Apenas administradores podem criar utilizadores")
        }

        val users = loadUsers()
        if (users.any { it.username == username }) {
            return false
        }

        val passwordHash = hashPassword(password)
        users.add(User(username, passwordHash, isAdmin))
        saveUsers(users)
        return true
    }

    fun changePassword(username: String, newPassword: String) {
        val users = loadUsers()
        val userIndex = users.indexOfFirst { it.username == username }

        if (userIndex != -1) {
            val newHash = hashPassword(newPassword)
            users[userIndex] = users[userIndex].copy(passwordHash = newHash)
            saveUsers(users)
        }
    }

    private fun setCurrentUser(user: User) {
        val prefs = getSharedPreferences()
        val editor = prefs.edit()
        val json = Gson().toJson(user)
        editor.putString(KEY_CURRENT_USER, json)
        editor.apply()
    }

    private fun getSharedPreferences(): SharedPreferences {
        return App.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun getAllUsers(): List<User> {
        return loadUsers()
    }

    fun updateUser(oldUsername: String, newUsername: String, newPassword: String? = null, isAdmin: Boolean? = null): Boolean {
        if (!isAdmin() && getCurrentUser()?.username != oldUsername) {
            throw SecurityException("Apenas administradores podem editar outros utilizadores")
        }

        val users = loadUsers()
        val userIndex = users.indexOfFirst { it.username == oldUsername }

        if (userIndex == -1) {
            return false
        }

        val user = users[userIndex]
        val newPasswordHash = if (newPassword != null) hashPassword(newPassword) else user.passwordHash
        val newIsAdmin = isAdmin ?: user.isAdmin

        if (newUsername != oldUsername && users.any { it.username == newUsername }) {
            return false
        }

        users[userIndex] = User(newUsername, newPasswordHash, newIsAdmin)
        saveUsers(users)

        val currentUser = getCurrentUser()
        if (currentUser?.username == oldUsername) {
            setCurrentUser(users[userIndex])
        }

        return true
    }

    fun deleteUser(username: String): Boolean {
        if (!isAdmin()) {
            throw SecurityException("Apenas administradores podem eliminar utilizadores")
        }

        if (getCurrentUser()?.username == username) {
            throw SecurityException("Não podes eliminar a tua própria conta")
        }

        if (username == ADMIN_USERNAME) {
            throw SecurityException("Não podes eliminar o administrador padrão")
        }

        val users = loadUsers()
        val userIndex = users.indexOfFirst { it.username == username }

        if (userIndex == -1) {
            return false
        }

        users.removeAt(userIndex)
        saveUsers(users)
        return true
    }
}
