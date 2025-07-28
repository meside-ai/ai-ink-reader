package com.newbiechen.inkreader.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Result

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
 * DP转PX
 */
fun Context.dpToPx(dp: Float): Float {
    return dp * resources.displayMetrics.density
}

/**
 * PX转DP
 */
fun Context.pxToDp(px: Float): Float {
    return px / resources.displayMetrics.density
}

/**
 * LiveData扩展 - 安全观察
 */
fun <T> LiveData<T>.observeSafely(owner: LifecycleOwner, observer: Observer<T>) {
    try {
        observe(owner, observer)
    } catch (e: Exception) {
        Timber.e(e, "LiveData观察失败")
    }
}

/**
 * 通用扩展函数
 */

/**
 * String扩展 - 检查是否为有效的EPUB文件路径
 */
fun String.isEpubFile(): Boolean {
    return this.lowercase().endsWith(".epub")
}

/**
 * String扩展 - 从文件路径获取文件名
 */
fun String.getFileName(): String {
    return substringAfterLast("/").substringAfterLast("\\")
}

/**
 * String扩展 - 从文件路径获取不含扩展名的文件名
 */
fun String.getFileNameWithoutExtension(): String {
    return getFileName().substringBeforeLast(".")
}

/**
 * String扩展 - 获取文件扩展名
 */
fun String.getFileExtension(): String {
    return substringAfterLast(".", "")
}

/**
 * String扩展 - 截断字符串
 */
fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) {
        this
    } else {
        take(maxLength - suffix.length) + suffix
    }
}

/**
 * String扩展 - 移除HTML标签
 */
fun String.stripHtmlTags(): String {
    return replace(Regex("<[^>]*>"), "")
}

/**
 * String扩展 - 安全的整数转换
 */
fun String.toIntSafely(defaultValue: Int = 0): Int {
    return try {
        toInt()
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

/**
 * String扩展 - 安全的长整数转换
 */
fun String.toLongSafely(defaultValue: Long = 0L): Long {
    return try {
        toLong()
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

/**
 * Long扩展 - 时间戳转日期字符串
 */
fun Long.toDateString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return try {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        dateFormat.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

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
 * List扩展 - 安全获取元素
 */
fun <T> List<T>.getSafely(index: Int): T? {
    return if (index in 0 until size) {
        get(index)
    } else {
        null
    }
}

/**
 * List扩展 - 安全获取元素或默认值
 */
fun <T> List<T>.getSafelyOrDefault(index: Int, defaultValue: T): T {
    return getSafely(index) ?: defaultValue
}

/**
 * 工具函数
 */

/**
 * 安全执行函数 - 捕获异常并返回Result
 */
inline fun <T> runSafely(action: () -> T): Result<T> {
    return try {
        Result.success(action())
    } catch (e: Exception) {
        Timber.e(e, "执行操作失败")
        Result.failure(e)
    }
}

/**
 * 安全执行函数 - 带默认值
 */
inline fun <T> runSafelyWithDefault(defaultValue: T, action: () -> T): T {
    return try {
        action()
    } catch (e: Exception) {
        Timber.e(e, "执行操作失败，返回默认值")
        defaultValue
    }
}

/**
 * 延迟执行
 */
suspend fun delay(millis: Long) {
    kotlinx.coroutines.delay(millis)
}

/**
 * 条件执行
 */
inline fun <T> T.takeIfNotNull(predicate: (T) -> Boolean): T? {
    return if (predicate(this)) this else null
}

/**
 * 安全转换
 */
inline fun <T, R> T.mapSafely(transform: (T) -> R): R? {
    return try {
        transform(this)
    } catch (e: Exception) {
        Timber.e(e, "转换失败")
        null
    }
}

/**
 * 判断字符串是否为空或空白
 */
fun String?.isNullOrEmpty(): Boolean {
    return this?.isBlank() ?: true
}

/**
 * 判断字符串是否不为空且不为空白
 */
fun String?.isNotNullAndNotEmpty(): Boolean {
    return !isNullOrEmpty()
} 