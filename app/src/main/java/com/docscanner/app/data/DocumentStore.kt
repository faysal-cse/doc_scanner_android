package com.docscanner.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.docscanner.app.model.Folder
import com.docscanner.app.model.SavedDocument
import com.docscanner.app.util.PDFExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DocumentStore(private val context: Context) {

    @Serializable
    private data class StoreIndex(
        val documents: List<SavedDocument> = emptyList(),
        val folders: List<Folder> = emptyList()
    )

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val scansDir = File(context.filesDir, "Scans").also { it.mkdirs() }
    private val indexFile = File(context.filesDir, "scans_index.json")

    private val _documents = MutableStateFlow<List<SavedDocument>>(emptyList())
    val documents: StateFlow<List<SavedDocument>> = _documents.asStateFlow()

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    init {
        loadIndex()
    }

    fun save(pages: List<Bitmap>, name: String, folderID: String? = null) {
        val id = UUID.randomUUID().toString()
        val docDir = File(scansDir, id).also { it.mkdirs() }

        for ((i, bitmap) in pages.withIndex()) {
            val file = File(docDir, "page_$i.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        }

        // Create thumbnail
        val thumb = createThumbnail(pages.first(), 400)
        val thumbFile = File(docDir, "thumbnail.jpg")
        FileOutputStream(thumbFile).use { out ->
            thumb.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        val doc = SavedDocument(
            id = id,
            name = name,
            pageCount = pages.size,
            folderID = folderID
        )

        _documents.value = listOf(doc) + _documents.value
        saveIndex()
    }

    fun loadPages(document: SavedDocument): List<Bitmap> {
        val docDir = File(scansDir, document.id)
        val pages = mutableListOf<Bitmap>()
        for (i in 0 until document.pageCount) {
            val file = File(docDir, "page_$i.jpg")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.let { pages.add(it) }
            }
        }
        return pages
    }

    fun thumbnailFile(document: SavedDocument): File {
        return File(scansDir, "${document.id}/thumbnail.jpg")
    }

    fun rename(document: SavedDocument, newName: String) {
        _documents.value = _documents.value.map {
            if (it.id == document.id) it.copy(name = newName) else it
        }
        saveIndex()
    }

    fun delete(document: SavedDocument) {
        File(scansDir, document.id).deleteRecursively()
        _documents.value = _documents.value.filter { it.id != document.id }
        saveIndex()
    }

    fun deleteDocuments(ids: Set<String>) {
        ids.forEach { id -> File(scansDir, id).deleteRecursively() }
        _documents.value = _documents.value.filter { it.id !in ids }
        saveIndex()
    }

    fun moveDocument(document: SavedDocument, toFolderID: String?) {
        _documents.value = _documents.value.map {
            if (it.id == document.id) it.copy(folderID = toFolderID) else it
        }
        saveIndex()
    }

    fun moveDocuments(ids: Set<String>, toFolderID: String?) {
        _documents.value = _documents.value.map {
            if (it.id in ids) it.copy(folderID = toFolderID) else it
        }
        saveIndex()
    }

    fun documentsIn(folder: Folder): List<SavedDocument> {
        return _documents.value.filter { it.folderID == folder.id }
    }

    fun unfiledDocuments(): List<SavedDocument> {
        return _documents.value.filter { it.folderID == null }
    }

    fun documentCount(folder: Folder): Int {
        return _documents.value.count { it.folderID == folder.id }
    }

    fun createFolder(name: String, color: String) {
        val folder = Folder(name = name, colorName = color)
        _folders.value = _folders.value + folder
        saveIndex()
    }

    fun renameFolder(folder: Folder, newName: String) {
        _folders.value = _folders.value.map {
            if (it.id == folder.id) it.copy(name = newName) else it
        }
        saveIndex()
    }

    fun deleteFolder(folder: Folder) {
        _documents.value = _documents.value.map {
            if (it.folderID == folder.id) it.copy(folderID = null) else it
        }
        _folders.value = _folders.value.filter { it.id != folder.id }
        saveIndex()
    }

    fun exportPDF(document: SavedDocument): File? {
        val pages = loadPages(document)
        if (pages.isEmpty()) return null
        return PDFExporter.exportToFile(pages, document.name, context)
    }

    private fun loadIndex() {
        try {
            if (indexFile.exists()) {
                val data = indexFile.readText()
                val index = json.decodeFromString<StoreIndex>(data)
                _documents.value = index.documents
                _folders.value = index.folders
            }
        } catch (e: Exception) {
            _documents.value = emptyList()
            _folders.value = emptyList()
        }
    }

    private fun saveIndex() {
        try {
            val index = StoreIndex(
                documents = _documents.value,
                folders = _folders.value
            )
            val data = json.encodeToString(StoreIndex.serializer(), index)
            indexFile.writeText(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createThumbnail(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        if (scale >= 1f) return bitmap
        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
