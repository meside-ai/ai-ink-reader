package com.newbiechen.inkreader.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.databinding.FragmentBookListBinding
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.presentation.activities.MainActivity
import com.newbiechen.inkreader.presentation.adapters.BookListAdapter
import com.newbiechen.inkreader.presentation.viewmodels.BookListViewModel
import com.newbiechen.inkreader.utils.showToast

/**
 * 图书列表Fragment
 * 
 * 显示用户导入的所有EPUB图书列表，支持：
 * - 网格布局展示图书
 * - 搜索功能
 * - 选择模式（长按进入）
 * - 批量删除
 * - 下拉刷新
 * - 空状态显示
 */
@AndroidEntryPoint
class BookListFragment : Fragment(), MenuProvider {
    
    companion object {
        const val TAG = "BookListFragment"
        
        fun newInstance() = BookListFragment()
    }
    
    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BookListViewModel by viewModels()
    private lateinit var bookAdapter: BookListAdapter
    
    // 搜索相关
    private var searchView: SearchView? = null
    private var searchMenuItem: MenuItem? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Timber.d("BookListFragment视图创建完成")
        
        setupMenuProvider()
        setupRecyclerView()
        setupUI()
        observeViewModel()
    }
    
    /**
     * 设置菜单提供者
     */
    private fun setupMenuProvider() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        bookAdapter = BookListAdapter(
            onBookClick = { book ->
                openBook(book)
            },
            onBookLongClick = { book ->
                enterSelectionMode(book)
            },
            onSelectionChanged = { book, isSelected ->
                if (isSelected) {
                    viewModel.toggleBookSelection(book.bookId)
                } else {
                    viewModel.toggleBookSelection(book.bookId)
                }
            },
            onMoreClick = { book, view ->
                showBookOptionsMenu(book, view)
            }
        )
        
        binding.booksRecyclerView.apply {
            adapter = bookAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }
    
    /**
     * 设置UI控件
     */
    private fun setupUI() {
        setupSwipeRefresh()
        setupSearch()
        setupSelectionToolbar()
    }
    
    /**
     * 设置下拉刷新
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshBooks()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    /**
     * 设置搜索功能
     */
    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.search(query)
            }
            true
        }
    }
    
    /**
     * 设置选择模式工具栏
     */
    private fun setupSelectionToolbar() {
        binding.selectAllButton.setOnClickListener {
            viewModel.selectAllBooks()
        }
        
        binding.deleteSelectedButton.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }
    
    /**
     * 观察ViewModel数据变化
     */
    private fun observeViewModel() {
        // 观察图书列表
        viewModel.books.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
            updateEmptyState(books.isEmpty())
        }
        
        // 观察搜索结果
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            if (viewModel.isSearchMode.value) {
                bookAdapter.submitList(searchResults)
                updateEmptyState(searchResults.isEmpty())
            }
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察选择模式状态
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isSelectionMode.collectLatest { isSelectionMode ->
                        updateSelectionMode(isSelectionMode)
                        bookAdapter.setSelectionMode(isSelectionMode)
                        requireActivity().invalidateOptionsMenu() // 刷新菜单
                    }
                }
                
                launch {
                    viewModel.selectedBooks.collectLatest { selectedBooks ->
                        bookAdapter.updateSelectedBooks(selectedBooks)
                        updateSelectionCount(selectedBooks.size)
                    }
                }
                
                launch {
                    viewModel.isSearchMode.collectLatest { isSearchMode ->
                        updateSearchMode(isSearchMode)
                    }
                }
            }
        }
        
        // 观察空状态
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            updateEmptyState(isEmpty)
        }
    }
    
    /**
     * 更新空状态显示
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.booksRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    /**
     * 更新选择模式UI
     */
    private fun updateSelectionMode(isSelectionMode: Boolean) {
        binding.selectionToolbar.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        
        // 更新标题栏（如果需要）
        (requireActivity() as? MainActivity)?.supportActionBar?.let { actionBar ->
            if (isSelectionMode) {
                actionBar.title = "选择图书"
            } else {
                actionBar.title = getString(R.string.app_name)
            }
        }
    }
    
    /**
     * 更新选择数量显示
     */
    private fun updateSelectionCount(count: Int) {
        binding.selectionCountText.text = getString(R.string.selected_count, count)
        
        // 更新按钮状态
        binding.deleteSelectedButton.isEnabled = count > 0
        
        // 更新全选按钮文本
        val totalCount = bookAdapter.itemCount
        binding.selectAllButton.text = if (count == totalCount && count > 0) {
            getString(R.string.select_none)
        } else {
            getString(R.string.select_all)
        }
    }
    
    /**
     * 更新搜索模式UI
     */
    private fun updateSearchMode(isSearchMode: Boolean) {
        binding.searchCard.visibility = if (isSearchMode) View.VISIBLE else View.GONE
        
        if (isSearchMode) {
            binding.searchEditText.requestFocus()
        } else {
            binding.searchEditText.text?.clear()
        }
    }
    
    /**
     * 打开图书
     */
    private fun openBook(book: Book) {
        Timber.d("打开图书: ${book.title}")
        (requireActivity() as? MainActivity)?.showReadingActivity(book.bookId)
    }
    
    /**
     * 进入选择模式
     */
    private fun enterSelectionMode(book: Book) {
        Timber.d("进入选择模式，初始选中: ${book.title}")
        viewModel.enterSelectionMode(book.bookId)
    }
    
    /**
     * 显示图书选项菜单
     */
    private fun showBookOptionsMenu(book: Book, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.book_item_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete_book -> {
                    showDeleteBookDialog(book)
                    true
                }
                R.id.action_book_info -> {
                    showBookInfoDialog(book)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    /**
     * 显示删除图书确认对话框
     */
    private fun showDeleteBookDialog(book: Book) {
        // TODO: 实现删除确认对话框
        viewModel.deleteBook(book.bookId)
        showToast("《${book.title}》已删除")
    }
    
    /**
     * 显示图书信息对话框
     */
    private fun showBookInfoDialog(book: Book) {
        // TODO: 实现图书信息对话框
        showToast("图书信息功能将在后续版本实现")
    }
    
    /**
     * 显示批量删除确认对话框
     */
    private fun showDeleteConfirmDialog() {
        val selectedCount = viewModel.getSelectedBookCount()
        if (selectedCount == 0) {
            showToast("请先选择要删除的图书")
            return
        }
        
        // TODO: 实现确认对话框
        viewModel.deleteSelectedBooks()
    }
    
    /**
     * 刷新图书列表（供外部调用）
     */
    fun refreshBooks() {
        viewModel.refreshBooks()
        Timber.d("刷新图书列表")
    }
    
    // ===== MenuProvider接口实现 =====
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (viewModel.isSelectionMode.value) {
            // 选择模式菜单
            menuInflater.inflate(R.menu.selection_menu, menu)
        } else {
            // 普通模式菜单
            menuInflater.inflate(R.menu.book_list_menu, menu)
            
            // 设置搜索菜单项
            searchMenuItem = menu.findItem(R.id.action_search)
            searchView = searchMenuItem?.actionView as? SearchView
            searchView?.apply {
                queryHint = getString(R.string.search_books_hint)
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.let { viewModel.search(it) }
                        return true
                    }
                    
                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText.isNullOrBlank()) {
                            viewModel.stopSearch()
                        }
                        return true
                    }
                })
            }
        }
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_search -> {
                viewModel.startSearch()
                true
            }
            R.id.action_select_all -> {
                viewModel.selectAllBooks()
                true
            }
            R.id.action_delete_selected -> {
                showDeleteConfirmDialog()
                true
            }
            R.id.action_exit_selection -> {
                viewModel.exitSelectionMode()
                true
            }
            android.R.id.home -> {
                if (viewModel.isSelectionMode.value) {
                    viewModel.exitSelectionMode()
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView = null
        searchMenuItem = null
    }
} 