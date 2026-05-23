package com.polytron.auctionapp.view.components.itemcard

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ItemCard(
    item: ItemResponse,
    isSelected: Boolean = false,
    isPrinting: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onPrintClick: (() -> Unit)? = null
) {
    val formattedPrice = remember(item.basePrice) {
        formatRupiah(item.basePrice)
    }

    val borderColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFDFDFD)

    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, shape = shape),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
//                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Row: Nama & Kode
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.nameItem ?: "Tanpa Nama",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Cetak & Harga
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Harga Awal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
                if (onPrintClick != null) {
                    TextButton(
                        onClick = onPrintClick,
                        enabled = !isPrinting,
                        modifier = Modifier.padding(end = 16.dp, bottom = 8.dp)) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Cetak",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPrinting) "Mencetak..." else "Cetak",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}










