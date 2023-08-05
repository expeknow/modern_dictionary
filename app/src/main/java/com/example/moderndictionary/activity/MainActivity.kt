package com.example.moderndictionary.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moderndictionary.R
import com.example.moderndictionary.adapter.ListAdapter
import com.example.moderndictionary.databinding.ActivityMainBinding
import com.example.moderndictionary.room.DefinitionApp
import com.example.moderndictionary.room.DefinitionDao
import com.example.moderndictionary.room.DefinitionEntity
import com.example.moderndictionary.utils.Constants
import com.example.moderndictionary.utils.DefinitionModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding? = null
    var hasUserDoublePressed: Boolean = false
    private lateinit var dao : DefinitionDao
    private val savedWords = ArrayList<DefinitionModel>()
    private lateinit var adapter: ListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.tvLoadingInfo?.visibility = View.VISIBLE
        binding?.rvDefinitionList?.layoutManager = LinearLayoutManager(this@MainActivity)

        dao = (application as DefinitionApp).db.definitionDao()
        getAllSavedWords(dao)

        binding?.etSearchBar?.onRightDrawableClicked {
            searchWord()
        }

        binding?.etSearchBar?.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when(actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    searchWord()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Sends the word in Search Activity to show its definitions
     */
    private fun searchWord() {
        val word = binding?.etSearchBar?.text.toString()

        if(word.isNotEmpty()){
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra(Constants.WORD, word)
            startActivityForResult(intent, Constants.SEARCH_ACTIVITY_RESULT_CODE)
        }else{
            Snackbar.make(findViewById(R.id.cl_main_activity), "Please enter a word to search.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == Constants.SEARCH_ACTIVITY_RESULT_CODE){
                getAllSavedWords(dao)
            }
        }
    }

    /**
     * Gets all already saved words from Room database and shows them on homescreen recyclerView
     */
    private fun getAllSavedWords(dao: DefinitionDao) {
        lifecycleScope.launch {
            dao.fetchAllSavedWords().collect{ allSavedWords ->
                if(allSavedWords.isNotEmpty()){
                    binding?.tvLoadingInfo?.visibility = View.GONE
                    binding?.rvDefinitionList?.visibility = View.VISIBLE
                    savedWords.clear()
                    for(data in allSavedWords){
                        savedWords.add(
                            DefinitionModel(data.id, data.word, data.definition, data.example,
                            data.like, data.dislike, data.author, data.datePublishing, data.isBookmarked)
                        )
                    }
                    //Dummy card to show "Saved Words"
                    val dummySavedDefinitionCard = DefinitionModel(
                        -100, "Saved Words", "", "", 0,
                        0, "", "", false
                    )
                    savedWords.add(dummySavedDefinitionCard)
                    adapter = ListAdapter(savedWords, this@MainActivity)
                    runOnUiThread {
                        binding?.rvDefinitionList?.adapter = adapter
                        adapter.setOnClickListener(object : ListAdapter.OnClickListener{
                            //Handles what happens when we unbookmark a word
                            override fun onClick(
                                model: DefinitionModel,
                                position: Int
                            ) {
                                model.isBookmarked = false
                                lifecycleScope.launch {
                                    dao.delete(DefinitionEntity(model.id))
                                }
                                adapter.notifyItemRemoved(position)
                            }

                        })
                    }
                }else{
                    binding?.tvLoadingInfo?.visibility = View.VISIBLE
                    binding?.rvDefinitionList?.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


    /**
     * Called when search icon on search bar is clicked
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        //https://stackoverflow.com/questions/3554377/handling-click-events-on-a-drawable-within-an-edittext
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }

    override fun onBackPressed() {
        doublePressToExit()
    }

    private fun doublePressToExit() {
        if(hasUserDoublePressed){
            super.onBackPressed()
        }
        hasUserDoublePressed = true
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            "Press back again to exit.",
            Snackbar.LENGTH_LONG)
        snackBar.show()
        Handler().postDelayed({hasUserDoublePressed = false}, 2000)
    }


}