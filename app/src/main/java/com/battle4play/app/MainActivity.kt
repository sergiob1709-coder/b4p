package com.battle4play.app

import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.core.text.HtmlCompat
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.battle4play.app.ui.theme.Battle4PlayTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import org.json.JSONArray
import org.json.JSONObject
import android.text.method.LinkMovementMethod
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private const val PAGE_SIZE = 5
private const val POSTS_API_BASE_URL = "https://www.battle4play.com/wp-json/wp/v2/posts"
private const val PS5_SLUG = "playstation-5"
private const val XBOX_SERIES_SLUG = "xbox-series-x"
private const val SWITCH_SLUG = "nintendo-switch"
private const val PS4_SLUG = "playstation-4"
private const val XBOX_ONE_SLUG = "xbox-one"
private const val ORDENADORES_SLUG = "ordenadores"
private const val MOVIL_SLUG = "movil"
private const val ANALISIS_SLUG = "analisis"
private const val VIDEOJUEGOS_GRATIS_SLUG = "videojuegos-gratis"
private const val POKEMON_SLUG = "pokemon"
private const val CARTAS_SLUG = "cartas"
private const val SERIES_SLUG = "series"
private const val ENGLISH_CATEGORY_ID = 5185
private const val LIST_AD_UNIT_ID_EXAMPLE = "ca-app-pub-3940256099942544/6300978111"
private const val DETAIL_AD_UNIT_ID_EXAMPLE = "ca-app-pub-3940256099942544/6300978111"

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
    val sectionPath: String,
    val icon: ImageVector,
    val subtitle: String
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
    var savedPage by rememberSaveable { mutableStateOf(1) }
    var categoryItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var categoryPage by rememberSaveable { mutableStateOf(1) }
    var categoryLoading by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var detailLoading by remember { mutableStateOf(false) }
    var detailError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<CategoryFilter?>(null) }
    val categories = remember {
        listOf(
            CategoryFilter(
                title = "PS5",
                slug = PS5_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/playstation-5",
                icon = Icons.Default.VideogameAsset,
                subtitle = "PlayStation 5"
            ),
            CategoryFilter(
                title = "Xbox Series",
                slug = XBOX_SERIES_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/xbox-series-x",
                icon = Icons.Default.SportsEsports,
                subtitle = "Series X|S"
            ),
            CategoryFilter(
                title = "Nintendo Switch",
                slug = SWITCH_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/nintendo-switch",
                icon = Icons.Default.Games,
                subtitle = "Nintendo"
            ),
            CategoryFilter(
                title = "PS4",
                slug = PS4_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/playstation-4",
                icon = Icons.Default.VideogameAsset,
                subtitle = "PlayStation 4"
            ),
            CategoryFilter(
                title = "Xbox One",
                slug = XBOX_ONE_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/xbox-one",
                icon = Icons.Default.SportsEsports,
                subtitle = "Xbox One"
            ),
            CategoryFilter(
                title = "Ordenadores",
                slug = ORDENADORES_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/ordenadores",
                icon = Icons.Default.Games,
                subtitle = "PC"
            ),
            CategoryFilter(
                title = "Movil",
                slug = MOVIL_SLUG,
                sectionPath = "/secciones/noticias-de-videojuegos/movil",
                icon = Icons.Default.VideogameAsset,
                subtitle = "Juegos moviles"
            ),
            CategoryFilter(
                title = "Analisis",
                slug = ANALISIS_SLUG,
                sectionPath = "/secciones/analisis",
                icon = Icons.Default.SportsEsports,
                subtitle = "Reviews"
            ),
            CategoryFilter(
                title = "Videojuegos Gratis",
                slug = VIDEOJUEGOS_GRATIS_SLUG,
                sectionPath = "/noticias-de-videojuegos/videojuegos-gratis",
                icon = Icons.Default.Games,
                subtitle = "Gratis"
            ),
            CategoryFilter(
                title = "Pokemon",
                slug = POKEMON_SLUG,
                sectionPath = "/secciones/noticias-de-entretenimiento/pokemon",
                icon = Icons.Default.SportsEsports,
                subtitle = "Entretenimiento"
            ),
            CategoryFilter(
                title = "Cartas",
                slug = CARTAS_SLUG,
                sectionPath = "/secciones/cartas",
                icon = Icons.Default.VideogameAsset,
                subtitle = "Coleccionables"
            ),
            CategoryFilter(
                title = "Series",
                slug = SERIES_SLUG,
                sectionPath = "/secciones/noticias-de-series/series",
                icon = Icons.Default.Games,
                subtitle = "Noticias de series"
            )
        )
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun loadHome(page: Int) {
        homeLoading = true
        homeError = null
        Log.d("Battle4Play", "Loading home posts page $page from $POSTS_API_BASE_URL")
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
        Log.d(
            "Battle4Play",
            "Loading category ${category.slug} (${category.sectionPath}) page $page"
        )
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

    val orderedSavedItems = remember(savedItems) {
        savedItems.values.sortedByDescending { item -> item.savedAt }
    }
    val savedTotalPages = remember(orderedSavedItems.size) {
        if (orderedSavedItems.isEmpty()) {
            1
        } else {
            ((orderedSavedItems.size - 1) / PAGE_SIZE) + 1
        }
    }
    val savedPageItems = remember(orderedSavedItems, savedPage) {
        val startIndex = ((savedPage - 1).coerceAtLeast(0)) * PAGE_SIZE
        orderedSavedItems.drop(startIndex).take(PAGE_SIZE)
    }

    LaunchedEffect(savedTotalPages) {
        if (savedPage > savedTotalPages) {
            savedPage = savedTotalPages
        }
    }

    fun applyLoadedDetail(updatedItem: NewsItem) {
        homeItems = homeItems.map { item ->
            if (item.link == updatedItem.link) updatedItem else item
        }
        searchItems = searchItems.map { item ->
            if (item.link == updatedItem.link) updatedItem else item
        }
        categoryItems = categoryItems.map { item ->
            if (item.link == updatedItem.link) updatedItem else item
        }
        if (savedItems.containsKey(updatedItem.link)) {
            val existingSavedAt = savedItems[updatedItem.link]?.savedAt ?: updatedItem.savedAt
            val savedCopy = updatedItem.copy(savedAt = existingSavedAt)
            savedItems = savedItems + (updatedItem.link to savedCopy)
            SavedNewsStore.save(context, savedItems)
        }
    }

    fun openNewsDetail(item: NewsItem) {
        selectedItem = item
        detailError = null
        if (item.hasFullContent) {
            detailLoading = false
            return
        }
        if (item.postId <= 0) {
            detailLoading = false
            if (item.bodyHtml.isBlank()) {
                detailError = "No se pudo cargar el contenido completo."
            }
            return
        }
        detailLoading = true
        scope.launch {
            val fullBody = runCatching { RssRepository.fetchPostBody(item.postId) }
                .onFailure { error ->
                    Log.e("Battle4Play", "Error loading detail for post ${item.postId}", error)
                }
                .getOrNull()
            if (selectedItem?.link != item.link) return@launch
            detailLoading = false
            if (!fullBody.isNullOrBlank()) {
                val enrichedItem = item.copy(bodyHtml = fullBody, hasFullContent = true)
                selectedItem = enrichedItem
                applyLoadedDetail(enrichedItem)
            } else if (item.bodyHtml.isBlank()) {
                detailError = "No se pudo cargar el contenido completo."
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x9908140D),
                            Color(0x660F2B1D),
                            Color(0xCC08140D)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x4D56E293),
                            Color.Transparent,
                            Color(0x331F5A3C)
                        )
                    )
                )
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Battle4PlayHeader(
                        showBack = selectedItem == null && currentScreen == AppScreen.CategoryDetail,
                        onBack = {
                            if (selectedItem != null) {
                                selectedItem = null
                                detailLoading = false
                                detailError = null
                            } else if (currentScreen == AppScreen.CategoryDetail) {
                                currentScreen = AppScreen.Categories
                            }
                        },
                        showBookmark = false,
                        isBookmarked = selectedItem?.let { savedItems.containsKey(it.link) } ?: false,
                        onToggleBookmark = {
                            selectedItem?.let { item ->
                                savedItems = toggleSavedItem(savedItems, item)
                                SavedNewsStore.save(context, savedItems)
                            }
                        }
                    )
                    if (selectedItem == null) {
                        when (currentScreen) {
                            AppScreen.Search -> {
                                Spacer(modifier = Modifier.height(12.dp))
                                SearchPanel(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onSearch = {
                                        searchSubmittedQuery = searchQuery
                                        searchPage = 1
                                        searchItems = emptyList()
                                        searchError = null
                                    }
                                )
                            }
                            else -> Unit
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFFE6F3E7).copy(alpha = 0.85f)) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Home,
                        onClick = {
                            selectedItem = null
                            detailLoading = false
                            detailError = null
                            currentScreen = AppScreen.Home
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Categories || currentScreen == AppScreen.CategoryDetail,
                        onClick = {
                            selectedItem = null
                            detailLoading = false
                            detailError = null
                            currentScreen = AppScreen.Categories
                        },
                        icon = { Icon(Icons.Default.Category, contentDescription = "Categorías") },
                        label = { Text("Categorías") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Search,
                        onClick = {
                            selectedItem = null
                            detailLoading = false
                            detailError = null
                            currentScreen = AppScreen.Search
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        label = { Text("Buscar") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Saved,
                        onClick = {
                            selectedItem = null
                            detailLoading = false
                            detailError = null
                            savedPage = 1
                            currentScreen = AppScreen.Saved
                        },
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
                        onItemClick = { item -> openNewsDetail(item) },
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
                        onItemClick = { item -> openNewsDetail(item) },
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
                        onItemClick = { item -> openNewsDetail(item) },
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
                        items = savedPageItems,
                        isLoading = false,
                        errorMessage = null,
                        onRetry = {},
                        onItemClick = { item -> openNewsDetail(item) },
                        onToggleSaved = { item ->
                            savedItems = toggleSavedItem(savedItems, item)
                            SavedNewsStore.save(context, savedItems)
                        },
                        isItemSaved = { item -> savedItems.containsKey(item.link) },
                        showPagination = orderedSavedItems.isNotEmpty(),
                        currentPage = savedPage,
                        canMoveNext = savedPage < savedTotalPages,
                        canMovePrevious = savedPage > 1,
                        onPreviousPage = { if (savedPage > 1) savedPage -= 1 },
                        onNextPage = { if (savedPage < savedTotalPages) savedPage += 1 },
                        emptyMessage = "Todavía no has guardado noticias."
                    )
                }
            }
        } else {
            NewsDetail(
                item = selectedItem,
                isBookmarked = selectedItem?.let { savedItems.containsKey(it.link) } ?: false,
                isContentLoading = detailLoading,
                contentError = detailError,
                onBack = {
                    selectedItem = null
                    detailLoading = false
                    detailError = null
                },
                onToggleBookmark = {
                    selectedItem?.let { item ->
                        savedItems = toggleSavedItem(savedItems, item)
                        SavedNewsStore.save(context, savedItems)
                    }
                },
                onRetryLoadContent = {
                    selectedItem?.let { item ->
                        openNewsDetail(item.copy(hasFullContent = false))
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            )
        }
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
        modifier = modifier.background(Color.Transparent)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(categories, key = { it.slug }) { category ->
                CategoryTile(
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: CategoryFilter,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val gradientColors = when (category.slug) {
        PS5_SLUG -> listOf(Color(0xFF62E6A2), Color(0xFF2DBA73), Color(0xFF1A8D53))
        XBOX_SERIES_SLUG -> listOf(Color(0xFF70E8AE), Color(0xFF33C27A), Color(0xFF1D9A59))
        else -> listOf(Color(0xFF7DEDB8), Color(0xFF42C986), Color(0xFF26925A))
    }.map { it.copy(alpha = 0.80f) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, shape)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)), shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                shadowElevation = 0.dp
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.title,
                    tint = Color(0xFFF5FFF9),
                    modifier = Modifier
                        .padding(9.dp)
                        .size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = category.title,
                color = Color(0xEAF8FFFB),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.subtitle,
                color = Color(0xCCF0FFF5),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val panelShape = RoundedCornerShape(22.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = panelShape,
        color = Color(0x30122218),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Busca noticias") },
                placeholder = { Text("PS5, Xbox, Nintendo...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.14f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                    focusedTextColor = Color(0xFFF4FFF8),
                    unfocusedTextColor = Color(0xFFE8F8EF),
                    focusedBorderColor = Color(0xFF89EDAF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                    cursorColor = Color(0xFF9BFFBE),
                    focusedLabelColor = Color(0xFFE3FEEE),
                    unfocusedLabelColor = Color(0xCCE1F4E9),
                    focusedPlaceholderColor = Color(0xCFE0F4E8),
                    unfocusedPlaceholderColor = Color(0xB6D5E9DD),
                    focusedLeadingIconColor = Color(0xFF9DF6BE),
                    unfocusedLeadingIconColor = Color(0xCDE0F2E6)
                )
            )
            Button(
                onClick = onSearch,
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2FC974), Color(0xFF1E874F))
                        )
                    )
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                        RoundedCornerShape(14.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(text = "Buscar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Battle4PlayHeader(
    showBack: Boolean,
    onBack: () -> Unit,
    showBookmark: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    val headerShape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, headerShape)
            .clip(headerShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x66102218),
                        Color(0x99295F45),
                        Color(0x66102218)
                    )
                )
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)), headerShape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Battle4Play",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 82.dp),
            contentScale = ContentScale.Fit
        )
        if (showBack) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0x4D000000),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (showBookmark) {
            IconButton(
                onClick = onToggleBookmark,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0x4D000000),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Guardar noticia",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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
        modifier = modifier.background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, bottom = 12.dp),
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
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                            )
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
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF1F7F1).copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                text = emptyMessage,
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFF2B6B3F)
                            )
                        }
                    }
                }

                val listingAdPosition = 2
                var listingAdInserted = false
                items.forEachIndexed { index, item ->
                    if (!listingAdInserted && index == listingAdPosition) {
                        item(key = "listing_ad_page_$currentPage") {
                            ListingAdBlock(
                                adUnitId = LIST_AD_UNIT_ID_EXAMPLE,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        listingAdInserted = true
                    }
                    item(key = "news_${item.link}") {
                        NewsTitleCard(
                            item = item,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onItemClick(item) },
                            onToggleSaved = { onToggleSaved(item) },
                            isSaved = isItemSaved(item)
                        )
                    }
                }
                if (!listingAdInserted && items.isNotEmpty()) {
                    item(key = "listing_ad_page_$currentPage") {
                        ListingAdBlock(
                            adUnitId = LIST_AD_UNIT_ID_EXAMPLE,
                            modifier = Modifier.fillMaxWidth()
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
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.75f),
                                        Color.White.copy(alpha = 0.22f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Página anterior",
                            tint = Color(0xFF2B6B3F)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.20f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "Pagina $currentPage",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFFE9F9EE),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onNextPage,
                        enabled = canMoveNext,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.75f),
                                        Color.White.copy(alpha = 0.22f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                                RoundedCornerShape(16.dp)
                            )
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
                LoadingSpinner(
                    color = Color(0xFF2B6B3F)
                )
            }
        }
    }
}

@Composable
private fun LoadingSpinner(
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f
) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 900, easing = LinearEasing)
            )
            rotation.snapTo(0f)
        }
    }
    Canvas(
        modifier = modifier
            .size(48.dp)
    ) {
        drawArc(
            color = color,
            startAngle = rotation.value,
            sweepAngle = 280f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color(0xFFE3E3E3).copy(alpha = 0.6f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFF2F2F2).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFF2F2F2).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1F1F1F),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleSaved) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Guardar noticia",
                    tint = Color(0xFF214632)
                )
            }
        }
    }
}

@Composable
private fun NewsDetail(
    item: NewsItem?,
    isBookmarked: Boolean,
    isContentLoading: Boolean,
    contentError: String?,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onRetryLoadContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item == null) return
    val heroShape = RoundedCornerShape(28.dp)
    val glassShape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(280.dp)
                .shadow(12.dp, heroShape)
                .background(Color(0xFF1D1D1D), heroShape)
        ) {
            item.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(heroShape)
                        .background(Color.LightGray, heroShape),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(heroShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xB3000000))
                        ),
                        shape = heroShape
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0x8A0D2118),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleBookmark,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0x8A0D2118),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) {
                            Icons.Default.Bookmark
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = "Guardar noticia",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-10).dp),
            shape = glassShape,
            color = Color(0xFFF5FAF7).copy(alpha = 0.92f),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, Color(0xFFD4E6DA).copy(alpha = 0.75f), glassShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF8FFFB), Color(0xFFF1F8F3))
                        ),
                        shape = glassShape
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                when {
                    item.bodyHtml.isNotBlank() -> {
                        val htmlSplit = splitHtmlForInlineAd(item.bodyHtml)
                        if (htmlSplit != null) {
                            HtmlText(
                                html = htmlSplit.before,
                                modifier = Modifier.fillMaxWidth(),
                                textColor = Color(0xFF1B2A22),
                                contentPaddingDp = 0f,
                                trimEdgeWhitespace = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            DetailAdBlock(
                                adUnitId = DETAIL_AD_UNIT_ID_EXAMPLE,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HtmlText(
                                html = htmlSplit.after,
                                modifier = Modifier.fillMaxWidth(),
                                textColor = Color(0xFF1B2A22),
                                contentPaddingDp = 0f,
                                trimEdgeWhitespace = true
                            )
                        } else {
                            HtmlText(
                                html = item.bodyHtml,
                                modifier = Modifier.fillMaxWidth(),
                                textColor = Color(0xFF1B2A22)
                            )
                        }
                        if (isContentLoading) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Actualizando contenido...",
                                color = Color(0xFF3C7052),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    isContentLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingSpinner(color = Color(0xFF2B6B3F))
                        }
                    }
                    else -> {
                        Text(
                            text = contentError ?: "Contenido no disponible para esta noticia.",
                            color = Color(0xFF335540),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onRetryLoadContent) {
                            Text(text = "Reintentar carga")
                        }
                    }
                }
            }
        }
    }
}

data class NewsItem(
    val postId: Int = -1,
    val title: String,
    val link: String,
    val imageUrl: String?,
    val bodyHtml: String,
    val author: String,
    val hasFullContent: Boolean = false,
    val savedAt: Long = 0L
)

@Composable
private fun ListingAdBlock(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFEEF8F1).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color(0xFFC6E4D1).copy(alpha = 0.85f)),
        shadowElevation = 6.dp
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            factory = { context ->
                AdView(context).apply {
                    this.adUnitId = adUnitId
                    setAdSize(AdSize.BANNER)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                if (adView.adUnitId != adUnitId) {
                    adView.adUnitId = adUnitId
                    adView.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
private fun DetailAdBlock(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val context = LocalContext.current
        val adWidthDp = maxWidth.value.toInt().coerceAtLeast(280)
        val adaptiveSize = remember(adWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                adWidthDp
            )
        }
        val density = context.resources.displayMetrics.density
        val adHeightDp = remember(adaptiveSize, density) {
            (adaptiveSize.getHeightInPixels(context) / density).coerceAtLeast(50f).dp
        }
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(adHeightDp),
            factory = { viewContext ->
                AdView(viewContext).apply {
                    this.adUnitId = adUnitId
                    setAdSize(adaptiveSize)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                if (adView.adUnitId != adUnitId) {
                    adView.adUnitId = adUnitId
                    adView.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

private data class HtmlSplit(
    val before: String,
    val after: String
)

private fun splitHtmlForInlineAd(html: String): HtmlSplit? {
    val paragraphClosings = Regex("(?i)</p>").findAll(html).toList()
    val splitIndex = when {
        paragraphClosings.size >= 3 -> 2
        paragraphClosings.size >= 2 -> 1
        else -> return null
    }
    val splitAt = paragraphClosings[splitIndex].range.last + 1
    if (splitAt <= 0 || splitAt >= html.length) return null
    val before = cleanupHtmlAroundAdSplit(html.substring(0, splitAt), trailing = true)
    val after = cleanupHtmlAroundAdSplit(html.substring(splitAt), trailing = false)
    if (before.isBlank() || after.isBlank()) return null
    return HtmlSplit(before = before, after = after)
}

private fun cleanupHtmlAroundAdSplit(value: String, trailing: Boolean): String {
    var output = value
    if (trailing) {
        output = output.replace(
            Regex("(?is)(?:<p[^>]*>(?:\\s|&nbsp;|<br\\s*/?>)*</p>|<br\\s*/?>)+\\s*$"),
            ""
        )
    } else {
        output = output.replace(
            Regex("(?is)^\\s*(?:<p[^>]*>(?:\\s|&nbsp;|<br\\s*/?>)*</p>|<br\\s*/?>)+"),
            ""
        )
    }
    return output.trim()
}

@Composable
private fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF1F1F1F),
    contentPaddingDp: Float = 8f,
    trimEdgeWhitespace: Boolean = false
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            android.widget.TextView(context).apply {
                setTextColor(textColor.toArgb())
                textSize = 16.5f
                setLineSpacing(0f, 1.38f)
                includeFontPadding = false
                linksClickable = true
                setLinkTextColor(Color(0xFF1C7B56).toArgb())
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    justificationMode = android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NORMAL
                    breakStrategy = android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
                }
                movementMethod = LinkMovementMethod.getInstance()
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { view ->
            val density = view.resources.displayMetrics.density
            val headingSpan = HeadingSpan(
                startColor = Color(0xFF123A2B).toArgb(),
                endColor = Color(0xFF1F5B42).toArgb(),
                accentColor = Color(0xFF53D08A).toArgb(),
                underlineColor = Color(0xFF79E0A6).toArgb(),
                cornerRadius = 10f * density,
                horizontalPadding = 15f * density,
                verticalPadding = 7f * density,
                underlineHeight = 4f * density,
                accentStripeWidth = 5f * density
            )
            val tagHandler: android.text.Html.TagHandler = HeadingTagHandler(
                headingSpan = headingSpan,
                paddingPx = (10f * density).toInt()
            )
            val spanned = runCatching {
                HtmlCompat.fromHtml(
                    html,
                    HtmlCompat.FROM_HTML_MODE_LEGACY,
                    null,
                    tagHandler
                )
            }.getOrElse { error ->
                Log.e("Battle4Play", "Error rendering HTML content", error)
                HtmlCompat.fromHtml("", HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            view.text = if (trimEdgeWhitespace) trimSpannedEdges(spanned) else spanned
            view.setTextColor(textColor.toArgb())
            val padding = (contentPaddingDp * density).toInt()
            view.setPadding(padding, padding, padding, padding)
        }
    )
}

private fun trimSpannedEdges(spanned: Spanned): Spanned {
    val value = spanned.toString()
    var start = 0
    var end = value.length
    while (start < end && value[start].isWhitespace()) {
        start++
    }
    while (end > start && value[end - 1].isWhitespace()) {
        end--
    }
    if (start == 0 && end == value.length) return spanned
    val builder = SpannableStringBuilder(spanned)
    if (end < builder.length) {
        builder.delete(end, builder.length)
    }
    if (start > 0) {
        builder.delete(0, start)
    }
    return builder
}

private class HeadingTagHandler(
    private val headingSpan: HeadingSpan,
    private val paddingPx: Int
) : android.text.Html.TagHandler {
    override fun handleTag(
        opening: Boolean,
        tag: String,
        output: Editable,
        xmlReader: org.xml.sax.XMLReader
    ) {
        if (tag.equals("h2", ignoreCase = true) || tag.equals("title", ignoreCase = true)) {
            if (opening) {
                output.setSpan(HeadingMarker(), output.length, output.length, Spanned.SPAN_MARK_MARK)
            } else {
                val marker = getLastSpan(output, HeadingMarker::class.java) ?: return
                val start = output.getSpanStart(marker)
                val end = output.length
                output.removeSpan(marker)
                if (start != end) {
                    output.setSpan(headingSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    output.setSpan(
                        LeadingMarginSpan.Standard(paddingPx, paddingPx),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    output.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    output.setSpan(
                        ForegroundColorSpan(Color(0xFFF3FFF8).toArgb()),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    output.setSpan(RelativeSizeSpan(1.12f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    private fun <T> getLastSpan(text: Editable, kind: Class<T>): T? {
        val spans = text.getSpans(0, text.length, kind)
        return spans.lastOrNull()
    }
}

private class HeadingMarker

private class HeadingSpan(
    private val startColor: Int,
    private val endColor: Int,
    private val accentColor: Int,
    private val underlineColor: Int,
    private val cornerRadius: Float,
    private val horizontalPadding: Float,
    private val verticalPadding: Float,
    private val underlineHeight: Float,
    private val accentStripeWidth: Float
) : LineBackgroundSpan {
    override fun drawBackground(
        canvas: android.graphics.Canvas,
        paint: android.graphics.Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val originalColor = paint.color
        val originalShader = paint.shader
        val originalStyle = paint.style
        val textWidth = paint.measureText(text, start, end)
        val rect = RectF(
            left.toFloat(),
            top.toFloat() - verticalPadding,
            left + textWidth + horizontalPadding * 2,
            bottom.toFloat() + verticalPadding
        )
        paint.style = android.graphics.Paint.Style.FILL
        paint.shader = android.graphics.LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            startColor,
            endColor,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        paint.shader = null
        paint.color = accentColor
        canvas.drawRect(
            rect.left,
            rect.top,
            rect.left + accentStripeWidth,
            rect.bottom,
            paint
        )
        paint.color = underlineColor
        canvas.drawRoundRect(
            RectF(
                rect.left + accentStripeWidth,
                rect.bottom - underlineHeight,
                rect.right,
                rect.bottom
            ),
            cornerRadius / 2f,
            cornerRadius / 2f,
            paint
        )
        paint.color = originalColor
        paint.shader = originalShader
        paint.style = originalStyle
    }
}

private object RssRepository {
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val pageCache = mutableMapOf<String, List<NewsItem>>()
    private val categoryCache = mutableMapOf<String, Int>()
    private val postDetailCache = mutableMapOf<Int, String>()

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
        if (!categorySlug.isNullOrBlank() && categoryId == null) {
            Log.w("Battle4Play", "Category slug not found: $categorySlug")
            return@withContext emptyList()
        }
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

    suspend fun fetchPostBody(postId: Int): String? = withContext(Dispatchers.IO) {
        if (postId <= 0) return@withContext null
        postDetailCache[postId]?.let { cachedBody ->
            return@withContext cachedBody
        }
        val encodedFields = URLEncoder.encode("content.rendered", "UTF-8")
        val request = Request.Builder()
            .url("$POSTS_API_BASE_URL/$postId?_fields=$encodedFields")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Battle4PlayRSS"
            )
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("Battle4Play", "Post detail API request failed with ${response.code}")
                return@withContext null
            }
            val body = response.body ?: return@withContext null
            val json = runCatching { JSONObject(body.string()) }.getOrNull() ?: return@withContext null
            val html = json.optJSONObject("content")?.optString("rendered").orEmpty()
            val cleanedHtml = stripImagesFromHtml(html)
            if (cleanedHtml.isNotBlank()) {
                postDetailCache[postId] = cleanedHtml
            }
            cleanedHtml.ifBlank { null }
        }
    }

    private fun buildPostsUrl(
        page: Int,
        searchQuery: String?,
        categoryId: Int?
    ): String {
        val queryParams = mutableListOf(
            "per_page=$PAGE_SIZE",
            "_embed=author,wp:featuredmedia",
            "context=embed",
            "page=$page"
        )
        if (!searchQuery.isNullOrBlank()) {
            val encodedQuery = URLEncoder.encode(searchQuery, "UTF-8")
            queryParams.add("search=$encodedQuery")
        }
        if (categoryId != null) {
            queryParams.add("categories=$categoryId")
        }
        queryParams.add("categories_exclude=$ENGLISH_CATEGORY_ID")
        return POSTS_API_BASE_URL + "?" + queryParams.joinToString("&")
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
            val postId = post.optInt("id", -1)
            val title = post.optJSONObject("title")?.optString("rendered").orEmpty()
            val link = post.optString("link")
            if (link.isBlank()) continue
            val imageUrl = extractFeaturedImage(post)
            val author = extractAuthor(post)
            items.add(
                NewsItem(
                    postId = postId,
                    title = title.ifBlank { "Battle4Play" },
                    link = link,
                    imageUrl = imageUrl,
                    bodyHtml = "",
                    author = author,
                    hasFullContent = false
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

    private fun stripImagesFromHtml(value: String): String {
        return value.replace(Regex("<img[^>]*>"), "").trim()
    }
}

private fun toggleSavedItem(
    current: Map<String, NewsItem>,
    item: NewsItem
): Map<String, NewsItem> {
    return if (current.containsKey(item.link)) {
        current - item.link
    } else {
        val savedItem = item.copy(savedAt = System.currentTimeMillis())
        linkedMapOf(item.link to savedItem).apply {
            putAll(current)
        }
    }
}

private object SavedNewsStore {
    private const val PREFS_NAME = "battle4play_saved_news"
    private const val KEY_ITEMS = "items"

    fun load(context: Context): Map<String, NewsItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyMap()
        val json = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyMap()
        val items = linkedMapOf<String, NewsItem>()
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
        val bodyHtml = optString("bodyHtml").ifBlank { optString("bodyPlain") }
        val postId = when {
            has("postId") -> optInt("postId", -1)
            has("id") -> optInt("id", -1)
            else -> -1
        }
        val hasFullContent = if (has("hasFullContent")) {
            optBoolean("hasFullContent", false)
        } else {
            bodyHtml.isNotBlank()
        }
        val savedAt = if (has("savedAt")) {
            optLong("savedAt", 0L)
        } else {
            0L
        }
        return NewsItem(
            postId = postId,
            title = title,
            link = link,
            imageUrl = optString("imageUrl").ifBlank { null },
            bodyHtml = bodyHtml,
            author = optString("author"),
            hasFullContent = hasFullContent,
            savedAt = savedAt
        )
    }

    private fun NewsItem.toJson(): JSONObject {
        return JSONObject()
            .put("postId", postId)
            .put("title", title)
            .put("link", link)
            .put("imageUrl", imageUrl.orEmpty())
            .put("bodyHtml", bodyHtml)
            .put("author", author)
            .put("hasFullContent", hasFullContent)
            .put("savedAt", savedAt)
    }
}
