package top.deeke.groupcontrol.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.deeke.groupcontrol.database.entity.CommandEntity
import top.deeke.groupcontrol.model.Task
import top.deeke.groupcontrol.ui.theme.*
import top.deeke.groupcontrol.viewmodel.CommandViewModel
import top.deeke.groupcontrol.viewmodel.CommandViewModelFactory

@Composable
fun CommandScreen() {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    val context = LocalContext.current
    val commandViewModel: CommandViewModel = viewModel(factory = CommandViewModelFactory(context))
    val commands by commandViewModel.commands.collectAsState()
    val isLoading by commandViewModel.isLoading.collectAsState()
    val errorMessage by commandViewModel.errorMessage.collectAsState()
    
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            commandViewModel.importDeekeScriptJson(it, context)
        }
    }
    
    // 清除错误信息
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            commandViewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部操作栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "指令管理",
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 清除所有指令按钮
                if (commands.isNotEmpty()) {
                    Button(
                        onClick = {
                            showClearConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "清空",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
                
                // 导入指令按钮
                Button(
                    onClick = { filePickerLauncher.launch("application/json") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonBlue
                    ),
                    shape = RoundedCornerShape(6.dp),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "导入指令",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLoading) "导入中..." else "导入指令",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 错误信息显示
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "错误",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // 指令列表或空状态
        if (commands.isEmpty()) {
            // 空状态提示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "无指令",
                        modifier = Modifier.size(80.dp),
                        tint = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "暂无指令",
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "请点击上方按钮导入deekeScript.json文件",
                        color = textSecondaryColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // 指令列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(commands) { command ->
                    CommandCard(command = command)
                }
            }
        }
    }
    
    // 清空确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "确认清空",
                    color = textPrimaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "确定要清空所有指令吗？此操作不可撤销。",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        commandViewModel.deleteAllCommands()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("确认清空", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消", color = textSecondaryColor)
                }
            }
        )
    }
}

@Composable
fun CommandCard(command: CommandEntity) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = command.name,
                color = textPrimaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (command.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "功能: ${command.title}",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            if (command.jsFile.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "指令: ${command.jsFile}",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            if (command.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = command.description,
                    color = textSecondaryColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}