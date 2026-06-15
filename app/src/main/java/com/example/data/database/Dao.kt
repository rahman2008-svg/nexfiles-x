package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteFile)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteFile)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path LIMIT 1)")
    fun isFavoriteFlow(path: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path LIMIT 1)")
    suspend fun isFavorite(path: String): Boolean
}

@Dao
interface SecureFileDao {
    @Query("SELECT * FROM secure_files ORDER BY lockedAt DESC")
    fun getAllSecureFiles(): Flow<List<SecureFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecureFile(secureFile: SecureFile)

    @Query("DELETE FROM secure_files WHERE id = :id")
    suspend fun deleteSecureFileById(id: String)

    @Query("SELECT * FROM secure_files WHERE id = :id LIMIT 1")
    suspend fun getSecureFileById(id: String): SecureFile?
}

@Dao
interface VaultConfigDao {
    @Query("SELECT configValue FROM vault_config WHERE configKey = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfigValue(config: VaultConfig)

    @Query("DELETE FROM vault_config WHERE configKey = :key")
    suspend fun deleteConfigValue(key: String)
}
