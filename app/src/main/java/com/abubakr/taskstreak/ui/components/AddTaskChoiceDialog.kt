package com.abubakr.taskstreak.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddTaskChoiceDialog(
    onDismiss: () -> Unit,
    onCreateNewTask: () -> Unit,
    onBrowseTemplates: () -> Unit
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "إضافة جديدة" else "New Addition",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isArabic)
                        "اختر إنشاء مهمة أو عادة مخصصة، أو استعراض نماذج العادات الجاهزة:"
                    else
                        "Choose to create a custom habit or browse pre-made habit templates:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Create New Task
                ChoiceActionCard(
                    icon = Icons.Default.AddCircleOutline,
                    title = if (isArabic) "إنشاء مهمة جديدة" else "Create New Task",
                    description = if (isArabic) "عادة يومية، مخصصة، أو مهمة لمرة واحدة" else "Custom habit, recurring routine, or one-time task",
                    testTag = "choice_create_new_task",
                    onClick = {
                        onDismiss()
                        onCreateNewTask()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Browse Templates
                ChoiceActionCard(
                    icon = Icons.Default.AutoAwesome,
                    title = if (isArabic) "استعراض النماذج الجاهزة" else "Browse Templates",
                    description = if (isArabic) "عادات مثبتة علمياً وجاهزة للبدء فوراً" else "Scientifically proven routines & starter templates",
                    testTag = "choice_browse_templates",
                    onClick = {
                        onDismiss()
                        onBrowseTemplates()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("choice_cancel_btn")
            ) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun ChoiceActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
