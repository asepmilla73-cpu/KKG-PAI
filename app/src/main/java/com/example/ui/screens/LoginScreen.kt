package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed

@Composable
fun LoginScreen(
    authError: String?,
    authSuccess: String?,
    onLogin: (username: String, password: String) -> Unit,
    onRegisterMember: (
        username: String,
        fullName: String,
        schoolName: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Masuk, 1 = Daftar Anggota

    // Login state
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPass by remember { mutableStateOf(false) }

    // Register state
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regSchoolName by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var showRegPass by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EmeraldDeep,
                        EmeraldDark,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .safeDrawingPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo & Brand Identity
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.size(104.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_kkg_logo_default),
                    contentDescription = "Logo KKG PAI Kecamatan Gedebage",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "SIM-KKG PAI",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                shape = CircleShape,
                color = AmberAccent
            ) {
                Text(
                    text = "KECAMATAN GEDEBAGE • KOTA BANDUNG",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                onClearMessages()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { Icon(Icons.Rounded.Login, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("auth_tab_login")
                        ) {
                            Text("Masuk")
                        }
                        SegmentedButton(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                onClearMessages()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { Icon(Icons.Rounded.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("auth_tab_register")
                        ) {
                            Text("Daftar Anggota")
                        }
                    }

                    AnimatedVisibility(visible = !authError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExpenseContainer,
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = !authSuccess.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldContainer,
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authSuccess.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (selectedTab == 0) {
                        // Login Form
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = AmberDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Masuk sebagai Admin atau Anggota yang sudah diverifikasi Admin.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberDark
                                )
                            }
                        }

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                if (authError != null) onClearMessages()
                            },
                            label = { Text("Username") },
                            placeholder = { Text("Masukkan username") },
                            leadingIcon = { Icon(Icons.Rounded.AccountCircle, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input")
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                if (authError != null) onClearMessages()
                            },
                            label = { Text("Kata Sandi") },
                            placeholder = { Text("Masukkan kata sandi") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPass = !showLoginPass }) {
                                    Icon(
                                        imageVector = if (showLoginPass) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Tampilkan/Sembunyikan Kata Sandi"
                                    )
                                }
                            },
                            visualTransformation = if (showLoginPass) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Button(
                            onClick = { onLogin(loginUsername, loginPassword) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button")
                        ) {
                            Icon(Icons.Rounded.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masuk ke Aplikasi", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        // Member Registration Form
                        Text(
                            text = "Pendaftaran Anggota Baru",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDeep
                        )
                        Text(
                            text = "Setelah mendaftar, akun Anda akan diverifikasi terlebih dahulu oleh Admin KKG PAI Gedebage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Nama Lengkap & Gelar") },
                            placeholder = { Text("Misal: Ahmad Hidayat, S.Pd.I.") },
                            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_fullname_input")
                        )

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it.replace(" ", "") },
                            label = { Text("Username") },
                            placeholder = { Text("Misal: ahmad_pai (tanpa spasi)") },
                            leadingIcon = { Icon(Icons.Rounded.AccountCircle, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_username_input")
                        )

                        OutlinedTextField(
                            value = regSchoolName,
                            onValueChange = { regSchoolName = it },
                            label = { Text("Asal Sekolah / SD / MI") },
                            placeholder = { Text("Misal: SDN Rancabolang 01") },
                            leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_school_input")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Buat Kata Sandi") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showRegPass = !showRegPass }) {
                                    Icon(
                                        imageVector = if (showRegPass) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Tampilkan/Sembunyikan Kata Sandi"
                                    )
                                }
                            },
                            visualTransformation = if (showRegPass) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input")
                        )

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Konfirmasi Kata Sandi") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            visualTransformation = if (showRegPass) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_confirm_password_input")
                        )

                        Button(
                            onClick = {
                                onRegisterMember(
                                    regUsername,
                                    regFullName,
                                    regSchoolName,
                                    regPassword,
                                    regConfirmPassword
                                ) {
                                    regFullName = ""
                                    regUsername = ""
                                    regSchoolName = ""
                                    regPassword = ""
                                    regConfirmPassword = ""
                                    selectedTab = 0
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("register_submit_button")
                        ) {
                            Icon(Icons.Rounded.HowToReg, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daftar Anggota", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Kelompok Kerja Guru Pendidikan Agama Islam\nKecamatan Gedebage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
