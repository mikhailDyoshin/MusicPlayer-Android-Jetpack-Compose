package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.musicplayerapp.R


@Composable
fun AddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
            .wrapContentSize()
            .zIndex(2f)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.plus_icon),
            contentDescription = "Plus-icon",
            modifier = Modifier
                .size(60.dp)
                .padding(10.dp),
            tint = Color.Black
        )
    }
}

@Preview
@Composable
fun AddButtonPreview() {
    AddButton(onClick = {})
}