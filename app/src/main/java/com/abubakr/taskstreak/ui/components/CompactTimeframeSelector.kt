package com.abubakr.taskstreak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.util.TimeframeFilter

@Composable
fun CompactTimeframeSelector(
    selectedTimeframe: TimeframeFilter,
    counts: Map<TimeframeFilter, Int>,
    onSelectTimeframe: (TimeframeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    var expanded by remember { mutableStateOf(false) }

    val currentCount = counts[selectedTimeframe] ?: 0
    val isOverdueSelected = selectedTimeframe == TimeframeFilter.OVERDUE

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isOverdueSelected && currentCount > 0)
                DangerRed.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isOverdueSelected && currentCount > 0) DangerRed.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .testTag("compact_timeframe_selector_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (selectedTimeframe) {
                    TimeframeFilter.OVERDUE -> Icons.Default.ErrorOutline
                    TimeframeFilter.TODAY -> Icons.Default.Today
                    TimeframeFilter.TOMORROW -> Icons.Default.CalendarToday
                    TimeframeFilter.THIS_WEEK -> Icons.Default.DateRange
                    TimeframeFilter.THIS_MONTH -> Icons.Default.CalendarMonth
                }
                val iconTint = if (isOverdueSelected && currentCount > 0) DangerRed else MaterialTheme.colorScheme.primary

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${if (isArabic) "المهام:" else "Tasks:"} ${selectedTimeframe.getDisplayName(isArabic)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isOverdueSelected && currentCount > 0) DangerRed else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Count Badge Pill
                Surface(
                    shape = CircleShape,
                    color = if (isOverdueSelected && currentCount > 0) DangerRed else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$currentCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .testTag("timeframe_dropdown_menu")
        ) {
            TimeframeFilter.values().forEach { timeframe ->
                val count = counts[timeframe] ?: 0
                val isSelected = timeframe == selectedTimeframe
                val isOverdue = timeframe == TimeframeFilter.OVERDUE

                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val itemIcon = when (timeframe) {
                                    TimeframeFilter.OVERDUE -> Icons.Default.ErrorOutline
                                    TimeframeFilter.TODAY -> Icons.Default.Today
                                    TimeframeFilter.TOMORROW -> Icons.Default.CalendarToday
                                    TimeframeFilter.THIS_WEEK -> Icons.Default.DateRange
                                    TimeframeFilter.THIS_MONTH -> Icons.Default.CalendarMonth
                                }
                                val tint = if (isOverdue && count > 0) DangerRed
                                else if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant

                                Icon(
                                    imageVector = itemIcon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = timeframe.getDisplayName(isArabic),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isOverdue && count > 0) DangerRed
                                    else if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Badge count for this option
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOverdue && count > 0) DangerRed
                                else if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isOverdue && count > 0 || isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelectTimeframe(timeframe)
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    modifier = Modifier.testTag("timeframe_item_${timeframe.name}")
                )
            }
        }
    }
}
