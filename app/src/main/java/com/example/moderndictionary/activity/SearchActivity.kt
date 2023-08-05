package com.example.moderndictionary.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moderndictionary.R
import com.example.moderndictionary.adapter.ListAdapter
import com.example.moderndictionary.databinding.ActivitySearchBinding
import com.example.moderndictionary.room.DefinitionApp
import com.example.moderndictionary.room.DefinitionDao
import com.example.moderndictionary.room.DefinitionEntity
import com.example.moderndictionary.utils.Constants
import com.example.moderndictionary.utils.DefinitionModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private var definitionList = ArrayList<DefinitionModel>()
    private lateinit var binding : ActivitySearchBinding
    private lateinit var dao : DefinitionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dao = (application as DefinitionApp).db.definitionDao()

        val word = intent.getStringExtra(Constants.WORD)
        if(word.isNullOrEmpty()){
            Snackbar.make(findViewById(R.id.cl_search_activity),
            "Please enter any word to search definition!", Snackbar.LENGTH_LONG).show()
        }else{
            binding.etSearchBar.setText(word)
            binding.etSearchBar.setSelection(binding.etSearchBar.length())
            searchWord(word)
        }

        binding.etSearchBar.onRightDrawableClicked {
            val enteredWord = binding.etSearchBar.text.toString()
            if(enteredWord.isNotEmpty()){
                binding.etSearchBar.setText(word)
                binding.etSearchBar.setSelection(binding.etSearchBar.length())
                searchWord(enteredWord)
            }else{
                Snackbar.make(findViewById(R.id.cl_search_activity),
                    "Please enter any word to search definition!", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when(actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val enteredWord = binding.etSearchBar.text.toString()
                    if(enteredWord.isNotEmpty()){
                        binding.etSearchBar.setText(enteredWord)
                        binding.etSearchBar.setSelection(binding.etSearchBar.length())
                        searchWord(enteredWord)
                    }else{
                        Snackbar.make(findViewById(R.id.cl_search_activity),
                            "Please enter any word to search definition!", Snackbar.LENGTH_LONG).show()
                    }
                    true
                }
                else -> false
            }
        }
    }


    /**
     * Asynchronous class to make network calls to the API
     */
    private inner class callUrbanDictionary(private val word: String) : AsyncTask<Any, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            definitionList = ArrayList()
            binding.rvDefinitionList.visibility = View.GONE
            binding.tvLoadingInfo.visibility= View.VISIBLE
            binding.tvLoadingInfo.text = "Searching word... Please Wait!"
        }

        override fun doInBackground(vararg params: Any?): String {

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://mashape-community-urban-dictionary.p.rapidapi.com/define?term=${word}")
                .get()
                .addHeader("X-RapidAPI-Key", "9a7b8d0448msh489387a74d87052p1c4e4fjsn75679799c3ba")
                .addHeader("X-RapidAPI-Host", "mashape-community-urban-dictionary.p.rapidapi.com")
                .build()

            val response = client.newCall(request).execute()
            return response.body()!!.string()

        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonObject = JSONObject(result)

            //get a list of JSON objects
            val dataListArray = jsonObject.optJSONArray("list")
            val numbersOfObjects = dataListArray?.length()
            if(numbersOfObjects == 0){
                binding.tvLoadingInfo.text = "No definition found for given word. Please " +
                        "try another word."
                return
            }
            for(item in 0 until numbersOfObjects!!){
                //get every JSON object from the list
                val dataItemObject: JSONObject = dataListArray[item] as JSONObject
                //now you can access their value like a normal JSON object
                val definitionModel = DefinitionModel(
                    0,
                    dataItemObject.optString("word"),
                    dataItemObject.optString("definition"),
                    dataItemObject.optString("example"),
                    dataItemObject.optInt("thumbs_up"),
                    dataItemObject.optInt("thumbs_down"),
                    dataItemObject.optString("author"),
                    dataItemObject.optString("written_on"),
                    false
                )
                definitionList.add(definitionModel)
            }
            showDefinitionsInRv()
        }
    }

    /**
     * Shows the fetched definitions in recycler view
     */
    private fun showDefinitionsInRv(){
        binding.tvLoadingInfo.visibility = View.GONE
        binding.rvDefinitionList.visibility = View.VISIBLE
        binding.rvDefinitionList.layoutManager = LinearLayoutManager(this)

        val adapter = ListAdapter(definitionList, this@SearchActivity)
        binding.rvDefinitionList.adapter = adapter

        adapter.setOnClickListener(object: ListAdapter.OnClickListener{
            override fun onClick(
                model: DefinitionModel,
                position: Int
            ) {
                setResult(Activity.RESULT_OK)
                if(model.isBookmarked){
                        model.isBookmarked = false
                        lifecycleScope.launch {
                            dao.deleteByDefinition(model.definition)
                            adapter.notifyItemChanged(position)
                        }

                }else{
                        model.isBookmarked = true
                        lifecycleScope.launch{
                            lifecycleScope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    dao.isDefinitionAlreadySaved(model.definition)
                                }
                            if(!result){
                                dao.insert(
                                    DefinitionEntity(
                                        word = model.word,
                                        definition = model.definition,
                                        like = model.likes,
                                        dislike = model.dislikes,
                                        example = model.example,
                                        author = model.author,
                                        datePublishing = model.datePublished,
                                        isBookmarked = true
                                    )
                                )
                            }
                            adapter.notifyItemChanged(position)
                        }

                }
                }

        }
    } )
    }

    /**
     * Defines what happens when search icon at the end of search bar is clicked
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

    /**
     * calls the API to get definiition
     */
    private fun searchWord(word: String) {
        if(isInternetAvailable()){
            callUrbanDictionary(word).execute()
        }else{
            binding.tvLoadingInfo.visibility = View.VISIBLE
            binding.tvLoadingInfo.text = "Please Enable Internet Access."
            Snackbar.make(findViewById(R.id.cl_search_activity), "No internet connection available!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }


    private fun isInternetAvailable() : Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}