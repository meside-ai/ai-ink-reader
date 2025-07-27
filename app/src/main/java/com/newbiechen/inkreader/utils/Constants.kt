package com.newbiechen.inkreader.utils

/**
 * 应用常量定义
 * 
 * 统一管理应用中使用的各种常量
 */
object Constants {
    
    // 数据库相关
    const val DATABASE_NAME = "ink_reader_database"
    const val DATABASE_VERSION = 1
    
    // 文件和存储
    const val EPUB_CACHE_DIR = "epub_cache"
    const val IMAGE_CACHE_DIR = "image_cache"
    const val LOG_DIR = "logs"
    const val MAX_CACHE_SIZE = 100 * 1024 * 1024L // 100MB
    
    // OpenAI API相关
    const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
    const val DEFAULT_MODEL = "gpt-4"
    const val MAX_TOKENS = 500
    const val TEMPERATURE = 0.3f
    const val REQUEST_TIMEOUT = 30_000L // 30秒
    const val MAX_RETRY_ATTEMPTS = 3
    
    // 性能相关
    const val PAGE_SIZE = 20 // 分页大小
    const val MEMORY_CACHE_SIZE = 50 * 1024 * 1024 // 50MB内存缓存
    const val DISK_CACHE_SIZE = 200 * 1024 * 1024L // 200MB磁盘缓存
    
    // 阅读相关
    const val DEFAULT_TEXT_SIZE = 16f
    const val MIN_TEXT_SIZE = 12f
    const val MAX_TEXT_SIZE = 30f
    const val DEFAULT_LINE_SPACING = 1.5f
    const val DEFAULT_MARGIN = 20
    
    // 墨水屏优化
    const val FAST_REFRESH_COUNT_LIMIT = 10 // 连续快速刷新次数限制
    const val REFRESH_DELAY_MS = 100L // 刷新延迟
    const val TOUCH_RESPONSE_TIMEOUT = 500L // 触控响应超时
    
    // 日志相关
    const val LOG_TAG = "InkReader"
    const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024L // 10MB
    const val LOG_RETENTION_DAYS = 7
    
    // 权限请求码
    const val REQUEST_CODE_STORAGE_PERMISSION = 1001
    const val REQUEST_CODE_MICROPHONE_PERMISSION = 1002
    const val REQUEST_CODE_POST_NOTIFICATIONS = 1003
    
    // Intent和Bundle键
    const val EXTRA_BOOK_ID = "extra_book_id"
    const val EXTRA_CHAPTER_INDEX = "extra_chapter_index"
    const val EXTRA_EPUB_FILE_PATH = "extra_epub_file_path"
    const val EXTRA_SELECTED_TEXT = "extra_selected_text"
    
    // 偏好设置键
    const val PREF_API_KEY = "pref_api_key"
    const val PREF_TEXT_SIZE = "pref_text_size"
    const val PREF_LINE_SPACING = "pref_line_spacing"
    const val PREF_THEME_MODE = "pref_theme_mode"
    const val PREF_REFRESH_MODE = "pref_refresh_mode"
    const val PREF_AI_LANGUAGE = "pref_ai_language"
    
    // 网络相关
    const val CONNECTION_TIMEOUT = 30_000L
    const val READ_TIMEOUT = 30_000L
    const val WRITE_TIMEOUT = 30_000L
    
    // 错误消息
    const val ERROR_NETWORK_UNAVAILABLE = "网络不可用"
    const val ERROR_FILE_NOT_FOUND = "文件未找到"
    const val ERROR_INVALID_EPUB = "无效的EPUB文件"
    const val ERROR_API_KEY_INVALID = "API密钥无效"
    const val ERROR_UNKNOWN = "未知错误"
} 