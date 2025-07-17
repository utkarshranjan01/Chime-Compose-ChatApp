package com.utkarsh.chatapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.json.JsonNull.content


sealed class BottomNavItem(val title: String, val icon: @Composable (Color) -> Unit,  val selectedIcon: @Composable (Color) -> Unit, val route: String) {
    object Home : BottomNavItem("Home",
        { tint -> Icon(painter = painterResource(id = R.drawable.home), contentDescription = "Home", tint = tint) },
        { tint -> Icon(painter = painterResource(id = R.drawable.home_filled), contentDescription = "Home", tint = tint) },
        "ChatsScreen")

    object Status : BottomNavItem("Status",
        { tint -> Icon(painter = painterResource(id = R.drawable.add_a_photo), contentDescription = "Settings", tint = tint) }, // contentDescription updated
        { tint -> Icon(painter = painterResource(id = R.drawable.add_a_photo_filled), contentDescription = "Settings", tint = tint) }, // contentDescription updated
        "StatusPage")

    object Search : BottomNavItem("Settings",
        { tint -> Icon(painter = painterResource(id = R.drawable.settings), contentDescription = "Settings", tint = tint) }, // contentDescription updated
        { tint -> Icon(painter = painterResource(id = R.drawable.settings_filled), contentDescription = "Settings", tint = tint) }, // contentDescription updated
        "ChangeInfoPage")

//    object Search : BottomNavItem("Search", Icon(painterResource(id = R.drawable.home), Icons.Filled.PersonPin, "ChangeInfoPage")
//    object Profile : BottomNavItem("Profile", Icons.Filled.Home, "profile")
}

object NavigationState {
    var currentRoute by mutableStateOf("ChatsScreen")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomNavigationBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Status,
        BottomNavItem.Search,
//        BottomNavItem.Profile
    )

    NavigationBar {

        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->

            val isSelected = NavigationState.currentRoute == item.route // Use NavigationState

//            val scale by animateFloatAsState(
//                targetValue = if (isSelected) 1.2f else 1f,
//                animationSpec = tween(300)
//            )


            val iconToDisplay: @Composable (Color) -> Unit = { color ->
//                AnimatedContent(targetState = isSelected, label = "icon animation",
//                    transitionSpec = {
//                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
//                    }
//                    ) { selected ->
                    if (isSelected) {
                        item.selectedIcon(color)
                    } else {
                        item.icon(color)
                    }
//                }
            }


            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

            val textColor = if(isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant



            NavigationBarItem(
                icon = {
                    iconToDisplay(textColor)

//                    Icon(
//                        painter = if (isSelected) {
//                            item.selectedIcon(color)
//                        } else {
//                            item.icon(color)
//                        },
//                        contentDescription = item.title,
//                        tint = textColor,
//                        modifier = Modifier.animateContentSize()
////                        modifier = Modifier.scale(scale)  // <-- Scale animation applied here
//                    )
                },

                label = {
//                    AnimatedContent(targetState = isSelected, label = "label crossfade",
//                        ) { selected ->
//                        val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
//                        val textColor = if (selected) MaterialTheme.colorScheme.onSurface
//                        else MaterialTheme.colorScheme.onSurfaceVariant
                        Text(
                            text = item.title,
                            fontWeight = fontWeight,
                            color = textColor,
//                            modifier = Modifier.animateContentSize()
                        )
//                    }
                },
                selected = isSelected,

                onClick = {

                    if(NavigationState.currentRoute != item.route) {
                        NavigationState.currentRoute = item.route
                        if(item.route == "ChatsScreen")
                        {
                            navController.navigate(ChatsScreen) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        else if(item.route == "ChangeInfoPage")
                        {
                            navController.navigate(ChangeInfoPage) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        else if(item.route == "StatusPage")
                        {
                            navController.navigate(StatusPage) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }
    }
}