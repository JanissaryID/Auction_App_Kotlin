package com.polytron.auctionapp.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.utils.exportItemsToExcel
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.ItemDetailBottomSheet
import com.polytron.auctionapp.view.components.itemcard.ItemCardTransaction
import org.koin.compose.viewmodel.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTransactions(
    itemsViewModel: ItemsViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    navBack: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    val isLoading by itemsViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<ItemResponse?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalBase = items.sumOf { it.basePrice?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L }.toString()
    val totalPrice = items.sumOf { it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L }.toString()

    val filteredItems = items.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }
    val groupedTransactions = filteredItems.groupBy { it.orderID.orEmpty().ifBlank { "Tanpa Order ID" } }
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "Daftar Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                isRefreshing = isLoading,
                onRefresh = { itemsViewModel.fetchItems() },
            )
        },
        bottomBar = {
            if (filteredItems.isNotEmpty()) {
                BottomAppBar(tonalElevation = 4.dp, containerColor = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Awal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(formatRupiah(totalBase), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Lelang", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(formatRupiah(totalPrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { exportItemsToExcel(context = context, items = items) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Download, "Convert to Excel")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari berdasarkan nama atau kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true
            )
            if (filteredItems.isEmpty()) {
                EmptyItemState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    groupedTransactions.toSortedMap().forEach { (orderId, transactionItems) ->
                        item(key = orderId) {
                            val isExpanded = expandedGroups[orderId] ?: false
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = orderId,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${transactionItems.size} transaksi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { expandedGroups[orderId] = !isExpanded }) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                        Text(if (isExpanded) "Tutup" else "Detail")
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        transactionItems.forEach { item ->
                                            ItemCardTransaction(
                                                item = item,
                                                isSelected = false,
                                                onClicked = { selectedItem = item; showDetailSheet = true }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDetailSheet && selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = sheetState
        ) {
            ItemDetailBottomSheet(
                item = selectedItem!!,
                onDismissRequest = { showDetailSheet = false }
            )
        }
    }
}
