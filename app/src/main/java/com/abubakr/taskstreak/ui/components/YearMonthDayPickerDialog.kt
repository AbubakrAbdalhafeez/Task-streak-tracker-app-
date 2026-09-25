package com.abubakr.taskstreak.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import java.util.Locale

enum class DatePickerStep {
    YEAR,
    MONTH,
    DAY
}

@Composable
fun YearMonthDayPickerDialog(
    title: String,
    initialDate: String? = null,
    allowClear: Boolean = false,
    onDateSelected: (String) -> Unit,
    onClearDate: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    // Parse initial date or default to current date
    val todayCal = Calendar.getInstance()
    val initialCal = remember(initialDate) {
        val cal = Calendar.getInstance()
        if (!initialDate.isNullOrBlank()) {
            val parts = initialDate.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
            val m = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
            val d = parts.getOrNull(2)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
            cal.set(y, m, d)
        }
        cal
    }

    var selectedYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH) + 1) } // 1..12
    var selectedDay by remember { mutableIntStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }
    var currentStep by remember { mutableStateOf(DatePickerStep.YEAR) }

    val monthNamesAr = remember {
        listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
    }

    val monthNamesEn = remember {
        listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
    }

    val daysOfWeekAr = remember { listOf("أحد", "إثن", "ثلا", "أرب", "خمي", "جمع", "سبت") }
    val daysOfWeekEn = remember { listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Title and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentStep != DatePickerStep.YEAR) {
                            IconButton(
                                onClick = {
                                    currentStep = when (currentStep) {
                                        DatePickerStep.DAY -> DatePickerStep.MONTH
                                        DatePickerStep.MONTH -> DatePickerStep.YEAR
                                        else -> DatePickerStep.YEAR
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stepped Breadcrumbs (Year -> Month -> Day)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Step 1: Year
                    StepBreadcrumb(
                        label = selectedYear.toString(),
                        stepName = if (isArabic) "السنة" else "Year",
                        isActive = currentStep == DatePickerStep.YEAR,
                        onClick = { currentStep = DatePickerStep.YEAR },
                        modifier = Modifier.weight(1f)
                    )

                    // Step 2: Month
                    val monthLabel = if (isArabic) monthNamesAr.getOrElse(selectedMonth - 1) { "" }
                    else monthNamesEn.getOrElse(selectedMonth - 1) { "" }
                    StepBreadcrumb(
                        label = monthLabel,
                        stepName = if (isArabic) "الشهر" else "Month",
                        isActive = currentStep == DatePickerStep.MONTH,
                        onClick = { currentStep = DatePickerStep.MONTH },
                        modifier = Modifier.weight(1f)
                    )

                    // Step 3: Day
                    StepBreadcrumb(
                        label = selectedDay.toString(),
                        stepName = if (isArabic) "اليوم" else "Day",
                        isActive = currentStep == DatePickerStep.DAY,
                        onClick = { currentStep = DatePickerStep.DAY },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content for Current Step with smooth transition
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "DatePickerStepAnimation"
                ) { step ->
                    when (step) {
                        DatePickerStep.YEAR -> {
                            YearSelectionGrid(
                                selectedYear = selectedYear,
                                onYearSelected = { y ->
                                    selectedYear = y
                                    currentStep = DatePickerStep.MONTH
                                },
                                isArabic = isArabic
                            )
                        }

                        DatePickerStep.MONTH -> {
                            MonthSelectionGrid(
                                selectedMonth = selectedMonth,
                                monthNames = if (isArabic) monthNamesAr else monthNamesEn,
                                onMonthSelected = { m ->
                                    selectedMonth = m
                                    currentStep = DatePickerStep.DAY
                                }
                            )
                        }

                        DatePickerStep.DAY -> {
                            DaySelectionGrid(
                                year = selectedYear,
                                month = selectedMonth,
                                selectedDay = selectedDay,
                                daysOfWeek = if (isArabic) daysOfWeekAr else daysOfWeekEn,
                                todayYear = todayCal.get(Calendar.YEAR),
                                todayMonth = todayCal.get(Calendar.MONTH) + 1,
                                todayDay = todayCal.get(Calendar.DAY_OF_MONTH),
                                onDaySelected = { d ->
                                    selectedDay = d
                                    val formatted = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                                    onDateSelected(formatted)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Today Shortcut button
                    OutlinedButton(
                        onClick = {
                            val y = todayCal.get(Calendar.YEAR)
                            val m = todayCal.get(Calendar.MONTH) + 1
                            val d = todayCal.get(Calendar.DAY_OF_MONTH)
                            val formatted = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
                            onDateSelected(formatted)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isArabic) "اليوم" else "Today", fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (allowClear && onClearDate != null) {
                            TextButton(
                                onClick = {
                                    onClearDate()
                                    onDismiss()
                                }
                            ) {
                                Text(
                                    if (isArabic) "بدون تاريخ نهاية" else "No End Date",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (currentStep == DatePickerStep.DAY) {
                            Button(
                                onClick = {
                                    val formatted = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                                    onDateSelected(formatted)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(if (isArabic) "تأكيد" else "Confirm", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBreadcrumb(
    label: String,
    stepName: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stepName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun YearSelectionGrid(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    isArabic: Boolean
) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val years = remember(currentYear) {
        ((currentYear - 4)..(currentYear + 10)).toList()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isArabic) "1. اختر السنة:" else "1. Select Year:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(years) { year ->
                val isSelected = year == selectedYear
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onYearSelected(year) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = year.toString(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelectionGrid(
    selectedMonth: Int,
    monthNames: List<String>,
    onMonthSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "2. " + if (monthNames.firstOrNull() == "يناير") "اختر الشهر:" else "Select Month:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(12) { index ->
                val month = index + 1
                val isSelected = month == selectedMonth
                val name = monthNames.getOrElse(index) { "$month" }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMonthSelected(month) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySelectionGrid(
    year: Int,
    month: Int,
    selectedDay: Int,
    daysOfWeek: List<String>,
    todayYear: Int,
    todayMonth: Int,
    todayDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val cal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    // Calendar.DAY_OF_WEEK: 1 is Sunday, 2 is Monday, ... 7 is Saturday
    val firstDayOffset = remember(cal) { cal.get(Calendar.DAY_OF_WEEK) - 1 }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week days header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days Grid
        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (colIndex in 0..6) {
                        val cellIndex = rowIndex * 7 + colIndex
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = dayNumber == selectedDay
                            val isToday = year == todayYear && month == todayMonth && dayNumber == todayDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDaySelected(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        } else {
                            // Blank slot
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
