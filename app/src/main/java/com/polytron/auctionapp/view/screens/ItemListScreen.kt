package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.Item
import com.polytron.auctionapp.view.components.AddOrEditItemBottomSheet
import com.polytron.auctionapp.view.components.ItemCard
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    itemsViewModel: ItemsViewModel = koinInject(),
) {
    val items by itemsViewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()
//    var showAddBottomSheet by remember { mutableStateOf(false) }

    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var showAddEditBottomSheet by remember { mutableStateOf(false) }

    // Fetch data saat pertama kali ditampilkan
    LaunchedEffect(Unit) {
        itemsViewModel.fetchItems()
    }

    // Filter list berdasarkan pencarian nama atau kode
    val filteredItems = items.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daftar Barang",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = { itemsViewModel.fetchItems() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

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

            if (filteredItems.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredItems) { item ->
                        ItemCard(item){
                            selectedItem = item
                            showAddEditBottomSheet = true
                        }
                    }
                }
            }
        }
    }

    if (showAddEditBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddEditBottomSheet = false
                selectedItem = null // reset setelah dismiss
            },
            sheetState = sheetState
        ) {
            AddOrEditItemBottomSheet(
                itemToEdit = selectedItem,
                onDismiss = {
                    showAddEditBottomSheet = false
                    selectedItem = null // reset juga setelah tombol batal
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
                            // Tunggu patchItem selesai
                            itemsViewModel.patchItem(id, updatedItem)
                        }
                    } else {
                        // ➕ TAMBAH MODE
                        repeat(quantity) { index ->
                            val suffix = index + 1
                            val finalName = "$name $suffix"
                            val finalCode = "$code $suffix"

                            itemsViewModel.createItem(
                                nameItem = finalName,
                                codeItem = finalCode,
                                basePrice = base,
                                maxPrice = max,
                                time = "10",
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