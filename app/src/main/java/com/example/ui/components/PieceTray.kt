package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PuzzlePiece

@Composable
fun PieceTray(
  pieces: List<PuzzlePiece>,
  bitmap: ImageBitmap,
  rows: Int,
  cols: Int,
  placedCount: Int,
  totalCount: Int,
  borderOnlyFilter: Boolean,
  selectedPieceId: Int?,
  onToggleBorderFilter: () -> Unit,
  onSelectPiece: (Int?) -> Unit,
  onPlaceSelected: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Filter unplaced pieces
  val unplacedPieces = pieces.filter { !it.isPlaced }
  val displayedPieces = if (borderOnlyFilter) {
    unplacedPieces.filter { it.isBorder }
  } else {
    unplacedPieces
  }

  val borderRemaining = unplacedPieces.count { it.isBorder }
  val progressPercent = if (totalCount > 0) (placedCount * 100) / totalCount else 0

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp,
    shadowElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
    ) {
      // Progress Bar & Controls row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Border Pieces Filter Chip (Easybrain staple)
        FilterChip(
          selected = borderOnlyFilter,
          onClick = onToggleBorderFilter,
          label = {
            Text(
              text = if (borderOnlyFilter) "Borders ($borderRemaining)" else "Borders Only",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.CropSquare,
              contentDescription = "Borders Only",
              modifier = Modifier.size(16.dp)
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          modifier = Modifier.testTag("filter_border_pieces")
        )

        // Progress text and bar
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "$placedCount / $totalCount ($progressPercent%)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { placedCount.toFloat() / totalCount.coerceAtLeast(1) },
            modifier = Modifier
              .width(100.dp)
              .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Piece Carousel
      if (displayedPieces.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
          contentAlignment = Alignment.Center
        ) {
          if (unplacedPieces.isEmpty()) {
            Text(
              text = "🎉 All pieces placed!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "All border pieces placed!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Button(
                onClick = onToggleBorderFilter,
                modifier = Modifier.padding(top = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
              ) {
                Text("Show All Pieces (${unplacedPieces.size})")
              }
            }
          }
        }
      } else {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("piece_tray_row"),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          items(displayedPieces, key = { it.id }) { piece ->
            val isSelected = piece.id == selectedPieceId
            JigsawPieceThumbnail(
              piece = piece,
              bitmap = bitmap,
              totalRows = rows,
              totalCols = cols,
              isSelected = isSelected,
              sizeDp = 84.dp,
              onClick = {
                if (isSelected) {
                  onSelectPiece(null)
                } else {
                  onSelectPiece(piece.id)
                }
              }
            )
          }
        }
      }

      // Quick-action banner when a piece is selected
      AnimatedVisibility(
        visible = selectedPieceId != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Tap on the board to place piece",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )

          Button(
            onClick = onPlaceSelected,
            modifier = Modifier.testTag("btn_place_selected"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Snap to Slot")
          }
        }
      }
    }
  }
}
