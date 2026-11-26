package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgendaStatus(val code: String, val label: String) {
    UPCOMING("UPCOMING", "Akan Datang"),
    COMPLETED("COMPLETED", "Selesai");

    companion object {
        fun fromCode(code: String): AgendaStatus =
            entries.firstOrNull { it.code == code } ?: UPCOMING
    }
}

@Entity(tableName = "agendas")
data class AgendaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateTime: String,      // YYYY-MM-DD HH:mm
    val location: String,
    val speakerOrPic: String,
    val status: String = AgendaStatus.UPCOMING.code,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
