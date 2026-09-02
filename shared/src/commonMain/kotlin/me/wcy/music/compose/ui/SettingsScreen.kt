package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.theme.AppThemeColor

data class SettingChoice(val label: String, val value: String)

data class SettingItem(
    val key: String,
    val category: String,
    val title: String,
    val dialogTitle: String,
    val value: String,
    val options: List<SettingChoice>
)

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun SettingsScreen(
    items: List<SettingItem>,
    onItemChange: (key: String, value: String) -> Unit,
    onOpenSoundEffect: () -> Unit,
    onBack: () -> Unit
) {
    var editing by remember { mutableStateOf<SettingItem?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "设置", onBack = onBack)
        }
        items(items.size) { index ->
            val item = items[index]
            if (index == 0 || items[index - 1].category != item.category) {
                Text(
                    text = item.category,
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                )
            }
            SettingRow(
                item = item,
                onClick = {
                    if (item.options.isEmpty()) {
                        onOpenSoundEffect()
                    } else {
                        editing = item
                    }
                }
            )
        }
    }

    editing?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { editing = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = item.dialogTitle,
                    color = AppThemeColor.TextH1,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
                item.options.forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemChange(item.key, choice.value)
                                editing = null
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = choice.label,
                            color = if (choice.value == item.value) {
                                AppThemeColor.ThemeColor
                            } else {
                                AppThemeColor.TextH1
                            },
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (choice.value == item.value) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "已选",
                                tint = AppThemeColor.ThemeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    item: SettingItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = item.title,
            color = AppThemeColor.TextH1,
            fontSize = 15.sp
        )
        if (item.value.isNotBlank()) {
            Text(
                text = item.value,
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
