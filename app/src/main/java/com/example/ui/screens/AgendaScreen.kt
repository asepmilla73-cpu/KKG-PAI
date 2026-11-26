package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AgendaEntity
import com.example.data.model.AgendaStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgendaScreen(
    agendas: List<AgendaEntity>,
    isAdmin: Boolean,
    onUpsertAgenda: (AgendaEntity) -> Unit,
    onToggleStatus: (AgendaEntity) -> Unit,
    onDeleteAgenda: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf("ALL") }
    var editingAgenda by remember { mutableStateOf<AgendaEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<AgendaEntity?>(null) }

    val displayed = remember(agendas, filterStatus) {
        when (filterStatus) {
            AgendaStatus.UPCOMING.code -> agendas.filter { it.status == AgendaStatus.UPCOMING.code }
            AgendaStatus.COMPLETED.code -> agendas.filter { it.status == AgendaStatus.COMPLETED.code }
            else -> agendas
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingAgenda = null
                        showFormDialog = true
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = "Tambah Agenda") },
                    text = { Text("Tambah Agenda") },
                    modifier = Modifier.testTag("add_agenda_fab")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterStatus == "ALL",
                            onClick = { filterStatus = "ALL" },
                            label = { Text("Semua (${agendas.size})") }
                        )
                    }
                    item {
                        val upcomingCount = agendas.count { it.status == AgendaStatus.UPCOMING.code }
                        FilterChip(
                            selected = filterStatus == AgendaStatus.UPCOMING.code,
                            onClick = { filterStatus = AgendaStatus.UPCOMING.code },
                            label = { Text("Akan Datang ($upcomingCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberContainer,
                                selectedLabelColor = AmberDark
                            )
                        )
                    }
                    item {
                        val doneCount = agendas.count { it.status == AgendaStatus.COMPLETED.code }
                        FilterChip(
                            selected = filterStatus == AgendaStatus.COMPLETED.code,
                            onClick = { filterStatus = AgendaStatus.COMPLETED.code },
                            label = { Text("Selesai ($doneCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldDeep
                            )
                        )
                    }
                }
            }

            if (displayed.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "Belum ada agenda pada kategori ini",
                        message = if (isAdmin) {
                            "Tambahkan rapat rutin, workshop kurikulum, atau kegiatan Pentas PAI dengan menekan tombol Tambah Agenda."
                        } else {
                            "Belum ada jadwal kegiatan pada kategori ini."
                        },
                        actionLabel = if (isAdmin) "Tambah Agenda" else null,
                        onAction = if (isAdmin) {
                            {
                                editingAgenda = null
                                showFormDialog = true
                            }
                        } else null
                    )
                }
            } else {
                items(displayed, key = { it.id }) { agenda ->
                    AgendaItemCard(
                        agenda = agenda,
                        isAdmin = isAdmin,
                        onToggleStatus = { onToggleStatus(agenda) },
                        onEdit = {
                            editingAgenda = agenda
                            showFormDialog = true
                        },
                        onDelete = { deleteCandidate = agenda }
                    )
                }
            }
        }
    }

    if (showFormDialog) {
        AgendaFormDialog(
            initial = editingAgenda,
            onDismiss = { showFormDialog = false },
            onSave = { entity ->
                onUpsertAgenda(entity)
                showFormDialog = false
            }
        )
    }

    deleteCandidate?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Agenda?") },
            text = { Text("Hapus agenda '${item.title}' dari daftar kegiatan?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAgenda(item.id)
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
private fun AgendaItemCard(
    agenda: AgendaEntity,
    isAdmin: Boolean,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isUpcoming = agenda.status == AgendaStatus.UPCOMING.code
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agenda_card_${agenda.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUpcoming) AmberDark.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUpcoming) AmberContainer else EmeraldContainer
                ) {
                    Text(
                        text = if (isUpcoming) "Akan Datang" else "Selesai",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUpcoming) AmberDark else EmeraldDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (isAdmin) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Agenda",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Hapus Agenda",
                                tint = ExpenseRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = agenda.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = FormatUtils.formatDateTimeIndonesian(agenda.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = agenda.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pemateri / PJ: ${agenda.speakerOrPic}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (agenda.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = agenda.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_agenda_status_${agenda.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isUpcoming) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isUpcoming) Icons.Rounded.CheckCircle else Icons.Rounded.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isUpcoming) "Tandai Selesai" else "Kembalikan ke Akan Datang")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgendaFormDialog(
    initial: AgendaEntity?,
    onDismiss: () -> Unit,
    onSave: (AgendaEntity) -> Unit
) {
    val initialParts = initial?.dateTime?.split(" ")
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var datePart by remember { mutableStateOf(initialParts?.getOrNull(0) ?: FormatUtils.dateOffsetFromToday(3)) }
    var timePart by remember { mutableStateOf(initialParts?.getOrNull(1) ?: "09:00") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var speaker by remember { mutableStateOf(initial?.speakerOrPic ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: AgendaStatus.UPCOMING.code) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val isValid = title.isNotBlank() && location.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Tambah Agenda Kegiatan" else "Edit Agenda Kegiatan",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Kegiatan") },
                    placeholder = { Text("Misal: Rapat Rutin / Workshop Kurikulum") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agenda_title_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = datePart,
                        onValueChange = { datePart = it },
                        label = { Text("Tanggal") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Rounded.CalendarToday, contentDescription = "Pilih Tanggal")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = timePart,
                        onValueChange = { timePart = it },
                        label = { Text("Waktu") },
                        placeholder = { Text("09:00") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Tempat / Lokasi") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agenda_location_input")
                )

                OutlinedTextField(
                    value = speaker,
                    onValueChange = { speaker = it },
                    label = { Text("Pemateri / Penanggung Jawab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == AgendaStatus.UPCOMING.code,
                        onClick = { status = AgendaStatus.UPCOMING.code },
                        label = { Text("Akan Datang") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = status == AgendaStatus.COMPLETED.code,
                        onClick = { status = AgendaStatus.COMPLETED.code },
                        label = { Text("Selesai") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Singkat (Opsional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AgendaEntity(
                            id = initial?.id ?: 0L,
                            title = title.trim(),
                            dateTime = "${datePart.trim()} ${timePart.trim().ifBlank { "09:00" }}",
                            location = location.trim(),
                            speakerOrPic = speaker.trim().ifBlank { "Pengurus KKG PAI" },
                            status = status,
                            notes = notes.trim(),
                            createdAt = initial?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = isValid,
                modifier = Modifier.testTag("save_agenda_btn")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            datePart = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
