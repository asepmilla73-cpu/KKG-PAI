package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val code: String, val label: String) {
    ADMIN("ADMIN", "Admin"),
    MEMBER("MEMBER", "Anggota");

    companion object {
        fun fromCode(code: String): UserRole =
            entries.firstOrNull { it.code == code } ?: MEMBER
    }
}

enum class AccountStatus(val code: String, val label: String) {
    PENDING("PENDING", "Menunggu Verifikasi"),
    VERIFIED("VERIFIED", "Terverifikasi"),
    REJECTED("REJECTED", "Ditolak");

    companion object {
        fun fromCode(code: String): AccountStatus =
            entries.firstOrNull { it.code == code } ?: PENDING
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val fullName: String,
    val schoolName: String = "",
    val passwordHash: String,
    val role: String = UserRole.MEMBER.code,
    val status: String = AccountStatus.PENDING.code,
    val createdAt: Long = System.currentTimeMillis()
)
