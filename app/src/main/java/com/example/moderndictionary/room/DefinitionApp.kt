package com.example.moderndictionary.room

import android.app.Application

class DefinitionApp: Application() {
    val db by lazy{
        SavedWordDatabase.getInstance(this)
    }
}