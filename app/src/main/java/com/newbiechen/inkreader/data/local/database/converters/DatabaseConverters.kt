package com.newbiechen.inkreader.data.local.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * 数据库类型转换器
 * 
 * 用于Room数据库中复杂数据类型的序列化和反序列化
 */
class DatabaseConverters {
    
    private val gson = Gson()
    
    /**
     * Date类型转换器
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * 字符串列表转换器
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    /**
     * 整数列表转换器
     */
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let {
            val listType = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    /**
     * Map<String, String>转换器
     */
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(it, mapType)
        }
    }
    
    /**
     * Map<String, Any>转换器（用于存储JSON对象）
     */
    @TypeConverter
    fun fromAnyMap(value: Map<String, Any>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toAnyMap(value: String?): Map<String, Any>? {
        return value?.let {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(it, mapType)
        }
    }
} 