package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KopSuratConfig
import com.example.data.model.LetterDraft
import com.example.data.model.LetterTemplateType
import com.example.ui.components.Base64Image
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FormalLetterSerifFamily

@Composable
fun LetterGeneratorScreen(
    kopSurat: KopSuratConfig,
    letterDraft: LetterDraft,
    onUpdateKopSurat: ((KopSuratConfig) -> KopSuratConfig) -> Unit,
    onUpdateKopLogoUri: (Uri) -> Unit,
    onUpdateLetterDraft: ((LetterDraft) -> LetterDraft) -> Unit,
    onSelectTemplate: (LetterTemplateType) -> Unit,
    onExportPdf: () -> Unit,
    onExportWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMobileTab by remember { mutableIntStateOf(0) } // 0 = Form, 1 = Preview

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onUpdateKopLogoUri(uri)
        }
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Persistent Export Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_letter_pdf_btn")
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF (A4)", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onExportWord,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldDeep),
                        border = BorderStroke(1.dp, AmberAccent),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_letter_word_btn")
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Word (.doc)", maxLines = 1)
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isSplitPane = maxWidth >= 840.dp

            if (isSplitPane) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LetterFormPane(
                        kopSurat = kopSurat,
                        letterDraft = letterDraft,
                        onUpdateKopSurat = onUpdateKopSurat,
                        onPickLogo = {
                            logoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onUpdateLetterDraft = onUpdateLetterDraft,
                        onSelectTemplate = onSelectTemplate,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        LetterA4PreviewCard(kopSurat = kopSurat, draft = letterDraft)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        SegmentedButton(
                            selected = selectedMobileTab == 0,
                            onClick = { selectedMobileTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        ) {
                            Text("Form & Kop Surat")
                        }
                        SegmentedButton(
                            selected = selectedMobileTab == 1,
                            onClick = { selectedMobileTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        ) {
                            Text("Pratinjau A4")
                        }
                    }

                    if (selectedMobileTab == 0) {
                        LetterFormPane(
                            kopSurat = kopSurat,
                            letterDraft = letterDraft,
                            onUpdateKopSurat = onUpdateKopSurat,
                            onPickLogo = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onUpdateLetterDraft = onUpdateLetterDraft,
                            onSelectTemplate = onSelectTemplate,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            LetterA4PreviewCard(kopSurat = kopSurat, draft = letterDraft)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterFormPane(
    kopSurat: KopSuratConfig,
    letterDraft: LetterDraft,
    onUpdateKopSurat: ((KopSuratConfig) -> KopSuratConfig) -> Unit,
    onPickLogo: () -> Unit,
    onUpdateLetterDraft: ((LetterDraft) -> LetterDraft) -> Unit,
    onSelectTemplate: (LetterTemplateType) -> Unit,
    modifier: Modifier = Modifier
) {
    var kopExpanded by remember { mutableStateOf(false) }
    var templateMenuExpanded by remember { mutableStateOf(false) }
    val selectedTemplate = LetterTemplateType.fromCode(letterDraft.templateType)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Template Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Template Jenis Surat Resmi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDeep
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTemplate.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { templateMenuExpanded = true }
                                .testTag("letter_template_dropdown")
                        )
                        DropdownMenu(
                            expanded = templateMenuExpanded,
                            onDismissRequest = { templateMenuExpanded = false }
                        ) {
                            LetterTemplateType.entries.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.label) },
                                    onClick = {
                                        onSelectTemplate(t)
                                        templateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Collapsible Kop Surat Settings (Auto-Saved)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { kopExpanded = !kopExpanded }
                            .testTag("toggle_kop_surat_btn"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Pengaturan Kop Surat & Pengurus",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tersimpan otomatis setiap perubahan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldDeep
                                )
                            }
                        }
                        Icon(
                            imageVector = if (kopExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = "Buka/Tutup Kop Surat"
                        )
                    }

                    AnimatedVisibility(visible = kopExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Base64Image(
                                    base64 = kopSurat.logoBase64,
                                    contentDescription = "Logo KKG PAI",
                                    contentScale = ContentScale.Fit,
                                    fallbackIcon = Icons.Rounded.Image,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                )
                                OutlinedButton(
                                    onClick = onPickLogo,
                                    modifier = Modifier.testTag("pick_kop_logo_btn")
                                ) {
                                    Text("Ganti Logo KKG / Kemenag")
                                }
                            }
                            OutlinedTextField(
                                value = kopSurat.kkgName,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(kkgName = v) } },
                                label = { Text("Nama Organisasi KKG") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.districtName,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(districtName = v) } },
                                label = { Text("Kecamatan / Kabupaten") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.address,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(address = v) } },
                                label = { Text("Alamat Sekretariat") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.contactInfo,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(contactInfo = v) } },
                                label = { Text("Email / No. HP") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.chairpersonName,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(chairpersonName = v) } },
                                label = { Text("Nama Ketua KKG PAI") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.chairpersonNip,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(chairpersonNip = v) } },
                                label = { Text("NIP Ketua") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.secretaryName,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(secretaryName = v) } },
                                label = { Text("Nama Sekretaris KKG PAI") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kopSurat.secretaryNip,
                                onValueChange = { v -> onUpdateKopSurat { it.copy(secretaryNip = v) } },
                                label = { Text("NIP Sekretaris") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Letter Content Form Card (Auto-Saved)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Isian Naskah Surat",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = letterDraft.letterNumber,
                            onValueChange = { v -> onUpdateLetterDraft { it.copy(letterNumber = v) } },
                            label = { Text("Nomor Surat") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("letter_number_input")
                        )
                        OutlinedTextField(
                            value = letterDraft.attachment,
                            onValueChange = { v -> onUpdateLetterDraft { it.copy(attachment = v) } },
                            label = { Text("Lampiran") },
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    OutlinedTextField(
                        value = letterDraft.subject,
                        onValueChange = { v -> onUpdateLetterDraft { it.copy(subject = v) } },
                        label = { Text("Perihal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("letter_subject_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = letterDraft.cityName,
                            onValueChange = { v -> onUpdateLetterDraft { it.copy(cityName = v) } },
                            label = { Text("Kota/Kecamatan") },
                            modifier = Modifier.weight(0.9f)
                        )
                        OutlinedTextField(
                            value = letterDraft.letterDate,
                            onValueChange = { v -> onUpdateLetterDraft { it.copy(letterDate = v) } },
                            label = { Text("Tanggal Surat") },
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    OutlinedTextField(
                        value = letterDraft.recipient,
                        onValueChange = { v -> onUpdateLetterDraft { it.copy(recipient = v) } },
                        label = { Text("Tujuan Surat (Kepada)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = letterDraft.openingText,
                        onValueChange = { v -> onUpdateLetterDraft { it.copy(openingText = v) } },
                        label = { Text("Isi Pembuka") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldContainer.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Detail Pelaksanaan Kegiatan",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldDeep
                            )
                            OutlinedTextField(
                                value = letterDraft.eventDayDate,
                                onValueChange = { v -> onUpdateLetterDraft { it.copy(eventDayDate = v) } },
                                label = { Text("Hari / Tanggal") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = letterDraft.eventTime,
                                onValueChange = { v -> onUpdateLetterDraft { it.copy(eventTime = v) } },
                                label = { Text("Waktu") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = letterDraft.eventLocation,
                                onValueChange = { v -> onUpdateLetterDraft { it.copy(eventLocation = v) } },
                                label = { Text("Tempat / Lokasi") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = letterDraft.closingText,
                        onValueChange = { v -> onUpdateLetterDraft { it.copy(closingText = v) } },
                        label = { Text("Isi Penutup") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun LetterA4PreviewCard(
    kopSurat: KopSuratConfig,
    draft: LetterDraft,
    modifier: Modifier = Modifier
) {
    // Crisp white paper A4 aesthetic with formal serif typography
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("letter_a4_preview"),
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            // Kop Surat Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (kopSurat.logoBase64.isNotBlank()) {
                    Base64Image(
                        base64 = kopSurat.logoBase64,
                        contentDescription = "Logo Kop Surat",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = kopSurat.kkgName,
                        fontFamily = FormalLetterSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = kopSurat.districtName,
                        fontFamily = FormalLetterSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = kopSurat.address,
                        fontFamily = FormalLetterSerifFamily,
                        fontSize = 8.sp,
                        lineHeight = 11.sp,
                        color = Color(0xFF334155),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = kopSurat.contactInfo,
                        fontFamily = FormalLetterSerifFamily,
                        fontSize = 8.sp,
                        lineHeight = 11.sp,
                        color = Color(0xFF334155),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(thickness = 2.dp, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(thickness = 0.7.dp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            // Date & Metadata
            Text(
                text = "${draft.cityName}, ${draft.letterDate}",
                fontFamily = FormalLetterSerifFamily,
                fontSize = 9.5.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(4.dp))

            LetterMetaLine(label = "Nomor", value = draft.letterNumber, boldValue = false)
            LetterMetaLine(label = "Lampiran", value = draft.attachment, boldValue = false)
            LetterMetaLine(label = "Perihal", value = draft.subject, boldValue = true)

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Kepada,",
                fontFamily = FormalLetterSerifFamily,
                fontSize = 10.sp,
                color = Color.Black
            )
            Text(
                text = draft.recipient,
                fontFamily = FormalLetterSerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = draft.openingText,
                fontFamily = FormalLetterSerifFamily,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                LetterMetaLine(label = "Hari / Tanggal", value = draft.eventDayDate, boldValue = true, labelWidth = 76)
                LetterMetaLine(label = "Waktu", value = draft.eventTime, boldValue = true, labelWidth = 76)
                LetterMetaLine(label = "Tempat", value = draft.eventLocation, boldValue = true, labelWidth = 76)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = draft.closingText,
                fontFamily = FormalLetterSerifFamily,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(22.dp))
            // Signatures
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mengetahui,\nKetua KKG PAI",
                        fontFamily = FormalLetterSerifFamily,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(34.dp))
                    Text(
                        text = kopSurat.chairpersonName,
                        fontFamily = FormalLetterSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        textDecoration = TextDecoration.Underline,
                        color = Color.Black
                    )
                    Text(
                        text = "NIP. ${kopSurat.chairpersonNip}",
                        fontFamily = FormalLetterSerifFamily,
                        fontSize = 8.5.sp,
                        color = Color(0xFF334155)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "\nSekretaris KKG PAI",
                        fontFamily = FormalLetterSerifFamily,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(34.dp))
                    Text(
                        text = kopSurat.secretaryName,
                        fontFamily = FormalLetterSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        textDecoration = TextDecoration.Underline,
                        color = Color.Black
                    )
                    Text(
                        text = "NIP. ${kopSurat.secretaryNip}",
                        fontFamily = FormalLetterSerifFamily,
                        fontSize = 8.5.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterMetaLine(
    label: String,
    value: String,
    boldValue: Boolean,
    labelWidth: Int = 56
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = FormalLetterSerifFamily,
            fontSize = 9.5.sp,
            color = Color.Black,
            modifier = Modifier.width(labelWidth.dp)
        )
        Text(
            text = ": ",
            fontFamily = FormalLetterSerifFamily,
            fontSize = 9.5.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontFamily = FormalLetterSerifFamily,
            fontWeight = if (boldValue) FontWeight.Bold else FontWeight.Normal,
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}
