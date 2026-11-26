package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LetterPreferencesStore
import com.example.data.local.SimKkgDatabase
import com.example.data.local.SimKkgRepository
import com.example.data.model.AccountStatus
import com.example.data.model.AgendaEntity
import com.example.data.model.KopSuratConfig
import com.example.data.model.LetterDraft
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.util.ExportUtils
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SIM-KKG PAI", appName)
    }

    @Test
    fun `format rupiah formats Indonesian currency cleanly`() {
        assertEquals("Rp 1.600.000", FormatUtils.formatRupiah(1600000L))
        assertEquals("+ Rp 500.000", FormatUtils.formatRupiahSigned(500000L, isIncome = true))
        assertEquals("- Rp 250.000", FormatUtils.formatRupiahSigned(250000L, isIncome = false))
    }

    @Test
    fun `export xlsx and word doc generate valid OOXML ZIP and MSWord HTML bytes`() = runBlocking {
        val kop = KopSuratConfig()
        val draft = LetterDraft()
        assertTrue(kop.districtName.contains("GEDEBAGE"))

        val txs = listOf(
            TransactionEntity(
                id = 1L,
                date = "2026-11-20",
                type = TransactionType.INCOME.code,
                category = "Iuran Anggota",
                description = "Iuran Bulanan",
                amount = 1000000L
            )
        )

        val financeXlsx = ExportUtils.generateFinanceXlsx(kop, txs, "Semua Bulan")
        assertTrue(financeXlsx.size > 100)
        assertEquals('P'.code.toByte(), financeXlsx[0])
        assertEquals('K'.code.toByte(), financeXlsx[1])

        val letterDoc = ExportUtils.generateLetterWordDoc(kop, draft)
        val docText = String(letterDoc, Charsets.UTF_8)
        assertTrue(docText.contains("urn:schemas-microsoft-com:office:word"))
        assertTrue(docText.contains(kop.kkgName))
        assertTrue(docText.contains("Undangan Rapat Koordinasi &amp; Workshop Kurikulum Merdeka"))
    }

    @Test
    fun `admin login and member registration plus admin verification flow works`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, SimKkgDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val store = LetterPreferencesStore(context)
        val repo = SimKkgRepository(context, db, store)

        // Register member
        val regResult = repo.registerMember(
            rawUsername = "ustadz_ali",
            rawFullName = "Ali Muttaqin, S.Pd.I.",
            rawSchoolName = "SDN Rancabolang 01",
            rawPassword = "rahasia123"
        )
        assertTrue(regResult.isSuccess)

        // Unverified member cannot log in yet
        val pendingLogin = repo.login("ustadz_ali", "rahasia123")
        assertTrue(pendingLogin.isFailure)

        // Admin login with username admin & password 123 succeeds
        val badAdmin = repo.login("admin", "wrong")
        assertTrue(badAdmin.isFailure)
        val okAdmin = repo.login("admin", "123")
        assertTrue(okAdmin.isSuccess)
        assertEquals(UserRole.ADMIN.code, okAdmin.getOrNull()?.role)

        // Admin verifies the pending member
        repo.verifyMember("ustadz_ali")
        val member = repo.membersFlow.first().first { it.username == "ustadz_ali" }
        assertEquals(AccountStatus.VERIFIED.code, member.status)

        // Now verified member can log in
        repo.logout()
        val memberLogin = repo.login("ustadz_ali", "rahasia123")
        assertTrue(memberLogin.isSuccess)
        assertEquals("Ali Muttaqin, S.Pd.I.", memberLogin.getOrNull()?.fullName)
        db.close()
    }

    @Test
    fun `repository json backup export and restore roundtrip preserves records`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, SimKkgDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val store = LetterPreferencesStore(context)
        val repo = SimKkgRepository(context, db, store)

        repo.upsertTransaction(
            TransactionEntity(
                date = "2026-11-25",
                type = TransactionType.INCOME.code,
                category = "Iuran Anggota",
                description = "Uji Coba Backup",
                amount = 750000L
            )
        )
        repo.upsertAgenda(
            AgendaEntity(
                title = "Rapat Uji Coba",
                dateTime = "2026-11-30 09:00",
                location = "Aula KKG Gedebage",
                speakerOrPic = "Ketua KKG"
            )
        )

        val backupJson = repo.exportBackupJson()
        repo.resetAllData()
        assertEquals(0, repo.transactionsFlow.first().size)

        val restoreResult = repo.importBackupJson(backupJson)
        assertTrue(restoreResult.isSuccess)
        assertEquals(1, repo.transactionsFlow.first().size)
        assertEquals(750000L, repo.transactionsFlow.first().first().amount)
        assertEquals(1, repo.agendasFlow.first().size)
        db.close()
    }
}
