package com.polytron.auctionapp.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.itemcard.ItemCardSelectAuction
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemListSelectAuction(
    inventoryViewModel: InventoryViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    navBack: () -> Unit,
) {
    val items by inventoryViewModel.items.collectAsState()
    val selectedItems by inventoryViewModel.selectedItems.collectAsState()

    val filteredItemsStatZero = items.filter { it.status == 0 }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = filteredItemsStatZero.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "Pilih Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                onRefresh = { inventoryViewModel.fetchItems() },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedItems.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Langsung kembali saja, karena data sudah masuk ke VM
                        // saat onSelectToggle dipanggil tadi.
                        navBack()
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("${selectedItems.size} Barang Terpilih") },
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
                label = { Text("Cari Nama atau Kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true
            )

            if (selectedItems.isNotEmpty()) {
                TextButton(
                    onClick = { inventoryViewModel.clearSelectedItems() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Batal Pilih Semua")
                }
            }

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Barang tidak ditemukan")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(filteredItems, key = { it.id ?: "" }) { item ->
                        // Cek status terpilih berdasarkan ID yang ada di VM
                        val isSelected = selectedItems.any { it.id == item.id }

                        ItemCardSelectAuction(
                            item = item,
                            isSelected = isSelected,
                            onSelectToggle = {
                                if (isSelected) {
                                    inventoryViewModel.removeSelectedItem(item)
                                } else {
                                    inventoryViewModel.addSelectedItem(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}