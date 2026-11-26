package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object ImageCompressionUtils {
    private val bitmapCache = object : LruCache<Int, Bitmap>(32) {}

    suspend fun compressUriToBase64(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        jpegQuality: Int = 70
    ): String? = withContext(Dispatchers.IO) {
        try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return@withContext null

            var sampleSize = 1
            while (srcWidth / (sampleSize * 2) >= maxWidth) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext null

            encodeBitmapToBase64(decoded, maxWidth, jpegQuality)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun compressDrawableToBase64(
        context: Context,
        @DrawableRes drawableId: Int,
        maxWidth: Int = 800,
        jpegQuality: Int = 70
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
                ?: return@withContext null
            encodeBitmapToBase64(bitmap, maxWidth, jpegQuality)
        } catch (_: Exception) {
            null
        }
    }

    private fun encodeBitmapToBase64(
        source: Bitmap,
        maxWidth: Int,
        jpegQuality: Int
    ): String {
        val scaled = if (source.width > maxWidth) {
            val ratio = maxWidth.toFloat() / source.width.toFloat()
            val targetHeight = (source.height * ratio).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(source, maxWidth, targetHeight, true)
        } else {
            source
        }

        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)
        if (scaled !== source) {
            scaled.recycle()
        }
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        if (base64.isBlank()) return null
        val key = base64.hashCode()
        bitmapCache.get(key)?.let { return it }
        return try {
            val clean = base64.substringAfter("base64,", base64)
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                bitmapCache.put(key, bitmap)
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    fun decodeBase64ToImageBitmap(base64: String): ImageBitmap? {
        return decodeBase64ToBitmap(base64)?.asImageBitmap()
    }
}
