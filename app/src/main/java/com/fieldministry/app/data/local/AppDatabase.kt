package com.fieldministry.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fieldministry.app.data.local.dao.BarangayDao
import com.fieldministry.app.data.local.dao.BibleStudyDao
import com.fieldministry.app.data.local.dao.HouseholderDao
import com.fieldministry.app.data.local.dao.MunicipalityDao
import com.fieldministry.app.data.local.dao.ReturnVisitDao
import com.fieldministry.app.data.local.dao.SearchingDao
import com.fieldministry.app.data.local.entity.BarangayEntity
import com.fieldministry.app.data.local.entity.BibleStudyEntity
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.data.local.entity.ReturnVisitEntity
import com.fieldministry.app.data.local.entity.SearchingEntity

@Database(
    entities = [
        MunicipalityEntity::class,
        BarangayEntity::class,
        HouseholderEntity::class,
        SearchingEntity::class,
        BibleStudyEntity::class,
        ReturnVisitEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun municipalityDao(): MunicipalityDao
    abstract fun barangayDao(): BarangayDao
    abstract fun householderDao(): HouseholderDao
    abstract fun searchingDao(): SearchingDao
    abstract fun bibleStudyDao(): BibleStudyDao
    abstract fun returnVisitDao(): ReturnVisitDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "field_ministry_tracker.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }

        fun closeInstance() {
            instance?.close()
            instance = null
        }
    }
}
