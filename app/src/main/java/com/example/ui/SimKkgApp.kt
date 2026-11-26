package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.components.AutoSaveBadge
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.BackupRestoreDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.LetterGeneratorScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MemberManagementScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.viewmodel.PendingExport
import com.example.ui.viewmodel.SimKkgViewModel

enum class AppDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", "Beranda", Icons.Rounded.Home, Icons.Outlined.Home),
    FINANCE("finance", "Kas KKG", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    AGENDA("agenda", "Agenda", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    LETTERS("letters", "Surat", Icons.Rounded.Description, Icons.Outlined.Description),
    GALLERY("gallery", "Galeri", Icons.Rounded.PhotoLibrary, Icons.Outlined.PhotoLibrary),
    MEMBERS("members", "Anggota", Icons.Rounded.Group, Icons.Outlined.Group)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimKkgApp(
    viewModel: SimKkgViewModel = viewModel()
) {
    val authReady by viewModel.authReady.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()

    if (!authReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
        return
    }

    val activeUser = currentUser
    if (activeUser == null) {
        LoginScreen(
            authError = authError,
            authSuccess = authSuccess,
            onLogin = viewModel::login,
            onRegisterMember = viewModel::registerMember,
            onClearMessages = viewModel::clearAuthMessages
        )
        return
    }

    val isAdmin = activeUser.role == UserRole.ADMIN.code
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var lastExportedForShare by remember { mutableStateOf<PendingExport?>(null) }

    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.financeFilterMonth.collectAsStateWithLifecycle()
    val selectedType by viewModel.financeFilterType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.financeSearchQuery.collectAsStateWithLifecycle()
    val agendas by viewModel.agendas.collectAsStateWithLifecycle()
    val galleryItems by viewModel.galleryItems.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val pendingMemberCount by viewModel.pendingMemberCount.collectAsStateWithLifecycle()
    val kopSurat by viewModel.kopSurat.collectAsStateWithLifecycle()
    val letterDraft by viewModel.letterDraft.collectAsStateWithLifecycle()
    val pendingExport by viewModel.pendingExport.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // If a non-admin happens to be on MEMBERS, return to DASHBOARD
    LaunchedEffect(isAdmin, currentDestination) {
        if (!isAdmin && currentDestination == AppDestination.MEMBERS) {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    // SAF Launchers for exact MIME types
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri -> viewModel.completeExportToUri(uri) }
    )
    val saveExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri -> viewModel.completeExportToUri(uri) }
    )
    val saveWordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/msword"),
        onResult = { uri -> viewModel.completeExportToUri(uri) }
    )
    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> viewModel.completeExportToUri(uri) }
    )
    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) viewModel.importBackupFromUri(uri) }
    )

    LaunchedEffect(pendingExport) {
        val payload = pendingExport ?: return@LaunchedEffect
        lastExportedForShare = payload
        when (payload.mimeType) {
            "application/pdf" -> savePdfLauncher.launch(payload.fileName)
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> saveExcelLauncher.launch(payload.fileName)
            "application/msword" -> saveWordLauncher.launch(payload.fileName)
            "application/json" -> saveJsonLauncher.launch(payload.fileName)
            else -> saveJsonLauncher.launch(payload.fileName)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { msg ->
            val canShare = msg.startsWith("File ") && lastExportedForShare != null
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (canShare) "Bagikan" else null,
                duration = if (canShare) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                lastExportedForShare?.let { viewModel.sharePendingExportDirectly(it) }
            }
        }
    }

    BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
        currentDestination = AppDestination.DASHBOARD
    }

    val bottomNavDestinations = listOf(
        AppDestination.DASHBOARD,
        AppDestination.FINANCE,
        AppDestination.AGENDA,
        AppDestination.LETTERS,
        AppDestination.GALLERY
    )
    val railDestinations = if (isAdmin) AppDestination.entries else bottomNavDestinations

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useSidebarRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kkg_logo_default),
                                contentDescription = "Logo KKG PAI Gedebage",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "KKG PAI Gedebage",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDeep,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${activeUser.fullName} (${if (isAdmin) "Admin" else "Anggota"})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        if (useSidebarRail) {
                            AutoSaveBadge(
                                status = saveStatus,
                                compact = false,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        if (isAdmin) {
                            IconButton(
                                onClick = { currentDestination = AppDestination.MEMBERS },
                                modifier = Modifier.testTag("open_member_verification_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (pendingMemberCount > 0) {
                                            Badge(containerColor = AmberAccent, contentColor = Color.White) {
                                                Text(pendingMemberCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Group,
                                        contentDescription = "Verifikasi Anggota",
                                        tint = if (currentDestination == AppDestination.MEMBERS) EmeraldDeep else EmeraldPrimary
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { showBackupDialog = true },
                            modifier = Modifier.testTag("open_backup_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backup,
                                contentDescription = "Cadangan & Pemulihan Data",
                                tint = EmeraldPrimary
                            )
                        }
                        IconButton(
                            onClick = { showLogoutConfirm = true },
                            modifier = Modifier.testTag("logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = "Keluar",
                                tint = ExpenseRed
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useSidebarRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        bottomNavDestinations.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldDeep,
                                    selectedTextColor = EmeraldDeep,
                                    indicatorColor = EmeraldContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (useSidebarRail) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Row {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                header = { Spacer(modifier = Modifier.height(8.dp)) }
                            ) {
                                railDestinations.forEach { dest ->
                                    val selected = currentDestination == dest
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { currentDestination = dest },
                                        icon = {
                                            if (dest == AppDestination.MEMBERS && pendingMemberCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(containerColor = AmberAccent, contentColor = Color.White) {
                                                            Text(pendingMemberCount.toString())
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                        contentDescription = dest.label
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = dest.label
                                                )
                                            }
                                        },
                                        label = { Text(dest.label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = EmeraldDeep,
                                            selectedTextColor = EmeraldDeep,
                                            indicatorColor = EmeraldContainer
                                        ),
                                        modifier = Modifier.testTag("rail_tab_${dest.route}")
                                    )
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (!useSidebarRail) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .align(Alignment.End)
                        ) {
                            AutoSaveBadge(status = saveStatus, compact = false)
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (currentDestination) {
                            AppDestination.DASHBOARD -> DashboardScreen(
                                summary = dashboardSummary,
                                kopSurat = kopSurat,
                                isAdmin = isAdmin,
                                pendingMemberCount = pendingMemberCount,
                                onNavigateToFinance = { currentDestination = AppDestination.FINANCE },
                                onNavigateToAgenda = { currentDestination = AppDestination.AGENDA },
                                onNavigateToLetters = { currentDestination = AppDestination.LETTERS },
                                onNavigateToGallery = { currentDestination = AppDestination.GALLERY },
                                onNavigateToMembers = { currentDestination = AppDestination.MEMBERS }
                            )
                            AppDestination.FINANCE -> FinanceScreen(
                                transactions = filteredTransactions,
                                availableMonths = availableMonths,
                                selectedMonth = selectedMonth,
                                selectedType = selectedType,
                                searchQuery = searchQuery,
                                isAdmin = isAdmin,
                                onMonthChange = viewModel::setFinanceFilterMonth,
                                onTypeChange = viewModel::setFinanceFilterType,
                                onSearchChange = viewModel::setFinanceSearchQuery,
                                onUpsertTransaction = viewModel::upsertTransaction,
                                onDeleteTransaction = viewModel::deleteTransaction,
                                onExportExcel = viewModel::requestFinanceExcelExport,
                                onExportPdf = viewModel::requestFinancePdfExport
                            )
                            AppDestination.AGENDA -> AgendaScreen(
                                agendas = agendas,
                                isAdmin = isAdmin,
                                onUpsertAgenda = viewModel::upsertAgenda,
                                onToggleStatus = viewModel::toggleAgendaStatus,
                                onDeleteAgenda = viewModel::deleteAgenda
                            )
                            AppDestination.LETTERS -> LetterGeneratorScreen(
                                kopSurat = kopSurat,
                                letterDraft = letterDraft,
                                onUpdateKopSurat = viewModel::updateKopSurat,
                                onUpdateKopLogoUri = viewModel::updateKopLogoFromUri,
                                onUpdateLetterDraft = viewModel::updateLetterDraft,
                                onSelectTemplate = viewModel::selectLetterTemplate,
                                onExportPdf = viewModel::requestLetterPdfExport,
                                onExportWord = viewModel::requestLetterWordExport
                            )
                            AppDestination.GALLERY -> GalleryScreen(
                                galleryItems = galleryItems,
                                isAdmin = isAdmin,
                                onAddPhotoFromUri = viewModel::addGalleryPhotoFromUri,
                                onDeleteGalleryItem = viewModel::deleteGalleryItem
                            )
                            AppDestination.MEMBERS -> MemberManagementScreen(
                                members = members,
                                onVerifyMember = viewModel::verifyMember,
                                onRejectMember = viewModel::rejectMember,
                                onDeleteMember = viewModel::deleteMember
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        BackupRestoreDialog(
            isAdmin = isAdmin,
            onDismiss = { showBackupDialog = false },
            onExportJson = { viewModel.requestBackupJsonExport() },
            onImportJson = { openJsonLauncher.launch(arrayOf("application/json", "*/*")) },
            onResetData = { viewModel.resetAllData() }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Keluar dari Akun?") },
            text = { Text("Anda sedang masuk sebagai ${activeUser.fullName}. Ingin keluar ke halaman login?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        currentDestination = AppDestination.DASHBOARD
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_logout_btn")
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
