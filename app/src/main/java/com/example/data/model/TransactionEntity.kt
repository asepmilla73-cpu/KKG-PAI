package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val code: String, val label: String) {
    INCOME("INCOME", "Pemasukan"),
    EXPENSE("EXPENSE", "Pengeluaran");

    companion object {
        fun fromCode(code: String): TransactionType =
            entries.firstOrNull { it.code == code } ?: INCOME
    }
}

object TransactionCategories {
    val all = listOf(
        "Iuran Anggota",
        "Bantuan Dana",
        "Konsumsi Rapat",
        "Operasional",
        "Lainnya"
    )
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,          // YYYY-MM-DD
    val type: String,          // INCOME or EXPENSE
    val category: String,
    val description: String,
    val amount: Long,
    val createdAt: Long = System.currentTimeMillis()
)
