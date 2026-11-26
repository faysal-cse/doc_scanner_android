package com.docscanner.app.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Serializable
data class SavedDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val pageCount: Int,
    val folderID: String? = null
) {
    val formattedDate: String
        get() {
            val fmt = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            return fmt.format(Date(createdAt))
        }

    val shortDate: String
        get() {
            val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return fmt.format(Date(createdAt))
        }
}
