package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.AddOrEditItemBottomSheet
import com.polytron.auctionapp.view.components.fab.FabWithDelete
import com.polytron.auctionapp.view.components.itemcard.ItemCard
import com.polytron.auctionapp.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemList(
    mainViewModel: MainViewModel = koinInject(),
    navBack: () -> Unit
) {
    val idUser by mainViewModel.idUser.collectAsState()
    val items by mainViewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()
    var selectedItem by remember { mutableStateOf<ItemResponse?>(null) }
    var showAddEditBottomSheet by remember { mutableStateOf(false) }

    val selectedItems = remember { mutableStateListOf<ItemResponse>() }
    val isSelectionMode = selectedItems.isNotEmpty()

    var isDeleting by remember { mutableStateOf(false) }

    val totalBase = items.sumOf {
        it.basePrice?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
    }.toString()
    val totalMax = items.sumOf {
        it.maxPrice?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
    }.toString()

    // Fetch data saat pertama kali ditampilkan
//    LaunchedEffect(Unit) {
//        mainViewModel.fetchItems()
//    }

    // Filter berdasarkan pencarian nama/kode
    val filteredItems = items.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "${items.size} Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                onRefresh = { mainViewModel.fetchItems() },
            )
        },
        bottomBar = {
            if (filteredItems.isNotEmpty()) {
                BottomAppBar(
                    tonalElevation = 4.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Awal", style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Text(
                                formatRupiah(totalBase),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Maksimal", style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Text(
                                formatRupiah(totalMax),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FabWithDelete(
                isSelectionMode = isSelectionMode,
                isDeleting = isDeleting,
                onDelete = {
                    isDeleting = true
                    selectedItems.forEach {
                        mainViewModel.deleteItem(it.id!!)
                        delay(500)
                    }
                    selectedItems.clear()
                    mainViewModel.fetchItems()
                    isDeleting = false
                },
                onAddClick = {
                    showAddEditBottomSheet = true
                    selectedItem = null
                }
            )
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
                EmptyItemState()
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
                            },
                            onPrintClick = {
//                                printerViewModel.printItem(item)
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
                            mainViewModel.patchItem(id, updatedItem)
                        }
                    } else {
                        repeat(quantity) { index ->
                            val suffix = index + 1
                            val finalName = "$name $suffix"
                            val finalCode = "$code $suffix"
                            val item = ItemResponse(
                                nameItem = finalName,
                                codeItem = finalCode,
                                basePrice = base,
                                maxPrice = max,
                                admin = "admin",
                                user = idUser
                            )
                            mainViewModel.createItem(
                                item = item
                            )
                            delay(500)
                        }
                    }
                    mainViewModel.fetchItems()
                }
            )
        }
    }
}
