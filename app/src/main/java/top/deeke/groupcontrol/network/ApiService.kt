package top.deeke.groupcontrol.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ApiResponse(
    val code: Int,
    val message: String,
    val data: Any? = null
)

class ApiService {

    suspend fun checkServerConfig(
        serverUrl: String,
        route: String,
        token: String? = null
    ): ApiResponse =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$serverUrl$route")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                // 添加token到header
                Log.d("debug", "token=$token")
                token?.let {
                    connection.setRequestProperty("token", it)
                }
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
                        code = json.optInt("code", json.getInt("code")),
                        message = json.getString("msg"),
                        data = if (json.has("data")) json.get("data") else null
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

    suspend fun login(
        serverUrl: String,
        route: String,
        username: String,
        password: String
    ): ApiResponse = withContext(Dispatchers.IO) {
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
                put("mobile", username)
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

            Log.d("debug", "登录接口返回：$responseBody：$responseCode");

            connection.disconnect()

            // 尝试解析JSON响应
            try {
                val json = JSONObject(responseBody)
                ApiResponse(
                    code = json.optInt("code", json.getInt("code")),
                    message = json.optString("message", json.getString("msg")),
                    data = if (json.has("data")) json.get("data") else null
                )
            } catch (e: Exception) {
                Log.d("debug", "异常：${e.message}")
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

    suspend fun sendTaskToServer(
        serverUrl: String,
        route: String,
        token: String,
        taskData: Map<String, Any>
    ): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl$route")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("token", token)
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // 构建任务数据请求体
            val jsonData = JSONObject().apply {
                put("taskId", taskData["taskId"])
                put("taskName", taskData["taskName"])
                put("commandId", taskData["commandId"])
                put("deviceIds", taskData["deviceIds"])
                put("durationHours", taskData["durationHours"])
                put("durationMinutes", taskData["durationMinutes"])
                put("remark", taskData["remark"])
            }

            connection.outputStream.use { outputStream ->
                outputStream.write(jsonData.toString().toByteArray())
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode >= 200 && responseCode < 300) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            Log.d("debug", "任务发送接口返回：$responseBody：$responseCode")

            connection.disconnect()

            // 尝试解析JSON响应
            try {
                val json = JSONObject(responseBody)
                ApiResponse(
                    code = json.optInt("code", responseCode),
                    message = json.optString("message", json.optString("msg", "任务发送成功")),
                    data = if (json.has("data")) json.get("data") else null
                )
            } catch (e: Exception) {
                Log.d("debug", "任务发送响应解析异常：${e.message}")
                ApiResponse(
                    code = responseCode,
                    message = responseBody.ifEmpty { "任务发送失败" },
                    data = null
                )
            }

        } catch (e: Exception) {
            Log.e("debug", "任务发送网络异常：${e.message}")
            ApiResponse(
                code = -1,
                message = "网络错误: ${e.message}",
                data = null
            )
        }
    }
}
