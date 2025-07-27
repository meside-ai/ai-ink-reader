package com.newbiechen.inkreader.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android相关扩展函数
 */

/**
 * Context扩展 - 显示Toast
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Fragment扩展 - 显示Toast
 */
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

/**
 * Context扩展 - dp转px
 */
fun Context.dpToPx(dp: Float): Int {
    return (dp * resources.displayMetrics.density + 0.5f).toInt()
}

/**
 * Context扩展 - px转dp
 */
fun Context.pxToDp(px: Float): Int {
    return (px / resources.displayMetrics.density + 0.5f).toInt()
}

/**
 * LiveData扩展 - 简化观察
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}

/**
 * 字符串和数据处理扩展函数
 */

/**
 * String扩展 - 检查是否为有效的EPUB文件路径
 */
fun String.isEpubFile(): Boolean {
    return this.lowercase().endsWith(".epub")
}

/**
 * String扩展 - 获取文件名（不含扩展名）
 */
fun String.getFileNameWithoutExtension(): String {
    return this.substringAfterLast("/").substringBeforeLast(".")
}

/**
 * String扩展 - 获取文件扩展名
 */
fun String.getFileExtension(): String {
    return this.substringAfterLast(".", "")
}

/**
 * String扩展 - 截取指定长度的字符串并添加省略号
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength) + "..."
    }
}

/**
 * String扩展 - 移除HTML标签
 */
fun String.stripHtml(): String {
    return this.replace(Regex("<[^>]*>"), "")
}

/**
 * 时间处理扩展函数
 */

/**
 * Long扩展 - 时间戳转可读时间格式
 */
fun Long.toReadableTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Long扩展 - 时间戳转日期格式
 */
fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Long扩展 - 计算相对时间（几分钟前、几小时前等）
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "刚刚" // 1分钟内
        diff < 3600_000 -> "${diff / 60_000}分钟前" // 1小时内
        diff < 86400_000 -> "${diff / 3600_000}小时前" // 1天内
        diff < 2592000_000 -> "${diff / 86400_000}天前" // 30天内
        else -> this.toDateString() // 超过30天显示具体日期
    }
}

/**
 * 文件大小处理扩展函数
 */

/**
 * Long扩展 - 字节数转可读文件大小格式
 */
fun Long.toReadableFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return if (unitIndex == 0) {
        "${size.toInt()}${units[unitIndex]}"
    } else {
        "%.1f%s".format(size, units[unitIndex])
    }
}

/**
 * 集合处理扩展函数
 */

/**
 * List扩展 - 安全的get操作
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index >= 0 && index < size) get(index) else null
}

/**
 * 异常处理扩展函数
 */

/**
 * Result扩展 - 安全执行操作
 */
inline fun <T, R> T.runSafely(action: T.() -> R): Result<R> {
    return try {
        Result.success(action())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 其他工具扩展函数
 */

/**
 * Any扩展 - 空安全执行
 */
inline fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T {
    return if (condition) block() else this
}

/**
 * Boolean扩展 - 条件执行
 */
inline fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) block()
    return this
}

inline fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) block()
    return this
} 