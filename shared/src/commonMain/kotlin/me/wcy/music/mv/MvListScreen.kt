package me.wcy.music.mv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.component.CategoryChip
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.mv.bean.MvItem
import me.wcy.music.mv.viewmodel.MvListViewModel
import me.wcy.music.shared.util.formatPlayCount

@Composable
fun MvListScreen(
    viewModel: MvListViewModel,
    onBack: () -> Unit,
    onOpenMv: (Long) -> Unit
) {
    var selectedArea by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    val mvs by viewModel.mvs.collectAsState()

    LaunchedEffect(selectedArea, selectedType) {
        viewModel.loadMvs(selectedArea, selectedType)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "MV", onBack = onBack)
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(viewModel.areaList.size) { index ->
                    val (label, value) = viewModel.areaList[index]
                    CategoryChip(
                        label = label,
                        selected = value == selectedArea,
                        onClick = { selectedArea = value }
                    )
                }
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(viewModel.typeList.size) { index ->
                    val (label, value) = viewModel.typeList[index]
                    CategoryChip(
                        label = label,
                        selected = value == selectedType,
                        onClick = { selectedType = value }
                    )
                }
            }
        }
        val rows = mvs.chunked(2)
        items(rows.size) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rows[rowIndex].forEach { mv ->
                    MvCell(
                        mv = mv,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenMv(mv.id) }
                    )
                }
                if (rows[rowIndex].size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MvCell(
    mv: MvItem,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box {
            CoverImage(
                url = mv.getSmallCover(),
                cornerRadius = 4.dp,
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = formatPlayCount(mv.playCount),
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
        Text(
            text = mv.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
        Text(
            text = mv.artistName,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
