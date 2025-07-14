package com.polytron.auctionapp.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.utils.exportItemsToExcel
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.ItemDetailBottomSheet
import com.polytron.auctionapp.view.components.itemcard.ItemCardTransaction
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTransactions(
    itemsViewModel: ItemsViewModel = koinInject(),
    navBack: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var selectedItem by remember { mutableStateOf<ItemResponse?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    val selectedItems = remember { mutableStateListOf<ItemResponse>() }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalBase = items.sumOf {
        it.basePrice?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
    }.toString()
    val totalPrice = items.sumOf {
        it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
    }.toString()

    val context = LocalContext.current

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
            TopAppBarCustom(
                title = "Daftar Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                onRefresh = { itemsViewModel.fetchItems() },
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
                            Text("Total Lelang", style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Text(
                                formatRupiah(totalPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Download To Excel
                    exportItemsToExcel(context = context, items = items)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Download, "Convert to Excel")
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
                        ItemCardTransaction(
                            item = item,
                            isSelected = selectedItems.contains(item),
                            onClicked = {
                                selectedItem = item // ✅ Atur selectedItem sebelum buka sheet
                                showDetailSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDetailSheet && selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showDetailSheet = false
                selectedItem = null
            },
            sheetState = sheetState
        ) {
            ItemDetailBottomSheet(
                item = selectedItem!!,
                onDismissRequest = {
                    showDetailSheet = false
                    selectedItem = null
                }
            )
        }
    }
}