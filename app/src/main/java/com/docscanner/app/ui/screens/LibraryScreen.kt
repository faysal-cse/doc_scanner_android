package com.docscanner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.docscanner.app.data.DocumentStore
import com.docscanner.app.model.Folder
import com.docscanner.app.model.SavedDocument
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    documentStore: DocumentStore,
    onDocumentClick: (SavedDocument) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onBack: () -> Unit
) {
    val documents by documentStore.documents.collectAsState()
    val folders by documentStore.folders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var sortMode by remember { mutableStateOf(0) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderColor by remember { mutableStateOf("blue") }
    var renameDoc by remember { mutableStateOf<SavedDocument?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredDocs = remember(documents, searchQuery, sortMode) {
        val list = if (searchQuery.isBlank()) documents
                   else documents.filter { it.name.contains(searchQuery, ignoreCase = true) }
        when (sortMode) {
            1 -> list.sortedBy { it.createdAt }
            2 -> list.sortedBy { it.name.lowercase() }
            else -> list.sortedByDescending { it.createdAt }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) { Icon(Icons.Outlined.Sort, "Sort") }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Newest First") }, onClick = { sortMode = 0; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Oldest First") }, onClick = { sortMode = 1; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Name") }, onClick = { sortMode = 2; showSortMenu = false })
                        }
                    }
                    IconButton(onClick = { isEditing = !isEditing; selectedIds = emptySet() }) {
                        Icon(if (isEditing) Icons.Outlined.CheckCircle else Icons.Outlined.Edit,
                             if (isEditing) "Done" else "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { newFolderName = ""; showNewFolderDialog = true }) {
                Icon(Icons.Outlined.CreateNewFolder, "New Folder")
            }
        },
        bottomBar = {
            if (isEditing && selectedIds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { selectedIds = if (selectedIds.size == filteredDocs.size) emptySet() else filteredDocs.map { it.id }.toSet() }) {
                        Icon(Icons.Outlined.SelectAll, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedIds.size == filteredDocs.size) "Deselect" else "Select All")
                    }
                    TextButton(onClick = { showMoveDialog = true }) {
                        Icon(Icons.Outlined.DriveFileMove, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Move")
                    }
                    TextButton(onClick = {
                        documentStore.deleteDocuments(selectedIds)
                        selectedIds = emptySet(); isEditing = false
                    }) {
                        Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Search documents") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            if (folders.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Text("Folders", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(folders) { folder ->
                                FolderCardItem(folder, documentStore.documentCount(folder)) { onFolderClick(folder) }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All Documents", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            if (filteredDocs.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(if (searchQuery.isNotBlank()) "No results found" else "No documents yet",
                             color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    DocumentCardItem(
                        doc, documentStore.thumbnailFile(doc), isEditing, doc.id in selectedIds,
                        onClick = {
                            if (isEditing) selectedIds = if (doc.id in selectedIds) selectedIds - doc.id else selectedIds + doc.id
                            else onDocumentClick(doc)
                        }
                    )
                }
            }
        }
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                Column {
                    OutlinedTextField(value = newFolderName, onValueChange = { newFolderName = it }, label = { Text("Folder name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Folder.colorOptions) { color ->
                            val c = Folder(name = "", colorName = color).color()
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(c).clickable { newFolderColor = color }, contentAlignment = Alignment.Center) {
                                if (color == newFolderColor) Icon(Icons.Outlined.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { if (newFolderName.isNotBlank()) { documentStore.createFolder(newFolderName, newFolderColor); showNewFolderDialog = false } }) { Text("Create") } },
            dismissButton = { TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") } }
        )
    }

    renameDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { renameDoc = null }, title = { Text("Rename") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { documentStore.rename(doc, renameText); renameDoc = null }) { Text("Rename") } },
            dismissButton = { TextButton(onClick = { renameDoc = null }) { Text("Cancel") } }
        )
    }

    if (showMoveDialog) {
        MoveToFolderDialog(
            folders = folders,
            onSelect = { folderId -> documentStore.moveDocuments(selectedIds, folderId); selectedIds = emptySet(); isEditing = false; showMoveDialog = false },
            onNewFolder = { name, color -> documentStore.createFolder(name, color) },
            onDismiss = { showMoveDialog = false }
        )
    }
}

@Composable
private fun FolderCardItem(folder: Folder, count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(130.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(folder.color().copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Folder, null, tint = folder.color(), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(folder.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$count documents", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun DocumentCardItem(doc: SavedDocument, thumbnailFile: File, isEditing: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box {
                AsyncImage(model = thumbnailFile, contentDescription = doc.name, modifier = Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("${doc.pageCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (isEditing) {
                    Icon(if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(22.dp))
                }
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(doc.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(doc.shortDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}
