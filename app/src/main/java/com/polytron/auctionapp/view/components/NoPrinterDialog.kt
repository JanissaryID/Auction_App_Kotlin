package com.polytron.auctionapp.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrintDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun NoPrinterDialog(
    onDismiss: () -> Unit,
    onSaveOnly: () -> Unit,
    onConnectPrinter: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        // Properti ini kunci agar dialog bisa mengikuti lebar yang kita tentukan di Card
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Mengabaikan batasan lebar bawaan sistem
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Mengatur lebar 90% dari lebar layar agar konsisten
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PrintDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Printer Belum Terhubung",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Data siap disimpan. Anda ingin menyimpan saja atau hubungkan printer agar struk bisa dicetak?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tombol aksi dibuat penuh lebar agar seragam
                Button(
                    onClick = onConnectPrinter,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hubungkan Printer", modifier = Modifier.padding(vertical = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSaveOnly,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Saja", modifier = Modifier.padding(vertical = 4.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}