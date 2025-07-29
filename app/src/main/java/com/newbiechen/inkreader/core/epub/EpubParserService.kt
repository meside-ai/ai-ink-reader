package com.newbiechen.inkreader.core.epub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.newbiechen.inkreader.core.epub.models.EpubChapter
import com.newbiechen.inkreader.core.epub.models.EpubMetadata
import com.newbiechen.inkreader.core.epub.models.ValidationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface EpubParserService {
    suspend fun parseEpub(filePath: String): Result<EpubMetadata>
    suspend fun extractChapters(filePath: String): Result<List<EpubChapter>>
    suspend fun getChapterContent(filePath: String, chapterHref: String): Result<String>
    suspend fun extractCoverImage(filePath: String, outputDir: String): Result<String?>
    suspend fun validateEpubFile(filePath: String): ValidationResult
    suspend fun getWordCount(htmlContent: String): Int
}

@Singleton
class EpubParserServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : EpubParserService {

    private val epubReader = EpubReader()

    override suspend fun parseEpub(filePath: String): Result<EpubMetadata> = withContext(Dispatchers.IO) {
        try {
            val inputStream = FileInputStream(filePath)
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val metadata = book.metadata
            val chapters = book.spine.spineReferences

            val epubMetadata = EpubMetadata(
                title = metadata.titles.firstOrNull() ?: "Unknown Title",
                author = metadata.authors.firstOrNull()?.let { "${it.firstname} ${it.lastname}".trim() } ?: "Unknown Author",
                publisher = metadata.publishers.firstOrNull() ?: null,
                language = metadata.language ?: "zh-CN",
                identifier = metadata.identifiers.firstOrNull()?.value,
                description = metadata.descriptions.firstOrNull(),
                subjects = metadata.subjects.toList(),
                publishDate = metadata.dates.firstOrNull()?.value,
                rights = metadata.rights.firstOrNull(),
                totalChapters = chapters.size,
                wordCount = 0 // Will be calculated during chapter processing
            )

            Result.success(epubMetadata)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse EPUB: ${e.message}", e))
        }
    }

    override suspend fun extractChapters(filePath: String): Result<List<EpubChapter>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = FileInputStream(filePath)
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val chapters = mutableListOf<EpubChapter>()
            val spineReferences = book.spine.spineReferences

            spineReferences.forEachIndexed { index, spineReference ->
                val resource = spineReference.resource
                val content = String(resource.data, Charsets.UTF_8)
                val cleanContent = cleanHtmlContent(content)
                val wordCount = getWordCount(cleanContent)
                
                // Extract title from content or use default
                val title = extractTitleFromHtml(content) ?: "Chapter ${index + 1}"

                chapters.add(
                    EpubChapter(
                        id = resource.id ?: "${index}",
                        title = title,
                        href = resource.href,
                        order = index,
                        htmlContent = cleanContent,
                        wordCount = wordCount
                    )
                )
            }

            Result.success(chapters)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to extract chapters: ${e.message}", e))
        }
    }

    override suspend fun getChapterContent(filePath: String, chapterHref: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = FileInputStream(filePath)
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val resource = book.resources.getByHref(chapterHref)
            if (resource != null) {
                val content = String(resource.data, Charsets.UTF_8)
                val cleanContent = cleanHtmlContent(content)
                Result.success(cleanContent)
            } else {
                Result.failure(Exception("Chapter not found: $chapterHref"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get chapter content: ${e.message}", e))
        }
    }

    override suspend fun extractCoverImage(filePath: String, outputDir: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val inputStream = FileInputStream(filePath)
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val coverImage = book.coverImage
            if (coverImage != null) {
                val outputFile = File(outputDir, "cover_${System.currentTimeMillis()}.jpg")
                val bitmap = BitmapFactory.decodeByteArray(coverImage.data, 0, coverImage.data.size)
                
                if (bitmap != null) {
                    val outputStream = FileOutputStream(outputFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.close()
                    Result.success(outputFile.absolutePath)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to extract cover image: ${e.message}", e))
        }
    }

    override suspend fun validateEpubFile(filePath: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext ValidationResult(false, "File does not exist")
            }

            if (!file.canRead()) {
                return@withContext ValidationResult(false, "Cannot read file")
            }

            if (!filePath.lowercase().endsWith(".epub")) {
                return@withContext ValidationResult(false, "File is not an EPUB file")
            }

            // Try to parse the EPUB
            val inputStream = FileInputStream(filePath)
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val warnings = mutableListOf<String>()
            
            if (book.metadata.titles.isEmpty()) {
                warnings.add("No title found")
            }
            
            if (book.metadata.authors.isEmpty()) {
                warnings.add("No author found")
            }
            
            if (book.spine.spineReferences.isEmpty()) {
                warnings.add("No chapters found")
            }

            ValidationResult(true, null, warnings)
        } catch (e: Exception) {
            ValidationResult(false, "Invalid EPUB file: ${e.message}")
        }
    }

    override suspend fun getWordCount(htmlContent: String): Int = withContext(Dispatchers.Default) {
        val text = Jsoup.parse(htmlContent).text()
        // Count words for both Chinese and English
        val chineseChars = text.count { it.toString().matches(Regex("[\\u4e00-\\u9fa5]")) }
        val englishWords = text.split(Regex("\\s+")).filter { 
            it.isNotBlank() && it.matches(Regex(".*[a-zA-Z].*")) 
        }.size
        
        chineseChars + englishWords
    }

    private fun cleanHtmlContent(rawHtml: String): String {
        // Use Jsoup to clean and format HTML content
        val document = Jsoup.parse(rawHtml)
        
        // Remove unnecessary elements
        document.select("script, style, meta, link").remove()
        
        // Clean with whitelist to keep only basic formatting
        val safelist = Safelist.relaxed()
            .addTags("div", "span", "section", "article")
            .addAttributes("div", "class", "id")
            .addAttributes("span", "class", "id")
            .addAttributes("p", "class", "style")
            .addAttributes("h1", "h2", "h3", "h4", "h5", "h6", "style")
        
        return Jsoup.clean(document.html(), safelist)
    }

    private fun extractTitleFromHtml(htmlContent: String): String? {
        val document = Jsoup.parse(htmlContent)
        
        // Try to find title in various ways
        val titleSelectors = listOf("h1", "h2", "title", ".chapter-title", "#chapter-title")
        
        for (selector in titleSelectors) {
            val element = document.select(selector).first()
            if (element != null && element.text().isNotBlank()) {
                return element.text().trim()
            }
        }
        
        return null
    }
} 