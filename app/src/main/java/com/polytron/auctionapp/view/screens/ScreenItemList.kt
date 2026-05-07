package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.toSharedItem
import com.polytron.auctionapp.shared.viewmodel.ItemsSharedViewModel
import com.polytron.auctionapp.ui.viewmodel.AuthViewModel
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.ui.viewmodel.PrinterViewModel
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.AddOrEditItemBottomSheet
import com.polytron.auctionapp.view.components.dialog.PrinterListDialog
import com.polytron.auctionapp.view.components.fab.FabWithDelete
import com.polytron.auctionapp.view.components.itemcard.ItemCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemList(
    itemsViewModel: ItemsViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    authViewModel: AuthViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    printerViewModel: PrinterViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    bluetoothHelper: BluetoothHelper,
    navBack: () -> Unit
) {
    val context = LocalContext.current
    val idUser by authViewModel.idUser.collectAsState()
    val items by itemsViewModel.items.collectAsState()
    val printerDevice by printerViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by printerViewModel.showBluetoothDevice.collectAsState()

    // Inject shared ViewModel as singleton
    val sharedVm: ItemsSharedViewModel = org.koin.compose.koinInject()
    val sharedState by sharedVm.state.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
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

    // Refresh items on first load
    LaunchedEffect(Unit) {
        sharedVm.refreshItems()
    }

    val filteredItems = items.filter { item ->
        sharedState.filteredItems.any { it.id == item.id }
    }
    val groupedItems = sharedState.groupedByName
        .mapValues { (_, sharedItems) ->
            filteredItems.filter { item -> sharedItems.any { it.id == item.id } }
        }
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "${items.size} Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                onRefresh = {
                    sharedVm.refreshItems()
                },
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
                            Text("Total Maksimal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(formatRupiah(totalMax), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
                        itemsViewModel.deleteItem(it.id!!)
                        delay(500)
                    }
                    selectedItems.clear()
                    itemsViewModel.fetchItems()
                    isDeleting = false
                },
                onAddClick = {
                    showAddEditBottomSheet = true
                    selectedItem = null
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = sharedState.searchQuery,
                onValueChange = { sharedVm.updateSearchQuery(it) },
                label = { Text("Cari berdasarkan nama atau kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true
            )
            if (isSelectionMode) {
                TextButton(onClick = { selectedItems.clear() }, modifier = Modifier.align(Alignment.End)) {
                    Text("Batal Seleksi")
                }
            }
            if (filteredItems.isEmpty()) {
                EmptyItemState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    groupedItems.toSortedMap().forEach { (groupName, groupedItemList) ->
                        item(key = groupName) {
                            val isExpanded = expandedGroups[groupName] ?: false
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = groupName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${groupedItemList.size} item",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { expandedGroups[groupName] = !isExpanded }) {
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
                                        groupedItemList.forEach { item ->
                                            ItemCard(
                                                item = item,
                                                isSelected = selectedItems.contains(item),
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        if (selectedItems.contains(item)) selectedItems.remove(item) else selectedItems.add(item)
                                                    } else {
                                                        selectedItem = item
                                                        showAddEditBottomSheet = true
                                                    }
                                                },
                                                onLongClick = { if (!selectedItems.contains(item)) selectedItems.add(item) },
                                                onPrintClick = {
                                                    val device = printerDevice
                                                    if (device != null) {
                                                        BluetoothPrinter().printBarcodeLabel(device = device, itemName = item.nameItem.orEmpty(), itemCode = item.codeItem.orEmpty())
                                                    } else {
                                                        Toast.makeText(context, "Belum ada printer yang terhubung", Toast.LENGTH_SHORT).show()
                                                        bluetoothHelper.requestBluetooth(
                                                            onReady = { printerViewModel.showBluetoothDevice(true) },
                                                            onFailure = { Toast.makeText(context, "Bluetooth gagal: $it", Toast.LENGTH_SHORT).show() }
                                                        )
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
            }
        }
    }

    if (showAddEditBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showAddEditBottomSheet = false }, sheetState = sheetState) {
            AddOrEditItemBottomSheet(
                itemToEdit = selectedItem,
                onDismiss = { showAddEditBottomSheet = false },
                onSubmit = { name, code, base, max, quantity ->
                    if (selectedItem != null) {
                        selectedItem!!.id?.let { id ->
                            itemsViewModel.patchItem(id, selectedItem!!.copy(nameItem = name, codeItem = code, basePrice = base, maxPrice = max))
                        }
                    } else {
                        repeat(quantity) { index ->
                            val suffix = index + 1
                            itemsViewModel.createItem(ItemResponse(nameItem = "$name $suffix", codeItem = "$code $suffix", basePrice = base, maxPrice = max, admin = "admin", user = idUser))
                            delay(500)
                        }
                    }
                    itemsViewModel.fetchItems()
                }
            )
        }
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device -> printerViewModel.setSelectedPrinter(device); printerViewModel.showBluetoothDevice(false) },
            onDismiss = { printerViewModel.showBluetoothDevice(false) }
        )
    }
}
