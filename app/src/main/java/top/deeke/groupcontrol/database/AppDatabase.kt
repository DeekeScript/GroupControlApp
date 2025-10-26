package top.deeke.groupcontrol.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import top.deeke.groupcontrol.database.dao.CommandDao
import top.deeke.groupcontrol.database.entity.CommandEntity

@Database(
    entities = [CommandEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "group_control_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
