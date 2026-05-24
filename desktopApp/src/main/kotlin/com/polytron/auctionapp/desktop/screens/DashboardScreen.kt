package com.polytron.auctionapp.desktop.screens

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopPanel
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.PaymentMethod
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun DashboardScreen(
    items: List<ItemResponse>,
    isLoggedIn: Boolean,
    userName: String?,
    printerNames: List<String>,
    selectedPrinterName: String?,
    onPrinterSelected: (String) -> Unit,
    onRefreshPrinters: () -> Unit,
    onTestPrinter: () -> Unit
) {
    val totalBase = items.sumOf { it.basePrice?.toLongOrNull() ?: 0L }.toString()
    val totalBaseX3 = (items.sumOf { it.basePrice?.toLongOrNull() ?: 0L } * 3).toString()
    val totalAuctionValue = items.sumOf { it.price?.toLongOrNull() ?: 0L }
    val totalAuction = totalAuctionValue.toString()
    val totalAuctionQris = items.totalAuctionFor(PaymentMethod.QRIS).toString()
    val totalAuctionCash = items.totalAuctionFor(PaymentMethod.Cash).toString()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile("Total barang", items.size.toString(), Modifier.weight(1f))
            MetricTile("Siap lelang", items.count { it.status == 0 }.toString(), Modifier.weight(1f))
            MetricTile("Selesai", items.count { it.status == 3 }.toString(), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile("Nilai dasar", formatRupiah(totalBase), Modifier.weight(1f))
            MetricTile("Nilai lelang", formatRupiah(totalAuction), Modifier.weight(1f))
            MetricTile("Nilai dasar x3", formatRupiah(totalBaseX3), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile("Nominal lelang QRIS", formatRupiah(totalAuctionQris), Modifier.weight(1f))
            MetricTile("Nominal lelang Cash", formatRupiah(totalAuctionCash), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile(
                label = "Status login",
                value = if (isLoggedIn) userName?.takeIf { it.isNotBlank() } ?: "Login" else "Belum login",
                modifier = Modifier.weight(1f)
            )
            PrinterSelectionPanel(
                printerNames = printerNames,
                selectedPrinterName = selectedPrinterName,
                onPrinterSelected = onPrinterSelected,
                onRefreshPrinters = onRefreshPrinters,
                onTestPrinter = onTestPrinter,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

private fun List<ItemResponse>.totalAuctionFor(paymentMethod: PaymentMethod): Long =
    sumOf { item ->
        if (item.typePayment?.trim()?.equals(paymentMethod.label, ignoreCase = true) == true) {
            item.price?.toLongOrNull() ?: 0L
        } else {
            0L
        }
    }

@Composable
private fun PrinterSelectionPanel(
    printerNames: List<String>,
    selectedPrinterName: String?,
    onPrinterSelected: (String) -> Unit,
    onRefreshPrinters: () -> Unit,
    onTestPrinter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    DesktopPanel(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Printer thermal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedPrinterName ?: "Belum ada printer dipilih",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onRefreshPrinters,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Refresh")
            }

            Button(
                onClick = onTestPrinter,
                enabled = selectedPrinterName != null,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tes")
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = printerNames.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesktopDimens.ControlHeight),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = selectedPrinterName ?: "Pilih printer",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                printerNames.forEach { printerName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = printerName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(360.dp)
                            )
                        },
                        onClick = {
                            onPrinterSelected(printerName)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (printerNames.isEmpty()) {
            Text(
                text = "Tidak ada printer Windows atau port COM Bluetooth yang terdeteksi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
