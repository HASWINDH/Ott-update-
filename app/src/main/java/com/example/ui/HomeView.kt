package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Movie
import com.example.data.NotificationItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(viewModel: OttViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedMovieId by viewModel.selectedMovieId.collectAsStateWithLifecycle()
    val selectedMovie by viewModel.selectedMovie.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // To navigate to notifications sub-popup
    var showNotifPopup by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "OTT",
                            color = OttRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "UPDATES",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(
                            onClick = { showNotifPopup = !showNotifPopup },
                            modifier = Modifier.testTag("notif_bell_button")
                        ) {
                            Icon(
                                imageVector = if (notifications.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (notifications.isNotEmpty()) OttGold else TextWhite
                            )
                        }
                        if (notifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(OttRed, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OttBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = OttSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
                    Triple("Search", Icons.Default.Search, Icons.Outlined.Search),
                    Triple("Platforms", Icons.Default.Tv, Icons.Outlined.Tv),
                    Triple("Watchlist", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder),
                    Triple("Profile", Icons.Default.Person, Icons.Outlined.Person)
                )

                tabs.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) filledIcon else outlinedIcon,
                                contentDescription = label,
                                tint = if (isSelected) OttRed else TextGray
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) TextWhite else TextGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = OttSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        },
        containerColor = OttBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content tabs
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(viewModel)
                    1 -> SearchScreen(viewModel)
                    2 -> PlatformsScreen(viewModel)
                    3 -> WatchlistScreen(viewModel)
                    4 -> ProfileScreen(viewModel)
                }
            }

            // Quick notifications inline drop-down overlay
            if (showNotifPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showNotifPopup = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.6f)
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .shadow(16.dp, RoundedCornerShape(16.dp))
                            .clickable(enabled = false) {},
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = OttSurface),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Alerts & Updates",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { viewModel.clearAllNotifications() },
                                    modifier = Modifier.testTag("clear_all_notif_btn")
                                ) {
                                    Text("Clear All", color = OttRed, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (notifications.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsNone,
                                        contentDescription = null,
                                        tint = TextGray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No release alerts scheduled",
                                        color = TextGray,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(notifications) { notif ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    OttSurfaceVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(OttGold, CircleShape)
                                                    .align(Alignment.CenterVertically)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = notif.title,
                                                    color = TextWhite,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = notif.message,
                                                    color = TextLightGray,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { showNotifPopup = false },
                                colors = ButtonDefaults.buttonColors(containerColor = OttRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .testTag("close_notif_panel"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close", color = TextWhite)
                            }
                        }
                    }
                }
            }

            // Movie Details Full-Screen Overlay
            if (selectedMovieId != null && selectedMovie != null) {
                MovieDetailsOverlay(
                    movie = selectedMovie!!,
                    onDismiss = { viewModel.selectMovie(null) },
                    viewModel = viewModel
                )
            }
        }
    }
}

// ==================== HOME SCREEN ====================
@Composable
fun HomeScreen(viewModel: OttViewModel) {
    val featured by viewModel.featuredMovies.collectAsStateWithLifecycle()
    val trending by viewModel.trendingMovies.collectAsStateWithLifecycle()
    val upcoming by viewModel.upcomingMovies.collectAsStateWithLifecycle()
    val newSeries by viewModel.newSeries.collectAsStateWithLifecycle()
    val allMovies by viewModel.allMovies.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBackground),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Featured Billboard Carousel
        if (featured.isNotEmpty()) {
            item {
                FeaturedBillboard(featured = featured, onMovieClick = { viewModel.selectMovie(it.id) })
            }
        }

        // Latest Releases
        item {
            MovieRowSection(
                title = "Latest OTT Releases",
                movies = allMovies.take(6),
                onMovieClick = { viewModel.selectMovie(it.id) }
            )
        }

        // Upcoming Releases
        item {
            MovieRowSection(
                title = "Upcoming Releases Releasing Soon",
                movies = upcoming,
                showBadge = true,
                badgeText = "Releasing Soon",
                onMovieClick = { viewModel.selectMovie(it.id) }
            )
        }

        // Trending Now
        item {
            MovieRowSection(
                title = "Trending Content 🔥",
                movies = trending,
                onMovieClick = { viewModel.selectMovie(it.id) }
            )
        }

        // New Web Series
        item {
            MovieRowSection(
                title = "Hot Web Series 📺",
                movies = newSeries,
                onMovieClick = { viewModel.selectMovie(it.id) }
            )
        }

        // Top IMDb releases
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Top IMDb Rated Releases",
                    color = OttGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                allMovies.sortedByDescending { it.imdbRating }.take(5).forEach { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(OttSurface, RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectMovie(movie.id) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ImageThumbnailCard(
                            imageUrl = movie.posterUrl,
                            title = movie.title,
                            modifier = Modifier.size(60.dp, 80.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(getPlatformColor(movie.ottPlatform), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(movie.ottPlatform, color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(movie.genre.split("/").firstOrNull() ?: "", color = TextGray, fontSize = 12.sp)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = OttGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = movie.imdbRating.toString(),
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedBillboard(
    featured: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    // Show top featured item
    val item = featured.firstOrNull() ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable { onMovieClick(item) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.posterUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(android.R.drawable.ic_menu_gallery)
        )

        // Overlay Cinematic Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            OttBackground.copy(alpha = 0.4f),
                            OttBackground
                        ),
                        startY = 100f
                    )
                )
        )

        // Billboard Details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(OttRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "FEATURED SPREE",
                    color = TextWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(getPlatformColor(item.ottPlatform), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(item.ottPlatform, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = OttGold, modifier = Modifier.size(14.dp))
                    Text(text = "${item.imdbRating}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(text = "•", color = TextGray)
                Text(text = item.genre, color = TextLightGray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onMovieClick(item) },
                    colors = ButtonDefaults.buttonColors(containerColor = OttRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("billboard_view_details")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Overview")
                }
            }
        }
    }
}

@Composable
fun MovieRowSection(
    title: String,
    movies: List<Movie>,
    showBadge: Boolean = false,
    badgeText: String = "",
    onMovieClick: (Movie) -> Unit
) {
    if (movies.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(movies) { movie ->
                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable { onMovieClick(movie) }
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(170.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(movie.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = movie.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(android.R.drawable.ic_menu_gallery)
                        )

                        // Platform Sticker badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(getPlatformColor(movie.ottPlatform).copy(alpha = 0.9f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(movie.ottPlatform, color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // Bottom gradient on card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .align(Alignment.BottomStart)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = OttGold, modifier = Modifier.size(12.dp))
                            Text(text = "${movie.imdbRating}", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = movie.title,
                        color = TextLightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (showBadge) badgeText else movie.releaseDate,
                        color = if (showBadge) OttGold else TextGray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Helper thumbnail cards to prevent blank screens
@Composable
fun ImageThumbnailCard(imageUrl: String, title: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(android.R.drawable.ic_menu_gallery)
        )
    }
}

// ==================== SEARCH / ASK AI SCREEN ====================
@Composable
fun SearchScreen(viewModel: OttViewModel) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val platformFilter by viewModel.filterPlatform.collectAsStateWithLifecycle()
    val genreFilter by viewModel.filterGenre.collectAsStateWithLifecycle()
    val languageFilter by viewModel.filterLanguage.collectAsStateWithLifecycle()
    val results by viewModel.searchedMovies.collectAsStateWithLifecycle()

    // AI Variables
    val askQuery by viewModel.askQuery.collectAsStateWithLifecycle()
    val aiState by viewModel.aiSearchState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Header Search
        item {
            Text(
                text = "Search & Discover",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search title, synopsis, genres...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OttRed) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextWhite)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("local_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = OttRed,
                    unfocusedBorderColor = BorderGray,
                    focusedContainerColor = OttSurface,
                    unfocusedContainerColor = OttSurface
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }

        // Live Filters Options
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FILTERS", color = OttGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                // Platform scroll row
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val platforms = listOf("All", "Netflix", "Prime Video", "JioHotstar", "Sony LIV", "ZEE5", "Aha")
                    platforms.forEach { item ->
                        FilterChip(
                            selected = platformFilter == item,
                            onClick = { viewModel.filterPlatform.value = item },
                            label = { Text(item, color = TextWhite) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OttRed,
                                containerColor = OttSurface
                            ),
                            border = BorderStroke(1.dp, if (platformFilter == item) OttRed else BorderGray)
                        )
                    }
                }

                // Genre scroll row
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val genres = listOf("All", "Action", "Thriller", "Drama", "Sci-Fi", "Comedy", "Family")
                    genres.forEach { item ->
                        FilterChip(
                            selected = genreFilter == item,
                            onClick = { viewModel.filterGenre.value = item },
                            label = { Text(item, color = TextWhite) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OttGold,
                                containerColor = OttSurface
                            ),
                            border = BorderStroke(1.dp, if (genreFilter == item) OttGold else BorderGray)
                        )
                    }
                }
            }
        }

        // Local Filtered Catalog List
        if (query.isNotEmpty() || platformFilter != "All" || genreFilter != "All") {
            item {
                Text(
                    text = "Matching Releases (${results.size})",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (results.isEmpty()) {
                item {
                    Text("No local releases match your active filters.", color = TextGray, fontSize = 13.sp)
                }
            } else {
                items(results) { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OttSurface, RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectMovie(movie.id) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ImageThumbnailCard(
                            imageUrl = movie.posterUrl,
                            title = movie.title,
                            modifier = Modifier.size(50.dp, 70.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(movie.title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(movie.genre, color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(getPlatformColor(movie.ottPlatform), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(movie.ottPlatform, color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Rating: ${movie.imdbRating}", color = OttGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextGray
                        )
                    }
                }
            }
        }

        // --- ASK ANY MOVIE (AI CHAT LOOKUP) SECTION ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OttSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OttGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OttGold, modifier = Modifier.size(24.dp))
                        Text(
                            text = "ASK ANY MOVIE (AI FINDER)",
                            color = OttGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Query any movie/show. Gemini will analyze availability, streaming dates, and IMDb rating instantly!",
                        color = TextLightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = askQuery,
                            onValueChange = { viewModel.askQuery.value = it },
                            placeholder = { Text("e.g. Inception or Leo 2", color = TextGray, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = OttGold,
                                unfocusedBorderColor = BorderGray,
                                focusedContainerColor = OttBackground,
                                unfocusedContainerColor = OttBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                viewModel.searchAskAnyMovie()
                            })
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.searchAskAnyMovie()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OttGold),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .testTag("ai_search_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Find", color = OttBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rendering AI Loading / Success / Error States
                    AnimatedContent(targetState = aiState, label = "AIStateAnimation") { state ->
                        when (state) {
                            is AiSearchState.Idle -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Awaiting movie name query...",
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            is AiSearchState.Loading -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = OttGold, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "Consulting Gemini neural core metadata...",
                                        color = OttGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            is AiSearchState.Error -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = state.message,
                                        color = OttRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(onClick = { viewModel.clearAiSearch() }) {
                                        Text("Clear", color = TextWhite)
                                    }
                                }
                            }

                            is AiSearchState.Success -> {
                                val m = state.movie
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(OttBackground, RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = m.title,
                                            color = TextWhite,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = { viewModel.clearAiSearch() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = TextGray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(getPlatformColor(m.ottPlatform), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(m.ottPlatform, color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = OttGold, modifier = Modifier.size(14.dp))
                                            Text(" ${m.imdbRating}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Release Date: ${m.releaseDate}", color = OttGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Languages: ${m.languages}", color = TextLightGray, fontSize = 12.sp)
                                    Text("Genre: ${m.genre}", color = TextLightGray, fontSize = 12.sp)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = m.synopsis,
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { viewModel.saveAiSearchedMovieToWatchlist(m) },
                                        colors = ButtonDefaults.buttonColors(containerColor = OttRed),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ai_add_watchlist"),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save & Add to Watchlist")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== PLATFORMS SCREEN ====================
@Composable
fun PlatformsScreen(viewModel: OttViewModel) {
    val activePlatform by viewModel.activePlatform.collectAsStateWithLifecycle()
    val movies by viewModel.platformMovies.collectAsStateWithLifecycle()

    val platforms = listOf("Netflix", "Prime Video", "JioHotstar", "Sony LIV", "ZEE5", "Aha")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(text = "OTT Streaming Platforms", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = "Tap any network brand below to view exclusive catalogues", color = TextGray, fontSize = 12.sp)
        }

        // Platform Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(platforms) { platform ->
                val isActive = activePlatform == platform
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .clickable { viewModel.activePlatform.value = platform },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) getPlatformColor(platform) else OttSurface
                    ),
                    border = BorderStroke(1.dp, if (isActive) getPlatformColor(platform) else BorderGray)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = platform,
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = BorderGray, thickness = 1.dp)

        // Platform Header and content category counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$activePlatform Curations",
                color = getPlatformColor(activePlatform),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${movies.size} Titles Available",
                color = TextGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No releases added for $activePlatform yet.", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies) { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OttSurface, RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectMovie(movie.id) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImageThumbnailCard(
                            imageUrl = movie.posterUrl,
                            title = movie.title,
                            modifier = Modifier.size(60.dp, 84.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = movie.genre, color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Rating: ${movie.imdbRating}", color = OttGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (movie.isUpcoming) {
                                    Box(
                                        modifier = Modifier
                                            .background(OttGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Upcoming", color = OttGold, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF2E7D32).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Streaming Now", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
                    }
                }
            }
        }
    }
}

// ==================== WATCHLIST SCREEN ====================
@Composable
fun WatchlistScreen(viewModel: OttViewModel) {
    val watchlist by viewModel.watchlistMovies.collectAsStateWithLifecycle()
    val allMovies by viewModel.allMovies.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(text = "My Personal Watchlist", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = "All saved movies, web series, and scheduled release alerts", color = TextGray, fontSize = 12.sp)
        }

        if (watchlist.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your watchlist is empty!",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add titles from Home, Search, or Gemini AI lookup.",
                    color = TextGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(watchlist) { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OttSurface, RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectMovie(movie.id) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImageThumbnailCard(
                            imageUrl = movie.posterUrl,
                            title = movie.title,
                            modifier = Modifier.size(60.dp, 84.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(movie.title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(movie.genre, color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(getPlatformColor(movie.ottPlatform), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(movie.ottPlatform, color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if (movie.reminderSet) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OttGold, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleWatchlist(movie) },
                            modifier = Modifier.testTag("remove_watchlist_item_${movie.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OttRed)
                        }
                    }
                }
            }
        }

        // PERSISTENT PERSONAL RECOMMENDATIONS
        if (watchlist.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OttSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Recommend, contentDescription = null, tint = OttGold)
                        Text(
                            text = "AI Personal Recommendations",
                            color = OttGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Compute dynamic local recommendation based on watchlist genres
                    val favoriteGenre = watchlist.map { it.genre.split("/").first().trim() }
                        .groupBy { it }
                        .maxByOrNull { it.value.size }?.key ?: ""

                    val recommendedMovie = allMovies.filter {
                        !it.isWatchlisted && it.genre.contains(favoriteGenre, ignoreCase = true)
                    }.randomOrNull()

                    if (recommendedMovie != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable { viewModel.selectMovie(recommendedMovie.id) },
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ImageThumbnailCard(
                                imageUrl = recommendedMovie.posterUrl,
                                title = recommendedMovie.title,
                                modifier = Modifier.size(45.dp, 60.dp)
                            )
                            Column {
                                Text(
                                    text = "Because you like $favoriteGenre:",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = recommendedMovie.title,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Streaming on ${recommendedMovie.ottPlatform} • ⭐ ${recommendedMovie.imdbRating}",
                                    color = TextLightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Add more movies in genres like Action/Thriller for custom streaming suggestions.",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ==================== PROFILE / ADMIN SCREEN ====================
@Composable
fun ProfileScreen(viewModel: OttViewModel) {
    val watchlist by viewModel.watchlistMovies.collectAsStateWithLifecycle()
    val allMovies by viewModel.allMovies.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "My Profile Hub", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        // STATS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OttSurface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${watchlist.size}", color = OttRed, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(text = "Watchlist", color = TextGray, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(1.dp, 40.dp)
                            .background(BorderGray)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val upcomingAlerts = watchlist.filter { it.reminderSet }.size
                        Text(text = "$upcomingAlerts", color = OttGold, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(text = "Reminders", color = TextGray, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(1.dp, 40.dp)
                            .background(BorderGray)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${allMovies.size}", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(text = "Total Cataloged", color = TextGray, fontSize = 12.sp)
                    }
                }
            }
        }

        // RENDERING ADMIN PANEL IN Profile Screen
        item {
            if (!isAdminLoggedIn) {
                // Render Admin Login section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OttSurface),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = OttRed)
                            Text(text = "Admin Release Portal Login", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val adminUser by viewModel.adminUsername.collectAsStateWithLifecycle()
                        val adminPass by viewModel.adminPassword.collectAsStateWithLifecycle()
                        val adminErrorState by viewModel.adminErrorState.collectAsStateWithLifecycle()

                        OutlinedTextField(
                            value = adminUser,
                            onValueChange = { viewModel.adminUsername.value = it },
                            label = { Text("Admin Username") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_username_field"),
                            keyboardOptions = KeyboardOptions(
                                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                                autoCorrectEnabled = false
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = OttBackground,
                                unfocusedContainerColor = OttBackground
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = adminPass,
                            onValueChange = { viewModel.adminPassword.value = it },
                            label = { Text("Admin Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_field"),
                            keyboardOptions = KeyboardOptions(
                                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Password
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = OttBackground,
                                unfocusedContainerColor = OttBackground
                            ),
                            singleLine = true
                        )

                        if (adminErrorState != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = adminErrorState!!, color = OttRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.loginAdmin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OttRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_login_submit_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Secure Login", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Admin dashboard logged in
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Admin Access Granted ✅", color = OttGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.logoutAdmin() }) {
                            Text("Logout", color = OttRed)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleAdminScreen(!isAdminMode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdminMode) OttRed else OttSurface
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isAdminMode) "Hide Upload Form" else "Open Release Upload Form")
                        }
                    }

                    if (isAdminMode) {
                        AdminUploadForm(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUploadForm(viewModel: OttViewModel) {
    val fTitle by viewModel.formTitle.collectAsStateWithLifecycle()
    val fPoster by viewModel.formPoster.collectAsStateWithLifecycle()
    val fPlatform by viewModel.formPlatform.collectAsStateWithLifecycle()
    val fReleaseDate by viewModel.formReleaseDate.collectAsStateWithLifecycle()
    val fRating by viewModel.formRating.collectAsStateWithLifecycle()
    val fGenre by viewModel.formGenre.collectAsStateWithLifecycle()
    val fLanguages by viewModel.formLanguages.collectAsStateWithLifecycle()
    val fRuntime by viewModel.formRuntime.collectAsStateWithLifecycle()
    val fSynopsis by viewModel.formSynopsis.collectAsStateWithLifecycle()
    val fTrailer by viewModel.formTrailer.collectAsStateWithLifecycle()
    
    val fFeatured by viewModel.formIsFeatured.collectAsStateWithLifecycle()
    val fUpcoming by viewModel.formIsUpcoming.collectAsStateWithLifecycle()
    val fTrending by viewModel.formIsTrending.collectAsStateWithLifecycle()
    val fNewSeries by viewModel.formIsNewSeries.collectAsStateWithLifecycle()

    val formStatus by viewModel.formStatus.collectAsStateWithLifecycle()

    var platformMenuExpanded by remember { mutableStateOf(false) }
    val platforms = listOf("Netflix", "Prime Video", "JioHotstar", "Sony LIV", "ZEE5", "Aha")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        colors = CardDefaults.cardColors(containerColor = OttSurfaceVariant),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Publish New OTT Release", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = fTitle,
                onValueChange = { viewModel.formTitle.value = it },
                label = { Text("Title *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_form_title"),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
            )

            OutlinedTextField(
                value = fPoster,
                onValueChange = { viewModel.formPoster.value = it },
                label = { Text("Poster Image URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
            )

            // Platform Dropdown Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fPlatform,
                    onValueChange = {},
                    label = { Text("OTT Platform") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { platformMenuExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextWhite)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                DropdownMenu(
                    expanded = platformMenuExpanded,
                    onDismissRequest = { platformMenuExpanded = false },
                    modifier = Modifier.background(OttSurface)
                ) {
                    platforms.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform, color = TextWhite) },
                            onClick = {
                                viewModel.formPlatform.value = platform
                                platformMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = fReleaseDate,
                onValueChange = { viewModel.formReleaseDate.value = it },
                label = { Text("Release Date / Streaming Tag") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fRating,
                    onValueChange = { viewModel.formRating.value = it },
                    label = { Text("IMDb") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = fRuntime,
                    onValueChange = { viewModel.formRuntime.value = it },
                    label = { Text("Runtime") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
            }

            OutlinedTextField(
                value = fGenre,
                onValueChange = { viewModel.formGenre.value = it },
                label = { Text("Genres * (e.g. Crime / Thriller)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
            )

            OutlinedTextField(
                value = fLanguages,
                onValueChange = { viewModel.formLanguages.value = it },
                label = { Text("Languages Available") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
            )

            OutlinedTextField(
                value = fSynopsis,
                onValueChange = { viewModel.formSynopsis.value = it },
                label = { Text("Synopsis / Story-line *") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                minLines = 3
            )

            OutlinedTextField(
                value = fTrailer,
                onValueChange = { viewModel.formTrailer.value = it },
                label = { Text("YouTube Trailer URL") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
            )

            // Flags
            Text("Release Classifications", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fFeatured, onCheckedChange = { viewModel.formIsFeatured.value = it })
                    Text("Featured", color = TextWhite, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fUpcoming, onCheckedChange = { viewModel.formIsUpcoming.value = it })
                    Text("Upcoming", color = TextWhite, fontSize = 12.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fTrending, onCheckedChange = { viewModel.formIsTrending.value = it })
                    Text("Trending", color = TextWhite, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fNewSeries, onCheckedChange = { viewModel.formIsNewSeries.value = it })
                    Text("Web Series", color = TextWhite, fontSize = 12.sp)
                }
            }

            if (formStatus != null) {
                Text(
                    text = formStatus!!,
                    color = if (formStatus!!.startsWith("Success")) Color.Green else OttRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = { viewModel.uploadMovieFromAdmin() },
                colors = ButtonDefaults.buttonColors(containerColor = OttRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_upload_submit"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish to Repository", fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ==================== MOVIE DETAILS DIRECT OVERLAY ====================
@Composable
fun MovieDetailsOverlay(
    movie: Movie,
    onDismiss: () -> Unit,
    viewModel: OttViewModel
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = OttSurface),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner Aspect Poster Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(movie.posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(android.R.drawable.ic_menu_gallery)
                    )

                    // Top Blur / Shading
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.7f),
                                        Color.Transparent,
                                        OttSurface
                                    )
                                )
                            )
                    )

                    // Close Button
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .testTag("close_movie_details")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                    }

                    // Platform Sticker Brand Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .background(getPlatformColor(movie.ottPlatform), RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = movie.ottPlatform,
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Movie Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = movie.title,
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "IMDb Rating", tint = OttGold, modifier = Modifier.size(20.dp))
                            Text(
                                text = movie.imdbRating.toString(),
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Specifications Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = movie.releaseDate, color = OttGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "|", color = BorderGray)
                        Text(text = movie.runtime, color = TextLightGray, fontSize = 13.sp)
                        Text(text = "|", color = BorderGray)
                        Text(text = movie.genre.split("/").firstOrNull() ?: "", color = TextGray, fontSize = 13.sp)
                    }

                    HorizontalDivider(color = BorderGray, thickness = 1.dp)

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Watch trailer
                        Button(
                            onClick = {
                                val url = movie.trailerUrl
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Streaming trailer on browser...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OttRed),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("watch_trailer_btn"),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trailer", fontWeight = FontWeight.Bold)
                        }

                        // Add Watchlist
                        val isWatchlisted = movie.isWatchlisted
                        Button(
                            onClick = { viewModel.toggleWatchlist(movie) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWatchlisted) Color(0xFF6B1116) else OttSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_watchlist_btn"),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isWatchlisted) OttRed else BorderGray)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isWatchlisted) OttRed else TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isWatchlisted) "Watchlisted" else "Watchlist", color = TextWhite)
                        }

                        // Set Reminder (Upcomings)
                        val reminderSet = movie.reminderSet
                        IconButton(
                            onClick = { viewModel.setReminder(movie) },
                            modifier = Modifier
                                .background(
                                    if (reminderSet) OttGold.copy(alpha = 0.2f) else OttSurfaceVariant,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(BorderStroke(1.dp, if (reminderSet) OttGold else BorderGray), RoundedCornerShape(6.dp))
                                .testTag("set_reminder_btn")
                        ) {
                            Icon(
                                imageVector = if (reminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Reminder",
                                tint = if (reminderSet) OttGold else TextWhite
                            )
                        }

                        // Share Intent
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Check out ${movie.title}!")
                                    putExtra(Intent.EXTRA_TEXT, "Look what is coming on ${movie.ottPlatform}! \"${movie.title}\" streams on ${movie.releaseDate}. Check it out on OTT Updates!")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share with friends"))
                            },
                            modifier = Modifier
                                .background(OttSurfaceVariant, RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(6.dp))
                                .testTag("share_movie_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = TextWhite)
                        }
                    }

                    HorizontalDivider(color = BorderGray, thickness = 1.dp)

                    // Synopsis Block
                    Text(text = "Synopsis", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = movie.synopsis,
                        color = TextLightGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Language Block
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Available Languages", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = movie.languages, color = TextGray, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


// --- Brand Theme Color helpers for aesthetic premium branding ---
fun getPlatformColor(platform: String): Color {
    return when (platform.trim().lowercase()) {
        "netflix" -> Color(0xFFE50914)
        "prime video" -> Color(0xFF00A8E1)
        "jiohotstar" -> Color(0xFF1E2F97)
        "sony liv" -> Color(0xFFDF7E25)
        "zee5" -> Color(0xFF820B82)
        "aha" -> Color(0xFFFF5722)
        else -> Color(0xFF5A5A5A)
    }
}
