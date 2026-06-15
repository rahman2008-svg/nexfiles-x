package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.FavoriteFile
import com.example.data.database.SecureFile
import com.example.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

// Data Structures
data class FileItem(
    val file: File,
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
    val category: String,
    val isFavorite: Boolean = false
)

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val photosBytes: Long,
    val videosBytes: Long,
    val audioBytes: Long,
    val docsBytes: Long,
    val apkBytes: Long,
    val zipBytes: Long,
    val junkBytes: Long,
    val duplicateBytes: Long
)

data class DuplicateGroup(
    val size: Long,
    val original: File,
    val duplicates: List<File>
)

data class ApkItem(
    val label: String,
    val packageName: String,
    val apkPath: String,
    val size: Long,
    val isSystem: Boolean,
    var isBackedUp: Boolean = false
)

data class ClipboardData(
    val files: Set<File>,
    val isCut: Boolean
)

class FileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FileRepository(
        application,
        db.favoriteDao(),
        db.secureFileDao(),
        db.vaultConfigDao()
    )

    // Flow states
    val favorites: StateFlow<List<FavoriteFile>> = repository.favorites.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val secureFiles: StateFlow<List<SecureFile>> = repository.secureFiles.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Navigation and screen routing
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    // Browsing storage state
    private val _sandboxRoot = MutableStateFlow<File>(File(""))
    val sandboxRoot: StateFlow<File> = _sandboxRoot.asStateFlow()

    private val _currentDir = MutableStateFlow<File>(File(""))
    val currentDir: StateFlow<File> = _currentDir.asStateFlow()

    private val _browsingFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val browsingFiles: StateFlow<List<FileItem>> = _browsingFiles.asStateFlow()

    // Screen-specific selection / action state
    private val _selectedFiles = MutableStateFlow<Set<File>>(emptySet())
    val selectedFiles: StateFlow<Set<File>> = _selectedFiles.asStateFlow()

    private val _clipboard = MutableStateFlow<ClipboardData?>(null)
    val clipboard: StateFlow<ClipboardData?> = _clipboard.asStateFlow()

    // Analytics / Diagnostics
    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()

    private val _largeFilesList = MutableStateFlow<List<FileItem>>(emptyList())
    val largeFilesList: StateFlow<List<FileItem>> = _largeFilesList.asStateFlow()

    private val _duplicateFileList = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateFileList: StateFlow<List<DuplicateGroup>> = _duplicateFileList.asStateFlow()

    // App state flags
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // APK manager state
    private val _apkList = MutableStateFlow<List<ApkItem>>(emptyList())
    val apkList: StateFlow<List<ApkItem>> = _apkList.asStateFlow()

    // Secure Vault specific state
    private val _vaultPIN = MutableStateFlow<String?>(null)
    val vaultPIN: StateFlow<String?> = _vaultPIN.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    // Media Viewer parameters
    private val _activeMediaPhoto = MutableStateFlow<File?>(null)
    val activeMediaPhoto: StateFlow<File?> = _activeMediaPhoto.asStateFlow()

    private val _activeMediaVideo = MutableStateFlow<File?>(null)
    val activeMediaVideo: StateFlow<File?> = _activeMediaVideo.asStateFlow()

    private val _activeMediaAudio = MutableStateFlow<File?>(null)
    val activeMediaAudio: StateFlow<File?> = _activeMediaAudio.asStateFlow()

    private val _audioPlaylist = MutableStateFlow<List<File>>(emptyList())
    val audioPlaylist: StateFlow<List<File>> = _audioPlaylist.asStateFlow()

    private val _audioPlayingState = MutableStateFlow(false)
    val audioPlayingState: StateFlow<Boolean> = _audioPlayingState.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            initializeSandbox()
            loadVaultPin()
            refreshAll()
        }
    }

    private suspend fun initializeSandbox() = withContext(Dispatchers.IO) {
        val appSandbox = File(getApplication<Application>().filesDir, "nexfiles_sandbox")
        if (!appSandbox.exists()) {
            appSandbox.mkdirs()
        }
        _sandboxRoot.value = appSandbox
        if (_currentDir.value.path.isEmpty() || !_currentDir.value.exists()) {
            _currentDir.value = appSandbox
        }

        // Generate mock offline files in sandbox so the user can test the applet smoothly
        val samplePhotosDir = File(appSandbox, "Photos")
        val sampleVideosDir = File(appSandbox, "Videos")
        val sampleAudioDir = File(appSandbox, "Audio")
        val sampleDocsDir = File(appSandbox, "Documents")
        val sampleApksDir = File(appSandbox, "APKs")
        val sampleDownloadsDir = File(appSandbox, "Downloads")
        val sampleZipDir = File(appSandbox, "ZIP")

        val dirs = listOf(
            samplePhotosDir, sampleVideosDir, sampleAudioDir,
            sampleDocsDir, sampleApksDir, sampleDownloadsDir, sampleZipDir
        )

        dirs.forEach { if (!it.exists()) it.mkdirs() }

        // Photos
        createSampleFile(File(samplePhotosDir, "sunset_beach.jpg"), "Sunset image", 1024 * 180)
        createSampleFile(File(samplePhotosDir, "nature_wallpaper_hd.png"), "HD wall", 1024 * 650)
        createSampleFile(File(samplePhotosDir, "screenshot_duplicate.png"), "Dup bytes", 1024 * 120)
        createSampleFile(File(samplePhotosDir, "family_snap_copy.png"), "Dup bytes", 1024 * 120) // Identical size duplicate

        // Videos
        createSampleFile(File(sampleVideosDir, "nexvora_teaser.mp4"), "NexVora cinematic trailer", 1024 * 1024 * 12)
        createSampleFile(File(sampleVideosDir, "vlog_dhaka_city.mp4"), "Dhaka vlog details", 1024 * 1024 * 105) // Large file

        // Audio
        createSampleFile(File(sampleAudioDir, "ambient_focus_beat.mp3"), "Relaxing music track", 1024 * 1200)
        createSampleFile(File(sampleAudioDir, "rain_sleep_sounds.wav"), "White noise clip", 1024 * 2400)

        // Documents
        val bioText = """
            ==================================
            🚀 NexFiles X - Brand Document 🚀
            ==================================
            NexFiles X is a high-performance, offline-first smart file optimizer.
            
            👨💻 Developer Profile:
            - Developer: Prince AR Abdur Rahman
            - Company: NexVora Lab's Ofc
            - Mission: Fast, Privacy-Friendly, Offline-First Android Experiences.
            
            📱 Premium Offline Suite of Products:
            1. NexPlay X - Ultimate Offline Cinema & Sound Center
            2. NexLens Studio X - Raw Photo Suite with offline filters
            3. Study AI - Intelligent Offline Textbook & Core Exam companion
            4. LifeSphere OS - Dynamic offline local dashboard launcher
            5. Smart Day Planner X - Minimalistic hourly matrix organizer
            
            Thanks for choosing NexFiles X! Your security is guaranteed offline.
        """.trimIndent()
        val bioFile = File(sampleDocsDir, "prince_biography.txt")
        if (!bioFile.exists()) {
            bioFile.writeText(bioText)
        }

        createSampleFile(File(sampleDocsDir, "project_charter.pdf"), "Charter specifications", 1024 * 340)
        createSampleFile(File(sampleDocsDir, "financial_statement_2026.docx"), "Doc statement", 1024 * 120)
        createSampleFile(File(sampleDocsDir, "financial_statement_dup.docx"), "Doc statement", 1024 * 120) // Duplicate

        // APKs
        createSampleFile(File(sampleApksDir, "study_ai_companion.apk"), "Offline core app", 1024 * 6200)

        // Downloads
        createSampleFile(File(sampleDownloadsDir, "billing_reciept.temp"), "Temp cache file", 1024 * 8)
        createSampleFile(File(sampleDownloadsDir, "chrome_pending.tmp"), "Temp file download", 1024 * 35)
        File(sampleDownloadsDir, "Empty_Workspace").mkdir()
        File(sampleDownloadsDir, "Unused_Temp_Docs").mkdir()

        // ZIP
        createSampleFile(File(sampleZipDir, "travel_archives.zip"), "Compressed backups", 1024 * 410)
    }

    private fun createSampleFile(file: File, prefixText: String, targetSize: Long) {
        if (!file.exists()) {
            try {
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { out ->
                    val bytes = prefixText.toByteArray()
                    out.write(bytes)
                    val remaining = targetSize - bytes.size
                    if (remaining > 0) {
                        val bufferSize = if (remaining > 4096) 4096 else remaining.toInt()
                        val emptyBuffer = ByteArray(bufferSize)
                        var left = remaining
                        while (left > 0) {
                            val writeChunk = if (left > bufferSize) bufferSize else left.toInt()
                            out.write(emptyBuffer, 0, writeChunk)
                            left -= writeChunk
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Refresh everything
    fun refreshAll() {
        viewModelScope.launch {
            _isScanning.value = true
            loadDirectoryFiles()
            scanStorageMetrics()
            scanInstalledApks()
            _isScanning.value = false
        }
    }

    // Load active directory contents
    fun loadDirectoryFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = _currentDir.value
            if (!dir.exists() || !dir.isDirectory) return@launch

            val files = dir.listFiles()?.toList() ?: emptyList()
            val mapped = files.map { file ->
                val cat = getFileCategory(file)
                FileItem(
                    file = file,
                    name = file.name,
                    size = if (file.isDirectory) getFolderSize(file) else file.length(),
                    isDirectory = file.isDirectory,
                    lastModified = file.lastModified(),
                    category = cat,
                    isFavorite = repository.isFavorite(file.absolutePath)
                )
            }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

            _browsingFiles.value = mapped
        }
    }

    fun searchFiles(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            loadDirectoryFiles()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val root = _sandboxRoot.value
            val matches = mutableListOf<File>()
            searchRecursively(root, query.lowercase(), matches)

            val mapped = matches.map { file ->
                FileItem(
                    file = file,
                    name = file.name,
                    size = if (file.isDirectory) getFolderSize(file) else file.length(),
                    isDirectory = file.isDirectory,
                    lastModified = file.lastModified(),
                    category = getFileCategory(file),
                    isFavorite = repository.isFavorite(file.absolutePath)
                )
            }
            _browsingFiles.value = mapped
        }
    }

    private fun searchRecursively(dir: File, query: String, results: MutableList<File>) {
        val list = dir.listFiles() ?: return
        for (f in list) {
            if (f.name.lowercase().contains(query)) {
                results.add(f)
            }
            if (f.isDirectory) {
                searchRecursively(f, query, results)
            }
        }
    }

    // Navigate in file explorer path
    fun navigateInto(dir: File) {
        if (dir.exists() && dir.isDirectory) {
            _currentDir.value = dir
            _selectedFiles.value = emptySet()
            loadDirectoryFiles()
        }
    }

    fun navigateUp() {
        val current = _currentDir.value
        val root = _sandboxRoot.value
        if (current.absolutePath != root.absolutePath) {
            current.parentFile?.let { parent ->
                _currentDir.value = parent
                _selectedFiles.value = emptySet()
                loadDirectoryFiles()
            }
        }
    }

    // Diagnostics storage metrics
    private suspend fun scanStorageMetrics() = withContext(Dispatchers.IO) {
        val root = _sandboxRoot.value
        if (!root.exists()) return@withContext

        var totalPhotos = 0L
        var totalVideos = 0L
        var totalAudios = 0L
        var totalDocs = 0L
        var totalApks = 0L
        var totalZips = 0L
        var totalDuplicatesSize = 0L
        var totalJunk = 0L

        val allFiles = mutableListOf<File>()
        val emptyFolders = mutableListOf<File>()
        val largeFilesAcc = mutableListOf<FileItem>()

        // Duplicates analyzer mapping
        val sizeGroupMap = mutableMapOf<Long, MutableList<File>>()

        fun gatherAndAnalyze(file: File) {
            if (file.isDirectory) {
                val contents = file.listFiles()
                if (contents == null || contents.isEmpty()) {
                    emptyFolders.add(file)
                    totalJunk += 4096 // Mock representation: empty directory consumes system block inodes
                } else {
                    contents.forEach { gatherAndAnalyze(it) }
                }
            } else {
                allFiles.add(file)
                val isJunkType = file.name.endsWith(".tmp") || file.name.endsWith(".temp")
                if (isJunkType) {
                    totalJunk += file.length()
                }

                val size = file.length()
                val cat = getFileCategory(file)
                when (cat) {
                    "Photos" -> totalPhotos += size
                    "Videos" -> totalVideos += size
                    "Audio" -> totalAudios += size
                    "Documents" -> totalDocs += size
                    "APKs" -> totalApks += size
                    "ZIP" -> totalZips += size
                }

                // Size mapping for duplicates
                if (size > 0) {
                    val list = sizeGroupMap.getOrPut(size) { mutableListOf() }
                    list.add(file)
                }

                // Large files selector (threshold: >50MB)
                if (size > 1024 * 1024 * 50) {
                    largeFilesAcc.add(
                        FileItem(
                            file = file,
                            name = file.name,
                            size = size,
                            isDirectory = false,
                            lastModified = file.lastModified(),
                            category = cat
                        )
                    )
                }
            }
        }

        // Run scanner recursively
        gatherAndAnalyze(root)

        // Select duplicates with matching sizes
        val duplicates = mutableListOf<DuplicateGroup>()
        sizeGroupMap.forEach { (size, list) ->
            if (list.size > 1) {
                // Verify duplicate authenticity (for simple sandbox demo, size matching is adequate and extremely low on memory)
                val original = list[0]
                val redundant = list.subList(1, list.size)
                duplicates.add(DuplicateGroup(size, original, redundant))
                totalDuplicatesSize += size * (list.size - 1)
            }
        }

        // Custom Cache Simulation: adding context.cacheDir details
        val rawCache = getFolderSize(getApplication<Application>().cacheDir)
        totalJunk += rawCache

        _storageStats.value = StorageStats(
            totalBytes = 100 * 1024 * 1024 * 1024, // Simulated static disk sizing (100GB total)
            freeBytes = 64 * 1024 * 1024 * 1024,
            usedBytes = 36 * 1024 * 1024 * 1024,
            photosBytes = totalPhotos,
            videosBytes = totalVideos,
            audioBytes = totalAudios,
            docsBytes = totalDocs,
            apkBytes = totalApks,
            zipBytes = totalZips,
            junkBytes = totalJunk,
            duplicateBytes = totalDuplicatesSize
        )

        _largeFilesList.value = largeFilesAcc.sortedByDescending { it.size }
        _duplicateFileList.value = duplicates
    }

    // Installed application inventory
    private suspend fun scanInstalledApks() = withContext(Dispatchers.IO) {
        val pm = getApplication<Application>().packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        val sandboxApkDir = File(_sandboxRoot.value, "APKs")

        val list = packages.mapNotNull { pkg ->
            val ai = pkg.applicationInfo ?: return@mapNotNull null
            // Focus on non-system visible applications or a diverse list
            val isSys = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (pkg.packageName == getApplication<Application>().packageName) return@mapNotNull null

            val apkFile = File(ai.sourceDir)
            if (!apkFile.exists()) return@mapNotNull null

            val label = pm.getApplicationLabel(ai).toString()
            val backupFile = File(sandboxApkDir, "Backup_$label.apk")

            ApkItem(
                label = label,
                packageName = pkg.packageName,
                apkPath = ai.sourceDir,
                size = apkFile.length(),
                isSystem = isSys,
                isBackedUp = backupFile.exists()
            )
        }.sortedBy { it.label.lowercase() }

        _apkList.value = list
    }

    // Real APK extractor implementation
    fun extractApk(apkItem: ApkItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val srcFile = File(apkItem.apkPath)
                if (!srcFile.exists()) return@launch

                val backupDir = File(_sandboxRoot.value, "APKs")
                if (!backupDir.exists()) backupDir.mkdirs()

                val backupFile = File(backupDir, "Backup_${apkItem.label}.apk")
                srcFile.copyTo(backupFile, overwrite = true)

                // Update UI state
                val updated = _apkList.value.map {
                    if (it.packageName == apkItem.packageName) {
                        it.copy(isBackedUp = true)
                    } else it
                }
                _apkList.value = updated
                scanStorageMetrics()
                loadDirectoryFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Toggle multi-selection files
    fun toggleFileSelection(file: File) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(file)) {
            current.remove(file)
        } else {
            current.add(file)
        }
        _selectedFiles.value = current
    }

    fun selectAllFiles() {
        val all = _browsingFiles.value.map { it.file }.toSet()
        _selectedFiles.value = all
    }

    fun clearFileSelection() {
        _selectedFiles.value = emptySet()
    }

    // Actions Clipboard
    fun copySelected(isCut: Boolean) {
        if (_selectedFiles.value.isNotEmpty()) {
            _clipboard.value = ClipboardData(_selectedFiles.value, isCut)
        }
    }

    fun pasteClipboard() {
        val data = _clipboard.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val destination = _currentDir.value
            for (f in data.files) {
                if (!f.exists()) continue
                val targetFile = File(destination, f.name)
                try {
                    if (data.isCut) {
                        f.renameTo(targetFile)
                    } else {
                        if (f.isDirectory) {
                            f.copyRecursively(targetFile, overwrite = true)
                        } else {
                            f.copyTo(targetFile, overwrite = true)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _clipboard.value = null
            _selectedFiles.value = emptySet()
            loadDirectoryFiles()
            scanStorageMetrics()
        }
    }

    // File operation mechanics
    fun performRename(file: File, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val destination = File(file.parentFile, newName)
            if (file.exists() && !destination.exists()) {
                file.renameTo(destination)
                loadDirectoryFiles()
            }
        }
    }

    fun performBatchDelete(files: Set<File>) {
        viewModelScope.launch(Dispatchers.IO) {
            files.forEach { f ->
                if (f.exists()) {
                    if (f.isDirectory) {
                        f.deleteRecursively()
                    } else {
                        f.delete()
                    }
                }
            }
            _selectedFiles.value = emptySet()
            loadDirectoryFiles()
            scanStorageMetrics()
        }
    }

    // Favorites DB persistence
    fun toggleFavorite(fileItem: FileItem) {
        viewModelScope.launch {
            if (fileItem.isFavorite) {
                repository.removeFavorite(fileItem.file.absolutePath)
            } else {
                repository.addFavorite(
                    path = fileItem.file.absolutePath,
                    name = fileItem.name,
                    size = fileItem.size,
                    category = fileItem.category,
                    isDirectory = fileItem.isDirectory
                )
            }
            loadDirectoryFiles()
        }
    }

    // Junk Cleanup mechanics
    fun cleanAllJunk() {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete cache files
            getApplication<Application>().cacheDir.deleteRecursively()

            // Delete sandbox temp files (.tmp, .temp)
            cleanSandboxJunkRecursive(_sandboxRoot.value)

            scanStorageMetrics()
            loadDirectoryFiles()
        }
    }

    private fun cleanSandboxJunkRecursive(dir: File) {
        val list = dir.listFiles() ?: return
        for (f in list) {
            if (f.isDirectory) {
                cleanSandboxJunkRecursive(f)
                // If directory became empty, clean it as well
                if (f.listFiles()?.isEmpty() == true) {
                    f.delete()
                }
            } else {
                if (f.name.endsWith(".tmp") || f.name.endsWith(".temp")) {
                    f.delete()
                }
            }
        }
    }

    // Duplicates solver
    fun deleteDuplicates(group: DuplicateGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            group.duplicates.forEach { f ->
                if (f.exists()) f.delete()
            }
            scanStorageMetrics()
            loadDirectoryFiles()
        }
    }

    // File compressors: ZIP & Unzip
    fun compressToZip(files: Set<File>, zipName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val outputZip = File(_currentDir.value, if (zipName.endsWith(".zip")) zipName else "$zipName.zip")
            try {
                ZipOutputStream(FileOutputStream(outputZip)).use { zos ->
                    for (file in files) {
                        addZipEntryRecursive(file, file.name, zos)
                    }
                }
                _selectedFiles.value = emptySet()
                loadDirectoryFiles()
                scanStorageMetrics()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addZipEntryRecursive(file: File, relativePath: String, zos: ZipOutputStream) {
        if (file.isDirectory) {
            val children = file.listFiles() ?: return
            if (children.isEmpty()) {
                zos.putNextEntry(ZipEntry("$relativePath/"))
                zos.closeEntry()
            } else {
                for (child in children) {
                    addZipEntryRecursive(child, "$relativePath/${child.name}", zos)
                }
            }
        } else {
            zos.putNextEntry(ZipEntry(relativePath))
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(4096)
                var len: Int
                while (fis.read(buffer).also { len = it } > 0) {
                    zos.write(buffer, 0, len)
                }
            }
            zos.closeEntry()
        }
    }

    fun decompressZip(zipFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val unzipDirName = zipFile.nameWithoutExtension + "_extracted"
            val targetDir = File(zipFile.parentFile, unzipDirName)
            if (!targetDir.exists()) targetDir.mkdirs()

            try {
                ZipInputStream(FileInputStream(zipFile)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val newFile = File(targetDir, entry.name)
                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                val buffer = ByteArray(4096)
                                var len: Int
                                while (zis.read(buffer).also { len = it } > 0) {
                                    fos.write(buffer, 0, len)
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
                loadDirectoryFiles()
                scanStorageMetrics()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Secure Vault passcode configurations
    private fun loadVaultPin() {
        viewModelScope.launch {
            _vaultPIN.value = repository.getVaultPin()
        }
    }

    fun setupVaultPin(pin: String) {
        viewModelScope.launch {
            repository.setVaultPin(pin)
            _vaultPIN.value = pin
            _isVaultUnlocked.value = true
        }
    }

    fun verifyVaultPin(pin: String): Boolean {
        val matches = _vaultPIN.value == pin
        if (matches) {
            _isVaultUnlocked.value = true
        }
        return matches
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun resetVault() {
        viewModelScope.launch {
            repository.clearVaultPin()
            _vaultPIN.value = null
            _isVaultUnlocked.value = false
        }
    }

    fun moveFileToVault(fileItem: FileItem) {
        viewModelScope.launch {
            val hiddenResult = repository.hideFile(
                originalPath = fileItem.file.absolutePath,
                displayName = fileItem.name,
                category = fileItem.category
            )
            if (hiddenResult) {
                loadDirectoryFiles()
                scanStorageMetrics()
            }
        }
    }

    fun restoreFileFromVault(secureFile: SecureFile) {
        viewModelScope.launch {
            val restoredResult = repository.restoreFile(secureFile.id)
            if (restoredResult) {
                loadDirectoryFiles()
                scanStorageMetrics()
            }
        }
    }

    // Media Viewer controller setters
    fun viewPhoto(file: File?) {
        _activeMediaPhoto.value = file
        if (file != null) navigateTo("view_photo")
    }

    fun viewVideo(file: File?) {
        _activeMediaVideo.value = file
        if (file != null) navigateTo("view_video")
    }

    fun playAudio(file: File) {
        _activeMediaAudio.value = file
        _audioPlayingState.value = true

        // Find sibling audio files to compile playlist
        val parent = file.parentFile
        val siblings = parent?.listFiles()?.filter { f ->
            val cat = getFileCategory(f)
            cat == "Audio"
        }?.toList() ?: listOf(file)

        _audioPlaylist.value = siblings
        navigateTo("view_audio")
    }

    fun toggleAudioPlayback() {
        _audioPlayingState.value = !_audioPlayingState.value
    }

    fun skipNextAudio() {
        val list = _audioPlaylist.value
        val current = _activeMediaAudio.value
        if (list.isNotEmpty() && current != null) {
            val idx = list.indexOf(current)
            if (idx != -1 && idx < list.size - 1) {
                _activeMediaAudio.value = list[idx + 1]
            } else {
                _activeMediaAudio.value = list[0]
            }
        }
    }

    fun skipPreviousAudio() {
        val list = _audioPlaylist.value
        val current = _activeMediaAudio.value
        if (list.isNotEmpty() && current != null) {
            val idx = list.indexOf(current)
            if (idx != -1 && idx > 0) {
                _activeMediaAudio.value = list[idx - 1]
            } else {
                _activeMediaAudio.value = list[list.size - 1]
            }
        }
    }

    // Local Helper Methods
    private fun getFileCategory(file: File): String {
        if (file.isDirectory) return "Folders"
        val ext = file.extension.lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp" -> "Photos"
            "mp4", "mkv", "3gp", "avi", "webm" -> "Videos"
            "mp3", "wav", "m4a", "ogg", "flac" -> "Audio"
            "pdf", "docx", "doc", "xlsx", "xls", "txt", "ppt", "pptx" -> "Documents"
            "apk" -> "APKs"
            "zip", "rar", "tar", "gz" -> "ZIP"
            else -> "Downloads"
        }
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) getFolderSize(f) else f.length()
        }
        return size
    }
}
