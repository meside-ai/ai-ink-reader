package com.newbiechen.inkreader.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.newbiechen.inkreader.BuildConfig

/**
 * 墨水屏AI阅读器应用入口类
 * 
 * 职责：
 * - 初始化Hilt依赖注入
 * - 配置全局日志系统
 * - 设置应用级别的配置
 */
@HiltAndroidApp
class InkReaderApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        initializeLogging()
        initializeDebugTools()
        
        Timber.i("墨水屏AI阅读器启动 - 版本: ${BuildConfig.VERSION_NAME}")
    }
    
    /**
     * 初始化日志系统
     */
    private fun initializeLogging() {
        if (BuildConfig.IS_DEBUG_BUILD) {
            // Debug模式下使用详细日志
            Timber.plant(Timber.DebugTree())
        } else {
            // Release模式下使用发布版日志（可以添加Crashlytics等）
            Timber.plant(ReleaseTree())
        }
    }
    
    /**
     * 初始化调试工具
     */
    private fun initializeDebugTools() {
        if (BuildConfig.IS_DEBUG_BUILD) {
            // Debug模式下的工具初始化
            // StrictMode、LeakCanary等会自动初始化
            Timber.d("调试工具已启用")
        }
    }
    
    /**
     * 发布版本日志树 - 只记录警告和错误
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= android.util.Log.WARN) {
                // 在这里可以添加崩溃报告服务
                // 比如：Crashlytics.log(message)
                super.log(priority, tag, message, t)
            }
        }
    }
} 