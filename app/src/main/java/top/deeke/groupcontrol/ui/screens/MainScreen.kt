package top.deeke.groupcontrol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.deeke.groupcontrol.R
import top.deeke.groupcontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    username: String = "",
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    // 从strings.xml中获取app_name
    val appName = context.getString(R.string.app_name)

    // 调整标签页顺序：设备、指令、任务、配置
    val tabs = listOf("设备", "指令", "任务", "配置")

    // 根据主题选择颜色
    val backgroundColor = if (isDarkTheme) TechDark else TechLight
    val surfaceColor = if (isDarkTheme) TechDarkSurface else TechLightSurface
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = appName,
                    color = textPrimaryColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                var expanded by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "用户",
                        tint = if (isDarkTheme) NeonBlue else NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (username.isNotBlank()) username.substring(
                            0,
                            3
                        ) + "****" + username.substring(7, 11) else "未登录",
                        color = textPrimaryColor,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "更多"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("退出") },
                            onClick = {
                                expanded = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = surfaceColor
            )
        )

        // 标签页
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = surfaceColor,
            contentColor = if (isDarkTheme) NeonBlue else NeonCyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) {
                                if (isDarkTheme) NeonBlue else NeonCyan
                            } else {
                                textSecondaryColor
                            }
                        )
                    }
                )
            }
        }

        // 内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(backgroundColor, surfaceColor)
                    )
                )
        ) {
            when (selectedTab) {
                0 -> DeviceScreen() // 设备管理
                1 -> CommandScreen() // 指令管理
                2 -> TaskScreen() // 任务管理
                3 -> ConfigScreen() // 服务器配置
            }
        }
    }
}