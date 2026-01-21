package com.battle4play.app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import androidx.compose.material.CircularProgressIndicator

private const val PAGE_SIZE = 6
private const val POSTS_API_URL =
    "https://www.battle4play.com/wp-json/wp/v2/posts?per_page=$PAGE_SIZE&_embed"
private const val PS5_SLUG = "playstation-5"
private const val XBOX_SERIES_SLUG = "xbox-series-x"
private const val SWITCH_SLUG = "nintendo-switch"

private enum class AppScreen {
    Home,
    Categories,
    CategoryDetail,
    Search,
    Saved
}

private data class CategoryFilter(
    val title: String,
    val slug: String,
    val enabled: Boolean = true
)

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
    var selectedItem by remember { mutableStateOf<NewsItem?>(null) }
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Home) }
    var savedItems by remember { mutableStateOf<Map<String, NewsItem>>(emptyMap()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchSubmittedQuery by rememberSaveable { mutableStateOf("") }
    var homeItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var homePage by rememberSaveable { mutableStateOf(1) }
    var homeLoading by remember { mutableStateOf(true) }
    var homeError by remember { mutableStateOf<String?>(null) }
    var searchItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var searchPage by rememberSaveable { mutableStateOf(1) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var categoryItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var categoryPage by rememberSaveable { mutableStateOf(1) }
    var categoryLoading by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<CategoryFilter?>(null) }
    var ps5Enabled by rememberSaveable { mutableStateOf(true) }
    var xboxEnabled by rememberSaveable { mutableStateOf(true) }
    var switchEnabled by rememberSaveable { mutableStateOf(true) }
    val categories = remember(ps5Enabled, xboxEnabled, switchEnabled) {
        listOf(
            CategoryFilter(title = "PS5", slug = PS5_SLUG, enabled = ps5Enabled),
            CategoryFilter(title = "Xbox Series", slug = XBOX_SERIES_SLUG, enabled = xboxEnabled),
            CategoryFilter(title = "Nintendo Switch", slug = SWITCH_SLUG, enabled = switchEnabled)
        )
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun loadHome(page: Int) {
        homeLoading = true
        homeError = null
        Log.d("Battle4Play", "Loading home posts page $page from $POSTS_API_URL")
        try {
            homeItems = RssRepository.fetchNews(page = page)
            if (homeItems.isEmpty()) {
                homeError = "No hay noticias disponibles en este momento."
            }
        } catch (error: IOException) {
            Log.e("Battle4Play", "Network error loading posts", error)
            homeError = "No se pudieron cargar las noticias. Revisa tu conexión."
        } catch (error: Exception) {
            Log.e("Battle4Play", "Unexpected error loading posts", error)
            homeError = "Hubo un problema procesando las noticias."
        } finally {
            homeLoading = false
        }
    }

    suspend fun loadSearch(page: Int, query: String) {
        if (query.isBlank()) return
        searchLoading = true
        searchError = null
        Log.d("Battle4Play", "Loading search page $page for query $query")
        try {
            searchItems = RssRepository.fetchNews(page = page, searchQuery = query)
            if (searchItems.isEmpty()) {
                searchError = "No se encontraron noticias con tu búsqueda."
            }
        } catch (error: IOException) {
            Log.e("Battle4Play", "Network error loading search", error)
            searchError = "No se pudieron cargar las noticias. Revisa tu conexión."
        } catch (error: Exception) {
            Log.e("Battle4Play", "Unexpected error loading search", error)
            searchError = "Hubo un problema procesando las noticias."
        } finally {
            searchLoading = false
        }
    }

    suspend fun loadCategory(page: Int, category: CategoryFilter) {
        categoryLoading = true
        categoryError = null
        Log.d("Battle4Play", "Loading category ${category.slug} page $page")
        try {
            categoryItems = RssRepository.fetchNews(page = page, categorySlug = category.slug)
            if (categoryItems.isEmpty()) {
                categoryError = "No hay noticias disponibles en esta categoría."
            }
        } catch (error: IOException) {
            Log.e("Battle4Play", "Network error loading category", error)
            categoryError = "No se pudieron cargar las noticias. Revisa tu conexión."
        } catch (error: Exception) {
            Log.e("Battle4Play", "Unexpected error loading category", error)
            categoryError = "Hubo un problema procesando las noticias."
        } finally {
            categoryLoading = false
        }
    }

    LaunchedEffect(homePage) {
        Log.d("Battle4Play", "Battle4PlayScreen composed")
        loadHome(homePage)
    }

    LaunchedEffect(searchPage, searchSubmittedQuery) {
        loadSearch(searchPage, searchSubmittedQuery)
    }

    LaunchedEffect(categoryPage, selectedCategory) {
        selectedCategory?.let { category ->
            loadCategory(categoryPage, category)
        }
    }

    LaunchedEffect(Unit) {
        savedItems = SavedNewsStore.load(context)
    }

    Scaffold(
        topBar = {
            when {
                selectedItem != null -> {
                    TopAppBar(
                        title = { Text(text = "Detalle") },
                        navigationIcon = {
                            IconButton(onClick = { selectedItem = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        actions = {
                            selectedItem?.let { item ->
                                IconButton(onClick = {
                                    savedItems = toggleSavedItem(savedItems, item)
                                    SavedNewsStore.save(context, savedItems)
                                }) {
                                    Icon(
                                        imageVector = if (savedItems.containsKey(item.link)) {
                                            Icons.Default.Bookmark
                                        } else {
                                            Icons.Outlined.BookmarkBorder
                                        },
                                        contentDescription = "Guardar noticia"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFE6F3E7),
                            titleContentColor = Color(0xFF1F5D3A)
                        )
                    )
                }
                currentScreen == AppScreen.CategoryDetail -> {
                    TopAppBar(
                        title = { Text(text = selectedCategory?.title ?: "Categoría") },
                        navigationIcon = {
                            IconButton(onClick = {
                                currentScreen = AppScreen.Categories
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFE6F3E7),
                            titleContentColor = Color(0xFF1F5D3A)
                        )
                    )
                    when (currentScreen) {
                        AppScreen.Search -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Busca noticias") },
                                singleLine = true
                            )
                        }
                        AppScreen.Categories -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            CategorySwitchRow(
                                label = "PS5",
                                checked = ps5Enabled,
                                onCheckedChange = { ps5Enabled = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CategorySwitchRow(
                                label = "Xbox Series",
                                checked = xboxEnabled,
                                onCheckedChange = { xboxEnabled = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CategorySwitchRow(
                                label = "Nintendo Switch",
                                checked = switchEnabled,
                                onCheckedChange = { switchEnabled = it }
                            )
                        }
                        else -> Unit
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF89D398), Color(0xFFF4F9F4))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "BATTLE4PLAY",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF1F5D3A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Noticias y novedades",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF2E6C44)
                        )
                        if (currentScreen == AppScreen.Search) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Busca noticias") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    searchSubmittedQuery = searchQuery
                                    searchPage = 1
                                    searchItems = emptyList()
                                    searchError = null
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(text = "Buscar")
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE6F3E7)) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Home,
                    onClick = { currentScreen = AppScreen.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Categories || currentScreen == AppScreen.CategoryDetail,
                    onClick = { currentScreen = AppScreen.Categories },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Categorías") },
                    label = { Text("Categorías") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Search,
                    onClick = { currentScreen = AppScreen.Search },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Saved,
                    onClick = { currentScreen = AppScreen.Saved },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Guardados") },
                    label = { Text("Guardados") }
                )
            }
        }
    ) { paddingValues ->
        if (selectedItem == null) {
            when (currentScreen) {
                AppScreen.Home -> {
                    NewsListContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        items = homeItems,
                        isLoading = homeLoading,
                        errorMessage = homeError,
                        onRetry = {
                            scope.launch {
                                loadHome(homePage)
                            }
                        },
                        onItemClick = { selectedItem = it },
                        onToggleSaved = { item ->
                            savedItems = toggleSavedItem(savedItems, item)
                            SavedNewsStore.save(context, savedItems)
                        },
                        isItemSaved = { item -> savedItems.containsKey(item.link) },
                        showPagination = true,
                        currentPage = homePage,
                        canMoveNext = homeItems.size == PAGE_SIZE && !homeLoading,
                        canMovePrevious = homePage > 1,
                        onPreviousPage = { if (homePage > 1) homePage -= 1 },
                        onNextPage = { if (homeItems.size == PAGE_SIZE) homePage += 1 },
                        emptyMessage = null
                    )
                }
                AppScreen.Categories -> {
                    CategoryButtonsContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        categories = categories,
                        onCategorySelected = { category ->
                            selectedCategory = category
                            categoryPage = 1
                            categoryItems = emptyList()
                            categoryError = null
                            categoryLoading = true
                            currentScreen = AppScreen.CategoryDetail
                        }
                    )
                }
                AppScreen.CategoryDetail -> {
                    val emptyMessage = if (categoryLoading) {
                        null
                    } else {
                        "No hay noticias disponibles en esta categoría."
                    }
                    NewsListContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        items = categoryItems,
                        isLoading = categoryLoading,
                        errorMessage = categoryError,
                        onRetry = {
                            selectedCategory?.let { category ->
                                scope.launch {
                                    loadCategory(categoryPage, category)
                                }
                            }
                        },
                        onItemClick = { selectedItem = it },
                        onToggleSaved = { item ->
                            savedItems = toggleSavedItem(savedItems, item)
                            SavedNewsStore.save(context, savedItems)
                        },
                        isItemSaved = { item -> savedItems.containsKey(item.link) },
                        showPagination = true,
                        currentPage = categoryPage,
                        canMoveNext = categoryItems.size == PAGE_SIZE && !categoryLoading,
                        canMovePrevious = categoryPage > 1,
                        onPreviousPage = { if (categoryPage > 1) categoryPage -= 1 },
                        onNextPage = { if (categoryItems.size == PAGE_SIZE) categoryPage += 1 },
                        emptyMessage = emptyMessage
                    )
                }
                AppScreen.Search -> {
                    val emptyMessage = if (searchSubmittedQuery.isBlank()) {
                        "Escribe un término para buscar noticias."
                    } else {
                        null
                    }
                    NewsListContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        items = searchItems,
                        isLoading = searchLoading,
                        errorMessage = searchError,
                        onRetry = {
                            scope.launch {
                                loadSearch(searchPage, searchSubmittedQuery)
                            }
                        },
                        onItemClick = { selectedItem = it },
                        onToggleSaved = { item ->
                            savedItems = toggleSavedItem(savedItems, item)
                            SavedNewsStore.save(context, savedItems)
                        },
                        isItemSaved = { item -> savedItems.containsKey(item.link) },
                        showPagination = searchSubmittedQuery.isNotBlank(),
                        currentPage = searchPage,
                        canMoveNext = searchItems.size == PAGE_SIZE && !searchLoading,
                        canMovePrevious = searchPage > 1,
                        onPreviousPage = { if (searchPage > 1) searchPage -= 1 },
                        onNextPage = { if (searchItems.size == PAGE_SIZE) searchPage += 1 },
                        emptyMessage = emptyMessage
                    )
                }
                AppScreen.Saved -> {
                    NewsListContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        items = savedItems.values.toList(),
                        isLoading = false,
                        errorMessage = null,
                        onRetry = {},
                        onItemClick = { selectedItem = it },
                        onToggleSaved = { item ->
                            savedItems = toggleSavedItem(savedItems, item)
                            SavedNewsStore.save(context, savedItems)
                        },
                        isItemSaved = { item -> savedItems.containsKey(item.link) },
                        showPagination = false,
                        currentPage = 1,
                        canMoveNext = false,
                        canMovePrevious = false,
                        onPreviousPage = {},
                        onNextPage = {},
                        emptyMessage = "Todavía no has guardado noticias."
                    )
                }
            }
        } else {
            NewsDetail(
                item = selectedItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun CategoryButtonsContent(
    modifier: Modifier,
    categories: List<CategoryFilter>,
    onCategorySelected: (CategoryFilter) -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF89D398), Color(0xFFF6FAF6))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                Button(
                    onClick = { onCategorySelected(category) },
                    enabled = category.enabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = category.title)
                }
            }
        }
    }
}

@Composable
private fun CategorySwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Button(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label)
    }
}

@Composable
private fun NewsListContent(
    modifier: Modifier,
    items: List<NewsItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onItemClick: (NewsItem) -> Unit,
    onToggleSaved: (NewsItem) -> Unit,
    isItemSaved: (NewsItem) -> Boolean,
    showPagination: Boolean,
    currentPage: Int,
    canMoveNext: Boolean,
    canMovePrevious: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    emptyMessage: String?
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF89D398), Color(0xFFF6FAF6))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                                Button(onClick = onRetry) {
                                    Text(text = "Reintentar")
                                }
                            }
                        }
                    }
                }

                if (items.isEmpty() && errorMessage == null && emptyMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F7F1))
                        ) {
                            Text(
                                text = emptyMessage,
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFF2B6B3F)
                            )
                        }
                    }
                }

                items.forEach { item ->
                    item {
                        NewsTitleCard(
                            item = item,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onItemClick(item) },
                            onToggleSaved = { onToggleSaved(item) },
                            isSaved = isItemSaved(item)
                        )
                    }
                }
            }
            if (showPagination) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousPage,
                        enabled = canMovePrevious,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE2F1E5), RoundedCornerShape(12.dp))
                            .shadow(6.dp, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Página anterior",
                            tint = Color(0xFF2B6B3F)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = onNextPage,
                        enabled = canMoveNext,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE2F1E5), RoundedCornerShape(12.dp))
                            .shadow(6.dp, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Página siguiente",
                            tint = Color(0xFF2B6B3F)
                        )
                    }
                }
            }
        }
        if (isLoading && showPagination) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2B6B3F),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
private fun NewsTitleCard(
    item: NewsItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
    isSaved: Boolean
) {
    Card(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F7F1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Por ${item.author}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleSaved) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Guardar noticia"
                )
            }
        }
    }
}

@Composable
private fun NewsDetail(item: NewsItem?, modifier: Modifier = Modifier) {
    if (item == null) return
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Por ${item.author}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(12.dp))
        item.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(12.dp))
        }
        Text(
            text = item.bodyPlain,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

data class NewsItem(
    val title: String,
    val link: String,
    val imageUrl: String?,
    val bodyPlain: String,
    val author: String
)

private object RssRepository {
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val pageCache = mutableMapOf<String, List<NewsItem>>()
    private val categoryCache = mutableMapOf<String, Int>()

    suspend fun fetchNews(
        page: Int,
        searchQuery: String? = null,
        categorySlug: String? = null
    ): List<NewsItem> = withContext(Dispatchers.IO) {
        val cacheKey = "$page|${searchQuery.orEmpty()}|${categorySlug.orEmpty()}"
        pageCache[cacheKey]?.let { cachedItems ->
            return@withContext cachedItems
        }
        val categoryId = categorySlug?.let { fetchCategoryId(it) }
        val request = Request.Builder()
            .url(buildPostsUrl(page, searchQuery, categoryId))
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
            pageCache[cacheKey] = items
            items
        }
    }

    private fun buildPostsUrl(
        page: Int,
        searchQuery: String?,
        categoryId: Int?
    ): String {
        val queryParams = mutableListOf("page=$page")
        if (!searchQuery.isNullOrBlank()) {
            val encodedQuery = URLEncoder.encode(searchQuery, "UTF-8")
            queryParams.add("search=$encodedQuery")
        }
        if (categoryId != null) {
            queryParams.add("categories=$categoryId")
        }
        return POSTS_API_URL + "&" + queryParams.joinToString("&")
    }

    private fun fetchCategoryId(slug: String): Int? {
        categoryCache[slug]?.let { cachedId ->
            return cachedId
        }
        val request = Request.Builder()
            .url("https://www.battle4play.com/wp-json/wp/v2/categories?slug=$slug")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Battle4PlayRSS"
            )
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("Battle4Play", "Category API request failed with ${response.code}")
                return null
            }
            val body = response.body ?: return null
            val json = runCatching { JSONArray(body.string()) }.getOrNull() ?: return null
            val categoryId = json.optJSONObject(0)?.optInt("id") ?: return null
            if (categoryId > 0) {
                categoryCache[slug] = categoryId
                return categoryId
            }
            return null
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
            val body = post.optJSONObject("content")?.optString("rendered").orEmpty()
            val author = extractAuthor(post)
            items.add(
                NewsItem(
                    title = title.ifBlank { "Battle4Play" },
                    link = link,
                    imageUrl = imageUrl,
                    bodyPlain = htmlToPlainText(body),
                    author = author
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

    private fun extractAuthor(post: org.json.JSONObject): String {
        val embedded = post.optJSONObject("_embedded") ?: return "Battle4Play"
        val authorArray = embedded.optJSONArray("author") ?: return "Battle4Play"
        val author = authorArray.optJSONObject(0)?.optString("name").orEmpty()
        return author.ifBlank { "Battle4Play" }
    }

    private fun htmlToPlainText(value: String): String {
        return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace("\uFFFC", "")
            .replace("\uFFFD", "")
            .trim()
    }
}

private fun toggleSavedItem(
    current: Map<String, NewsItem>,
    item: NewsItem
): Map<String, NewsItem> {
    return if (current.containsKey(item.link)) {
        current - item.link
    } else {
        current + (item.link to item)
    }
}

private object SavedNewsStore {
    private const val PREFS_NAME = "battle4play_saved_news"
    private const val KEY_ITEMS = "items"

    fun load(context: Context): Map<String, NewsItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyMap()
        val json = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyMap()
        val items = mutableMapOf<String, NewsItem>()
        for (index in 0 until json.length()) {
            val entry = json.optJSONObject(index) ?: continue
            val item = entry.toNewsItem() ?: continue
            items[item.link] = item
        }
        return items
    }

    fun save(context: Context, items: Map<String, NewsItem>) {
        val json = JSONArray()
        items.values.forEach { item ->
            json.put(item.toJson())
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ITEMS, json.toString())
            .apply()
    }

    private fun JSONObject.toNewsItem(): NewsItem? {
        val title = optString("title")
        val link = optString("link")
        if (link.isBlank()) return null
        return NewsItem(
            title = title,
            link = link,
            imageUrl = optString("imageUrl").ifBlank { null },
            bodyPlain = optString("bodyPlain"),
            author = optString("author")
        )
    }

    private fun NewsItem.toJson(): JSONObject {
        return JSONObject()
            .put("title", title)
            .put("link", link)
            .put("imageUrl", imageUrl.orEmpty())
            .put("bodyPlain", bodyPlain)
            .put("author", author)
    }
}
