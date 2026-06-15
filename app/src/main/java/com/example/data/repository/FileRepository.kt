package com.example.data.repository

import android.content.Context
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class FileRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao,
    private val secureFileDao: SecureFileDao,
    private val vaultConfigDao: VaultConfigDao
) {
    val favorites: Flow<List<FavoriteFile>> = favoriteDao.getAllFavorites()
    val secureFiles: Flow<List<SecureFile>> = secureFileDao.getAllSecureFiles()

    // 1. Favorite Operations
    suspend fun addFavorite(path: String, name: String, size: Long, category: String, isDirectory: Boolean) {
        withContext(Dispatchers.IO) {
            favoriteDao.insertFavorite(
                FavoriteFile(
                    path = path,
                    name = name,
                    size = size,
                    category = category,
                    isDirectory = isDirectory
                )
            )
        }
    }

    suspend fun removeFavorite(path: String) {
        withContext(Dispatchers.IO) {
            favoriteDao.deleteFavorite(
                FavoriteFile(
                    path = path,
                    name = "",
                    size = 0,
                    category = "",
                    isDirectory = false
                )
            )
        }
    }

    fun isFavoriteFlow(path: String): Flow<Boolean> = favoriteDao.isFavoriteFlow(path)

    suspend fun isFavorite(path: String): Boolean = favoriteDao.isFavorite(path)

    // 2. Vault Pin Operations
    suspend fun setVaultPin(pin: String) {
        withContext(Dispatchers.IO) {
            vaultConfigDao.setConfigValue(VaultConfig("vault_pin", pin))
        }
    }

    suspend fun getVaultPin(): String? {
        return withContext(Dispatchers.IO) {
            vaultConfigDao.getConfigValue("vault_pin")
        }
    }

    suspend fun clearVaultPin() {
        withContext(Dispatchers.IO) {
            vaultConfigDao.deleteConfigValue("vault_pin")
        }
    }

    // 3. Vault File Operations (Hide & Scramble)
    private fun getVaultDirectory(): File {
        val dir = File(context.filesDir, "secured_vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Hides a file:
     * - Copy file to application sandboxed 'secured_vault' with a scrambled UUID.
     * - Encrypt bytes using simple XOR bitmask cipher (reversible, fast, low-footprint, satisfies "Privacy Vault")
     * - Save mapping inside secure_files database.
     * - Delete original file.
     */
    suspend fun hideFile(originalPath: String, displayName: String, category: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val originalFile = File(originalPath)
                if (!originalFile.exists() || originalFile.isDirectory) return@withContext false

                val fileId = UUID.randomUUID().toString()
                val scrambledName = "sec_$fileId.bin"
                val vaultFile = File(getVaultDirectory(), scrambledName)

                // Simple XOR encryption for offline security (low CPU/RAM usage)
                val buffer = ByteArray(1024 * 64)
                FileInputStream(originalFile).use { input ->
                    FileOutputStream(vaultFile).use { output ->
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            // XOR cipher key: 0x5A
                            for (i in 0 until bytesRead) {
                                buffer[i] = (buffer[i].toInt() xor 0x5A).toByte()
                            }
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                // Insert into Secure DB
                val secureFile = SecureFile(
                    id = fileId,
                    originalPath = originalPath,
                    secureFileName = scrambledName,
                    displayName = displayName,
                    size = originalFile.length(),
                    category = category,
                    lockedAt = System.currentTimeMillis()
                )
                secureFileDao.insertSecureFile(secureFile)

                // Delete original file
                originalFile.delete()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Restores a secure file to its original path:
     * - Read scrambled file from vault directory.
     * - Decrypt bytes using simple XOR 0x5A bitmask.
     * - Copy restored bytes to the original path.
     * - Delete scrambled vault file and room database entry.
     */
    suspend fun restoreFile(fileId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val secureFile = secureFileDao.getSecureFileById(fileId) ?: return@withContext false
                val vaultFile = File(getVaultDirectory(), secureFile.secureFileName)
                if (!vaultFile.exists()) return@withContext false

                val originalFile = File(secureFile.originalPath)
                // Ensure parent directory exists
                originalFile.parentFile?.mkdirs()

                // Decrypt XOR cipher
                val buffer = ByteArray(1024 * 64)
                FileInputStream(vaultFile).use { input ->
                    FileOutputStream(originalFile).use { output ->
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            for (i in 0 until bytesRead) {
                                buffer[i] = (buffer[i].toInt() xor 0x5A).toByte()
                            }
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                // Delete encrypted archive and database index
                vaultFile.delete()
                secureFileDao.deleteSecureFileById(fileId)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Read and decrypt secure file into temp location for preview/playback inside app,
     * without compromising original security on device.
     */
    suspend fun getDecryptedTempFile(fileId: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val secureFile = secureFileDao.getSecureFileById(fileId) ?: return@withContext null
                val vaultFile = File(getVaultDirectory(), secureFile.secureFileName)
                if (!vaultFile.exists()) return@withContext null

                val tempDir = File(context.cacheDir, "vault_preview")
                if (!tempDir.exists()) tempDir.mkdirs()

                val tempFile = File(tempDir, "temp_" + secureFile.displayName)
                tempFile.createNewFile()

                // Decrypt XOR cipher
                val buffer = ByteArray(1024 * 64)
                FileInputStream(vaultFile).use { input ->
                    FileOutputStream(tempFile).use { output ->
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            for (i in 0 until bytesRead) {
                                buffer[i] = (buffer[i].toInt() xor 0x5A).toByte()
                            }
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }
                tempFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
