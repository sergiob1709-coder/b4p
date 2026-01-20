package com.battle4play.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import com.battle4play.app.ui.theme.Battle4PlayTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import kotlin.math.ceil

private const val RSS_URL = "https://www.battle4play.com/feed/"
private const val PAGE_SIZE = 6

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Battle4PlayTheme {
                Battle4PlayApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Battle4PlayApp() {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<NewsItem?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadRss() {
        isLoading = true
        errorMessage = null
        try {
            items = RssRepository.fetchNews()
            if (items.isNotEmpty()) {
                selectedItem = items.first()
            }
        } catch (error: IOException) {
            errorMessage = "No se pudo cargar el RSS. Revisa tu conexión o la URL del feed."
        } catch (error: Exception) {
            errorMessage = "Hubo un problema procesando el RSS."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadRss()
    }

    val totalPages = if (items.isEmpty()) 1 else ceil(items.size / PAGE_SIZE.toDouble()).toInt()
    val pageItems = items.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Battle4Play RSS")
                        Text(
                            text = "Fuente: $RSS_URL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            errorMessage?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = it, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            currentPage = 0
                            selectedItem = null
                            scope.launch {
                                loadRss()
                            }
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) currentPage -= 1 },
                    enabled = currentPage > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Página anterior")
                }
                Text(text = "Página ${currentPage + 1} de $totalPages")
                IconButton(
                    onClick = { if (currentPage < totalPages - 1) currentPage += 1 },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Página siguiente")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pageItems) { item ->
                    NewsCard(item = item, onClick = { selectedItem = item })
                }
            }

            AnimatedVisibility(visible = selectedItem != null) {
                selectedItem?.let { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            item.imageUrl?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = item.pubDate,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.plainDescription,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Abrir en navegador")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(92.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(92.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.plainDescription,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class NewsItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val imageUrl: String?
) {
    val plainDescription: String = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString()
        .trim()
}

private object RssRepository {
    private val client = OkHttpClient()

    suspend fun fetchNews(): List<NewsItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(RSS_URL)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Battle4PlayRSS"
            )
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}")
        }
        val body = response.body?.string() ?: throw IOException("Respuesta vacía")
        parseRss(body)
    }

    private fun parseRss(xml: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var currentItem: NewsItemBuilder? = null
        var currentText = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "item" -> currentItem = NewsItemBuilder()
                        "media:content", "media:thumbnail" -> {
                            val imageUrl = parser.getAttributeValue(null, "url")
                            if (!imageUrl.isNullOrBlank()) {
                                currentItem?.imageUrl = imageUrl
                            }
                        }
                        "enclosure" -> {
                            val type = parser.getAttributeValue(null, "type") ?: ""
                            val url = parser.getAttributeValue(null, "url")
                            if (type.startsWith("image") && !url.isNullOrBlank()) {
                                currentItem?.imageUrl = url
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> currentText = parser.text
                XmlPullParser.END_TAG -> {
                    when (parser.name.lowercase()) {
                        "item" -> {
                            currentItem?.build()?.let { items.add(it) }
                            currentItem = null
                        }
                        "title" -> currentItem?.title = currentText.trim()
                        "link" -> currentItem?.link = currentText.trim()
                        "description" -> currentItem?.description = currentText.trim()
                        "content:encoded" -> {
                            if (currentItem?.description.isNullOrBlank()) {
                                currentItem?.description = currentText.trim()
                            }
                        }
                        "pubdate" -> currentItem?.pubDate = currentText.trim()
                    }
                }
            }
            eventType = parser.next()
        }

        return items
            .map { item ->
                val imageFromDescription = item.imageUrl ?: extractImageFromDescription(item.description)
                item.copy(imageUrl = imageFromDescription)
            }
            .filter { it.title.isNotBlank() }
    }

    private fun extractImageFromDescription(description: String): String? {
        val regex = Regex("<img[^>]+src=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
        return regex.find(description)?.groups?.get(1)?.value
    }

    private data class NewsItemBuilder(
        var title: String = "",
        var link: String = "",
        var description: String = "",
        var pubDate: String = "",
        var imageUrl: String? = null
    ) {
        fun build(): NewsItem = NewsItem(
            title = title,
            link = link,
            description = description,
            pubDate = pubDate,
            imageUrl = imageUrl
        )
    }
}
