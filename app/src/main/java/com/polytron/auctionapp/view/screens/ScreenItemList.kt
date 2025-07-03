package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.Item
import com.polytron.auctionapp.view.components.AddOrEditItemBottomSheet
import com.polytron.auctionapp.view.components.ItemCard
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemList(
    itemsViewModel: ItemsViewModel = koinInject(),
    onBack: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var showAddEditBottomSheet by remember { mutableStateOf(false) }

    val selectedItems = remember { mutableStateListOf<Item>() }
    val isSelectionMode = selectedItems.isNotEmpty()

    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch data saat pertama kali ditampilkan
    LaunchedEffect(Unit) {
        itemsViewModel.fetchItems()
    }

    // Filter berdasarkan pencarian nama/kode
    val filteredItems = items.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Daftar Barang") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { itemsViewModel.fetchItems() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        if (!isDeleting) {
                            isDeleting = true
                            coroutineScope.launch {
                                selectedItems.forEach {
                                    itemsViewModel.deleteItem(it.id!!)
                                    delay(500)
                                }
                                selectedItems.clear()
                                itemsViewModel.fetchItems()
                                isDeleting = false
                            }
                        }
                    },
                    containerColor = if (isDeleting) Color.Gray else Color.Red,
                    modifier = Modifier.alpha(if (isDeleting) 0.6f else 1f)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = Color.White)
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        showAddEditBottomSheet = true
                        selectedItem = null
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Item")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari berdasarkan nama atau kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                singleLine = true
            )

            if (isSelectionMode) {
                TextButton(
                    onClick = { selectedItems.clear() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Batal Seleksi")
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada item yang ditemukan",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredItems) { item ->
                        ItemCard(
                            item = item,
                            isSelected = selectedItems.contains(item),
                            onClick = {
                                if (isSelectionMode) {
                                    if (selectedItems.contains(item)) {
                                        selectedItems.remove(item)
                                    } else {
                                        selectedItems.add(item)
                                    }
                                } else {
                                    selectedItem = item
                                    showAddEditBottomSheet = true
                                }
                            },
                            onLongClick = {
                                if (!selectedItems.contains(item)) {
                                    selectedItems.add(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddEditBottomSheet = false
                selectedItem = null
            },
            sheetState = sheetState
        ) {
            AddOrEditItemBottomSheet(
                itemToEdit = selectedItem,
                onDismiss = {
                    showAddEditBottomSheet = false
                    selectedItem = null
                },
                onSubmit = { name, code, base, max, quantity ->
                    if (selectedItem != null) {
                        selectedItem!!.id?.let { id ->
                            val updatedItem = selectedItem!!.copy(
                                nameItem = name,
                                codeItem = code,
                                basePrice = base,
                                maxPrice = max
                            )
                            itemsViewModel.patchItem(id, updatedItem)
                        }
                    } else {
                        repeat(quantity) { index ->
                            val suffix = index + 1
                            val finalName = "$name $suffix"
                            val finalCode = "$code $suffix"
                            itemsViewModel.createItem(
                                nameItem = finalName,
                                codeItem = finalCode,
                                basePrice = base,
                                maxPrice = max,
                                orderID = "",
                                admin = "admin"
                            )
                            delay(500)
                        }
                    }
                    itemsViewModel.fetchItems()
                }
            )
        }
    }
}
