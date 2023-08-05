package com.example.moderndictionary.room

import androidx.room.*
import com.example.moderndictionary.room.DefinitionEntity
import com.example.moderndictionary.utils.DefinitionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface DefinitionDao {
    @Insert
    suspend fun insert(definitionEntity: DefinitionEntity)

    @Update
    suspend fun update(definitionEntity: DefinitionEntity)

    @Delete
    suspend fun delete(definitionEntity: DefinitionEntity)

    @Query("SELECT * FROM `saved-words-table`")
    fun fetchAllSavedWords(): Flow<List<DefinitionEntity>>

    @Query("DELETE FROM `saved-words-table` WHERE definition = :definition")
    suspend fun deleteByDefinition(definition: String)

    @Query("SELECT EXISTS (SELECT * FROM `saved-words-table` WHERE definition = :definition)")
    fun isDefinitionAlreadySaved(definition: String): Boolean

}