package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SecureFile
import com.example.ui.screens.Neon_Pink_Fallback
import com.example.ui.viewmodel.DuplicateGroup
import com.example.ui.viewmodel.FileItem
import com.example.ui.viewmodel.FileViewModel
import com.example.ui.viewmodel.StorageStats
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: FileViewModel,
    onCategoryClick: (String) -> Unit
) {
    val stats by viewModel.storageStats.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NexFiles X",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Optimization Offline Suite",
                    fontSize = 12.sp,
                    color = CyberBlue
                )
            }
            IconButton(
                onClick = {
                    viewModel.refreshAll()
                },
                modifier = Modifier.border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Scanner",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytics Ring Chart Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Storage Analytics",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            stats?.let { s ->
                val freeStr = formatSize(s.freeBytes)
                val usedStr = formatSize(s.photosBytes + s.videosBytes + s.audioBytes + s.docsBytes + s.apkBytes + s.zipBytes)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StorageDonutChart(
                        photos = s.photosBytes,
                        videos = s.videosBytes,
                        audio = s.audioBytes,
                        docs = s.docsBytes,
                        apks = s.apkBytes,
                        zips = s.zipBytes,
                        free = s.freeBytes,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        LegendItem("Photos", formatSize(s.photosBytes), CyberBlue)
                        LegendItem("Videos", formatSize(s.videosBytes), ActiveGreen)
                        LegendItem("Audio", formatSize(s.audioBytes), DeepPurple)
                        LegendItem("Documents", formatSize(s.docsBytes), SoftGold)
                        LegendItem("APKs", formatSize(s.apkBytes), Neon_Pink_Fallback)
                        LegendItem("ZIP Base", formatSize(s.zipBytes), WarningOrange)
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberBlue)
            }
        }

        // Smart Clean Suggestion Card
        stats?.let { s ->
            val cleanableSize = s.junkBytes + s.duplicateBytes
            if (cleanableSize > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            viewModel.navigateTo("clean")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)), // bg-indigo-600
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧹", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Clean up ${formatSize(cleanableSize)}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Junk files and duplicate files",
                                    color = Color(0xFFE0E7FF), // text-indigo-100
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.navigateTo("clean")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "CLEAN",
                                color = Color(0xFF4F46E5), // Indigo-600
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Grid Selector
        Text(
            text = "Categories",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val categories = listOf(
            CategoryData("Photos", Icons.Default.Image, CyberBlue),
            CategoryData("Videos", Icons.Default.Movie, ActiveGreen),
            CategoryData("Audio", Icons.Default.MusicNote, DeepPurple),
            CategoryData("Documents", Icons.Default.Description, SoftGold),
            CategoryData("APKs", Icons.Default.Android, Neon_Pink_Fallback),
            CategoryData("ZIP", Icons.Default.FolderZip, WarningOrange)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onCategoryClick(cat.name) },
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cat.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = cat.name,
                                tint = cat.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggestions / Clean Card
        stats?.let { s ->
            if (s.junkBytes > 0 || s.duplicateBytes > 0) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ActiveGreen.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ActiveGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clean Up",
                                tint = ActiveGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Optimizer Recommendations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            val cleanable = s.junkBytes + s.duplicateBytes
                            Text(
                                text = "Clean up details: Save up to ${formatSize(cleanable)} space",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Button(
                            onClick = { viewModel.navigateTo("clean") },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Solve", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer products matrix
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = DeepPurple.copy(alpha = 0.3f),
            onClick = { viewModel.navigateTo("about") }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Workspaces,
                        contentDescription = "NexVora",
                        tint = DeepPurple
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "About Developer & Products",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Prince AR Abdur Rahman - NexVora Lab's Ofc",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Icon(Icons.Default.ChevronRight, "More", tint = Color.LightGray)
            }
        }
    }
}

data class CategoryData(val name: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorerScreen(viewModel: FileViewModel) {
    val currentDir by viewModel.currentDir.collectAsStateWithLifecycle()
    val files by viewModel.browsingFiles.collectAsStateWithLifecycle()
    val selected by viewModel.selectedFiles.collectAsStateWithLifecycle()
    val clipboard by viewModel.clipboard.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Operations dialogue state
    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var showZipDialog by remember { mutableStateOf(false) }
    var zipInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Top Path Row
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            border = BorderStroke(0.5.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateUp() },
                    enabled = currentDir.parentFile != null && currentDir.name != "nexfiles_sandbox"
                ) {
                    Icon(Icons.Default.ArrowUpward, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Root/ ... /${currentDir.name}",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (selected.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearFileSelection() }) {
                        Icon(Icons.Default.Close, "Cancel Selected", tint = DangerRed)
                    }
                }
            }
        }

        // Real Search Field
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.searchFiles(it) },
            placeholder = { Text("Search offline files...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchFiles("") }) {
                        Icon(Icons.Default.Clear, "Clear Search", tint = Color.Gray)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GlassBg,
                unfocusedContainerColor = GlassBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Files listing
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen,
                        "Empty Folder",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No offline files found in this directory", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(files) { item ->
                    val isChecked = selected.contains(item.file)
                    val cardBorderColor = if (isChecked) CyberBlue else GlassBorder
                    val backgroundVal = if (isChecked) Color(0x3000A2FF) else GlassBg

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (selected.isNotEmpty()) {
                                        viewModel.toggleFileSelection(item.file)
                                    } else {
                                        if (item.isDirectory) {
                                            viewModel.navigateInto(item.file)
                                        } else {
                                            // Open specific media viewer
                                            when (item.category) {
                                                "Photos" -> viewModel.viewPhoto(item.file)
                                                "Videos" -> viewModel.viewVideo(item.file)
                                                "Audio" -> viewModel.playAudio(item.file)
                                                else -> {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "Previewing: ${item.name}",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
                                            }
                                        }
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleFileSelection(item.file)
                                }
                            ),
                        colors = CardDefaults.cardColors(containerColor = backgroundVal),
                        border = BorderStroke(0.5.dp, cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File format Icon
                            val iconPair = getFileIcon(item)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(iconPair.second.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconPair.first,
                                    contentDescription = item.category,
                                    tint = iconPair.second,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatSize(item.size)}  •  ${formatDate(item.lastModified)}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            // Favorite Icon
                            IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite Toggle",
                                    tint = if (item.isFavorite) SoftGold else Color.Gray
                                )
                            }

                            // Selection Box
                            if (selected.isNotEmpty()) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleFileSelection(item.file) },
                                    colors = CheckboxDefaults.colors(checkedColor = CyberBlue)
                                )
                            } else {
                                // Three dots local actions menu
                                var expandedActions by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { expandedActions = true }) {
                                        Icon(Icons.Default.MoreVert, "Actions", tint = Color.LightGray)
                                    }
                                    DropdownMenu(
                                        expanded = expandedActions,
                                        onDismissRequest = { expandedActions = false },
                                        modifier = Modifier.background(DarkSurfaceVariant)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Rename", color = Color.White) },
                                            onClick = {
                                                expandedActions = false
                                                showRenameDialog = item.file
                                                renameInput = item.file.name
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Favorite", color = Color.White) },
                                            onClick = {
                                                expandedActions = false
                                                viewModel.toggleFavorite(item)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Hide in Vault", color = Color.White) },
                                            onClick = {
                                                expandedActions = false
                                                viewModel.moveFileToVault(item)
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Moved to Secure Vault!",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        )
                                        if (item.name.endsWith(".zip")) {
                                            DropdownMenuItem(
                                                text = { Text("Unzip Extractions", color = Color.White) },
                                                onClick = {
                                                    expandedActions = false
                                                    viewModel.decompressZip(item.file)
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "ZIP Uncompressed successfully",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = DangerRed) },
                                            onClick = {
                                                expandedActions = false
                                                viewModel.performBatchDelete(setOf(item.file))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Batch Clipboard menu controller
        AnimatedVisibility(
            visible = selected.isNotEmpty() || clipboard != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selected.isNotEmpty()) {
                        IconButton(onClick = { viewModel.copySelected(isCut = false) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ContentCopy, "Copy", tint = CyberBlue, modifier = Modifier.size(20.dp))
                                Text("Copy", fontSize = 9.sp, color = Color.White)
                            }
                        }
                        IconButton(onClick = { viewModel.copySelected(isCut = true) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ContentCut, "Cut", tint = WarningOrange, modifier = Modifier.size(20.dp))
                                Text("Cut", fontSize = 9.sp, color = Color.White)
                            }
                        }
                        IconButton(onClick = { showZipDialog = true }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FolderZip, "Compress", tint = SoftGold, modifier = Modifier.size(20.dp))
                                Text("ZIP", fontSize = 9.sp, color = Color.White)
                            }
                        }
                        IconButton(onClick = { viewModel.performBatchDelete(selected) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Delete, "Delete", tint = DangerRed, modifier = Modifier.size(20.dp))
                                Text("Delete", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }

                    if (clipboard != null) {
                        Button(
                            onClick = { viewModel.pasteClipboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen)
                        ) {
                            Icon(Icons.Default.ContentPaste, "Paste", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste HERE", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog != null) {
        val target = showRenameDialog!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Offline File", color = Color.White) },
            text = {
                TextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    colors = TextFieldDefaults.colors(focusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotEmpty()) {
                            viewModel.performRename(target, renameInput)
                        }
                        showRenameDialog = null
                    }
                ) {
                    Text("Save", color = CyberBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Close", color = Color.Gray)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }

    // Zip Dialog
    if (showZipDialog) {
        AlertDialog(
            onDismissRequest = { showZipDialog = false },
            title = { Text("ZIP Compression", color = Color.White) },
            text = {
                Column {
                    Text("Set offline compressed archive name:", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = zipInput,
                        onValueChange = { zipInput = it },
                        placeholder = { Text("my_photos") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (zipInput.isNotEmpty()) {
                            viewModel.compressToZip(selected, zipInput)
                        }
                        showZipDialog = false
                    }
                ) {
                    Text("Compress(ZIP)", color = CyberBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showZipDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

fun getFileIcon(item: FileItem): Pair<ImageVector, Color> {
    if (item.isDirectory) return Pair(Icons.Default.Folder, SoftGold)
    return when (item.category) {
        "Photos" -> Pair(Icons.Default.Image, CyberBlue)
        "Videos" -> Pair(Icons.Default.Movie, ActiveGreen)
        "Audio" -> Pair(Icons.Default.MusicNote, DeepPurple)
        "Documents" -> Pair(Icons.Default.Description, SoftGold)
        "APKs" -> Pair(Icons.Default.Android, Neon_Pink_Fallback)
        "ZIP" -> Pair(Icons.Default.FolderZip, WarningOrange)
        else -> Pair(Icons.Default.Article, Color.LightGray)
    }
}

fun formatDate(ts: Long): String {
    val df = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return df.format(Date(ts))
}

@Composable
fun CleanScreen(viewModel: FileViewModel) {
    val stats by viewModel.storageStats.collectAsStateWithLifecycle()
    val duplicates by viewModel.duplicateFileList.collectAsStateWithLifecycle()
    val largeFiles by viewModel.largeFilesList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core Header
        Text(
            text = "Smart Optimizer & Cleaner",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Keep offline RAM clean & storage optimized",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center visual cleaner ring
        stats?.let { s ->
            val cleanable = s.junkBytes + s.duplicateBytes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ActiveGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = GlassBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatSize(cleanable),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActiveGreen
                    )
                    Text(
                        text = "Total Storage Recoverable",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.cleanAllJunk()
                            Toast.makeText(context, "Offline Junk cleaned completely!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Icon(Icons.Default.CleaningServices, "Wand", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clean All", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duplicates Group Listing
        Text(
            text = "Duplicate Finder (${duplicates.size} Groups)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (duplicates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassBg, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Excellent! No duplicate size records found.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                duplicates.forEach { dupGroup ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dupGroup.original.name,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Loss: ${formatSize(dupGroup.size * dupGroup.duplicates.size)}",
                                    fontSize = 11.sp,
                                    color = DangerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Path: ${dupGroup.original.parentFile?.name} | ${dupGroup.duplicates.size} copies",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.deleteDuplicates(dupGroup)
                                    Toast.makeText(context, "Duplicates solved offline!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x30FF3D00)),
                                modifier = Modifier.align(Alignment.End),
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text("Remove Redundant Copies", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Files Checker
        Text(
            text = "Large Files Detector (>50MB)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (largeFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassBg, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No heavy files currently consumed on sandbox.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                largeFiles.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg),
                        border = BorderStroke(0.5.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(WarningOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.InsertDriveFile, "Large File", tint = WarningOrange)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Allocated: ${formatSize(item.size)}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.performBatchDelete(setOf(item.file))
                                    Toast.makeText(context, "Deleted heavy file!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = DangerRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VaultScreen(viewModel: FileViewModel) {
    val isVaultConfigured by viewModel.vaultPIN.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val secureFileList by viewModel.secureFiles.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pinText by remember { mutableStateOf("") }
    var setupText by remember { mutableStateOf("") }
    var confirmText by remember { mutableStateOf("") }
    var inSetupPhase by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isVaultConfigured == null) {
            // First time setup screen
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = "Setup Lock",
                modifier = Modifier.size(64.dp),
                tint = DeepPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Set Secure Vault PIN", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Lock your sensitive files offline using XOR cipher", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = setupText,
                onValueChange = { if (it.length <= 4) setupText = it },
                label = { Text("Enter 4-Digit PIN") },
                colors = TextFieldDefaults.colors(focusedTextColor = Color.White),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = confirmText,
                onValueChange = { if (it.length <= 4) confirmText = it },
                label = { Text("Confirm PIN") },
                colors = TextFieldDefaults.colors(focusedTextColor = Color.White),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (setupText.length == 4 && setupText == confirmText) {
                        viewModel.setupVaultPin(setupText)
                    } else {
                        Toast.makeText(context, "PINs do not match or empty", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Initialize Vault", color = Color.White)
            }
        } else if (!isUnlocked) {
            // Unlocking PIN Grid Code pad
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked Space",
                modifier = Modifier.size(64.dp),
                tint = DeepPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Vault Secure Area", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Enter your secret 4-digit token", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Display PIN dots representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until 4) {
                    val active = i < pinText.length
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (active) DeepPurple else Color.DarkGray)
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful grid keypad
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Reset", "0", "Back")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(keys) { key ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassBg)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                when (key) {
                                    "Back" -> {
                                        if (pinText.isNotEmpty()) {
                                            pinText = pinText.substring(0, pinText.length - 1)
                                        }
                                    }
                                    "Reset" -> {
                                        viewModel.resetVault()
                                        Toast.makeText(context, "Vault reset. Enter new PIN.", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        if (pinText.length < 4) {
                                            pinText += key
                                            if (pinText.length == 4) {
                                                val verified = viewModel.verifyVaultPin(pinText)
                                                if (!verified) {
                                                    Toast.makeText(context, "Incorrect Code!", Toast.LENGTH_SHORT).show()
                                                    pinText = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Vault Area Opened! Show list of locked files
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Encrypted Privacy Vault",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${secureFileList.size} files scrambled with XOR bitmask",
                        fontSize = 12.sp,
                        color = ActiveGreen
                    )
                }

                IconButton(
                    onClick = { viewModel.lockVault() },
                    modifier = Modifier.background(DeepPurple, CircleShape)
                ) {
                    Icon(Icons.Default.Lock, "Lock Folder", tint = Color.White)
                }
            }

            if (secureFileList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderSpecial, "No Vault", modifier = Modifier.size(64.dp), tint = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Vault is empty!\nGo to local files explorer and click 'Hide in Vault' on any file.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(secureFileList) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassBg),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, "Secure", tint = DeepPurple)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = formatSize(item.size),
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                                Box {
                                    var optActions by remember { mutableStateOf(false) }
                                    IconButton(onClick = { optActions = true }) {
                                        Icon(Icons.Default.MoreVert, "Vault Actions", tint = Color.LightGray)
                                    }
                                    DropdownMenu(
                                        expanded = optActions,
                                        onDismissRequest = { optActions = false },
                                        modifier = Modifier.background(DarkSurfaceVariant)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Restore original", color = Color.White) },
                                            onClick = {
                                                optActions = false
                                                viewModel.restoreFileFromVault(item)
                                                Toast.makeText(context, "File restored to storage!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Remove from device", color = DangerRed) },
                                            onClick = {
                                                optActions = false
                                                // Clear record
                                                viewModel.performBatchDelete(setOf(File(context.filesDir, "secured_vault/" + item.secureFileName)))
                                                viewModel.refreshAll()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolsScreen(viewModel: FileViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NexFiles Premium Offline Tools",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Advanced offline tools for smart files and quick extracts",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tool 1: APK Manager
        MetricRow(
            icon = Icons.Default.Backup,
            title = "APK Backup & Extractor",
            subtitle = "Extract, backup and share your installed application binaries",
            accentColor = Neon_Pink_Fallback,
            onClick = { viewModel.navigateTo("apk_manager") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 2: Share offline
        MetricRow(
            icon = Icons.Default.QrCodeScanner,
            title = "Quick Share & QR Pairing",
            subtitle = "Transfer files locally without internet over WiFi Hotspots",
            accentColor = CyberBlue,
            onClick = { viewModel.navigateTo("share") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 3: About Prince
        MetricRow(
            icon = Icons.Default.AccountBox,
            title = "Developer & About",
            subtitle = "Prince AR Abdur Rahman - NexVora Lab's Ofc",
            accentColor = DeepPurple,
            onClick = { viewModel.navigateTo("about") }
        )
    }
}

@Composable
fun ApkManagerScreen(viewModel: FileViewModel) {
    val apks by viewModel.apkList.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showSystemApps by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("APK Backup Center", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Export offline `.apk` files directly", fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Systems", fontSize = 11.sp, color = Color.LightGray)
                Switch(
                    checked = showSystemApps,
                    onCheckedChange = { showSystemApps = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Neon_Pink_Fallback)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning && apks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Neon_Pink_Fallback)
            }
        } else {
            val filtered = apks.filter { showSystemApps || !it.isSystem }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { apk ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Neon_Pink_Fallback.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Android, apk.label, tint = Neon_Pink_Fallback)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = apk.label,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${apk.packageName}  •  ${formatSize(apk.size)}",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (apk.isBackedUp) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Extracted",
                                    tint = ActiveGreen
                                )
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.extractApk(apk)
                                        Toast.makeText(context, "${apk.label} extracted to APKs!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Neon_Pink_Fallback),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("Extract", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShareScreen(viewModel: FileViewModel) {
    var sharePhase by remember { mutableStateOf("ready") } // ready, transmitting, paused, paired
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (sharePhase == "ready") {
            Icon(Icons.Default.QrCodeScanner, "QR Code", modifier = Modifier.size(80.dp), tint = CyberBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Offline Peer Sharing",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Zero-data local transmissions (WiFi Direct / QR Pairing)",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { sharePhase = "paired" },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
            ) {
                Icon(Icons.Default.QrCode, "Show QR")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Connection QR", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Scanning local receivers offline...", Toast.LENGTH_SHORT).show()
                    sharePhase = "transmitting"
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlassBg),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
            ) {
                Icon(Icons.Default.CompassCalibration, "Scan")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Receiver's QR", color = Color.White)
            }
        } else if (sharePhase == "paired") {
            // Render simulated beautiful QR Pairing panel
            Card(
                modifier = Modifier.size(240.dp).border(1.dp, GlassBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing static mock QR code Matrix grid
                        val rectSize = size.width / 8f
                        for (r in 0 until 8) {
                            for (c in 0 until 8) {
                                val drawActive = (r+c) % 2 == 0 || (r in 0..2 && c in 0..2) || (r in 5..7 && c in 0..2) || (r in 0..2 && c in 5..7)
                                if (drawActive) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(c * rectSize, r * rectSize),
                                        size = Size(rectSize - 2f, rectSize - 2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Scan to pair local device", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Simulating local hotspot pairing channel", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { sharePhase = "ready" },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
            ) {
                Text("Disconnect", color = Color.White)
            }
        } else {
            // Transmitting animation state
            CircularProgressIndicator(color = CyberBlue, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connecting Offline Senders...", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Using Local WiFi-Direct pipeline (0.00KB cellular consumed)", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { sharePhase = "ready" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

@Composable
fun AboutScreen(viewModel: FileViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aesthetic avatar halo
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberBlue, DeepPurple, Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DeveloperMode,
                contentDescription = "Prince AR Abdur Rahman",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Prince AR Abdur Rahman",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Chief Architect & Founder, NexVora Lab's Ofc",
            fontSize = 12.sp,
            color = CyberBlue,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Mission: Build high performance, offline-first applications prioritizing user data privacy & extreme lightweight execution engines.",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NexVora Premium Product Suite",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))

        val productRowItems = listOf(
            ProductSpec("NexPlay X", "Ultimate local media viewer & cinema sound system", Icons.Default.PlayCircle, ActiveGreen),
            ProductSpec("NexLens Studio X", "Ultra premium DSLR color grading suite", Icons.Default.Camera, CyberBlue),
            ProductSpec("Study AI", "Personal study text parser & AI quiz examiner", Icons.Default.Book, SoftGold),
            ProductSpec("LifeSphere OS", "Ambient central widgets scheduler for local space", Icons.Default.Language, DeepPurple),
            ProductSpec("Smart Day Planner X", "Aesthetic grid hourly focus routine blocker", Icons.Default.CalendarMonth, WarningOrange)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            productRowItems.forEach { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(product.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(product.icon, product.title, tint = product.color, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(product.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(product.desc, color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("© 2026 NexVora Lab's Ofc. All rights reserved.", color = Color.DarkGray, fontSize = 10.sp)
    }
}

data class ProductSpec(val title: String, val desc: String, val icon: ImageVector, val color: Color)
