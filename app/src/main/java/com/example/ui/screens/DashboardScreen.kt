package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AgendaEntity
import com.example.data.model.KopSuratConfig
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.DashboardSummary
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    kopSurat: KopSuratConfig,
    isAdmin: Boolean,
    pendingMemberCount: Int,
    onNavigateToFinance: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToLetters: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToMembers: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pending member verification banner for Admin
        if (isAdmin && pendingMemberCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable(onClick = onNavigateToMembers)
                        .testTag("dashboard_pending_members_banner"),
                    colors = CardDefaults.cardColors(containerColor = AmberContainer),
                    border = BorderStroke(1.dp, AmberAccent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HowToReg,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$pendingMemberCount Anggota Menunggu Verifikasi",
                                style = MaterialTheme.typography.titleSmall,
                                color = AmberDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ketuk untuk menyetujui atau menolak pendaftar baru.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberDark
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Buka Verifikasi Anggota",
                            tint = AmberDark
                        )
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            DashboardHeroBanner(
                kopSurat = kopSurat,
                isAdmin = isAdmin,
                onQuickFinance = onNavigateToFinance,
                onQuickLetter = onNavigateToLetters
            )
        }

        // 4 Summary Cards (Adaptive 2x2 or 4-column)
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 720.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryStatCard(
                            title = "Total Saldo Kas",
                            value = FormatUtils.formatRupiah(summary.totalBalance),
                            subtitle = "Saldo bersih terkini",
                            icon = Icons.Rounded.AccountBalanceWallet,
                            containerColor = EmeraldContainer.copy(alpha = 0.65f),
                            iconBg = EmeraldPrimary,
                            iconTint = Color.White,
                            valueColor = EmeraldDeep,
                            onClick = onNavigateToFinance,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Pemasukan Bulan Ini",
                            value = FormatUtils.formatRupiah(summary.monthlyIncome),
                            subtitle = FormatUtils.formatMonthYearLabel(FormatUtils.currentYearMonth()),
                            icon = Icons.Rounded.ArrowDownward,
                            containerColor = MaterialTheme.colorScheme.surface,
                            iconBg = IncomeContainer,
                            iconTint = IncomeGreen,
                            valueColor = IncomeGreen,
                            onClick = onNavigateToFinance,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Pengeluaran Bulan Ini",
                            value = FormatUtils.formatRupiah(summary.monthlyExpense),
                            subtitle = FormatUtils.formatMonthYearLabel(FormatUtils.currentYearMonth()),
                            icon = Icons.Rounded.ArrowUpward,
                            containerColor = MaterialTheme.colorScheme.surface,
                            iconBg = ExpenseContainer,
                            iconTint = ExpenseRed,
                            valueColor = ExpenseRed,
                            onClick = onNavigateToFinance,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Agenda Terdekat",
                            value = "${summary.upcomingAgendaCount} Kegiatan",
                            subtitle = "Terjadwal aktif",
                            icon = Icons.Rounded.EventAvailable,
                            containerColor = MaterialTheme.colorScheme.surface,
                            iconBg = AmberContainer,
                            iconTint = AmberDark,
                            valueColor = AmberDark,
                            onClick = onNavigateToAgenda,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryStatCard(
                                title = "Total Saldo Kas",
                                value = FormatUtils.formatRupiah(summary.totalBalance),
                                subtitle = "Saldo bersih terkini",
                                icon = Icons.Rounded.AccountBalanceWallet,
                                containerColor = EmeraldContainer.copy(alpha = 0.65f),
                                iconBg = EmeraldPrimary,
                                iconTint = Color.White,
                                valueColor = EmeraldDeep,
                                onClick = onNavigateToFinance,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                title = "Agenda Terdekat",
                                value = "${summary.upcomingAgendaCount} Kegiatan",
                                subtitle = "Terjadwal aktif",
                                icon = Icons.Rounded.EventAvailable,
                                containerColor = MaterialTheme.colorScheme.surface,
                                iconBg = AmberContainer,
                                iconTint = AmberDark,
                                valueColor = AmberDark,
                                onClick = onNavigateToAgenda,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryStatCard(
                                title = "Pemasukan Bulan Ini",
                                value = FormatUtils.formatRupiah(summary.monthlyIncome),
                                subtitle = FormatUtils.formatMonthYearLabel(FormatUtils.currentYearMonth()),
                                icon = Icons.Rounded.ArrowDownward,
                                containerColor = MaterialTheme.colorScheme.surface,
                                iconBg = IncomeContainer,
                                iconTint = IncomeGreen,
                                valueColor = IncomeGreen,
                                onClick = onNavigateToFinance,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                title = "Pengeluaran Bulan Ini",
                                value = FormatUtils.formatRupiah(summary.monthlyExpense),
                                subtitle = FormatUtils.formatMonthYearLabel(FormatUtils.currentYearMonth()),
                                icon = Icons.Rounded.ArrowUpward,
                                containerColor = MaterialTheme.colorScheme.surface,
                                iconBg = ExpenseContainer,
                                iconTint = ExpenseRed,
                                valueColor = ExpenseRed,
                                onClick = onNavigateToFinance,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section: 3 Agenda Terdekat
        item {
            SectionHeader(
                title = "3 Agenda Kegiatan Terdekat",
                subtitle = "Jadwal pertemuan & kegiatan KKG PAI mendatang",
                actionLabel = "Lihat Semua",
                onAction = onNavigateToAgenda
            )
        }

        if (summary.upcomingAgendas.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.CalendarMonth,
                    title = "Belum ada agenda mendatang",
                    message = "Tambahkan jadwal rapat rutin, workshop, atau Pentas PAI agar anggota terinformasi.",
                    actionLabel = "Tambah Agenda",
                    onAction = onNavigateToAgenda
                )
            }
        } else {
            items(summary.upcomingAgendas, key = { "dash_agenda_${it.id}" }) { agenda ->
                DashboardAgendaPreviewCard(
                    agenda = agenda,
                    onClick = onNavigateToAgenda
                )
            }
        }

        // Section: Ringkasan Transaksi Terakhir
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Transaksi Kas Terakhir",
                subtitle = "Catatan pemasukan & pengeluaran terbaru",
                actionLabel = "Kelola Kas",
                onAction = onNavigateToFinance
            )
        }

        if (summary.recentTransactions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Payments,
                    title = "Belum ada catatan transaksi",
                    message = "Catat iuran anggota, bantuan dana, atau pengeluaran operasional di menu Kas KKG.",
                    actionLabel = "Catat Transaksi",
                    onAction = onNavigateToFinance
                )
            }
        } else {
            items(summary.recentTransactions, key = { "dash_tx_${it.id}" }) { tx ->
                DashboardTransactionRow(
                    tx = tx,
                    onClick = onNavigateToFinance
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DashboardHeroBanner(
    kopSurat: KopSuratConfig,
    isAdmin: Boolean,
    onQuickFinance: () -> Unit,
    onQuickLetter: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_kkg_banner),
                contentDescription = "Banner KKG PAI",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                EmeraldDeep.copy(alpha = 0.92f),
                                EmeraldDark.copy(alpha = 0.80f),
                                EmeraldPrimary.copy(alpha = 0.65f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AmberAccent.copy(alpha = 0.95f)
                ) {
                    Text(
                        text = "SISTEM INFORMASI MANAJEMEN KKG PAI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = kopSurat.kkgName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = kopSurat.districtName,
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onQuickFinance,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = EmeraldDeep
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("dashboard_quick_finance_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAdmin) "Catat Kas" else "Lihat Kas", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onQuickLetter,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("dashboard_quick_letter_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buat Surat", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconBg: Color,
    iconTint: Color,
    valueColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardAgendaPreviewCard(
    agenda: AgendaEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmberContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = AmberDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = agenda.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = FormatUtils.formatDateTimeIndonesian(agenda.dateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = agenda.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = agenda.speakerOrPic,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTransactionRow(
    tx: TransactionEntity,
    onClick: () -> Unit
) {
    val isIncome = tx.type == TransactionType.INCOME.code
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) IncomeContainer else ExpenseContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = if (isIncome) "Pemasukan" else "Pengeluaran",
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tx.category} • ${FormatUtils.formatIsoDateShort(tx.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = FormatUtils.formatRupiahSigned(tx.amount, isIncome),
                style = MaterialTheme.typography.titleSmall,
                color = if (isIncome) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
