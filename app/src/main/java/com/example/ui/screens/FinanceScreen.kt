package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionCategories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeContainer
import com.example.ui.theme.IncomeGreen
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    transactions: List<TransactionEntity>,
    availableMonths: List<String>,
    selectedMonth: String,
    selectedType: String,
    searchQuery: String,
    isAdmin: Boolean,
    onMonthChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onUpsertTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<TransactionEntity?>(null) }
    var showMonthMenu by remember { mutableStateOf(false) }

    val totalIn = transactions.filter { it.type == TransactionType.INCOME.code }.sumOf { it.amount }
    val totalOut = transactions.filter { it.type == TransactionType.EXPENSE.code }.sumOf { it.amount }
    val netBalance = totalIn - totalOut

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingTransaction = null
                        showFormDialog = true
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = "Tambah Transaksi") },
                    text = { Text("Catat Transaksi") },
                    modifier = Modifier.testTag("add_transaction_fab")
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
            // Balance Summary Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Saldo Kas (${FormatUtils.formatMonthYearLabel(selectedMonth)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatRupiah(netBalance),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Pemasukan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldContainer
                                    )
                                    Text(
                                        text = FormatUtils.formatRupiah(totalIn),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Pengeluaran",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExpenseContainer
                                    )
                                    Text(
                                        text = FormatUtils.formatRupiah(totalOut),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Export Row: Excel (.xlsx) & PDF
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportExcel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldDeep),
                        border = BorderStroke(1.dp, EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_finance_excel_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Excel (.xlsx)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_finance_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Laporan PDF", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Search & Month / Type Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Cari keterangan atau kategori transaksi...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("finance_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box {
                            FilterChip(
                                selected = selectedMonth != "ALL",
                                onClick = { showMonthMenu = true },
                                label = { Text(FormatUtils.formatMonthYearLabel(selectedMonth)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.FilterList,
                                        contentDescription = "Filter Bulan",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            DropdownMenu(
                                expanded = showMonthMenu,
                                onDismissRequest = { showMonthMenu = false }
                            ) {
                                availableMonths.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(FormatUtils.formatMonthYearLabel(m)) },
                                        onClick = {
                                            onMonthChange(m)
                                            showMonthMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedType == "ALL",
                                    onClick = { onTypeChange("ALL") },
                                    label = { Text("Semua") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedType == TransactionType.INCOME.code,
                                    onClick = { onTypeChange(TransactionType.INCOME.code) },
                                    label = { Text("Pemasukan") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IncomeContainer,
                                        selectedLabelColor = EmeraldDeep
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedType == TransactionType.EXPENSE.code,
                                    onClick = { onTypeChange(TransactionType.EXPENSE.code) },
                                    label = { Text("Pengeluaran") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExpenseContainer,
                                        selectedLabelColor = ExpenseRed
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Payments,
                        title = "Tidak ada transaksi ditemukan",
                        message = if (isAdmin) {
                            "Ubah filter pencarian atau tekan tombol 'Catat Transaksi' untuk menambahkan data kas baru."
                        } else {
                            "Belum ada transaksi kas yang cocok dengan filter pencarian."
                        },
                        actionLabel = if (isAdmin) "Catat Transaksi" else null,
                        onAction = if (isAdmin) {
                            {
                                editingTransaction = null
                                showFormDialog = true
                            }
                        } else null
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionHistoryCard(
                        tx = tx,
                        isAdmin = isAdmin,
                        onEdit = {
                            editingTransaction = tx
                            showFormDialog = true
                        },
                        onDelete = { deleteCandidate = tx }
                    )
                }
            }
        }
    }

    if (showFormDialog) {
        TransactionFormDialog(
            initial = editingTransaction,
            onDismiss = { showFormDialog = false },
            onSave = { entity ->
                onUpsertTransaction(entity)
                showFormDialog = false
            }
        )
    }

    deleteCandidate?.let { tx ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Transaksi?") },
            text = {
                Text("Hapus catatan '${tx.description}' senilai ${FormatUtils.formatRupiah(tx.amount)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx.id)
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
private fun TransactionHistoryCard(
    tx: TransactionEntity,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = tx.type == TransactionType.INCOME.code
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${tx.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) IncomeContainer else ExpenseContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = FormatUtils.formatIsoDateShort(tx.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FormatUtils.formatRupiahSigned(tx.amount, isIncome),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isIncome) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isAdmin) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Transaksi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus Transaksi",
                            tint = ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionFormDialog(
    initial: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var date by remember { mutableStateOf(initial?.date ?: FormatUtils.todayIsoDate()) }
    var type by remember { mutableStateOf(initial?.type ?: TransactionType.INCOME.code) }
    var category by remember { mutableStateOf(initial?.category ?: TransactionCategories.all.first()) }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var amountDigits by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedAmount = amountDigits.toLongOrNull() ?: 0L
    val isValid = description.isNotBlank() && parsedAmount > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Catat Transaksi Kas" else "Edit Transaksi Kas",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.INCOME.code,
                        onClick = { type = TransactionType.INCOME.code },
                        label = { Text("Pemasukan") },
                        leadingIcon = {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeContainer,
                            selectedLabelColor = EmeraldDeep
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TransactionType.EXPENSE.code,
                        onClick = { type = TransactionType.EXPENSE.code },
                        label = { Text("Pengeluaran") },
                        leadingIcon = {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseContainer,
                            selectedLabelColor = ExpenseRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Pilih Tanggal")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true },
                        enabled = false
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showCategoryDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        TransactionCategories.all.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Transaksi") },
                    placeholder = { Text("Misal: Iuran Kas Anggota Bulan November") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_description_input")
                )

                OutlinedTextField(
                    value = if (amountDigits.isBlank()) "" else FormatUtils.formatThousandsInput(amountDigits),
                    onValueChange = { input ->
                        amountDigits = input.filter { it.isDigit() }
                    },
                    label = { Text("Nominal (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        TransactionEntity(
                            id = initial?.id ?: 0L,
                            date = date.trim().ifBlank { FormatUtils.todayIsoDate() },
                            type = type,
                            category = category,
                            description = description.trim(),
                            amount = parsedAmount,
                            createdAt = initial?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = isValid,
                modifier = Modifier.testTag("save_transaction_btn")
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
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            date = sdf.format(Date(millis))
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
            DatePicker(state = datePickerState)
        }
    }
}
