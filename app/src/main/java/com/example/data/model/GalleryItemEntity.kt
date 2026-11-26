package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gallery_items")
data class GalleryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,          // YYYY-MM-DD
    val description: String,
    val imageBase64: String,
    val createdAt: Long = System.currentTimeMillis()
)
