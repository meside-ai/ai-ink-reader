package com.newbiechen.inkreader.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.io.File
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.utils.toReadableFileSize
import com.newbiechen.inkreader.utils.toDateString

/**
 * 图书列表适配器
 * 
 * 使用ListAdapter和DiffUtil优化性能，支持：
 * - 图书信息展示
 * - 选择模式
 * - 点击事件处理
 * - 封面图片加载
 */
class BookListAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onBookLongClick: (Book) -> Unit,
    private val onSelectionChanged: (Book, Boolean) -> Unit,
    private val onMoreClick: (Book, View) -> Unit
) : ListAdapter<Book, BookListAdapter.BookViewHolder>(BookDiffCallback()) {
    
    // 是否处于选择模式
    private var isSelectionMode = false
    
    // 选中的图书ID集合
    private var selectedBooks = setOf<String>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
    }
    
    /**
     * 设置选择模式
     */
    fun setSelectionMode(isSelectionMode: Boolean) {
        if (this.isSelectionMode != isSelectionMode) {
            this.isSelectionMode = isSelectionMode
            notifyDataSetChanged() // 刷新所有项以显示/隐藏复选框
        }
    }
    
    /**
     * 更新选中状态
     */
    fun updateSelectedBooks(selectedBooks: Set<String>) {
        val oldSelected = this.selectedBooks
        this.selectedBooks = selectedBooks
        
        // 只刷新选中状态发生变化的项
        val changedItems = (oldSelected + selectedBooks) - (oldSelected intersect selectedBooks)
        changedItems.forEach { bookId ->
            val position = findPositionByBookId(bookId)
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }
    
    /**
     * 根据图书ID查找位置
     */
    private fun findPositionByBookId(bookId: String): Int {
        for (i in 0 until itemCount) {
            if (getItem(i).bookId == bookId) {
                return i
            }
        }
        return -1
    }
    
    /**
     * ViewHolder
     */
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val selectionCheckBox: CheckBox = itemView.findViewById(R.id.selection_checkbox)
        private val bookCoverImage: ImageView = itemView.findViewById(R.id.book_cover_image)
        private val readingProgressBar: ProgressBar = itemView.findViewById(R.id.reading_progress_bar)
        private val bookTitleText: TextView = itemView.findViewById(R.id.book_title_text)
        private val bookAuthorText: TextView = itemView.findViewById(R.id.book_author_text)
        private val bookSizeText: TextView = itemView.findViewById(R.id.book_size_text)
        private val bookChaptersText: TextView = itemView.findViewById(R.id.book_chapters_text)
        private val bookMoreButton: ImageButton = itemView.findViewById(R.id.book_more_button)
        private val bookLastReadText: TextView = itemView.findViewById(R.id.book_last_read_text)
        
        fun bind(book: Book) {
            // 设置基本信息
            bookTitleText.text = book.getDisplayTitle()
            bookAuthorText.text = book.author
            bookSizeText.text = book.fileSize.toReadableFileSize()
            bookChaptersText.text = itemView.context.getString(
                R.string.sample_book_chapters, 
                book.totalChapters
            ).replace("15", book.totalChapters.toString())
            
            // 设置最后阅读时间
            bookLastReadText.text = formatLastReadTime(book.lastOpenedAt)
            
            // 设置选择状态
            updateSelectionState(book)
            
            // 加载封面图片
            loadCoverImage(book)
            
            // 设置阅读进度（暂时使用随机值作为示例）
            val progress = (book.bookId.hashCode() % 100).coerceAtLeast(0)
            readingProgressBar.progress = progress
            
            // 设置点击事件
            setupClickListeners(book)
        }
        
        /**
         * 更新选择状态
         */
        private fun updateSelectionState(book: Book) {
            val isSelected = selectedBooks.contains(book.bookId)
            
            // 显示/隐藏复选框
            selectionCheckBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            selectionCheckBox.isChecked = isSelected
            
            // 设置复选框点击事件
            selectionCheckBox.setOnClickListener {
                onSelectionChanged(book, selectionCheckBox.isChecked)
            }
            
            // 调整卡片的视觉状态
            itemView.isSelected = isSelected
            itemView.alpha = if (isSelectionMode && !isSelected) 0.6f else 1.0f
        }
        
        /**
         * 加载封面图片
         */
        private fun loadCoverImage(book: Book) {
            val context = itemView.context
            
            if (!book.coverImagePath.isNullOrBlank() && File(book.coverImagePath).exists()) {
                // 加载本地封面图片
                Glide.with(context)
                    .load(book.coverImagePath)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_book_placeholder)
                            .error(R.drawable.ic_book_placeholder)
                            .transform(RoundedCorners(16))
                    )
                    .into(bookCoverImage)
            } else {
                // 使用默认占位符
                Glide.with(context)
                    .load(R.drawable.ic_book_placeholder)
                    .apply(RequestOptions().transform(RoundedCorners(16)))
                    .into(bookCoverImage)
            }
        }
        
        /**
         * 设置点击事件
         */
        private fun setupClickListeners(book: Book) {
            // 整个item的点击事件
            itemView.setOnClickListener {
                if (isSelectionMode) {
                    // 选择模式下切换选中状态
                    val newCheckedState = !selectedBooks.contains(book.bookId)
                    onSelectionChanged(book, newCheckedState)
                } else {
                    // 正常模式下打开图书
                    onBookClick(book)
                }
            }
            
            // 长按事件
            itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    onBookLongClick(book)
                    true
                } else {
                    false
                }
            }
            
            // 更多选项按钮点击事件
            bookMoreButton.setOnClickListener { view ->
                onMoreClick(book, view)
            }
        }
        
        /**
         * 格式化最后阅读时间
         */
        private fun formatLastReadTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000L -> itemView.context.getString(R.string.time_just_now) // 1分钟内
                diff < 3600_000L -> { // 1小时内
                    val minutes = (diff / 60_000L).toInt()
                    itemView.context.getString(R.string.time_minutes_ago, minutes)
                }
                diff < 86400_000L -> { // 1天内
                    val hours = (diff / 3600_000L).toInt()
                    itemView.context.getString(R.string.time_hours_ago, hours)
                }
                diff < 2592000_000L -> { // 30天内
                    val days = (diff / 86400_000L).toInt()
                    itemView.context.getString(R.string.time_days_ago, days)
                }
                else -> {
                    // 超过30天显示具体日期
                    timestamp.toDateString("MM-dd")
                }
            }
        }
    }
    
    /**
     * DiffUtil回调
     */
    private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.bookId == newItem.bookId
        }
        
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
} 