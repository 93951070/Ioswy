package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.artist.bean.HotArtistData
import me.wcy.music.dj.bean.DjRadioData

/** 每日推荐横向歌曲卡：封面 + 歌名 + 歌手 */
@Composable
fun DailySongCard(
    song: SongData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = song.al.getSmallCover(),
            cornerRadius = 10.dp,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = song.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
        Text(
            text = song.ar.firstOrNull()?.name ?: "",
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

/** 热门歌手圆形头像卡 */
@Composable
fun ArtistCircleItem(
    artist: HotArtistData,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = artist.picUrl,
            cornerRadius = 30.dp,
            modifier = Modifier.size(60.dp)
        )
        Text(
            text = artist.name,
            color = AppThemeColor.TextH1,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** 电台精选横滑卡：封面 + 电台名 + 主播 */
@Composable
fun DjRadioCard(
    radio: DjRadioData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = radio.picUrl,
            cornerRadius = 10.dp,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = radio.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
        Text(
            text = radio.dj.nickname,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
