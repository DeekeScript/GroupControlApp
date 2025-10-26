package top.deeke.groupcontrol.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.deeke.groupcontrol.model.ServerConfig

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    
    // 服务器配置相关
    private val serverUrlKey = stringPreferencesKey("server_url")
    private val requestFrequencyKey = intPreferencesKey("request_frequency")
    private val sendRouteKey = stringPreferencesKey("send_route")
    
    // 获取服务器配置
    val serverConfig: Flow<ServerConfig> = context.dataStore.data.map { preferences ->
        ServerConfig(
            serverUrl = preferences[serverUrlKey] ?: "",
            requestFrequency = preferences[requestFrequencyKey] ?: 5000,
            sendRoute = preferences[sendRouteKey] ?: "/api/send"
        )
    }
    
    // 保存服务器配置
    suspend fun saveServerConfig(config: ServerConfig) {
        context.dataStore.edit { preferences ->
            preferences[serverUrlKey] = config.serverUrl
            preferences[requestFrequencyKey] = config.requestFrequency
            preferences[sendRouteKey] = config.sendRoute
        }
    }
}