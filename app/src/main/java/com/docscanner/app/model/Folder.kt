package com.docscanner.app.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Folder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorName: String = "blue",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun color(): Color = colorMap[colorName] ?: Color(0xFF338FFF)

    companion object {
        val colorOptions = listOf("blue", "purple", "green", "orange", "red", "pink", "teal", "indigo")

        private val colorMap = mapOf(
            "blue" to Color(0xFF338FFF),
            "purple" to Color(0xFF9C27B0),
            "green" to Color(0xFF4CAF50),
            "orange" to Color(0xFFFF9800),
            "red" to Color(0xFFF44336),
            "pink" to Color(0xFFE91E63),
            "teal" to Color(0xFF009688),
            "indigo" to Color(0xFF3F51B5)
        )
    }
}
