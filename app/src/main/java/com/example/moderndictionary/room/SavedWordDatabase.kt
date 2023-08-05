package com.example.moderndictionary.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DefinitionEntity::class], version = 1)
abstract class SavedWordDatabase : RoomDatabase() {
    abstract fun definitionDao(): DefinitionDao
    companion object {
        @Volatile
        private var INSTANCE : SavedWordDatabase? = null
        @JvmStatic
        fun getInstance(context: Context): SavedWordDatabase {
            synchronized(this){
                var instance = INSTANCE

                //if instance does not exists, we create it or else we return existing one
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SavedWordDatabase::class.java,
                        "savedWord_database"
                    ).build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}