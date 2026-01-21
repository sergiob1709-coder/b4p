package com.battle4play.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.AsyncImage
import com.battle4play.app.ui.theme.Battle4PlayTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.math.ceil
import java.util.concurrent.TimeUnit

private const val POSTS_API_URL =
    "https://www.battle4play.com/wp-json/wp/v2/posts?per_page=6&_embed"
private const val PAGE_SIZE = 6
private const val MAX_ITEMS = 6

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("Battle4Play", "MainActivity onCreate")
        setContent {
            Battle4PlayTheme {
                Battle4PlayScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Battle4PlayScreen() {
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
        Log.d("Battle4Play", "Loading posts from $POSTS_API_URL")
        try {
            items = RssRepository.fetchNews()
            selectedItem = items.firstOrNull()
            if (items.isEmpty()) {
                errorMessage = "No hay noticias disponibles en este momento."
            }
        } catch (error: IOException) {
            Log.e("Battle4Play", "Network error loading posts", error)
            errorMessage = "No se pudieron cargar las noticias. Revisa tu conexión."
        } catch (error: Exception) {
            Log.e("Battle4Play", "Unexpected error loading posts", error)
            errorMessage = "Hubo un problema procesando las noticias."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        Log.d("Battle4Play", "Battle4PlayScreen composed")
        loadRss()
    }

    val totalPages = if (items.isEmpty()) 1 else ceil(items.size / PAGE_SIZE.toDouble()).toInt()
    val pageItems = items.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Battle4Play Noticias")
                        Text(
                            text = "Fuente: Battle4Play",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            errorMessage?.let { message ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer)
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
            }

            if (totalPages > 1) {
                item {
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
                }
            }

            items(pageItems) { item ->
                NewsCard(
                    item = item,
                    onClick = { selectedItem = item },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                AnimatedVisibility(visible = selectedItem != null) {
                    selectedItem?.let { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
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

data class NewsItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val imageUrl: String?
) {
    val plainDescription: String = runCatching {
        HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
    }.getOrDefault(description)
}

private object RssRepository {
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchNews(): List<NewsItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(POSTS_API_URL)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Battle4PlayRSS"
            )
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("Battle4Play", "Posts API request failed with ${response.code}")
                return@withContext emptyList()
            }
            val body = response.body ?: return@withContext emptyList()
            val items = parsePosts(body.string())
            if (items.isEmpty()) {
                Log.w("Battle4Play", "Posts API parsed with 0 items")
            }
            items.take(MAX_ITEMS)
        }
    }

    private fun parsePosts(payload: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        val json = runCatching { JSONArray(payload) }.getOrNull() ?: return items
        for (index in 0 until json.length()) {
            val post = json.optJSONObject(index) ?: continue
            val title = post.optJSONObject("title")?.optString("rendered").orEmpty()
            val link = post.optString("link")
            if (link.isBlank()) continue
            val description = post.optJSONObject("excerpt")?.optString("rendered").orEmpty()
            val pubDate = post.optString("date")
            val imageUrl = extractFeaturedImage(post)
            items.add(
                NewsItem(
                    title = title.ifBlank { "Battle4Play" },
                    link = link,
                    description = description,
                    pubDate = pubDate,
                    imageUrl = imageUrl
                )
            )
        }
        return items
    }

    private fun extractFeaturedImage(post: JSONObject): String? {
        val embedded = post.optJSONObject("_embedded") ?: return null
        val mediaArray = embedded.optJSONArray("wp:featuredmedia") ?: return null
        val media = mediaArray.optJSONObject(0) ?: return null
        val directUrl = media.optString("source_url")
        if (directUrl.isNotBlank()) {
            return directUrl
        }
        val sizes = media.optJSONObject("media_details")
            ?.optJSONObject("sizes")
            ?.optJSONObject("medium")
        val sizedUrl = sizes?.optString("source_url")
        return sizedUrl?.takeIf { it.isNotBlank() }
    }
}
