package com.grid.pos.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel

@Composable
fun UISwitch(
        modifier: Modifier = Modifier,
        text: String = "Checked",
        textColor: Color = SettingsModel.textColor,
        enabled: Boolean = true,
        checked: Boolean = false,
        onCheckedChange: ((Boolean) -> Unit)?
) {
    Row(
        modifier = modifier.alpha(if (enabled) 1f else .5f),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            style = TextStyle(
                textDecoration = TextDecoration.None,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = textColor
        )

        Switch(modifier = Modifier
            .wrapContentWidth()
            .height(60.dp)
            .wrapContentHeight(align = Alignment.CenterVertically),
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            })
    }
}