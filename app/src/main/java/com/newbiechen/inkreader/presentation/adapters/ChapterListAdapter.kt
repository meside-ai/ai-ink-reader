package com.newbiechen.inkreader.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.domain.entities.Chapter

/**
 * 章节列表适配器
 */
class ChapterListAdapter(
    private val onChapterClick: (Chapter) -> Unit = {}
) : ListAdapter<Chapter, ChapterListAdapter.ChapterViewHolder>(ChapterDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chapterNumberText: TextView = itemView.findViewById(R.id.text_chapter_number)
        private val chapterTitleText: TextView = itemView.findViewById(R.id.text_chapter_title)
        private val chapterInfoText: TextView = itemView.findViewById(R.id.text_chapter_info)
        
        fun bind(chapter: Chapter) {
            chapterNumberText.text = "${chapter.order}"
            chapterTitleText.text = chapter.title
            chapterInfoText.text = "${chapter.wordCount} 字"
            
            itemView.setOnClickListener {
                onChapterClick(chapter)
            }
        }
    }
    
    private class ChapterDiffCallback : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem.chapterId == newItem.chapterId
        }
        
        override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem == newItem
        }
    }
} 