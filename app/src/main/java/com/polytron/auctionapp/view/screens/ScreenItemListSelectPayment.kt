package com.polytron.auctionapp.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.itemcard.ItemCardSelectPayment
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemListSelectPayment(
    itemsViewModel: ItemsViewModel = koinInject(),
    navBack: () -> Unit,
) {
    val items by itemsViewModel.items.collectAsState()
    val filteredItemsStatOne = items.filter { it.status == 1 }
    var searchQuery by remember { mutableStateOf("") }

    val selectedItemsState by itemsViewModel.selectedItems.collectAsState()
    val selectedItems = remember { mutableStateListOf<ItemResponse>() }
    var isInitialized by remember { mutableStateOf(false) }
    val isSelectionMode = selectedItems.isNotEmpty()

    LaunchedEffect(Unit) {
        itemsViewModel.fetchItems()
    }

    LaunchedEffect(selectedItemsState) {
        if (!isInitialized) {
            selectedItems.clear()
            selectedItems.addAll(selectedItemsState)
            isInitialized = true
        }
    }

    val filteredItems = filteredItemsStatOne.filter {
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
        floatingActionButton = {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        itemsViewModel.setSelectedItems(selectedItems.toList())
                        navBack() // aksi saat selesai memilih
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Selesai Pilih") },
                    text = { Text("${selectedItems.size} Barang") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
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
                    onClick = {
                        selectedItems.clear()
                        itemsViewModel.clearSelectedItems()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Batal Pilih")
                }
            }

            if (filteredItems.isEmpty()) {
                // Empty state
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)) {
                    items(filteredItems) { item ->
                        ItemCardSelectPayment(
                            item = item,
                            isSelected = selectedItems.contains(item),
                            onSelectToggle = {
                                if (selectedItems.contains(item)) {
                                    selectedItems.remove(item)
                                } else {
                                    selectedItems.add(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}