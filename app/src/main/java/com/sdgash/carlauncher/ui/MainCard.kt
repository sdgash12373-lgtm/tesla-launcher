package com.sdgash.carlauncher.ui

import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sdgash.carlauncher.data.AppEntry

/**
 * The big right-hand card. Shows the map placeholder, or a chosen app such as
 * Android Auto. Tapping opens that app over the card's area (when the head unit
 * supports floating windows) or full screen.
 */
@Composable
fun MainCard(
    cardApp: AppEntry?,
    onOpen: (AppEntry, Rect) -> Unit,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var bounds by remember { mutableStateOf(Rect()) }
    Box(
        modifier.onGloballyPositioned { c ->
            val b = c.boundsInWindow()
            bounds = Rect(b.left.toInt(), b.top.toInt(), b.right.toInt(), b.bottom.toInt())
        }
    ) {
        if (cardApp == null) {
            MapPlaceholder(Modifier.fillMaxSize())
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CarColors.Panel)
                    .clickable { onOpen(cardApp, bounds) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(cardApp.icon, contentDescription = cardApp.label, modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(14.dp))
                Text(cardApp.label, color = CarColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text("點一下開啟", color = CarColors.TextSecondary, fontSize = 15.sp)
            }
        }
        Row(
            Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CarColors.PanelRaised.copy(alpha = 0.9f))
                .clickable(onClick = onChoose)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Edit, null, tint = CarColors.TextPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("自訂卡片", color = CarColors.TextPrimary, fontSize = 14.sp)
        }
    }
}

/** Dialog to choose what the big card shows. Projection apps are listed first. */
@Composable
fun CardAppPicker(
    candidates: List<AppEntry>,
    current: AppEntry?,
    floating: Boolean,
    supportsFloating: Boolean,
    onPick: (AppEntry?) -> Unit,
    onFloatingChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .heightIn(max = 600.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CarColors.Panel)
                .padding(16.dp)
        ) {
            Text("地圖卡片要顯示什麼？", color = CarColors.TextPrimary, fontSize = 20.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { onFloatingChange(!floating) }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("在卡片範圍內以浮動視窗開啟", color = CarColors.TextPrimary, fontSize = 15.sp)
                    Text(
                        if (supportsFloating) "車機支援浮動視窗" else "車機未開啟浮動視窗，會以全螢幕開啟",
                        color = CarColors.TextSecondary, fontSize = 12.sp,
                    )
                }
                Switch(
                    checked = floating, onCheckedChange = onFloatingChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = CarColors.Accent),
                )
            }
            Spacer(Modifier.height(6.dp))
            LazyColumn {
                item {
                    PickerRow(selected = current == null, onClick = { onPick(null) }) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Map, null, tint = CarColors.TextPrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("地圖", color = CarColors.TextPrimary, fontSize = 16.sp)
                    }
                }
                items(candidates, key = { it.key + it.user.hashCode() }) { app ->
                    PickerRow(selected = current?.key == app.key, onClick = { onPick(app) }) {
                        Image(app.icon, null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            app.label, color = CarColors.TextPrimary, fontSize = 16.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerRow(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) CarColors.Accent.copy(alpha = 0.25f) else CarColors.Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}
