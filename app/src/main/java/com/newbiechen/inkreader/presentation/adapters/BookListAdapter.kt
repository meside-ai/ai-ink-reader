package com.newbiechen.inkreader.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.domain.entities.Book

/**
 * 图书列表适配器
 */
class BookListAdapter(
    private val onBookClick: (Book) -> Unit = {},
    private val onBookLongClick: (Book) -> Boolean = { false }
) : ListAdapter<Book, BookListAdapter.BookViewHolder>(BookDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.text_book_title)
        private val authorText: TextView = itemView.findViewById(R.id.text_book_author)
        private val publisherText: TextView = itemView.findViewById(R.id.text_book_publisher)
        private val chaptersText: TextView = itemView.findViewById(R.id.text_book_chapters)
        
        fun bind(book: Book) {
            titleText.text = book.title
            authorText.text = "作者：${book.author}"
            publisherText.text = book.publisher ?: "未知出版社"
            chaptersText.text = "${book.totalChapters} 章"
            
            itemView.setOnClickListener {
                onBookClick(book)
            }
            
            itemView.setOnLongClickListener {
                onBookLongClick(book)
            }
        }
    }
    
    private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.bookId == newItem.bookId
        }
        
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
} 