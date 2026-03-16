package com.example.housify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.housify.BottomNavItem
import com.example.housify.bottomNavItems
import com.example.housify.ui.theme.HousifyTheme


@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    onSelected: (route: Any) -> Unit,
    currentRoute: String
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        items.forEach { item ->
            val selected = item.isSelected(currentRoute)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelected(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(25.dp),
                        tint = if (selected) MaterialTheme.colorScheme.inverseOnSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) MaterialTheme.colorScheme.inverseOnSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun BottomNavBarPreview() {
    HousifyTheme(dynamicColor = false) {
        BottomNavBar(
            items = bottomNavItems,
            onSelected = {},
            currentRoute = bottomNavItems[0].route::class.qualifiedName!!
        )
    }
}