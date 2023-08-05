package com.example.moderndictionary.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moderndictionary.utils.DefinitionModel
import com.example.moderndictionary.R
import com.example.moderndictionary.databinding.WordDefinitionItemviewBinding

class ListAdapter(private val definitionList: List<DefinitionModel>, private val context: Context) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>(){

    private var onClickListener : OnClickListener? = null

    class ViewHolder(binding: WordDefinitionItemviewBinding) : RecyclerView.ViewHolder(binding.root){
        val tvWord = binding.tvWord
        val tvDefinition = binding.tvDefinition
        val tvExample = binding.tvExample
        val tvAuthor = binding.tvAuthor
        val btnLikes = binding.btnLIke
        val btnDislikes = binding.btnDislike
        var ibBookmarkWord = binding.ibBookmarkWord
        val llLikesAndBookmark = binding.llLikesAndBookmarkIcons
        val llCardView = binding.llCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(WordDefinitionItemviewBinding.inflate(
            LayoutInflater.from(parent.context), parent,false
        ))
    }

    override fun getItemCount(): Int {
        return definitionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val definition = definitionList[definitionList.size - position -1]

        //For saved word card
        if(definition.id == -100){
            holder.tvWord.text = definition.word
            holder.llLikesAndBookmark.visibility = View.GONE
            holder.tvDefinition.visibility = View.GONE
            holder.tvAuthor.visibility = View.GONE
            holder.tvExample.visibility = View.GONE
            holder.tvWord.gravity = Gravity.CENTER
            holder.llCardView.setBackgroundColor(context.getColor(
                R.color.definition_card_background_shadow
            ))
            holder.tvWord.textSize = 40f
            holder.llCardView.setPadding(0,10,0,0)
            holder.tvWord.setTextColor(context.getColor(R.color.saved_words_text_color))

        //for word definition card
        }else{
            //Set reused Card To default Settings
            if(holder.llLikesAndBookmark.visibility == View.GONE){
                holder.llLikesAndBookmark.visibility = View.VISIBLE
                holder.tvDefinition.visibility = View.VISIBLE
                holder.tvAuthor.visibility = View.VISIBLE
                holder.tvExample.visibility = View.VISIBLE
                holder.tvWord.gravity = Gravity.START
                holder.llCardView.setBackgroundColor(context.getColor(
                    R.color.definition_card_background
                ))
                holder.tvWord.textSize = 35f
                holder.llCardView.setPadding(16,10,10,10)
                holder.tvWord.setTextColor(context.getColor(R.color.searched_word_color))
            }

            //Set data for the card
            holder.tvWord.text = definition.word
            holder.tvDefinition.text = definition.definition
            holder.tvExample.text = definition.example
            holder.tvAuthor.text = definition.author
            holder.btnLikes.text = definition.likes.toString()
            holder.btnDislikes.text = definition.dislikes.toString()

            if(!definition.isBookmarked){
                holder.ibBookmarkWord.setImageResource(R.drawable.ic_add_bookmark)
            }else{
                holder.ibBookmarkWord.setImageResource(R.drawable.ic_bookmarked)
            }
        }
        holder.ibBookmarkWord.setOnClickListener {
            if(onClickListener != null){
                onClickListener?.onClick(definition, position)
            }
        }

    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(model: DefinitionModel, position: Int)
    }
}