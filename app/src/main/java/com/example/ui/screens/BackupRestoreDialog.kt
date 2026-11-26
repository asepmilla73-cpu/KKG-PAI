package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed

@Composable
fun BackupRestoreDialog(
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onResetData: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cadangan & Pemulihan Data",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seluruh transaksi, agenda, galeri, kop surat, dan draf surat tersimpan otomatis secara lokal. Unduh cadangan (.json) secara berkala untuk keamanan ekstra.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Button(
                    onClick = {
                        onExportJson()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_backup_json_button")
                ) {
                    Icon(Icons.Rounded.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Backup (.json)")
                }

                if (isAdmin) {
                    OutlinedButton(
                        onClick = {
                            onImportJson()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_backup_json_button")
                    ) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import / Restore (.json)")
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ExpenseContainer.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Zona Berbahaya (Admin)",
                                style = MaterialTheme.typography.labelLarge,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Menghapus seluruh transaksi kas, agenda, galeri foto, anggota terdaftar, dan mengatur ulang draf surat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { confirmReset = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                border = BorderStroke(1.dp, ExpenseRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reset_data_button")
                            ) {
                                Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset Semua Data")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = ExpenseRed
                )
            },
            title = { Text("Konfirmasi Reset Data") },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus seluruh data SIM-KKG PAI? Tindakan ini tidak dapat dibatalkan kecuali Anda memiliki file cadangan (.json)."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmReset = false
                        onResetData()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_reset_data_button")
                ) {
                    Text("Ya, Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
