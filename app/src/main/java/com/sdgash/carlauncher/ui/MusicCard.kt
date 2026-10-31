package com.sdgash.carlauncher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdgash.carlauncher.data.MediaControls
import com.sdgash.carlauncher.data.NowPlaying

/** Now-playing card with album art and previous / play-pause / next. */
@Composable
fun MusicCard(
    nowPlaying: NowPlaying?,
    hasAccess: Boolean,
    media: MediaControls,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CarColors.PanelRaised)
            .padding(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { if (hasAccess) media.openSource() else media.requestAccess() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(CarColors.Panel),
                contentAlignment = Alignment.Center,
            ) {
                val art = nowPlaying?.art
                if (art != null) {
                    Image(art, null, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp))
                } else {
                    Icon(Icons.Filled.MusicNote, null, tint = CarColors.TextSecondary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                val (title, subtitle) = when {
                    !hasAccess -> "點此允許讀取音樂資訊" to "需要開啟「通知存取權」"
                    nowPlaying == null -> "目前沒有播放音樂" to "開啟音樂 App 開始播放"
                    else -> nowPlaying.title.ifBlank { "未知曲目" } to
                        listOf(nowPlaying.artist, nowPlaying.sourceLabel).filter { it.isNotBlank() }.joinToString(" · ")
                }
                Text(
                    title, color = CarColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle, color = CarColors.TextSecondary, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (hasAccess) {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val enabled = nowPlaying != null
                ControlButton(Icons.Filled.SkipPrevious, "上一首", 40.dp, enabled, media::previous)
                ControlButton(
                    if (nowPlaying?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "播放/暫停", 48.dp, enabled, media::playPause, filled = true,
                )
                ControlButton(Icons.Filled.SkipNext, "下一首", 40.dp, enabled, media::next)
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    size: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
    filled: Boolean = false,
) {
    val tint = if (enabled) CarColors.TextPrimary else CarColors.TextSecondary.copy(alpha = 0.4f)
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (filled) CarColors.Panel else CarColors.PanelRaised)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(size * 0.6f))
    }
}
