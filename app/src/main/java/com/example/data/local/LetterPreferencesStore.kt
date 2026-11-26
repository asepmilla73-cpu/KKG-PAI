package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.KopSuratConfig
import com.example.data.model.LetterDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.letterDataStore: DataStore<Preferences> by preferencesDataStore(name = "sim_kkg_preferences")

class LetterPreferencesStore(private val context: Context) {

    private object Keys {
        val HAS_SEEDED = booleanPreferencesKey("has_seeded_initial_data")
        val GEDEBAGE_BRAND_APPLIED = booleanPreferencesKey("gedebage_brand_v1_applied")
        val LOGGED_IN_USERNAME = stringPreferencesKey("logged_in_username")
        val KOP_SURAT_JSON = stringPreferencesKey("kop_surat_json")
        val LETTER_DRAFT_JSON = stringPreferencesKey("letter_draft_json")
    }

    val kopSuratFlow: Flow<KopSuratConfig> = context.letterDataStore.data.map { prefs ->
        val raw = prefs[Keys.KOP_SURAT_JSON]
        if (raw.isNullOrBlank()) KopSuratConfig() else decodeKop(raw)
    }

    val letterDraftFlow: Flow<LetterDraft> = context.letterDataStore.data.map { prefs ->
        val raw = prefs[Keys.LETTER_DRAFT_JSON]
        if (raw.isNullOrBlank()) LetterDraft() else decodeDraft(raw)
    }

    val loggedInUsernameFlow: Flow<String?> = context.letterDataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USERNAME]?.takeIf { it.isNotBlank() }
    }

    suspend fun setLoggedInUsername(username: String?) {
        context.letterDataStore.edit { prefs ->
            if (username.isNullOrBlank()) {
                prefs.remove(Keys.LOGGED_IN_USERNAME)
            } else {
                prefs[Keys.LOGGED_IN_USERNAME] = username
            }
        }
    }

    suspend fun hasSeeded(): Boolean {
        return context.letterDataStore.data.first()[Keys.HAS_SEEDED] == true
    }

    suspend fun markSeeded() {
        context.letterDataStore.edit { prefs ->
            prefs[Keys.HAS_SEEDED] = true
        }
    }

    suspend fun isGedebageBrandApplied(): Boolean {
        return context.letterDataStore.data.first()[Keys.GEDEBAGE_BRAND_APPLIED] == true
    }

    suspend fun markGedebageBrandApplied() {
        context.letterDataStore.edit { prefs ->
            prefs[Keys.GEDEBAGE_BRAND_APPLIED] = true
        }
    }

    suspend fun getKopSuratOnce(): KopSuratConfig {
        val raw = context.letterDataStore.data.first()[Keys.KOP_SURAT_JSON]
        return if (raw.isNullOrBlank()) KopSuratConfig() else decodeKop(raw)
    }

    suspend fun getLetterDraftOnce(): LetterDraft {
        val raw = context.letterDataStore.data.first()[Keys.LETTER_DRAFT_JSON]
        return if (raw.isNullOrBlank()) LetterDraft() else decodeDraft(raw)
    }

    suspend fun saveKopSurat(config: KopSuratConfig) {
        context.letterDataStore.edit { prefs ->
            prefs[Keys.KOP_SURAT_JSON] = encodeKop(config).toString()
        }
    }

    suspend fun saveLetterDraft(draft: LetterDraft) {
        context.letterDataStore.edit { prefs ->
            prefs[Keys.LETTER_DRAFT_JSON] = encodeDraft(draft).toString()
        }
    }

    suspend fun resetLetterSettings(defaultLogoBase64: String) {
        context.letterDataStore.edit { prefs ->
            prefs[Keys.KOP_SURAT_JSON] = encodeKop(KopSuratConfig(logoBase64 = defaultLogoBase64)).toString()
            prefs[Keys.LETTER_DRAFT_JSON] = encodeDraft(LetterDraft()).toString()
            prefs[Keys.HAS_SEEDED] = true
            prefs[Keys.GEDEBAGE_BRAND_APPLIED] = true
        }
    }

    companion object {
        fun encodeKop(config: KopSuratConfig): JSONObject = JSONObject().apply {
            put("kkgName", config.kkgName)
            put("districtName", config.districtName)
            put("address", config.address)
            put("contactInfo", config.contactInfo)
            put("logoBase64", config.logoBase64)
            put("chairpersonName", config.chairpersonName)
            put("chairpersonNip", config.chairpersonNip)
            put("secretaryName", config.secretaryName)
            put("secretaryNip", config.secretaryNip)
        }

        fun decodeKop(jsonStr: String): KopSuratConfig {
            return try {
                val o = JSONObject(jsonStr)
                val def = KopSuratConfig()
                KopSuratConfig(
                    kkgName = o.optString("kkgName", def.kkgName),
                    districtName = o.optString("districtName", def.districtName),
                    address = o.optString("address", def.address),
                    contactInfo = o.optString("contactInfo", def.contactInfo),
                    logoBase64 = o.optString("logoBase64", def.logoBase64),
                    chairpersonName = o.optString("chairpersonName", def.chairpersonName),
                    chairpersonNip = o.optString("chairpersonNip", def.chairpersonNip),
                    secretaryName = o.optString("secretaryName", def.secretaryName),
                    secretaryNip = o.optString("secretaryNip", def.secretaryNip)
                )
            } catch (_: Exception) {
                KopSuratConfig()
            }
        }

        fun encodeDraft(draft: LetterDraft): JSONObject = JSONObject().apply {
            put("templateType", draft.templateType)
            put("letterNumber", draft.letterNumber)
            put("attachment", draft.attachment)
            put("subject", draft.subject)
            put("letterDate", draft.letterDate)
            put("cityName", draft.cityName)
            put("recipient", draft.recipient)
            put("eventDayDate", draft.eventDayDate)
            put("eventTime", draft.eventTime)
            put("eventLocation", draft.eventLocation)
            put("openingText", draft.openingText)
            put("closingText", draft.closingText)
        }

        fun decodeDraft(jsonStr: String): LetterDraft {
            return try {
                val o = JSONObject(jsonStr)
                val def = LetterDraft()
                LetterDraft(
                    templateType = o.optString("templateType", def.templateType),
                    letterNumber = o.optString("letterNumber", def.letterNumber),
                    attachment = o.optString("attachment", def.attachment),
                    subject = o.optString("subject", def.subject),
                    letterDate = o.optString("letterDate", def.letterDate),
                    cityName = o.optString("cityName", def.cityName),
                    recipient = o.optString("recipient", def.recipient),
                    eventDayDate = o.optString("eventDayDate", def.eventDayDate),
                    eventTime = o.optString("eventTime", def.eventTime),
                    eventLocation = o.optString("eventLocation", def.eventLocation),
                    openingText = o.optString("openingText", def.openingText),
                    closingText = o.optString("closingText", def.closingText)
                )
            } catch (_: Exception) {
                LetterDraft()
            }
        }
    }
}
