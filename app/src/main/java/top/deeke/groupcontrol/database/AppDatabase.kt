package top.deeke.groupcontrol.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import top.deeke.groupcontrol.database.dao.CommandDao
import top.deeke.groupcontrol.database.dao.DeviceDao
import top.deeke.groupcontrol.database.entity.CommandEntity
import top.deeke.groupcontrol.database.entity.DeviceEntity

@Database(
    entities = [CommandEntity::class, DeviceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun deviceDao(): DeviceDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // 数据库迁移策略
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建设备表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `devices` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL DEFAULT '',
                        `remark` TEXT NOT NULL DEFAULT '',
                        `deviceId` TEXT NOT NULL DEFAULT '',
                        `status` TEXT NOT NULL DEFAULT 'OFFLINE',
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "group_control_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // 如果迁移失败，允许破坏性迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
