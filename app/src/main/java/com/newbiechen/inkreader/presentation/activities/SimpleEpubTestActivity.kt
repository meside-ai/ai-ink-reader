package com.newbiechen.inkreader.presentation.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newbiechen.inkreader.presentation.viewmodels.FilePickerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@AndroidEntryPoint
class SimpleEpubTestActivity : AppCompatActivity() {

    private val filePickerViewModel: FilePickerViewModel by viewModels()
    private lateinit var statusText: TextView
    private lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createUI()
        observeViewModel()
    }

    private fun createUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        statusText = TextView(this).apply {
            text = "简化EPUB测试 - 准备创建内部测试文件"
            textSize = 16f
            setPadding(0, 0, 0, 20)
        }

        testButton = Button(this).apply {
            text = "创建并测试EPUB"
            textSize = 16f
            setOnClickListener {
                createAndTestEpub()
            }
        }

        layout.addView(statusText)
        layout.addView(testButton)
        setContentView(layout)
    }

    private fun createAndTestEpub() {
        statusText.text = "正在创建测试EPUB文件..."
        testButton.isEnabled = false
        
        try {
            Log.d("SimpleEpubTest", "Starting EPUB creation and test")
            
            // 创建测试EPUB文件在内部存储
            val testFile = createSimpleEpubFile()
            
            if (testFile.exists()) {
                statusText.text = "EPUB文件已创建，大小: ${testFile.length()} bytes"
                Log.d("SimpleEpubTest", "EPUB file created: ${testFile.absolutePath}, size: ${testFile.length()}")
                
                // 创建URI并测试
                val fileUri = Uri.fromFile(testFile)
                Log.d("SimpleEpubTest", "Created URI: $fileUri")
                
                statusText.text = "开始测试EPUB导入..."
                filePickerViewModel.processSelectedFile(fileUri)
                
            } else {
                statusText.text = "创建EPUB文件失败"
                Log.e("SimpleEpubTest", "Failed to create EPUB file")
                testButton.isEnabled = true
            }
            
        } catch (e: Exception) {
            Log.e("SimpleEpubTest", "Error in test", e)
            statusText.text = "测试出错: ${e.message}"
            testButton.isEnabled = true
            Toast.makeText(this, "测试失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createSimpleEpubFile(): File {
        val epubFile = File(filesDir, "simple_test.epub")
        
        // EPUB内容
        val mimetype = "application/epub+zip"
        val containerXml = """<?xml version="1.0"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>"""

        val contentOpf = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId" version="2.0">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>简单测试图书</dc:title>
    <dc:creator>测试作者</dc:creator>
    <dc:language>zh-CN</dc:language>
    <dc:identifier id="BookId">simple-test-001</dc:identifier>
  </metadata>
  <manifest>
    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
    <item id="chapter1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
  </manifest>
  <spine toc="ncx">
    <itemref idref="chapter1"/>
  </spine>
</package>"""

        val tocNcx = """<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
  <head>
    <meta name="dtb:uid" content="simple-test-001"/>
  </head>
  <docTitle>
    <text>简单测试图书</text>
  </docTitle>
  <navMap>
    <navPoint id="navpoint-1" playOrder="1">
      <navLabel><text>第一章</text></navLabel>
      <content src="chapter1.xhtml"/>
    </navPoint>
  </navMap>
</ncx>"""

        val chapterXhtml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>第一章</title>
</head>
<body>
  <h1>第一章：测试章节</h1>
  <p>这是一个简单的测试章节，用于验证EPUB解析功能是否正常工作。</p>
  <p>如果您能看到这段文字，说明EPUB文件已经成功解析并导入到应用中。</p>
</body>
</html>"""

        ZipOutputStream(FileOutputStream(epubFile)).use { zip ->
            // mimetype必须不压缩且放在第一个
            zip.setMethod(ZipOutputStream.STORED)
            val mimetypeEntry = ZipEntry("mimetype").apply {
                size = mimetype.toByteArray().size.toLong()
                crc = mimetype.toByteArray().let { bytes ->
                    val crc = java.util.zip.CRC32()
                    crc.update(bytes)
                    crc.value
                }
            }
            zip.putNextEntry(mimetypeEntry)
            zip.write(mimetype.toByteArray())
            zip.closeEntry()
            
            // 恢复压缩模式
            zip.setMethod(ZipOutputStream.DEFLATED)
            
            // META-INF目录
            zip.putNextEntry(ZipEntry("META-INF/container.xml"))
            zip.write(containerXml.toByteArray())
            zip.closeEntry()
            
            // OEBPS目录
            zip.putNextEntry(ZipEntry("OEBPS/content.opf"))
            zip.write(contentOpf.toByteArray())
            zip.closeEntry()
            
            zip.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
            zip.write(tocNcx.toByteArray())
            zip.closeEntry()
            
            zip.putNextEntry(ZipEntry("OEBPS/chapter1.xhtml"))
            zip.write(chapterXhtml.toByteArray())
            zip.closeEntry()
        }
        
        Log.d("SimpleEpubTest", "EPUB file created at: ${epubFile.absolutePath}")
        return epubFile
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            filePickerViewModel.uiState.collectLatest { state ->
                Log.d("SimpleEpubTest", "State update: loading=${state.isLoading}, error=${state.error}, bookId=${state.bookId}")
                
                when {
                    state.isLoading -> {
                        statusText.text = "正在处理EPUB文件..."
                    }
                    
                    state.error != null -> {
                        statusText.text = "错误: ${state.error}"
                        testButton.isEnabled = true
                        Toast.makeText(this@SimpleEpubTestActivity, "导入失败: ${state.error}", Toast.LENGTH_LONG).show()
                    }
                    
                    state.bookId != null -> {
                        statusText.text = "成功！图书ID: ${state.bookId}"
                        testButton.isEnabled = true
                        Toast.makeText(this@SimpleEpubTestActivity, "导入成功！", Toast.LENGTH_SHORT).show()
                        
                        // 验证内部文件
                        verifyInternalFiles()
                    }
                    
                    else -> {
                        testButton.isEnabled = true
                    }
                }
            }
        }
    }
    
    private fun verifyInternalFiles() {
        try {
            val booksDir = File(filesDir, "books")
            if (booksDir.exists()) {
                val epubFiles = booksDir.listFiles { file -> file.name.endsWith(".epub") }
                Log.d("SimpleEpubTest", "Found ${epubFiles?.size ?: 0} EPUB files in internal storage")
                epubFiles?.forEach { file ->
                    Log.d("SimpleEpubTest", "Internal EPUB: ${file.name}, size: ${file.length()}")
                }
            }
        } catch (e: Exception) {
            Log.e("SimpleEpubTest", "Error verifying internal files", e)
        }
    }
} 