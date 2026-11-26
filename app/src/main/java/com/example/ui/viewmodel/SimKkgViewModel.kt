package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LetterPreferencesStore
import com.example.data.local.SimKkgDatabase
import com.example.data.local.SimKkgRepository
import com.example.data.model.AgendaEntity
import com.example.data.model.AgendaStatus
import com.example.data.model.GalleryItemEntity
import com.example.data.model.KopSuratConfig
import com.example.data.model.LetterDraft
import com.example.data.model.LetterTemplateType
import com.example.data.model.LetterTemplates
import com.example.data.model.SaveStatus
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.util.ExportUtils
import com.example.util.FormatUtils
import com.example.util.ImageCompressionUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardSummary(
    val totalBalance: Long = 0L,
    val monthlyIncome: Long = 0L,
    val monthlyExpense: Long = 0L,
    val upcomingAgendaCount: Int = 0,
    val upcomingAgendas: List<AgendaEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList()
)

data class PendingExport(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
)

class SimKkgViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SimKkgRepository by lazy {
        val db = SimKkgDatabase.getInstance(application)
        val store = LetterPreferencesStore(application)
        SimKkgRepository(application, db, store)
    }

    val saveStatus: StateFlow<SaveStatus> = repository.saveStatus

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val members: StateFlow<List<UserEntity>> = repository.membersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMemberCount: StateFlow<Int> = repository.pendingMemberCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow<String?>(null)
    val authSuccess: StateFlow<String?> = _authSuccess.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private val _pendingExport = MutableStateFlow<PendingExport?>(null)
    val pendingExport: StateFlow<PendingExport?> = _pendingExport.asStateFlow()

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val agendas: StateFlow<List<AgendaEntity>> = repository.agendasFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val galleryItems: StateFlow<List<GalleryItemEntity>> = repository.galleryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Finance filters
    private val _financeFilterMonth = MutableStateFlow("ALL")
    val financeFilterMonth: StateFlow<String> = _financeFilterMonth.asStateFlow()

    private val _financeFilterType = MutableStateFlow("ALL")
    val financeFilterType: StateFlow<String> = _financeFilterType.asStateFlow()

    private val _financeSearchQuery = MutableStateFlow("")
    val financeSearchQuery: StateFlow<String> = _financeSearchQuery.asStateFlow()

    val availableMonths: StateFlow<List<String>> = allTransactions
        .map { list ->
            listOf("ALL") + list.mapNotNull { tx ->
                if (tx.date.length >= 7) tx.date.substring(0, 7) else null
            }.distinct().sortedDescending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ALL"))

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _financeFilterMonth,
        _financeFilterType,
        _financeSearchQuery
    ) { list, month, type, query ->
        val q = query.trim().lowercase()
        list.filter { tx ->
            val matchMonth = month == "ALL" || tx.date.startsWith(month)
            val matchType = type == "ALL" || tx.type == type
            val matchQuery = q.isEmpty() ||
                tx.description.lowercase().contains(q) ||
                tx.category.lowercase().contains(q)
            matchMonth && matchType && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        allTransactions,
        agendas
    ) { txList, agendaList ->
        val currentMonth = FormatUtils.currentYearMonth()
        val totalIncome = txList.filter { it.type == TransactionType.INCOME.code }.sumOf { it.amount }
        val totalExpense = txList.filter { it.type == TransactionType.EXPENSE.code }.sumOf { it.amount }
        val monthlyIn = txList.filter {
            it.type == TransactionType.INCOME.code && it.date.startsWith(currentMonth)
        }.sumOf { it.amount }
        val monthlyOut = txList.filter {
            it.type == TransactionType.EXPENSE.code && it.date.startsWith(currentMonth)
        }.sumOf { it.amount }

        val upcoming = agendaList.filter { it.status == AgendaStatus.UPCOMING.code }
        DashboardSummary(
            totalBalance = totalIncome - totalExpense,
            monthlyIncome = monthlyIn,
            monthlyExpense = monthlyOut,
            upcomingAgendaCount = upcoming.size,
            upcomingAgendas = upcoming.take(3),
            recentTransactions = txList.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Editable Kop Surat & Letter Draft with debounced auto-save
    private val _kopSurat = MutableStateFlow(KopSuratConfig())
    val kopSurat: StateFlow<KopSuratConfig> = _kopSurat.asStateFlow()

    private val _letterDraft = MutableStateFlow(LetterDraft())
    val letterDraft: StateFlow<LetterDraft> = _letterDraft.asStateFlow()

    private var kopSaveJob: Job? = null
    private var draftSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            _kopSurat.value = repository.kopSuratFlow.first()
            _letterDraft.value = repository.letterDraftFlow.first()
            _authReady.value = true
        }
    }

    private fun isAdminActive(): Boolean = currentUser.value?.role == UserRole.ADMIN.code

    // Auth Actions
    fun clearAuthMessages() {
        _authError.value = null
        _authSuccess.value = null
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            _authSuccess.value = null
            val res = repository.login(username, password)
            if (res.isFailure) {
                _authError.value = res.exceptionOrNull()?.message ?: "Gagal masuk."
            }
        }
    }

    fun registerMember(
        username: String,
        fullName: String,
        schoolName: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authError.value = null
            _authSuccess.value = null
            if (password != confirmPassword) {
                _authError.value = "Konfirmasi kata sandi tidak cocok."
                return@launch
            }
            val res = repository.registerMember(username, fullName, schoolName, password)
            if (res.isSuccess) {
                _authSuccess.value = res.getOrNull()
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message ?: "Pendaftaran gagal."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            clearAuthMessages()
        }
    }

    fun verifyMember(username: String) {
        viewModelScope.launch {
            if (!isAdminActive()) return@launch
            repository.verifyMember(username)
            _snackbarEvents.emit("Akun anggota @$username telah diverifikasi.")
        }
    }

    fun rejectMember(username: String) {
        viewModelScope.launch {
            if (!isAdminActive()) return@launch
            repository.rejectMember(username)
            _snackbarEvents.emit("Pendaftaran @$username ditolak.")
        }
    }

    fun deleteMember(username: String) {
        viewModelScope.launch {
            if (!isAdminActive()) return@launch
            repository.deleteMember(username)
            _snackbarEvents.emit("Data anggota @$username dihapus.")
        }
    }

    // Finance Actions
    fun setFinanceFilterMonth(month: String) {
        _financeFilterMonth.value = month
    }

    fun setFinanceFilterType(type: String) {
        _financeFilterType.value = type
    }

    fun setFinanceSearchQuery(query: String) {
        _financeSearchQuery.value = query
    }

    fun upsertTransaction(item: TransactionEntity) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat mengubah catatan kas.")
                return@launch
            }
            repository.upsertTransaction(item)
            _snackbarEvents.emit("Transaksi kas tersimpan otomatis.")
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat menghapus transaksi kas.")
                return@launch
            }
            repository.deleteTransaction(id)
            _snackbarEvents.emit("Transaksi kas dihapus.")
        }
    }

    // Agenda Actions
    fun upsertAgenda(item: AgendaEntity) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat mengubah agenda kegiatan.")
                return@launch
            }
            repository.upsertAgenda(item)
            _snackbarEvents.emit("Agenda kegiatan tersimpan otomatis.")
        }
    }

    fun toggleAgendaStatus(item: AgendaEntity) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat mengubah status agenda.")
                return@launch
            }
            repository.toggleAgendaStatus(item)
        }
    }

    fun deleteAgenda(id: Long) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat menghapus agenda.")
                return@launch
            }
            repository.deleteAgenda(id)
            _snackbarEvents.emit("Agenda kegiatan dihapus.")
        }
    }

    // Gallery Actions (Admin & Verified Members can contribute photos; delete restricted to Admin)
    fun addGalleryPhotoFromUri(
        uri: Uri,
        title: String,
        date: String,
        description: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value == null) return@launch
            repository.markSaving()
            val base64 = ImageCompressionUtils.compressUriToBase64(
                context = getApplication(),
                uri = uri,
                maxWidth = 800,
                jpegQuality = 70
            )
            if (base64.isNullOrBlank()) {
                repository.markSaved()
                _snackbarEvents.emit("Gagal memproses gambar. Coba pilih foto lain.")
                return@launch
            }
            repository.addGalleryItem(
                GalleryItemEntity(
                    title = title.ifBlank { "Dokumentasi Kegiatan KKG PAI Gedebage" },
                    date = date.ifBlank { FormatUtils.todayIsoDate() },
                    description = description,
                    imageBase64 = base64
                )
            )
            _snackbarEvents.emit("Foto dikompresi & tersimpan otomatis.")
            onComplete()
        }
    }

    fun deleteGalleryItem(id: Long) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat menghapus foto galeri.")
                return@launch
            }
            repository.deleteGalleryItem(id)
            _snackbarEvents.emit("Dokumentasi foto dihapus.")
        }
    }

    // Kop Surat & Letter Draft Auto-Save (Debounced)
    fun updateKopSurat(transform: (KopSuratConfig) -> KopSuratConfig) {
        val updated = transform(_kopSurat.value)
        _kopSurat.value = updated
        repository.markSaving()
        kopSaveJob?.cancel()
        kopSaveJob = viewModelScope.launch {
            delay(350)
            repository.saveKopSurat(updated)
        }
    }

    fun updateKopLogoFromUri(uri: Uri) {
        viewModelScope.launch {
            repository.markSaving()
            val base64 = ImageCompressionUtils.compressUriToBase64(
                context = getApplication(),
                uri = uri,
                maxWidth = 360,
                jpegQuality = 75
            )
            if (!base64.isNullOrBlank()) {
                val updated = _kopSurat.value.copy(logoBase64 = base64)
                _kopSurat.value = updated
                repository.saveKopSurat(updated)
                _snackbarEvents.emit("Logo Kop Surat diperbarui & tersimpan otomatis.")
            } else {
                repository.markSaved()
                _snackbarEvents.emit("Gagal memuat logo gambar.")
            }
        }
    }

    fun updateLetterDraft(transform: (LetterDraft) -> LetterDraft) {
        val updated = transform(_letterDraft.value)
        _letterDraft.value = updated
        repository.markSaving()
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(350)
            repository.saveLetterDraft(updated)
        }
    }

    fun selectLetterTemplate(type: LetterTemplateType) {
        val updated = _letterDraft.value.copy(
            templateType = type.code,
            subject = LetterTemplates.subjectFor(type),
            recipient = LetterTemplates.recipientFor(type),
            openingText = LetterTemplates.openingFor(type),
            closingText = LetterTemplates.closingFor(type)
        )
        _letterDraft.value = updated
        viewModelScope.launch {
            repository.saveLetterDraft(updated)
            _snackbarEvents.emit("Template ${type.label} diterapkan.")
        }
    }

    // Exports (PDF, Excel, Word, Backup JSON)
    fun requestFinancePdfExport() {
        viewModelScope.launch {
            val list = filteredTransactions.value
            val filterLabel = FormatUtils.formatMonthYearLabel(_financeFilterMonth.value)
            val bytes = ExportUtils.generateFinancePdf(_kopSurat.value, list, filterLabel)
            _pendingExport.value = PendingExport(
                fileName = "Laporan_Kas_KKG_PAI_Gedebage_${FormatUtils.todayIsoDate()}.pdf",
                mimeType = "application/pdf",
                bytes = bytes
            )
        }
    }

    fun requestFinanceExcelExport() {
        viewModelScope.launch {
            val list = filteredTransactions.value
            val filterLabel = FormatUtils.formatMonthYearLabel(_financeFilterMonth.value)
            val bytes = ExportUtils.generateFinanceXlsx(_kopSurat.value, list, filterLabel)
            _pendingExport.value = PendingExport(
                fileName = "Laporan_Kas_KKG_PAI_Gedebage_${FormatUtils.todayIsoDate()}.xlsx",
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bytes = bytes
            )
        }
    }

    fun requestLetterPdfExport() {
        viewModelScope.launch {
            val bytes = ExportUtils.generateLetterPdf(_kopSurat.value, _letterDraft.value)
            _pendingExport.value = PendingExport(
                fileName = "Surat_KKG_PAI_Gedebage_${FormatUtils.todayIsoDate()}.pdf",
                mimeType = "application/pdf",
                bytes = bytes
            )
        }
    }

    fun requestLetterWordExport() {
        viewModelScope.launch {
            val bytes = ExportUtils.generateLetterWordDoc(_kopSurat.value, _letterDraft.value)
            _pendingExport.value = PendingExport(
                fileName = "Surat_KKG_PAI_Gedebage_${FormatUtils.todayIsoDate()}.doc",
                mimeType = "application/msword",
                bytes = bytes
            )
        }
    }

    fun requestBackupJsonExport() {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            _pendingExport.value = PendingExport(
                fileName = "Backup_KKG_PAI_Gedebage_${FormatUtils.todayIsoDate()}.json",
                mimeType = "application/json",
                bytes = json.toByteArray(Charsets.UTF_8)
            )
        }
    }

    fun completeExportToUri(uri: Uri?) {
        val payload = _pendingExport.value
        _pendingExport.value = null
        if (uri == null || payload == null) return
        viewModelScope.launch {
            val result = ExportUtils.writeBytesToUri(getApplication(), uri, payload.bytes)
            if (result.isSuccess) {
                _snackbarEvents.emit("File ${payload.fileName} berhasil disimpan.")
            } else {
                _snackbarEvents.emit("Gagal menyimpan file: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun sharePendingExportDirectly(payload: PendingExport) {
        viewModelScope.launch {
            ExportUtils.shareBytes(
                context = getApplication(),
                fileName = payload.fileName,
                mimeType = payload.mimeType,
                bytes = payload.bytes
            )
        }
    }

    fun importBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat memulihkan cadangan data.")
                return@launch
            }
            try {
                val raw = getApplication<Application>().contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("File tidak dapat dibaca.")
                val result = repository.importBackupJson(raw)
                if (result.isSuccess) {
                    _kopSurat.value = repository.kopSuratFlow.first()
                    _letterDraft.value = repository.letterDraftFlow.first()
                    _snackbarEvents.emit(result.getOrDefault("Data berhasil dipulihkan."))
                } else {
                    _snackbarEvents.emit(result.exceptionOrNull()?.message ?: "Gagal memulihkan data.")
                }
            } catch (e: Exception) {
                _snackbarEvents.emit("Gagal membaca file cadangan: ${e.message}")
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            if (!isAdminActive()) {
                _snackbarEvents.emit("Hanya Admin yang dapat mereset seluruh data.")
                return@launch
            }
            repository.resetAllData()
            _kopSurat.value = repository.kopSuratFlow.first()
            _letterDraft.value = repository.letterDraftFlow.first()
            _snackbarEvents.emit("Seluruh data berhasil direset ke kondisi bersih.")
        }
    }
}
