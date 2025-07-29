package com.newbiechen.inkreader.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 墨水屏阅读器应用程序类
 */
@HiltAndroidApp
class InkReaderApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 基本初始化
        android.util.Log.d("InkReader", "应用程序启动成功")
    }
} 