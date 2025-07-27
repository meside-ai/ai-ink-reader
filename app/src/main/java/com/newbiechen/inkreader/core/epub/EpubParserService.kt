package com.newbiechen.inkreader.core.epub

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.newbiechen.inkreader.core.epub.models.*
import com.newbiechen.inkreader.utils.runSafely
import java.util.UUID

/**
 * EPUB解析服务实现
 * 
 * 负责解析EPUB文件，提取元数据、章节信息和内容
 */
@Singleton
class EpubParserService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val CONTAINER_PATH = "META-INF/container.xml"
        private const val MIMETYPE_PATH = "mimetype"
        private const val EXPECTED_MIMETYPE = "application/epub+zip"
        
        // 支持的图片格式
        private val SUPPORTED_IMAGE_FORMATS = setOf("jpg", "jpeg", "png", "gif", "webp", "svg")
        
        // 平均阅读速度（字符/分钟）
        private const val AVERAGE_READING_SPEED = 200
    }
    
    /**
     * 解析EPUB文件并返回元数据
     */
    suspend fun parseEpub(filePath: String): EpubParseResult = withContext(Dispatchers.IO) {
        runSafely {
            Timber.d("开始解析EPUB文件: $filePath")
            
            val file = File(filePath)
            if (!file.exists() || !file.canRead()) {
                throw FileNotFoundException("EPUB文件不存在或无法读取: $filePath")
            }
            
            // 验证EPUB文件格式
            validateEpubFile(file)
            
            // 解析EPUB内容
            val metadata = parseEpubContent(file)
            
            Timber.d("EPUB解析完成: ${metadata.title}, 章节数: ${metadata.chapters.size}")
            EpubParseResult.Success(metadata)
            
        }.getOrElse { exception ->
            Timber.e(exception, "EPUB解析失败: $filePath")
            EpubParseResult.Error(exception, "EPUB文件解析失败: ${exception.message}")
        }
    }
    
    /**
     * 验证EPUB文件格式
     */
    private fun validateEpubFile(file: File) {
        ZipInputStream(FileInputStream(file)).use { zipStream ->
            var entry: ZipEntry?
            var hasMimetype = false
            
            while (zipStream.nextEntry.also { entry = it } != null) {
                if (entry?.name == MIMETYPE_PATH) {
                    val mimetype = zipStream.readBytes().toString(Charsets.UTF_8).trim()
                    if (mimetype != EXPECTED_MIMETYPE) {
                        throw IllegalArgumentException("无效的EPUB文件类型: $mimetype")
                    }
                    hasMimetype = true
                    break
                }
            }
            
            if (!hasMimetype) {
                throw IllegalArgumentException("缺少mimetype文件，不是有效的EPUB格式")
            }
        }
    }
    
    /**
     * 解析EPUB内容
     */
    private fun parseEpubContent(file: File): EpubMetadata {
        val zipEntries = mutableMapOf<String, ByteArray>()
        
        // 读取所有ZIP条目到内存
        ZipInputStream(FileInputStream(file)).use { zipStream ->
            var entry: ZipEntry?
            while (zipStream.nextEntry.also { entry = it } != null) {
                entry?.let { zipEntry ->
                    if (!zipEntry.isDirectory) {
                        zipEntries[zipEntry.name] = zipStream.readBytes()
                    }
                }
            }
        }
        
        // 解析container.xml获取rootfile路径
        val containerXml = zipEntries[CONTAINER_PATH]
            ?: throw IllegalArgumentException("缺少container.xml文件")
        
        val rootfilePath = parseContainerXml(containerXml)
        
        // 解析rootfile（通常是.opf文件）
        val rootfileContent = zipEntries[rootfilePath]
            ?: throw IllegalArgumentException("找不到rootfile: $rootfilePath")
        
        return parseOpfFile(rootfileContent, zipEntries, file.absolutePath, file.length(), rootfilePath)
    }
    
    /**
     * 解析container.xml文件
     */
    private fun parseContainerXml(containerData: ByteArray): String {
        val containerXml = String(containerData, Charsets.UTF_8)
        val doc = Jsoup.parse(containerXml, "", org.jsoup.parser.Parser.xmlParser())
        
        val rootfileElement = doc.selectFirst("rootfile")
            ?: throw IllegalArgumentException("container.xml中找不到rootfile元素")
        
        return rootfileElement.attr("full-path")
            .takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("rootfile路径为空")
    }
    
    /**
     * 解析OPF文件（EPUB的核心元数据文件）
     */
    private fun parseOpfFile(
        opfData: ByteArray,
        zipEntries: Map<String, ByteArray>,
        filePath: String,
        fileSize: Long,
        opfPath: String
    ): EpubMetadata {
        val opfContent = String(opfData, Charsets.UTF_8)
        val doc = Jsoup.parse(opfContent, "", org.jsoup.parser.Parser.xmlParser())
        
        // 解析元数据
        val metadata = parseMetadata(doc)
        
        // 解析manifest（资源清单）
        val manifest = parseManifest(doc, opfPath)
        
        // 解析spine（阅读顺序）
        val spine = parseSpine(doc)
        
        // 解析导航信息（TOC）
        val chapters = parseNavigation(zipEntries, manifest, spine, opfPath)
        
        // 处理封面图片
        val coverImagePath = extractCoverImage(zipEntries, manifest, filePath)
        
        return EpubMetadata(
            bookId = UUID.randomUUID().toString(),
            title = metadata["title"] ?: "未知标题",
            author = metadata["creator"] ?: "未知作者",
            publisher = metadata["publisher"],
            description = metadata["description"],
            language = metadata["language"] ?: "zh-CN",
            isbn = metadata["identifier"],
            publicationDate = metadata["date"],
            coverImagePath = coverImagePath,
            filePath = filePath,
            fileSize = fileSize,
            totalChapters = chapters.size,
            chapters = chapters,
            spine = spine,
            manifest = manifest
        )
    }
    
    /**
     * 解析OPF元数据
     */
    private fun parseMetadata(doc: Document): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        doc.select("metadata > *").forEach { element ->
            val tagName = element.tagName().lowercase()
            val content = element.text().trim()
            
            when (tagName) {
                "dc:title" -> metadata["title"] = content
                "dc:creator" -> metadata["creator"] = content
                "dc:publisher" -> metadata["publisher"] = content
                "dc:description" -> metadata["description"] = content
                "dc:language" -> metadata["language"] = content
                "dc:identifier" -> metadata["identifier"] = content
                "dc:date" -> metadata["date"] = content
                "meta" -> {
                    // 处理meta标签
                    val name = element.attr("name")
                    val property = element.attr("property")
                    val content = element.attr("content")
                    
                    when {
                        name.isNotBlank() -> metadata[name] = content
                        property.isNotBlank() -> metadata[property] = content
                    }
                }
            }
        }
        
        return metadata
    }
    
    /**
     * 解析manifest（资源清单）
     */
    private fun parseManifest(doc: Document, opfPath: String): Map<String, ManifestItem> {
        val manifest = mutableMapOf<String, ManifestItem>()
        val opfDir = File(opfPath).parent ?: ""
        
        doc.select("manifest item").forEach { item ->
            val id = item.attr("id")
            val href = item.attr("href")
            val mediaType = item.attr("media-type")
            val properties = item.attr("properties")
                .split(" ")
                .filter { it.isNotBlank() }
            
            if (id.isNotBlank() && href.isNotBlank()) {
                // 处理相对路径
                val fullHref = if (opfDir.isNotBlank()) {
                    "$opfDir/$href".replace("//", "/")
                } else {
                    href
                }
                
                manifest[id] = ManifestItem(
                    id = id,
                    href = fullHref,
                    mediaType = mediaType,
                    properties = properties
                )
            }
        }
        
        return manifest
    }
    
    /**
     * 解析spine（阅读顺序）
     */
    private fun parseSpine(doc: Document): List<String> {
        return doc.select("spine itemref").map { itemref ->
            itemref.attr("idref")
        }.filter { it.isNotBlank() }
    }
    
    /**
     * 解析导航信息生成章节列表
     */
    private fun parseNavigation(
        zipEntries: Map<String, ByteArray>,
        manifest: Map<String, ManifestItem>,
        spine: List<String>,
        opfPath: String
    ): List<ChapterInfo> {
        // 首先尝试解析EPUB3的nav文件
        val navItem = manifest.values.find { it.isNavigation() }
        if (navItem != null) {
            val navContent = zipEntries[navItem.href]
            if (navContent != null) {
                return parseNavDocument(navContent)
            }
        }
        
        // 如果没有nav文件，根据spine生成章节
        return generateChaptersFromSpine(zipEntries, manifest, spine)
    }
    
    /**
     * 解析EPUB3导航文档
     */
    private fun parseNavDocument(navData: ByteArray): List<ChapterInfo> {
        val navContent = String(navData, Charsets.UTF_8)
        val doc = Jsoup.parse(navContent)
        val chapters = mutableListOf<ChapterInfo>()
        
        // 查找导航列表
        doc.select("nav[epub:type=toc] ol li, nav ol li").forEach { li ->
            val link = li.selectFirst("a")
            if (link != null) {
                val title = link.text().trim()
                val href = link.attr("href")
                
                if (title.isNotBlank() && href.isNotBlank()) {
                    // 分离锚点
                    val (path, anchor) = if (href.contains("#")) {
                        href.split("#", limit = 2).let { parts ->
                            parts[0] to parts.getOrNull(1)
                        }
                    } else {
                        href to null
                    }
                    
                    chapters.add(
                        ChapterInfo(
                            title = title,
                            href = path,
                            anchor = anchor,
                            level = getElementLevel(li)
                        )
                    )
                }
            }
        }
        
        return chapters
    }
    
    /**
     * 从spine生成章节列表
     */
    private fun generateChaptersFromSpine(
        zipEntries: Map<String, ByteArray>,
        manifest: Map<String, ManifestItem>,
        spine: List<String>
    ): List<ChapterInfo> {
        return spine.mapIndexedNotNull { index, itemId ->
            val manifestItem = manifest[itemId]
            if (manifestItem?.isHtmlContent() == true) {
                // 尝试从文件内容中提取标题
                val content = zipEntries[manifestItem.href]
                val title = if (content != null) {
                    extractTitleFromContent(content) ?: "第${index + 1}章"
                } else {
                    "第${index + 1}章"
                }
                
                ChapterInfo(
                    title = title,
                    href = manifestItem.href,
                    level = 1
                )
            } else {
                null
            }
        }
    }
    
    /**
     * 从HTML内容中提取标题
     */
    private fun extractTitleFromContent(contentData: ByteArray): String? {
        return try {
            val content = String(contentData, Charsets.UTF_8)
            val doc = Jsoup.parse(content)
            
            // 尝试多种方式提取标题
            listOf("h1", "h2", "h3", "title").firstNotNullOfOrNull { selector ->
                doc.selectFirst(selector)?.text()?.trim()?.takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取元素层级
     */
    private fun getElementLevel(element: org.jsoup.nodes.Element): Int {
        var level = 1
        var parent = element.parent()
        
        while (parent != null && parent.tagName() != "nav") {
            if (parent.tagName() == "ol" || parent.tagName() == "ul") {
                level++
            }
            parent = parent.parent()
        }
        
        return level
    }
    
    /**
     * 提取并保存封面图片
     */
    private fun extractCoverImage(
        zipEntries: Map<String, ByteArray>,
        manifest: Map<String, ManifestItem>,
        epubPath: String
    ): String? {
        // 查找封面图片
        val coverItem = manifest.values.find { item ->
            item.isImage() && (
                item.properties.contains("cover-image") ||
                item.id.contains("cover", ignoreCase = true) ||
                item.href.contains("cover", ignoreCase = true)
            )
        }
        
        if (coverItem != null) {
            val imageData = zipEntries[coverItem.href]
            if (imageData != null) {
                return saveCoverImage(imageData, epubPath, coverItem.href)
            }
        }
        
        return null
    }
    
    /**
     * 保存封面图片到本地缓存
     */
    private fun saveCoverImage(imageData: ByteArray, epubPath: String, originalPath: String): String? {
        return try {
            val cacheDir = File(context.cacheDir, "covers")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val extension = File(originalPath).extension.lowercase()
            val fileName = "${File(epubPath).nameWithoutExtension}_cover.$extension"
            val coverFile = File(cacheDir, fileName)
            
            coverFile.writeBytes(imageData)
            coverFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "保存封面图片失败")
            null
        }
    }
    
    /**
     * 获取章节内容
     */
    suspend fun getChapterContent(filePath: String, chapterPath: String): String = withContext(Dispatchers.IO) {
        ZipInputStream(FileInputStream(filePath)).use { zipStream ->
            var entry: ZipEntry?
            while (zipStream.nextEntry.also { entry = it } != null) {
                if (entry?.name == chapterPath) {
                    val content = zipStream.readBytes().toString(Charsets.UTF_8)
                    return@withContext processHtmlContent(content)
                }
            }
        }
        throw FileNotFoundException("章节文件不存在: $chapterPath")
    }
    
    /**
     * 处理HTML内容，优化为适合墨水屏显示
     */
    private fun processHtmlContent(htmlContent: String): String {
        val doc = Jsoup.parse(htmlContent)
        
        // 移除不需要的元素
        doc.select("script, style, meta, link[rel=stylesheet]").remove()
        
        // 优化图片标签
        doc.select("img").forEach { img ->
            img.attr("style", "max-width: 100%; height: auto;")
        }
        
        // 简化CSS
        doc.select("[style]").forEach { element ->
            element.removeAttr("style")
        }
        
        return doc.html()
    }
    
    /**
     * 验证EPUB文件
     */
    suspend fun validateEpubFile(filePath: String): EpubValidationResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            val file = File(filePath)
            
            // 基本文件检查
            if (!file.exists()) {
                errors.add("文件不存在")
                return@withContext EpubValidationResult(false, errors, warnings)
            }
            
            if (!file.canRead()) {
                errors.add("文件无法读取")
                return@withContext EpubValidationResult(false, errors, warnings)
            }
            
            if (file.length() == 0L) {
                errors.add("文件为空")
                return@withContext EpubValidationResult(false, errors, warnings)
            }
            
            // EPUB格式检查
            try {
                validateEpubFile(file)
            } catch (e: Exception) {
                errors.add("EPUB格式错误: ${e.message}")
            }
            
            // 大小检查
            if (file.length() > 100 * 1024 * 1024) { // 100MB
                warnings.add("文件较大，解析可能较慢")
            }
            
        } catch (e: Exception) {
            errors.add("验证过程出错: ${e.message}")
        }
        
        EpubValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
} 