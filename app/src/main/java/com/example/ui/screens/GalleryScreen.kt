package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.PuzzleProgressEntity
import com.example.data.model.PuzzleCatalogItem
import com.example.ui.components.formatElapsedTime

data class CategoryMeta(
  val id: String,
  val name: String,
  val icon: ImageVector,
  val subtitle: String,
  val previewDrawableId: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
  catalog: List<PuzzleCatalogItem>,
  selectedCategory: String,
  savedProgressMap: Map<String, PuzzleProgressEntity> = emptyMap(),
  onSelectCategory: (String) -> Unit,
  onSelectPuzzle: (PuzzleCatalogItem) -> Unit,
  onAddCustomPuzzle: (title: String, uri: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var isSearchActive by remember { mutableStateOf(false) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      onAddCustomPuzzle("My Custom Photo", uri.toString())
    }
  }

  val categoryMetadata = listOf(
    CategoryMeta("All", "All", Icons.Default.Extension, "Complete HD Gallery", R.drawable.puzzle_sunset_mountains),
    CategoryMeta("Nature", "Nature", Icons.Default.Landscape, "Mountains, Lakes & Aurora", R.drawable.puzzle_sunset_mountains),
    CategoryMeta("Space", "Space", Icons.Default.Nightlight, "Nebulas & Deep Cosmos", R.drawable.puzzle_cosmic_nebula),
    CategoryMeta("Travel", "Travel", Icons.Default.Flight, "Tropical Lagoons & Skies", R.drawable.puzzle_tropical_paradise),
    CategoryMeta("City", "City", Icons.Default.LocationCity, "Cyberpunk & Architecture", R.drawable.puzzle_neon_cyberpunk),
    CategoryMeta("Art", "Art", Icons.Default.Brush, "Sacred Geometry Mandalas", R.drawable.puzzle_mandala_art),
    CategoryMeta("Custom", "Custom", Icons.Default.PhotoLibrary, "Your Personal Photos")
  )

  // Filter puzzles based on selected category and search query
  val filteredPuzzles = catalog.filter { item ->
    val matchesCategory = if (selectedCategory == "All") true else item.category.equals(selectedCategory, ignoreCase = true)
    val matchesSearch = if (searchQuery.isBlank()) true else {
      item.title.contains(searchQuery, ignoreCase = true) || item.category.contains(searchQuery, ignoreCase = true)
    }
    matchesCategory && matchesSearch
  }

  val dailyPuzzle = catalog.firstOrNull { it.isDaily }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.Extension,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Jigsaw Puzzles",
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFFFD54F).copy(alpha = 0.25f),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
              Text(
                text = "HD",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD97706),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        },
        actions = {
          // Toggle Search Bar
          IconButton(
            onClick = {
              isSearchActive = !isSearchActive
              if (!isSearchActive) searchQuery = ""
            },
            modifier = Modifier.testTag("btn_toggle_search")
          ) {
            Icon(
              imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
              contentDescription = if (isSearchActive) "Close Search" else "Search Puzzles",
              tint = MaterialTheme.colorScheme.primary
            )
          }

          // Custom Photo Puzzle Action
          IconButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.testTag("btn_custom_photo_topbar")
          ) {
            Icon(
              Icons.Default.AddPhotoAlternate,
              contentDescription = "Create Custom Photo Puzzle",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
          )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_create_photo_puzzle")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Photo Puzzle", fontWeight = FontWeight.Bold)
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = innerPadding.calculateTopPadding() + 8.dp,
        bottom = innerPadding.calculateBottomPadding() + 80.dp
      ),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier
        .fillMaxSize()
        .testTag("gallery_grid")
    ) {
      // 1. Search Bar (when active)
      if (isSearchActive) {
        item(span = { GridItemSpan(2) }) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title or category (e.g., Mountain, Nebula)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("gallery_search_input")
          )
        }
      }

      // 2. Featured Daily Challenge Hero Card (Shown in "All" or "Nature" when not searching)
      if (searchQuery.isEmpty() && (selectedCategory == "All" || selectedCategory == "Nature") && dailyPuzzle != null) {
        item(span = { GridItemSpan(2) }) {
          DailyChallengeCard(
            puzzle = dailyPuzzle,
            savedProgress = savedProgressMap[dailyPuzzle.id],
            onPlay = { onSelectPuzzle(dailyPuzzle) }
          )
        }
      }

      // 3. Category Showcase Carousel Header
      item(span = { GridItemSpan(2) }) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Categories",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${categoryMetadata.size - 1} Themes",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Visual Category Cards Carousel
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(categoryMetadata, key = { it.id }) { cat ->
              val isSelected = cat.id == selectedCategory
              CategoryShowcaseCard(
                category = cat,
                isSelected = isSelected,
                puzzleCount = if (cat.id == "All") catalog.size else catalog.count { it.category.equals(cat.id, ignoreCase = true) },
                onClick = { onSelectCategory(cat.id) }
              )
            }
          }
        }
      }

      // 4. Category Active Banner & Result Count
      item(span = { GridItemSpan(2) }) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val activeMeta = categoryMetadata.find { it.id == selectedCategory }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = activeMeta?.icon ?: Icons.Default.Extension,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (selectedCategory == "All") "All HD Puzzles" else "${activeMeta?.name} Puzzles",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Text(
            text = "${filteredPuzzles.size} ${if (filteredPuzzles.size == 1) "image" else "images"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // 5. Custom Photo Puzzle Shortcut Card (Shown in "Custom" or "All" when not searching)
      if (searchQuery.isEmpty() && (selectedCategory == "Custom" || selectedCategory == "All")) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(0.85f)
              .clickable {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }
              .border(
                1.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
              )
              .testTag("card_add_custom_photo")
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Custom Puzzle",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Pick any HD photo to solve",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
              )
            }
          }
        }
      }

      // 6. Grid of HD Puzzle Images
      if (filteredPuzzles.isEmpty()) {
        item(span = { GridItemSpan(2) }) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No puzzles found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Try another category or search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = {
                    searchQuery = ""
                    onSelectCategory("All")
                  }
                ) {
                  Text("Reset Filters")
                }
              }
            }
          }
        }
      } else {
        items(filteredPuzzles, key = { it.id }) { item ->
          PuzzleCard(
            puzzle = item,
            savedProgress = savedProgressMap[item.id],
            onClick = { onSelectPuzzle(item) }
          )
        }
      }
    }
  }
}

/**
 * Visual Category Card for the horizontal carousel showcase.
 */
@Composable
fun CategoryShowcaseCard(
  category: CategoryMeta,
  isSelected: Boolean,
  puzzleCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
  val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = containerColor,
    border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
    shadowElevation = if (isSelected) 4.dp else 1.dp,
    modifier = modifier
      .width(135.dp)
      .height(76.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag("cat_card_${category.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = category.icon,
          contentDescription = null,
          tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(verticalArrangement = Arrangement.Center) {
        Text(
          text = category.name,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Text(
          text = "$puzzleCount ${if (puzzleCount == 1) "puzzle" else "puzzles"}",
          fontSize = 10.sp,
          color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun DailyChallengeCard(
  puzzle: PuzzleCatalogItem,
  savedProgress: PuzzleProgressEntity? = null,
  onPlay: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasProgress = savedProgress != null && !savedProgress.isCompleted
  val placedCount = if (hasProgress) {
    savedProgress!!.placedPieceIds.split(",").filter { it.isNotEmpty() }.size
  } else 0
  val percent = if (hasProgress && savedProgress!!.pieceCount > 0) (placedCount * 100) / savedProgress.pieceCount else 0

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onPlay() }
      .border(
        2.dp,
        Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF9100))),
        RoundedCornerShape(20.dp)
      )
      .testTag("card_daily_challenge")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(175.dp)
    ) {
      if (puzzle.drawableResId != null) {
        Image(
          painter = painterResource(id = puzzle.drawableResId),
          contentDescription = puzzle.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else if (puzzle.customUri != null) {
        AsyncImage(
          model = puzzle.customUri,
          contentDescription = puzzle.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      // Gradient overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
            )
          )
      )

      // Daily Badge on top-start
      Surface(
        color = Color(0xFFFF9100),
        shape = RoundedCornerShape(bottomEnd = 12.dp),
        modifier = Modifier.align(Alignment.TopStart)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (hasProgress) "IN PROGRESS • $percent%" else "DAILY CHALLENGE",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      // HD Badge on top-end
      Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(bottomStart = 8.dp),
        modifier = Modifier.align(Alignment.TopEnd)
      ) {
        Text(
          text = "ULTRA HD",
          color = Color(0xFFFFD54F),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }

      // Bottom info and Play button
      Row(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = puzzle.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = if (hasProgress) "$placedCount / ${savedProgress!!.pieceCount} pieces ($percent%)" else "${puzzle.lastPieceCount} Pieces • Relax & Solve",
            color = Color(0xFFFFE082),
            fontSize = 12.sp
          )
        }

        Button(
          onClick = onPlay,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (hasProgress) "Resume" else "Play", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun PuzzleCard(
  puzzle: PuzzleCatalogItem,
  savedProgress: PuzzleProgressEntity? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasProgress = savedProgress != null && !savedProgress.isCompleted
  val placedCount = if (hasProgress) {
    savedProgress!!.placedPieceIds.split(",").filter { it.isNotEmpty() }.size
  } else 0
  val totalPieces = if (hasProgress) savedProgress!!.pieceCount else puzzle.lastPieceCount
  val progressFloat = if (hasProgress && totalPieces > 0) placedCount.toFloat() / totalPieces else 0f

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(0.85f)
      .clickable { onClick() }
      .testTag("puzzle_card_${puzzle.id}")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .background(Color.Black)
      ) {
        if (puzzle.drawableResId != null) {
          Image(
            painter = painterResource(id = puzzle.drawableResId),
            contentDescription = puzzle.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else if (puzzle.customUri != null) {
          AsyncImage(
            model = puzzle.customUri,
            contentDescription = puzzle.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // HD Quality Badge
        Surface(
          color = Color.Black.copy(alpha = 0.6f),
          shape = RoundedCornerShape(bottomEnd = 8.dp),
          modifier = Modifier.align(Alignment.TopStart)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color(0xFFFFD54F),
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "HD",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        // Completion or Resume Badge on top-end
        if (puzzle.completed) {
          Surface(
            color = Color(0xFF10B981),
            shape = CircleShape,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .size(24.dp)
          ) {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = "Completed",
              tint = Color.White,
              modifier = Modifier.padding(3.dp)
            )
          }
        } else if (hasProgress) {
          Surface(
            color = Color(0xFFF59E0B),
            shape = RoundedCornerShape(bottomStart = 8.dp),
            modifier = Modifier.align(Alignment.TopEnd)
          ) {
            Text(
              text = "Resume",
              color = Color.Black,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Piece count tag on bottom-start
        Surface(
          color = Color.Black.copy(alpha = 0.65f),
          shape = RoundedCornerShape(topEnd = 8.dp),
          modifier = Modifier.align(Alignment.BottomStart)
        ) {
          Text(
            text = if (hasProgress) "$placedCount/$totalPieces pcs" else "${puzzle.lastPieceCount} pcs",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        // Mini progress bar overlay at the bottom of the image for in-progress puzzles
        if (hasProgress) {
          LinearProgressIndicator(
            progress = { progressFloat },
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .height(3.dp),
            color = Color(0xFFF59E0B),
            trackColor = Color.Black.copy(alpha = 0.4f)
          )
        }
      }

      // Card bottom metadata
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp)
      ) {
        Text(
          text = puzzle.title,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          maxLines = 1,
          color = MaterialTheme.colorScheme.onSurface
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = puzzle.category,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (puzzle.bestTimeSeconds != null) {
            Text(
              text = formatElapsedTime(puzzle.bestTimeSeconds),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFEAB308)
            )
          } else if (hasProgress) {
            Text(
              text = "In Progress",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFF59E0B)
            )
          }
        }
      }
    }
  }
}
