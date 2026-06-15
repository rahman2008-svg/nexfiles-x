package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteFile(
    @PrimaryKey val path: String,
    val name: String,
    val size: Long,
    val category: String,
    val isDirectory: Boolean,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "secure_files")
data class SecureFile(
    @PrimaryKey val id: String, // Unique identifier (e.g. random UUID)
    val originalPath: String,   // Original absolute file path before hiding
    val secureFileName: String, // Scrambled filename inside vault directory
    val displayName: String,    // Real name for list presentation
    val size: Long,
    val category: String,
    val lockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_config")
data class VaultConfig(
    @PrimaryKey val configKey: String, // e.g. "vault_pin" or "vault_lock_type"
    val configValue: String           // e.g. "1234" (hashed or plaintext PIN input)
)
