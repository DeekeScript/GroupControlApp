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
import top.deeke.groupcontrol.model.Device
import top.deeke.groupcontrol.model.DeviceStatus
import top.deeke.groupcontrol.model.Task
import top.deeke.groupcontrol.ui.theme.*
import top.deeke.groupcontrol.viewmodel.CommandViewModel
import top.deeke.groupcontrol.viewmodel.CommandViewModelFactory

@Composable
fun DeviceScreen() {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val textDisabledColor = if (isDarkTheme) TextDisabled else TextDisabledLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    // 设备列表状态
    val devices = remember { mutableStateListOf<Device>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    
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
                text = "设备管理",
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加设备",
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加设备",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 设备列表或空状态
        if (devices.isEmpty()) {
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
                        imageVector = Icons.Default.Phone,
                        contentDescription = "无设备",
                        modifier = Modifier.size(80.dp),
                        tint = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "暂无设备",
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "点击上方添加设备按钮\n添加您的第一个设备",
                        color = textSecondaryColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonBlue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加设备",
                            tint = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "添加设备",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // 设备列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(devices) { device ->
                    DeviceCard(
                        device = device,
                        onEdit = { 
                            selectedDevice = device
                            showEditDialog = true 
                        },
                        onDelete = { 
                            devices.remove(device)
                        }
                    )
                }
            }
        }
    }
    
    // 添加设备对话框
    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { device ->
                devices.add(device)
                showAddDialog = false
            }
        )
    }
    
    // 编辑设备对话框
    if (showEditDialog && selectedDevice != null) {
        EditDeviceDialog(
            device = selectedDevice!!,
            onDismiss = { 
                showEditDialog = false
                selectedDevice = null
            },
            onConfirm = { updatedDevice ->
                val index = devices.indexOfFirst { it.id == updatedDevice.id }
                if (index != -1) {
                    devices[index] = updatedDevice
                }
                showEditDialog = false
                selectedDevice = null
            }
        )
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val textDisabledColor = if (isDarkTheme) TextDisabled else TextDisabledLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.name,
                    color = textPrimaryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 操作按钮
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = NeonBlue
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = ErrorRed
                        )
                    }
                }
            }
            
            // 设备状态指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (device.status) {
                                    DeviceStatus.ONLINE -> SuccessGreen
                                    DeviceStatus.OFFLINE -> textDisabledColor
                                    DeviceStatus.BUSY -> WarningOrange
                                    DeviceStatus.ERROR -> ErrorRed
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (device.status) {
                            DeviceStatus.ONLINE -> "在线"
                            DeviceStatus.OFFLINE -> "离线"
                            DeviceStatus.BUSY -> "忙碌"
                            DeviceStatus.ERROR -> "错误"
                        },
                        color = when (device.status) {
                            DeviceStatus.ONLINE -> SuccessGreen
                            DeviceStatus.OFFLINE -> textDisabledColor
                            DeviceStatus.BUSY -> WarningOrange
                            DeviceStatus.ERROR -> ErrorRed
                        },
                        fontSize = 12.sp
                    )
                }
            }
            
            if (device.remark.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = device.remark,
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            if (device.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "位置",
                        modifier = Modifier.size(16.dp),
                        tint = textSecondaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = device.location,
                        color = textSecondaryColor,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "设备ID: ",
                color = textDisabledColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (Device) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加设备",
                color = if (isSystemInDarkTheme()) TextPrimary else TextPrimaryLight
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("设备名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("位置") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("设备ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && deviceId.isNotEmpty()) {
                        onConfirm(
                            Device(
                                id = System.currentTimeMillis().toInt(),
                                name = name,
                                remark = remark,
                                location = location,
                                deviceId = deviceId,
                                status = DeviceStatus.OFFLINE
                            )
                        )
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EditDeviceDialog(
    device: Device,
    onDismiss: () -> Unit,
    onConfirm: (Device) -> Unit
) {
    var name by remember { mutableStateOf(device.name) }
    var remark by remember { mutableStateOf(device.remark) }
    var location by remember { mutableStateOf(device.location) }
    var deviceId by remember { mutableStateOf(device.deviceId) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑设备",
                color = if (isSystemInDarkTheme()) TextPrimary else TextPrimaryLight
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("设备名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("位置") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("设备ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && deviceId.isNotEmpty()) {
                        onConfirm(
                            device.copy(
                                name = name,
                                remark = remark,
                                location = location,
                                deviceId = deviceId
                            )
                        )
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun CommandScreen() {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val context = LocalContext.current
    val viewModel: CommandViewModel = viewModel(
        factory = CommandViewModelFactory(context)
    )
    
    // 从ViewModel获取状态
    val commands by viewModel.commands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCommand by remember { mutableStateOf<CommandEntity?>(null) }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importDeekeScriptJson(it, context)
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
            
            Button(
                onClick = { filePickerLauncher.launch("application/json") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "导入指令",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoading) "导入中..." else "导入指令文件",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 错误消息显示
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) ErrorRed else ErrorRed
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "错误",
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
        
        // 指令列表或空状态
        if (commands.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "无指令",
                        modifier = Modifier.size(80.dp),
                        tint = if (isDarkTheme) TextSecondary else TextSecondaryLight
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
                        text = "请点击上方按钮导入deekeScrip.json文件",
                        color = if (isDarkTheme) TextSecondary else TextSecondaryLight,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(commands) { command ->
                    CommandCard(
                        command = command,
                        onEdit = { 
                            selectedCommand = command
                            showEditDialog = true 
                        },
                        onDelete = { 
                            viewModel.deleteCommand(command)
                        }
                    )
                }
            }
        }
    }
    
    // 编辑指令对话框
    if (showEditDialog && selectedCommand != null) {
        EditCommandDialog(
            command = selectedCommand!!,
            onDismiss = { 
                showEditDialog = false
                selectedCommand = null
            },
            onConfirm = { updatedCommand ->
                viewModel.updateCommand(updatedCommand)
                showEditDialog = false
                selectedCommand = null
            }
        )
    }
}

@Composable
fun CommandCard(
    command: CommandEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val textDisabledColor = if (isDarkTheme) TextDisabled else TextDisabledLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = command.name,
                    color = textPrimaryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 操作按钮
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = NeonBlue
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = ErrorRed
                        )
                    }
                }
            }
            
            if (command.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "标题: ${command.title}",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            if (command.jsFile.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "JS文件: ${command.jsFile}",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            if (command.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = command.description,
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EditCommandDialog(
    command: CommandEntity,
    onDismiss: () -> Unit,
    onConfirm: (CommandEntity) -> Unit
) {
    var name by remember { mutableStateOf(command.name) }
    var description by remember { mutableStateOf(command.description) }
    var content by remember { mutableStateOf(command.content) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑指令",
                color = if (isSystemInDarkTheme()) TextPrimary else TextPrimaryLight
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("指令名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("JSON内容") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && content.isNotEmpty()) {
                        onConfirm(
                            command.copy(
                                name = name,
                                description = description,
                                content = content
                            )
                        )
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun TaskScreen() {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    
    // 任务列表状态
    val tasks = remember { mutableStateListOf<Task>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    
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
                text = "任务管理",
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加任务",
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加任务",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 任务列表或空状态
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "无任务",
                        modifier = Modifier.size(80.dp),
                        tint = if (isDarkTheme) TextSecondary else TextSecondaryLight
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "暂无任务",
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "点击上方添加任务按钮\n添加您的第一个任务",
                        color = if (isDarkTheme) TextSecondary else TextSecondaryLight,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onEdit = { 
                            selectedTask = task
                            showEditDialog = true 
                        },
                        onDelete = { 
                            tasks.remove(task)
                        }
                    )
                }
            }
        }
    }
    
    // 添加任务对话框
    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { task ->
                tasks.add(task)
                showAddDialog = false
            }
        )
    }
    
    // 编辑任务对话框
    if (showEditDialog && selectedTask != null) {
        EditTaskDialog(
            task = selectedTask!!,
            onDismiss = { 
                showEditDialog = false
                selectedTask = null
            },
            onConfirm = { updatedTask ->
                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                if (index != -1) {
                    tasks[index] = updatedTask
                }
                showEditDialog = false
                selectedTask = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val textDisabledColor = if (isDarkTheme) TextDisabled else TextDisabledLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    color = textPrimaryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 操作按钮
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = NeonBlue
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = ErrorRed
                        )
                    }
                }
            }
            
            if (task.remark.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.remark,
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "设备数量: ",
                color = textDisabledColor,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "指令数量: ",
                color = textDisabledColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加任务",
                color = if (isSystemInDarkTheme()) TextPrimary else TextPrimaryLight
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("任务名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty()) {
                        onConfirm(
                            Task(
                                id = System.currentTimeMillis().toInt(),
                                name = name,
                                remark = remark
                            )
                        )
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    var name by remember { mutableStateOf(task.name) }
    var remark by remember { mutableStateOf(task.remark) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑任务",
                color = if (isSystemInDarkTheme()) TextPrimary else TextPrimaryLight
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("任务名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty()) {
                        onConfirm(
                            task.copy(
                                name = name,
                                remark = remark
                            )
                        )
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
