package com.vfstr.smartclass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Data Table with horizontal sticky columns and vertical sticky header.
 * Rule 15 compliant.
 */
@Composable
fun StickyDataTable(
    headers: List<String>,
    rows: List<List<String>>,
    columnWidths: List<Dp>,
    stickyColumns: Int = 2,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 52.dp
) {
    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0A0F1E))) {
        // Sticky Header Row (Vertically sticky by being outside LazyColumn)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111827))
                .border(width = 1.dp, color = Color(0x14FFFFFF))
        ) {
            // Sticky Headers
            for (i in 0 until stickyColumns) {
                TableCell(
                    text = headers[i],
                    width = columnWidths[i],
                    height = rowHeight,
                    isHeader = true,
                    isSticky = true
                )
            }
            // Scrollable Headers
            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                for (i in stickyColumns until headers.size) {
                    TableCell(
                        text = headers[i],
                        width = columnWidths[i],
                        height = rowHeight,
                        isHeader = true,
                        isSticky = false
                    )
                }
            }
        }

        // Data Rows
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(rows) { row ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Sticky Columns
                        for (i in 0 until stickyColumns) {
                            TableCell(
                                text = row.getOrNull(i) ?: "",
                                width = columnWidths[i],
                                height = rowHeight,
                                isHeader = false,
                                isSticky = true
                            )
                        }
                        // Scrollable Columns
                        Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                            for (i in stickyColumns until row.size) {
                                TableCell(
                                    text = row[i],
                                    width = columnWidths[i],
                                    height = rowHeight,
                                    isHeader = false,
                                    isSticky = false
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0x0AFFFFFF), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    width: Dp,
    height: Dp,
    isHeader: Boolean,
    isSticky: Boolean
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(if (isSticky) Color(0xFF111827) else Color.Transparent)
            .border(width = 0.5.dp, color = if (isSticky) Color(0x0DFFFFFF) else Color.Transparent)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isHeader) Color(0xFF00D4FF) else if (isSticky) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isHeader || isSticky) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Start,
            maxLines = 1
        )
    }
}
