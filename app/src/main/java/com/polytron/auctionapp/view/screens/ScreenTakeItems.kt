package com.polytron.auctionapp.view.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.itemcard.ItemCardPayment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTakeItems(
    itemsViewModel: ItemsViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    navBack: () -> Unit,
) {
    val items by itemsViewModel.items.collectAsState()
    val filteredItemsStatTwo = items.filter { it.status == 2 }

    var searchQuery by remember { mutableStateOf("") }
    val isSubmittingMap = remember { mutableStateMapOf<String, Boolean>() }
    val coroutineScope = rememberCoroutineScope()

    val filteredItems = filteredItemsStatTwo.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.buyer?.contains(searchQuery, ignoreCase = true) == true
    }

    val groupedFilteredItems = filteredItems.groupBy { it.orderID.orEmpty() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "Ambil Barang",
                onBack = { navBack() },
                showRefresh = true,
                onRefresh = { itemsViewModel.fetchItems() },
            )
        },
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true
            )

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Tidak ada nota yang ditemukan", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    groupedFilteredItems.forEach { (orderId, itemList) ->
                        item {
                            ItemCardPayment(
                                orderId = orderId,
                                items = itemList,
                                takeItemScreen = true,
                                isSubmitting = isSubmittingMap[orderId] == true,
                                onClick = {
                                    isSubmittingMap[orderId] = true
                                    coroutineScope.launch {
                                        itemList.forEach { item ->
                                            itemsViewModel.patchItem(item.id!!, item.copy(status = 3))
                                            delay(300)
                                        }
                                        isSubmittingMap[orderId] = false
                                        itemsViewModel.fetchItems()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}