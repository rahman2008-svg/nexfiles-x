package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.FileViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ViewPhotoScreen(viewModel: FileViewModel) {
    val photoFile by viewModel.activeMediaPhoto.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var scale by remember { mutableStateOf(1f) }
    var slideshowActive by remember { mutableStateOf(false) }
    var currentPhotoIndex by remember { mutableStateOf(0) }
    var showMetadata by remember { mutableStateOf(false) }

    // Slideshow simulator loop
    LaunchedEffect(slideshowActive) {
        if (slideshowActive) {
            while (true) {
                delay(3000)
                viewModel.skipNextAudio() // Skips to sibling in list
                Toast.makeText(context, "Slideshow playing...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Toolbar header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("explorer") }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                photoFile?.name ?: "Image Viewer",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            IconButton(onClick = { showMetadata = !showMetadata }) {
                Icon(Icons.Default.Info, "Metadata", tint = if (showMetadata) CyberBlue else Color.White)
            }
        }

        // Expanded interactive canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    "Photo preview",
                    modifier = Modifier
                        .size(140.dp * scale)
                        .clip(RoundedCornerShape(8.dp)),
                    tint = CyberBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                photoFile?.let { f ->
                    Text(f.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Interactive Offline Canvas Workspace", color = Color.Gray, fontSize = 11.sp)
                }
            }

            // Expanded Metadata Sheet
            androidx.compose.animation.AnimatedVisibility(
                visible = showMetadata,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                photoFile?.let { f ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Image Info Matrix", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Filename: ${f.name}", fontSize = 11.sp, color = Color.White)
                            Text("Resolution: Mock 1920 x 1080px (Lossless)", fontSize = 11.sp, color = Color.LightGray)
                            Text("Byte Size: ${formatSize(f.length())}", fontSize = 11.sp, color = Color.LightGray)
                            Text("AbsolutePath: ${f.absolutePath}", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        // Screen controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { scale = if (scale > 1f) scale - 0.5f else 1f },
                modifier = Modifier.background(GlassBg, CircleShape)
            ) {
                Icon(Icons.Default.ZoomOut, "Zoom Out", tint = Color.White)
            }

            Button(
                onClick = { slideshowActive = !slideshowActive },
                colors = ButtonDefaults.buttonColors(containerColor = if (slideshowActive) ActiveGreen else CyberBlue)
            ) {
                Icon(if (slideshowActive) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircle, "Slideshow")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (slideshowActive) "Stop Slideshow" else "Start Slideshow", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { scale = if (scale < 3f) scale + 0.5f else 3f },
                modifier = Modifier.background(GlassBg, CircleShape)
            ) {
                Icon(Icons.Default.ZoomIn, "Zoom In", tint = Color.White)
            }
        }
    }
}

@Composable
fun ViewVideoScreen(viewModel: FileViewModel) {
    val videoFile by viewModel.activeMediaVideo.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(1.0f) }
    var activeSubtitle by remember { mutableStateOf("None") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("explorer") }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                videoFile?.name ?: "Video Player",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
        }

        // Active Player Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PlayCircleFilled,
                    "Video playing",
                    modifier = Modifier.size(80.dp),
                    tint = ActiveGreen
                )
                Spacer(modifier = Modifier.height(16.dp))
                videoFile?.let { f ->
                    Text(f.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Rendering offline audio-visual matrices", color = Color.Gray, fontSize = 11.sp)
                }

                if (activeSubtitle != "None") {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "[Subtitle stream - $activeSubtitle]: \"Welcome to Prince's NexFiles X offline theater!\"",
                        color = SoftGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color(0xBA000000), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controllers
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Tracking bar Simulation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("0:23", fontSize = 10.sp, color = Color.LightGray)
                    LinearProgressIndicator(
                        progress = { 0.15f },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape),
                        color = ActiveGreen,
                        trackColor = Color.DarkGray
                    )
                    Text("3:14", fontSize = 10.sp, color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speeds selectors
                    TextButton(
                        onClick = {
                            speed = when (speed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                            Toast.makeText(context, "Rate altered to ${speed}x", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("${speed}x SPEED", fontSize = 12.sp, color = ActiveGreen, fontWeight = FontWeight.Bold)
                    }

                    // Main pause playback
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.background(ActiveGreen, CircleShape)
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Pause/Play", tint = Color.Black)
                    }

                    // Subtitles toggle
                    TextButton(
                        onClick = {
                            activeSubtitle = when (activeSubtitle) {
                                "None" -> "EN-Subtitle"
                                "EN-Subtitle" -> "BN-Subtitle"
                                else -> "None"
                            }
                        }
                    ) {
                        Text("CC: $activeSubtitle", fontSize = 12.sp, color = SoftGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ViewAudioScreen(viewModel: FileViewModel) {
    val audioFile by viewModel.activeMediaAudio.collectAsStateWithLifecycle()
    val playState by viewModel.audioPlayingState.collectAsStateWithLifecycle()
    val playlist by viewModel.audioPlaylist.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeEqualizer by remember { mutableStateOf("Flat") }
    var showQueueList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("explorer") }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                "Offline Sound Center",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            IconButton(onClick = { showQueueList = !showQueueList }) {
                Icon(Icons.Default.QueueMusic, "Queue Info", tint = if (showQueueList) DeepPurple else Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showQueueList) {
            // Display playlist items list queue
            Text(
                text = "Interactive Play Queue (${playlist.size} Items)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(playlist) { item ->
                    val tracking = item == audioFile
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.playAudio(item) },
                        colors = CardDefaults.cardColors(containerColor = if (tracking) Color(0x309100FF) else GlassBg)
                    ) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Icon(Icons.Default.MusicNote, "Sound icon", tint = if (tracking) DeepPurple else Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                item.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            // Sound desk visualizations
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Album,
                        "Record Disk rot",
                        modifier = Modifier.size(130.dp),
                        tint = DeepPurple
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    audioFile?.let { f ->
                        Text(
                            text = f.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            text = "NexPlay X Core engine  •  Eq: $activeEqualizer",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Equializer equalizer spectrum simulation
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val heights = if (playState) {
                            listOf(0.9f, 0.4f, 0.95f, 0.6f, 0.8f, 0.3f, 0.7f, 0.5f, 0.92f)
                        } else {
                            listOf(0.08f, 0.08f, 0.08f, 0.08f, 0.08f, 0.08f, 0.08f, 0.08f, 0.08f)
                        }
                        heights.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight(h)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(DeepPurple)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controller workspace UI
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Audio controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Equalizers
                    TextButton(
                        onClick = {
                            activeEqualizer = when (activeEqualizer) {
                                "Flat" -> "Bass Booster"
                                "Bass Booster" -> "Pop Sound"
                                "Pop Sound" -> "Rock Concert"
                                "Rock Concert" -> "Jazz Hall"
                                else -> "Flat"
                            }
                            Toast.makeText(context, "Equalizer shifted to $activeEqualizer", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("EQ: $activeEqualizer", color = DeepPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.skipPreviousAudio() }) {
                            Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White)
                        }
                        IconButton(
                            onClick = { viewModel.toggleAudioPlayback() },
                            modifier = Modifier.background(DeepPurple, CircleShape).size(48.dp)
                        ) {
                            Icon(if (playState) Icons.Default.Pause else Icons.Default.PlayArrow, "Pause/Play", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.skipNextAudio() }) {
                            Icon(Icons.Default.SkipNext, "Next", tint = Color.White)
                        }
                    }

                    // Loop state indicator
                    IconButton(onClick = { Toast.makeText(context, "Offline looping activated", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Loop, "Loop", tint = Color.LightGray)
                    }
                }
            }
        }
    }
}
