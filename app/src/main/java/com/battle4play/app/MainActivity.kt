package com.battle4play.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.battle4play.app.ui.theme.Battle4PlayTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val PAGE_SIZE = 6
private const val POSTS_API_URL =
    "https://www.battle4play.com/wp-json/wp/v2/posts?per_page=$PAGE_SIZE&_embed"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
    var items by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var currentPage by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadRss(page: Int) {
        isLoading = true
        errorMessage = null
        Log.d("Battle4Play", "Loading posts page $page from $POSTS_API_URL")
        try {
            items = RssRepository.fetchNews(page)
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

    LaunchedEffect(currentPage) {
        Log.d("Battle4Play", "Battle4PlayScreen composed")
        loadRss(currentPage)
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 12.dp),
        ) {
            if (isLoading) {
                Text(
                    text = "Cargando noticias...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(onClick = {
                            scope.launch {
                                loadRss(currentPage)
                            }
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { if (currentPage > 1) currentPage -= 1 },
                    enabled = currentPage > 1
                ) {
                    Text("<")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Página $currentPage",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { if (items.size == PAGE_SIZE) currentPage += 1 },
                    enabled = items.size == PAGE_SIZE && !isLoading
                ) {
                    Text(">")
                }
            }

            items.forEach { item ->
                NewsTitleCard(item = item, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun NewsTitleCard(item: NewsItem, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.padding(12.dp)) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
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
    val imageUrl: String?
)

private object RssRepository {
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchNews(page: Int): List<NewsItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$POSTS_API_URL&page=$page")
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
            items
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
            val imageUrl = extractFeaturedImage(post)
            items.add(
                NewsItem(
                    title = title.ifBlank { "Battle4Play" },
                    link = link,
                    imageUrl = imageUrl
                )
            )
        }
        return items
    }

    private fun extractFeaturedImage(post: org.json.JSONObject): String? {
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
