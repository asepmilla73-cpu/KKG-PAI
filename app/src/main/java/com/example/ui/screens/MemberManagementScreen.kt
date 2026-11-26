package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountStatus
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed

@Composable
fun MemberManagementScreen(
    members: List<UserEntity>,
    onVerifyMember: (String) -> Unit,
    onRejectMember: (String) -> Unit,
    onDeleteMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf("ALL") }
    var deleteCandidate by remember { mutableStateOf<UserEntity?>(null) }

    val pendingCount = members.count { it.status == AccountStatus.PENDING.code }
    val verifiedCount = members.count { it.status == AccountStatus.VERIFIED.code }
    val rejectedCount = members.count { it.status == AccountStatus.REJECTED.code }

    val displayed = remember(members, filterStatus) {
        if (filterStatus == "ALL") members
        else members.filter { it.status == filterStatus }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verifikasi & Daftar Anggota KKG PAI",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verifikasi guru PAI Kecamatan Gedebage yang mendaftar agar dapat masuk ke aplikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatMiniPill("Menunggu", pendingCount, AmberContainer, AmberDark, Modifier.weight(1f))
                        StatMiniPill("Aktif", verifiedCount, EmeraldContainer, EmeraldDeep, Modifier.weight(1f))
                        StatMiniPill("Ditolak", rejectedCount, ExpenseContainer, ExpenseRed, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filterStatus == "ALL",
                        onClick = { filterStatus = "ALL" },
                        label = { Text("Semua (${members.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == AccountStatus.PENDING.code,
                        onClick = { filterStatus = AccountStatus.PENDING.code },
                        label = { Text("Menunggu ($pendingCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberContainer,
                            selectedLabelColor = AmberDark
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == AccountStatus.VERIFIED.code,
                        onClick = { filterStatus = AccountStatus.VERIFIED.code },
                        label = { Text("Terverifikasi ($verifiedCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldContainer,
                            selectedLabelColor = EmeraldDeep
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == AccountStatus.REJECTED.code,
                        onClick = { filterStatus = AccountStatus.REJECTED.code },
                        label = { Text("Ditolak ($rejectedCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseContainer,
                            selectedLabelColor = ExpenseRed
                        )
                    )
                }
            }
        }

        if (displayed.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Group,
                    title = "Belum ada data pendaftar anggota",
                    message = "Anggota yang mendaftar melalui halaman Daftar Anggota akan muncul di sini untuk diverifikasi Admin."
                )
            }
        } else {
            items(displayed, key = { it.username }) { member ->
                MemberAccountCard(
                    member = member,
                    onVerify = { onVerifyMember(member.username) },
                    onReject = { onRejectMember(member.username) },
                    onDelete = { deleteCandidate = member }
                )
            }
        }
    }

    deleteCandidate?.let { user ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Akun Anggota?") },
            text = { Text("Hapus akun '${user.fullName}' (@${user.username}) secara permanen?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMember(user.username)
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
private fun StatMiniPill(
    label: String,
    count: Int,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = fg,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@Composable
private fun MemberAccountCard(
    member: UserEntity,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    val status = AccountStatus.fromCode(member.status)
    val badgeBg = when (status) {
        AccountStatus.PENDING -> AmberContainer
        AccountStatus.VERIFIED -> EmeraldContainer
        AccountStatus.REJECTED -> ExpenseContainer
    }
    val badgeFg = when (status) {
        AccountStatus.PENDING -> AmberDark
        AccountStatus.VERIFIED -> EmeraldDeep
        AccountStatus.REJECTED -> ExpenseRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("member_card_${member.username}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (status == AccountStatus.PENDING) AmberDark.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (status) {
                                AccountStatus.PENDING -> Icons.Rounded.HourglassTop
                                AccountStatus.VERIFIED -> Icons.Rounded.Person
                                AccountStatus.REJECTED -> Icons.Rounded.Block
                            },
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${member.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeFg,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus Anggota",
                            tint = ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = member.schoolName.ifBlank { "Guru PAI Kec. Gedebage" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (status != AccountStatus.VERIFIED) {
                    Button(
                        onClick = onVerify,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("verify_member_${member.username}")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifikasi")
                    }
                }
                if (status != AccountStatus.REJECTED) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        border = BorderStroke(1.dp, ExpenseRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_member_${member.username}")
                    ) {
                        Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (status == AccountStatus.VERIFIED) "Nonaktifkan" else "Tolak")
                    }
                }
            }
        }
    }
}
