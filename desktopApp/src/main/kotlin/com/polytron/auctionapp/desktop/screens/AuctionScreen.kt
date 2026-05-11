package com.polytron.auctionapp.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopTable
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.utils.formatRupiah

private val AuctionTableMinWidth = 1280.dp
private val AuctionCodeColumn = 130.dp
private val AuctionNameColumn = 300.dp
private val AuctionBasePriceColumn = 140.dp
private val AuctionMaxPriceColumn = 140.dp
private val AuctionBuyerColumn = 240.dp
private val AuctionPriceColumn = 160.dp
private val AuctionActionsColumn = 60.dp

@Composable
fun AuctionScreen(
    items: List<ItemResponse>,
    selectedItems: List<ItemResponse>,
    editingBuyers: Map<String, String>,
    editingPrices: Map<String, String>,
    onAddItem: () -> Unit,
    onBarcodeEntry: () -> Unit,
    onRemoveItem: (ItemResponse) -> Unit,
    onClearAll: () -> Unit,
    onBuyerChange: (String, String) -> Unit,
    onPriceChange: (String, String) -> Unit,
    onApplyPriceToAll: (String) -> Unit,
    onPrintReceipts: () -> Unit,
    onSubmit: () -> Unit
) {
    var globalPrice by remember { mutableStateOf("") }
    val isReadyToSubmit = selectedItems.isNotEmpty() && selectedItems.all { item ->
        val itemId = item.id ?: ""
        editingBuyers[itemId]?.isNotBlank() == true && 
        editingPrices[itemId]?.isNotBlank() == true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile(
                label = "Barang Tersedia",
                value = items.count { it.status == 0 }.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Barang Dipilih",
                value = selectedItems.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Menunggu Pembayaran",
                value = items.count { it.status == 1 }.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        DesktopToolbar(
            leading = {
                OutlinedButton(
                    onClick = onAddItem,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih Barang")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onBarcodeEntry,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Input Kode")
                }
            },
            actions = {
                Spacer(Modifier.weight(1f))
                if (selectedItems.isNotEmpty()) {
                    OutlinedTextField(
                        value = formatCurrencyInput(globalPrice),
                        onValueChange = { globalPrice = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("Harga semua") },
                        modifier = Modifier.width(180.dp).height(DesktopDimens.ControlHeight),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onApplyPriceToAll(globalPrice) },
                        enabled = globalPrice.isNotBlank(),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Terapkan")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Kosongkan")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onPrintReceipts,
                        enabled = isReadyToSubmit,
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cetak Nota")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = onSubmit,
                    enabled = isReadyToSubmit,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text("Simpan Lelang")
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            DesktopTable(minWidth = AuctionTableMinWidth) {
                AuctionRowHeader()
                if (selectedItems.isEmpty()) {
                    Text(
                        text = "Belum ada barang yang dipilih untuk lelang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    selectedItems.forEach { item ->
                        val itemId = item.id ?: ""
                        AuctionRow(
                            item = item,
                            buyer = editingBuyers[itemId] ?: "",
                            price = formatCurrencyInput(editingPrices[itemId] ?: ""),
                            onBuyerChange = { onBuyerChange(itemId, it) },
                            onPriceChange = { onPriceChange(itemId, it) },
                            onRemove = { onRemoveItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuctionRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Kode", Modifier.width(AuctionCodeColumn))
        HeaderCell("Nama Barang", Modifier.width(AuctionNameColumn))
        HeaderCell("Harga Dasar", Modifier.width(AuctionBasePriceColumn))
        HeaderCell("Harga Maks", Modifier.width(AuctionMaxPriceColumn))
        HeaderCell("Pemenang", Modifier.width(AuctionBuyerColumn))
        HeaderCell("Harga Lelang", Modifier.width(AuctionPriceColumn))
        HeaderCell("Aksi", Modifier.width(AuctionActionsColumn))
    }
}

@Composable
private fun AuctionRow(
    item: ItemResponse,
    buyer: String,
    price: String,
    onBuyerChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.width(AuctionCodeColumn))
        BodyCell(item.nameItem.orEmpty(), Modifier.width(AuctionNameColumn))
        BodyCell(formatRupiah(item.basePrice), Modifier.width(AuctionBasePriceColumn))
        BodyCell(formatRupiah(item.maxPrice), Modifier.width(AuctionMaxPriceColumn))
        
        OutlinedTextField(
            value = buyer,
            onValueChange = onBuyerChange,
            placeholder = { Text("Nama pemenang") },
            modifier = Modifier.width(AuctionBuyerColumn).height(DesktopDimens.ControlHeight),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            placeholder = { Text("Harga") },
            modifier = Modifier.width(AuctionPriceColumn).height(DesktopDimens.ControlHeight),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        
        Box(modifier = Modifier.width(AuctionActionsColumn)) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
