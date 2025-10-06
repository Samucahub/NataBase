package com.exemplo.natabase

data class User(
    val username: String,
    val passwordHash: String,
    val isAdmin: Boolean = false
)
