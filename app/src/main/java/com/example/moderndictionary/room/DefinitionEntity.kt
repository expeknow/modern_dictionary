package com.example.moderndictionary.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "saved-words-table")
data class DefinitionEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var word : String = "",
    var definition: String = "",
    var example: String = "",
    var like: Int = 0,
    var dislike: Int = 0,
    var author: String = "",
    var datePublishing : String = "",
    var isBookmarked : Boolean = false
)
