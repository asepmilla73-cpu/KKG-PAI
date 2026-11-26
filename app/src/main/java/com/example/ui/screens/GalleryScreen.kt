package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GalleryItemEntity
import com.example.ui.components.Base64Image
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryScreen(
    galleryItems: List<GalleryItemEntity>,
    isAdmin: Boolean,
    onAddPhotoFromUri: (uri: Uri, title: String, date: String, description: String, onDone: () -> Unit) -> Unit,
    onDeleteGalleryItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var lightboxItem by remember { mutableStateOf<GalleryItemEntity?>(null) }
    var deleteCandidate by remember { mutableStateOf<GalleryItemEntity?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                pendingImageUri = uri
            }
        }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Unggah Foto") },
                text = { Text("Unggah Dokumentasi") },
                modifier = Modifier.testTag("add_gallery_photo_fab")
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = EmeraldContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Compress,
                            contentDescription = null,
                            tint = EmeraldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Foto otomatis dikompresi (maks. lebar 800px, JPEG 70%) agar penyimpanan tetap ringan & cepat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldDeep
                        )
                    }
                }
            }

            if (galleryItems.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard(
                        icon = Icons.Rounded.PhotoLibrary,
                        title = "Galeri Dokumentasi Kosong",
                        message = "Unggah foto kegiatan rapat, workshop, atau Pentas PAI untuk mengabadikan momen KKG PAI.",
                        actionLabel = "Pilih Foto",
                        onAction = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            } else {
                items(galleryItems, key = { it.id }) { item ->
                    GalleryPhotoCard(
                        item = item,
                        onClick = { lightboxItem = item }
                    )
                }
            }
        }
    }

    pendingImageUri?.let { uri ->
        AddGalleryMetadataDialog(
            onDismiss = { pendingImageUri = null },
            onConfirm = { title, date, description ->
                onAddPhotoFromUri(uri, title, date, description) {
                    pendingImageUri = null
                }
            }
        )
    }

    lightboxItem?.let { item ->
        GalleryLightboxDialog(
            item = item,
            canDelete = isAdmin,
            onDismiss = { lightboxItem = null },
            onDeleteRequest = {
                lightboxItem = null
                deleteCandidate = item
            }
        )
    }

    deleteCandidate?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Dokumentasi?") },
            text = { Text("Hapus foto '${item.title}' dari galeri KKG PAI?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGalleryItem(item.id)
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun GalleryPhotoCard(
    item: GalleryItemEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("gallery_card_${item.id}"),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
        ) {
            Base64Image(
                base64 = item.imageBase64,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                fallbackIcon = Icons.Rounded.PhotoLibrary,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                            startY = 80f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = FormatUtils.formatIsoDateShort(item.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGalleryMetadataDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, date: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(FormatUtils.todayIsoDate()) }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Dokumentasi Foto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Kegiatan") },
                    placeholder = { Text("Misal: Workshop Kurikulum Merdeka PAI") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gallery_title_input")
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal Kegiatan") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Pilih Tanggal")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Singkat") },
                    placeholder = { Text("Keterangan peserta atau hasil kegiatan...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim(), date.trim(), description.trim()) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_gallery_btn")
            ) {
                Text("Kompres & Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun GalleryLightboxDialog(
    item: GalleryItemEntity,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = FormatUtils.formatIsoDateShort(item.date),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        if (canDelete) {
                            IconButton(onClick = onDeleteRequest) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Hapus Foto",
                                    tint = ExpenseRed
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Tutup Lightbox"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Base64Image(
                    base64 = item.imageBase64,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.06f))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
