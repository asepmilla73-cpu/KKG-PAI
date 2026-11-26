package com.example.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.R
import com.example.data.model.AccountStatus
import com.example.data.model.AgendaEntity
import com.example.data.model.AgendaStatus
import com.example.data.model.GalleryItemEntity
import com.example.data.model.KopSuratConfig
import com.example.data.model.LetterDraft
import com.example.data.model.SaveStatus
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.util.FormatUtils
import com.example.util.ImageCompressionUtils
import com.example.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class SimKkgRepository(
    private val context: Context,
    private val database: SimKkgDatabase,
    private val prefsStore: LetterPreferencesStore
) {
    private val transactionDao = database.transactionDao()
    private val agendaDao = database.agendaDao()
    private val galleryDao = database.galleryDao()
    private val userDao = database.userDao()

    private val _saveStatus = MutableStateFlow(SaveStatus.SAVED)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    val transactionsFlow: Flow<List<TransactionEntity>> = transactionDao.observeAll()
    val agendasFlow: Flow<List<AgendaEntity>> = agendaDao.observeAll()
    val galleryFlow: Flow<List<GalleryItemEntity>> = galleryDao.observeAll()
    val membersFlow: Flow<List<UserEntity>> = userDao.observeMembers()
    val pendingMemberCountFlow: Flow<Int> = userDao.observePendingMemberCount()
    val kopSuratFlow: Flow<KopSuratConfig> = prefsStore.kopSuratFlow
    val letterDraftFlow: Flow<LetterDraft> = prefsStore.letterDraftFlow

    val currentUserFlow: Flow<UserEntity?> = prefsStore.loggedInUsernameFlow.map { uname ->
        if (uname.isNullOrBlank()) null else userDao.findByUsername(uname)
    }

    fun markSaving() {
        _saveStatus.value = SaveStatus.SAVING
    }

    fun markSaved() {
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun ensureInitialized() {
        ensureDefaultAdmin()
        applyGedebageBrandIfNeeded()
        seedInitialDataIfEmpty()
    }

    private suspend fun ensureDefaultAdmin() {
        if (userDao.findByUsername("admin") == null) {
            userDao.insert(
                UserEntity(
                    username = "admin",
                    fullName = "Admin KKG PAI Gedebage",
                    schoolName = "Pengurus KKG PAI Kec. Gedebage",
                    passwordHash = PasswordHasher.hash("123"),
                    role = UserRole.ADMIN.code,
                    status = AccountStatus.VERIFIED.code
                )
            )
        }
    }

    private suspend fun applyGedebageBrandIfNeeded() {
        if (prefsStore.isGedebageBrandApplied()) return
        val logoBase64 = ImageCompressionUtils.compressDrawableToBase64(
            context = context,
            drawableId = R.drawable.img_kkg_logo_default,
            maxWidth = 360,
            jpegQuality = 85
        ).orEmpty()

        val existingKop = prefsStore.getKopSuratOnce()
        val updatedKop = if (existingKop.districtName.contains("SUKMAJAYA", ignoreCase = true) ||
            existingKop.logoBase64.isBlank()
        ) {
            KopSuratConfig(logoBase64 = logoBase64)
        } else {
            existingKop.copy(logoBase64 = logoBase64.ifBlank { existingKop.logoBase64 })
        }
        prefsStore.saveKopSurat(updatedKop)

        val existingDraft = prefsStore.getLetterDraftOnce()
        if (existingDraft.cityName.equals("Sukmajaya", ignoreCase = true)) {
            prefsStore.saveLetterDraft(LetterDraft())
        }
        prefsStore.markGedebageBrandApplied()
    }

    suspend fun seedInitialDataIfEmpty() {
        if (prefsStore.hasSeeded()) return
        val isEmpty = transactionDao.count() == 0 &&
            agendaDao.count() == 0 &&
            galleryDao.count() == 0
        if (!isEmpty) {
            prefsStore.markSeeded()
            return
        }

        _saveStatus.value = SaveStatus.SAVING

        val sampleTransactions = listOf(
            TransactionEntity(
                date = FormatUtils.dateOffsetFromToday(-8),
                type = TransactionType.INCOME.code,
                category = "Iuran Anggota",
                description = "Iuran Kas Rutin Anggota KKG PAI Gedebage",
                amount = 1600000L
            ),
            TransactionEntity(
                date = FormatUtils.dateOffsetFromToday(-4),
                type = TransactionType.INCOME.code,
                category = "Bantuan Dana",
                description = "Dana Stimulan Program Kerja KKG PAI Kemenag",
                amount = 2500000L
            ),
            TransactionEntity(
                date = FormatUtils.dateOffsetFromToday(-1),
                type = TransactionType.EXPENSE.code,
                category = "Konsumsi Rapat",
                description = "Konsumsi & Penggandaan Materi Rapat Koordinasi Gedebage",
                amount = 650000L
            )
        )

        val sampleAgendas = listOf(
            AgendaEntity(
                title = "Rapat Koordinasi & Penyusunan Kisi-Kisi PAS PAI",
                dateTime = FormatUtils.dateTimeOffsetFromToday(3, 8, 30),
                location = "Aula Sekretariat KKG PAI Kec. Gedebage",
                speakerOrPic = "H. Ahmad Fauzi, S.Pd.I., M.Pd.",
                status = AgendaStatus.UPCOMING.code,
                notes = "Membawa perangkat ajar Kurikulum Merdeka kelas 1-6."
            ),
            AgendaEntity(
                title = "Workshop Implementasi Deep Learning & Moderasi PAI",
                dateTime = FormatUtils.dateTimeOffsetFromToday(10, 9, 0),
                location = "SDN Rancabolang / Kec. Gedebage",
                speakerOrPic = "Dr. KH. Ridwan Masykur, M.Ag.",
                status = AgendaStatus.UPCOMING.code,
                notes = "Diikuti seluruh guru PAI SD/MI se-Kecamatan Gedebage."
            )
        )

        val workshopBase64 = ImageCompressionUtils.compressDrawableToBase64(
            context = context,
            drawableId = R.drawable.img_sample_workshop,
            maxWidth = 800,
            jpegQuality = 70
        ).orEmpty()

        val logoBase64 = ImageCompressionUtils.compressDrawableToBase64(
            context = context,
            drawableId = R.drawable.img_kkg_logo_default,
            maxWidth = 360,
            jpegQuality = 85
        ).orEmpty()

        val sampleGallery = if (workshopBase64.isNotBlank()) {
            listOf(
                GalleryItemEntity(
                    title = "Bimbingan Teknis Kurikulum Merdeka PAI Gedebage",
                    date = FormatUtils.dateOffsetFromToday(-12),
                    description = "Sesi diskusi kelompok penyusunan modul ajar PAI berdiferensiasi bersama pengurus KKG PAI Kecamatan Gedebage.",
                    imageBase64 = workshopBase64
                )
            )
        } else {
            emptyList()
        }

        database.withTransaction {
            transactionDao.insertAll(sampleTransactions)
            agendaDao.insertAll(sampleAgendas)
            if (sampleGallery.isNotEmpty()) {
                galleryDao.insertAll(sampleGallery)
            }
        }

        if (logoBase64.isNotBlank()) {
            prefsStore.saveKopSurat(KopSuratConfig(logoBase64 = logoBase64))
        }
        prefsStore.saveLetterDraft(LetterDraft())
        prefsStore.markSeeded()
        prefsStore.markGedebageBrandApplied()
        _saveStatus.value = SaveStatus.SAVED
    }

    // Auth & Member Verification
    suspend fun login(rawUsername: String, rawPassword: String): Result<UserEntity> {
        ensureDefaultAdmin()
        val username = rawUsername.trim().lowercase()
        if (username.isEmpty() || rawPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("Username dan kata sandi wajib diisi."))
        }
        val user = userDao.findByUsername(username)
            ?: return Result.failure(IllegalArgumentException("Username tidak ditemukan."))

        if (!PasswordHasher.verify(rawPassword, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Kata sandi salah. Silakan periksa kembali."))
        }

        if (user.role != UserRole.ADMIN.code) {
            when (AccountStatus.fromCode(user.status)) {
                AccountStatus.PENDING -> return Result.failure(
                    IllegalStateException("Akun Anda (${user.fullName}) masih menunggu verifikasi Admin KKG PAI Gedebage.")
                )
                AccountStatus.REJECTED -> return Result.failure(
                    IllegalStateException("Pendaftaran akun Anda ditolak oleh Admin. Hubungi pengurus KKG PAI Gedebage.")
                )
                AccountStatus.VERIFIED -> Unit
            }
        }

        prefsStore.setLoggedInUsername(user.username)
        return Result.success(user)
    }

    suspend fun registerMember(
        rawUsername: String,
        rawFullName: String,
        rawSchoolName: String,
        rawPassword: String
    ): Result<String> {
        ensureDefaultAdmin()
        val username = rawUsername.trim().lowercase()
        val fullName = rawFullName.trim()
        val schoolName = rawSchoolName.trim()

        if (fullName.length < 3) {
            return Result.failure(IllegalArgumentException("Nama lengkap minimal 3 karakter."))
        }
        if (username.length < 3 || username.contains(" ")) {
            return Result.failure(IllegalArgumentException("Username minimal 3 karakter dan tanpa spasi."))
        }
        if (username == "admin") {
            return Result.failure(IllegalArgumentException("Username 'admin' sudah digunakan oleh sistem."))
        }
        if (rawPassword.length < 3) {
            return Result.failure(IllegalArgumentException("Kata sandi minimal 3 karakter."))
        }
        if (userDao.findByUsername(username) != null) {
            return Result.failure(IllegalArgumentException("Username '$username' sudah terdaftar. Gunakan username lain."))
        }

        _saveStatus.value = SaveStatus.SAVING
        userDao.insert(
            UserEntity(
                username = username,
                fullName = fullName,
                schoolName = schoolName.ifBlank { "Guru PAI Kec. Gedebage" },
                passwordHash = PasswordHasher.hash(rawPassword),
                role = UserRole.MEMBER.code,
                status = AccountStatus.PENDING.code
            )
        )
        _saveStatus.value = SaveStatus.SAVED
        return Result.success("Pendaftaran berhasil! Akun '$username' menunggu verifikasi Admin.")
    }

    suspend fun logout() {
        prefsStore.setLoggedInUsername(null)
    }

    suspend fun getActiveUserOnce(): UserEntity? {
        val uname = prefsStore.loggedInUsernameFlow.first() ?: return null
        return userDao.findByUsername(uname)
    }

    suspend fun verifyMember(username: String) {
        val actor = getActiveUserOnce() ?: return
        if (actor.role != UserRole.ADMIN.code) return
        _saveStatus.value = SaveStatus.SAVING
        userDao.updateMemberStatus(username, AccountStatus.VERIFIED.code)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun rejectMember(username: String) {
        val actor = getActiveUserOnce() ?: return
        if (actor.role != UserRole.ADMIN.code) return
        _saveStatus.value = SaveStatus.SAVING
        userDao.updateMemberStatus(username, AccountStatus.REJECTED.code)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun deleteMember(username: String) {
        val actor = getActiveUserOnce() ?: return
        if (actor.role != UserRole.ADMIN.code) return
        _saveStatus.value = SaveStatus.SAVING
        userDao.deleteMember(username)
        _saveStatus.value = SaveStatus.SAVED
    }

    // Finance, Agenda, Gallery, Letter operations
    suspend fun upsertTransaction(item: TransactionEntity) {
        _saveStatus.value = SaveStatus.SAVING
        if (item.id == 0L) {
            transactionDao.insert(item)
        } else {
            transactionDao.update(item)
        }
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun deleteTransaction(id: Long) {
        _saveStatus.value = SaveStatus.SAVING
        transactionDao.deleteById(id)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun upsertAgenda(item: AgendaEntity) {
        _saveStatus.value = SaveStatus.SAVING
        if (item.id == 0L) {
            agendaDao.insert(item)
        } else {
            agendaDao.update(item)
        }
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun toggleAgendaStatus(item: AgendaEntity) {
        _saveStatus.value = SaveStatus.SAVING
        val next = if (item.status == AgendaStatus.UPCOMING.code) {
            AgendaStatus.COMPLETED.code
        } else {
            AgendaStatus.UPCOMING.code
        }
        agendaDao.updateStatus(item.id, next)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun deleteAgenda(id: Long) {
        _saveStatus.value = SaveStatus.SAVING
        agendaDao.deleteById(id)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun addGalleryItem(item: GalleryItemEntity) {
        _saveStatus.value = SaveStatus.SAVING
        galleryDao.insert(item)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun deleteGalleryItem(id: Long) {
        _saveStatus.value = SaveStatus.SAVING
        galleryDao.deleteById(id)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun saveKopSurat(config: KopSuratConfig) {
        _saveStatus.value = SaveStatus.SAVING
        prefsStore.saveKopSurat(config)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun saveLetterDraft(draft: LetterDraft) {
        _saveStatus.value = SaveStatus.SAVING
        prefsStore.saveLetterDraft(draft)
        _saveStatus.value = SaveStatus.SAVED
    }

    suspend fun exportBackupJson(): String {
        val transactions = transactionDao.getAllOnce()
        val agendas = agendaDao.getAllOnce()
        val gallery = galleryDao.getAllOnce()
        val members = userDao.getMembersOnce()
        val kop = prefsStore.getKopSuratOnce()
        val draft = prefsStore.getLetterDraftOnce()

        val root = JSONObject().apply {
            put("app", "SIM-KKG PAI")
            put("chapter", "Kecamatan Gedebage")
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())
            put("transactions", JSONArray().apply {
                transactions.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("date", t.date)
                        put("type", t.type)
                        put("category", t.category)
                        put("description", t.description)
                        put("amount", t.amount)
                        put("createdAt", t.createdAt)
                    })
                }
            })
            put("agendas", JSONArray().apply {
                agendas.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id)
                        put("title", a.title)
                        put("dateTime", a.dateTime)
                        put("location", a.location)
                        put("speakerOrPic", a.speakerOrPic)
                        put("status", a.status)
                        put("notes", a.notes)
                        put("createdAt", a.createdAt)
                    })
                }
            })
            put("gallery", JSONArray().apply {
                gallery.forEach { g ->
                    put(JSONObject().apply {
                        put("id", g.id)
                        put("title", g.title)
                        put("date", g.date)
                        put("description", g.description)
                        put("imageBase64", g.imageBase64)
                        put("createdAt", g.createdAt)
                    })
                }
            })
            put("members", JSONArray().apply {
                members.forEach { m ->
                    put(JSONObject().apply {
                        put("username", m.username)
                        put("fullName", m.fullName)
                        put("schoolName", m.schoolName)
                        put("passwordHash", m.passwordHash)
                        put("role", m.role)
                        put("status", m.status)
                        put("createdAt", m.createdAt)
                    })
                }
            })
            put("kopSurat", LetterPreferencesStore.encodeKop(kop))
            put("letterDraft", LetterPreferencesStore.encodeDraft(draft))
        }
        return root.toString(2)
    }

    suspend fun importBackupJson(rawJson: String): Result<String> {
        return try {
            _saveStatus.value = SaveStatus.SAVING
            val root = JSONObject(rawJson)
            if (root.optString("app") != "SIM-KKG PAI" && !root.has("transactions")) {
                _saveStatus.value = SaveStatus.ERROR
                return Result.failure(IllegalArgumentException("Format file cadangan SIM-KKG PAI tidak valid."))
            }

            val txList = mutableListOf<TransactionEntity>()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArray.length()) {
                val o = txArray.getJSONObject(i)
                txList.add(
                    TransactionEntity(
                        id = 0L,
                        date = o.optString("date", FormatUtils.todayIsoDate()),
                        type = o.optString("type", TransactionType.INCOME.code),
                        category = o.optString("category", "Lainnya"),
                        description = o.optString("description", ""),
                        amount = o.optLong("amount", 0L),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val agendaList = mutableListOf<AgendaEntity>()
            val agendaArray = root.optJSONArray("agendas") ?: JSONArray()
            for (i in 0 until agendaArray.length()) {
                val o = agendaArray.getJSONObject(i)
                agendaList.add(
                    AgendaEntity(
                        id = 0L,
                        title = o.optString("title", "Agenda KKG"),
                        dateTime = o.optString("dateTime", FormatUtils.todayIsoDate() + " 09:00"),
                        location = o.optString("location", "-"),
                        speakerOrPic = o.optString("speakerOrPic", "-"),
                        status = o.optString("status", AgendaStatus.UPCOMING.code),
                        notes = o.optString("notes", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val galleryList = mutableListOf<GalleryItemEntity>()
            val galleryArray = root.optJSONArray("gallery") ?: JSONArray()
            for (i in 0 until galleryArray.length()) {
                val o = galleryArray.getJSONObject(i)
                galleryList.add(
                    GalleryItemEntity(
                        id = 0L,
                        title = o.optString("title", "Dokumentasi"),
                        date = o.optString("date", FormatUtils.todayIsoDate()),
                        description = o.optString("description", ""),
                        imageBase64 = o.optString("imageBase64", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val memberList = mutableListOf<UserEntity>()
            val memberArray = root.optJSONArray("members") ?: JSONArray()
            for (i in 0 until memberArray.length()) {
                val o = memberArray.getJSONObject(i)
                val uname = o.optString("username", "").trim().lowercase()
                if (uname.isNotBlank() && uname != "admin") {
                    memberList.add(
                        UserEntity(
                            username = uname,
                            fullName = o.optString("fullName", uname),
                            schoolName = o.optString("schoolName", ""),
                            passwordHash = o.optString("passwordHash", ""),
                            role = UserRole.MEMBER.code,
                            status = o.optString("status", AccountStatus.PENDING.code),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            database.withTransaction {
                transactionDao.deleteAll()
                agendaDao.deleteAll()
                galleryDao.deleteAll()
                userDao.deleteAllMembers()
                if (txList.isNotEmpty()) transactionDao.insertAll(txList)
                if (agendaList.isNotEmpty()) agendaDao.insertAll(agendaList)
                if (galleryList.isNotEmpty()) galleryDao.insertAll(galleryList)
                if (memberList.isNotEmpty()) userDao.insertAll(memberList)
            }

            root.optJSONObject("kopSurat")?.let {
                prefsStore.saveKopSurat(LetterPreferencesStore.decodeKop(it.toString()))
            }
            root.optJSONObject("letterDraft")?.let {
                prefsStore.saveLetterDraft(LetterPreferencesStore.decodeDraft(it.toString()))
            }
            prefsStore.markSeeded()
            prefsStore.markGedebageBrandApplied()
            ensureDefaultAdmin()
            _saveStatus.value = SaveStatus.SAVED
            Result.success("Berhasil memulihkan ${txList.size} transaksi, ${agendaList.size} agenda, ${galleryList.size} foto, & ${memberList.size} anggota.")
        } catch (e: Exception) {
            _saveStatus.value = SaveStatus.ERROR
            Result.failure(e)
        }
    }

    suspend fun resetAllData() {
        _saveStatus.value = SaveStatus.SAVING
        database.withTransaction {
            transactionDao.deleteAll()
            agendaDao.deleteAll()
            galleryDao.deleteAll()
            userDao.deleteAllMembers()
        }
        val logoBase64 = ImageCompressionUtils.compressDrawableToBase64(
            context = context,
            drawableId = R.drawable.img_kkg_logo_default,
            maxWidth = 360,
            jpegQuality = 85
        ).orEmpty()
        prefsStore.resetLetterSettings(logoBase64)
        ensureDefaultAdmin()
        _saveStatus.value = SaveStatus.SAVED
    }
}
