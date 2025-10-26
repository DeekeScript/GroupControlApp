package top.deeke.groupcontrol.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ApiResponse(
    val code: Int,
    val message: String,
    val data: String? = null
)

class ApiService {
    
    suspend fun checkServerConfig(serverUrl: String, route: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl$route")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode >= 200 && responseCode < 300) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            connection.disconnect()
            
            // 尝试解析JSON响应
            try {
                val json = JSONObject(responseBody)
                ApiResponse(
                    code = json.optInt("code", responseCode),
                    message = json.optString("message", "请求成功"),
                    data = if (json.has("data")) json.getString("data") else null
                )
            } catch (e: Exception) {
                // 如果不是JSON格式，返回原始响应码
                ApiResponse(
                    code = responseCode,
                    message = responseBody.ifEmpty { "请求失败" },
                    data = null
                )
            }
            
        } catch (e: Exception) {
            ApiResponse(
                code = -1,
                message = "网络错误: ${e.message}",
                data = null
            )
        }
    }
    
    suspend fun login(serverUrl: String, route: String, username: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl$route")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            // 构建登录请求体
            val loginData = JSONObject().apply {
                put("username", username)
                put("password", password)
            }
            
            connection.outputStream.use { outputStream ->
                outputStream.write(loginData.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode >= 200 && responseCode < 300) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            connection.disconnect()
            
            // 尝试解析JSON响应
            try {
                val json = JSONObject(responseBody)
                ApiResponse(
                    code = json.optInt("code", responseCode),
                    message = json.optString("message", "登录成功"),
                    data = if (json.has("data")) json.getString("data") else null
                )
            } catch (e: Exception) {
                ApiResponse(
                    code = responseCode,
                    message = responseBody.ifEmpty { "登录失败" },
                    data = null
                )
            }
            
        } catch (e: Exception) {
            ApiResponse(
                code = -1,
                message = "网络错误: ${e.message}",
                data = null
            )
        }
    }
}
