package top.deeke.groupcontrol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.MainActivity
import top.deeke.groupcontrol.R
import top.deeke.groupcontrol.data.DataStoreManager
import top.deeke.groupcontrol.model.ServerConfig
import top.deeke.groupcontrol.network.ApiService

class CommandService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dataStoreManager: DataStoreManager
    private val apiService = ApiService()
    private var currentJob: Job? = null
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "command_service_channel"
        const val ACTION_START_SERVICE = "start_service"
        const val ACTION_STOP_SERVICE = "stop_service"
    }
    
    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> startService()
            ACTION_STOP_SERVICE -> stopService()
        }
        return START_STICKY
    }
    
    private fun startService() {
        startForeground(NOTIFICATION_ID, createNotification())
        
        currentJob?.cancel()
        currentJob = serviceScope.launch {
            dataStoreManager.serverConfig.collect { config ->
                if (config.serverUrl.isNotBlank() && config.sendRoute.isNotBlank()) {
                    startPolling(config)
                }
            }
        }
    }
    
    private fun stopService() {
        currentJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private suspend fun startPolling(config: ServerConfig) {
        while (serviceScope.isActive) {
            try {
                // 获取当前token
                val currentToken = dataStoreManager.authToken.first()
                val response = apiService.checkServerConfig(config.serverUrl, config.sendRoute, currentToken)
                
                // 更新通知显示最新状态
                val statusText = if (response.code == 200) "正常" else "异常 (${response.code})"
                val notification = createNotification("状态: $statusText")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
                
                // 如果返回401或402，停止服务（需要重新登录）
                if (response.code == 401 || response.code == 402) {
                    // 清除无效token
                    dataStoreManager.clearAuthToken()
                    stopService()
                    break
                }
                
            } catch (e: Exception) {
                // 网络错误时也更新通知
                val notification = createNotification("网络错误: ${e.message}")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
            
            // 等待配置的间隔时间
            delay(config.requestFrequency.toLong())
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "远程服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "定时请求服务器配置"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(status: String = "服务运行中"): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("远程服务")
            .setContentText(status)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
