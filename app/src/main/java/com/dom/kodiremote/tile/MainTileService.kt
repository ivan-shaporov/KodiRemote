package com.dom.kodiremote.tile

import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonDefaults
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.dom.kodiremote.presentation.MainActivity
import com.dom.kodiremote.kodi.NetworkKodiClient

private const val RESOURCES_VERSION = "0"
private const val TILE_FRESHNESS_INTERVAL_MS = 2_000L
private const val TILE_ENTER_UPDATE_MIN_INTERVAL_MS = 1_000L
private const val TILE_TIMELINE_ENTRY_VALIDITY_MS = 2_500L

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    companion object {
        private var lastEnterUpdateElapsedMs: Long = 0L
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        // When the user swipes onto the tile, the system may initially show a cached timeline.
        // Request an update so tileRequest() runs and the play/pause icon reflects Kodi's
        // current player state.
        val now = SystemClock.elapsedRealtime()
        if (now - lastEnterUpdateElapsedMs >= TILE_ENTER_UPDATE_MIN_INTERVAL_MS) {
            lastEnterUpdateElapsedMs = now
            TileService.getUpdater(this).requestUpdate(MainTileService::class.java)
        }
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources()

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val client = NetworkKodiClient()

        if (requestParams.currentState.lastClickableId.isNotEmpty()) {
            try {
                when (requestParams.currentState.lastClickableId) {
                    "kodi_${MainActivity.TILE_ACTION_PLAY_PAUSE}" -> client.playPause()
                    "kodi_${MainActivity.TILE_ACTION_STOP}" -> client.stop()
                    "kodi_${MainActivity.TILE_ACTION_PREVIOUS}" -> client.previous()
                    "kodi_${MainActivity.TILE_ACTION_NEXT}" -> client.next()
                    "kodi_${MainActivity.TILE_ACTION_SEEK_BACK_LARGE}" -> client.seekBy(-MainActivity.SEEK_OFFSET_LARGE_SECONDS)
                    "kodi_${MainActivity.TILE_ACTION_SEEK_BACK_SMALL}" -> client.seekBy(-MainActivity.SEEK_OFFSET_SMALL_SECONDS)
                    "kodi_${MainActivity.TILE_ACTION_SEEK_FORWARD_SMALL}" -> client.seekBy(MainActivity.SEEK_OFFSET_SMALL_SECONDS)
                    "kodi_${MainActivity.TILE_ACTION_SEEK_FORWARD_LARGE}" -> client.seekBy(MainActivity.SEEK_OFFSET_LARGE_SECONDS)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isPlaying = client.isPlaying()
        return tile(this, isPlaying)
    }
}

private fun resources(): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .addIdToImageMapping("ic_play_arrow", ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(com.dom.kodiremote.R.drawable.ic_play_arrow)
                .build())
            .build())
        .addIdToImageMapping("ic_pause", ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(com.dom.kodiremote.R.drawable.ic_pause)
                .build())
            .build())
        .addIdToImageMapping("ic_stop", ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(com.dom.kodiremote.R.drawable.ic_stop)
                .build())
            .build())
        .addIdToImageMapping("ic_skip_previous", ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(com.dom.kodiremote.R.drawable.ic_skip_previous)
                .build())
            .build())
        .addIdToImageMapping("ic_skip_next", ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(com.dom.kodiremote.R.drawable.ic_skip_next)
                .build())
            .build())
        .build()
}

private fun tile(
    context: Context,
    isPlaying: Boolean
): TileBuilders.Tile {
    val nowWallClockMs = System.currentTimeMillis()

    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setValidity(
                    TimelineBuilders.TimeInterval.Builder()
                        .setStartMillis(nowWallClockMs)
                        .setEndMillis(nowWallClockMs + TILE_TIMELINE_ENTRY_VALIDITY_MS)
                        .build()
                )
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(context, isPlaying))
                        .build()
                )
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        // IMPORTANT: In Tiles, a freshness interval of 0 DISABLES auto-refresh.
        // If the system shows a cached timeline when you swipe back to the tile, it may not call
        // tileRequest() again unless the tile is considered stale. Using a short interval makes
        // the platform re-fetch the tile around the time it comes on-screen (best-effort; the
        // system may still throttle).
        .setFreshnessIntervalMillis(TILE_FRESHNESS_INTERVAL_MS)
        .build()
}

private fun tileLayout(
    context: Context,
    isPlaying: Boolean
): LayoutElementBuilders.LayoutElement {

    fun clickableFor(action: String): ModifiersBuilders.Clickable {
        return ModifiersBuilders.Clickable.Builder()
            .setId("kodi_$action")
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
            .build()
    }

    val controlButtonSize = ButtonDefaults.DEFAULT_SIZE.value * 0.81f

    val playPauseButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_PLAY_PAUSE))
        .setSize(controlButtonSize)
        .setIconContent(if (isPlaying) "ic_pause" else "ic_play_arrow")
        .setContentDescription(if (isPlaying) "Pause" else "Play")
        .setButtonColors(ButtonDefaults.PRIMARY_COLORS)
        .build()

    val stopButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_STOP))
        .setSize(controlButtonSize)
        .setIconContent("ic_stop")
        .setContentDescription("Stop")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val nextButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_NEXT))
        .setSize(controlButtonSize)
        .setIconContent("ic_skip_next")
        .setContentDescription("Next")
        .setButtonColors(ButtonDefaults.PRIMARY_COLORS)
        .build()

    val previousButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_PREVIOUS))
        .setSize(controlButtonSize)
        .setIconContent("ic_skip_previous")
        .setContentDescription("Previous")
        .setButtonColors(ButtonDefaults.PRIMARY_COLORS)
        .build()

    val seekBackLargeButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_SEEK_BACK_LARGE))
        .setSize(controlButtonSize)
        .setTextContent("-${MainActivity.SEEK_OFFSET_LARGE_SECONDS}")
        .setContentDescription("Back 30 seconds")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val seekBackSmallButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_SEEK_BACK_SMALL))
        .setSize(controlButtonSize)
        .setTextContent("-${MainActivity.SEEK_OFFSET_SMALL_SECONDS}")
        .setContentDescription("Back 10 seconds")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val seekForwardSmallButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_SEEK_FORWARD_SMALL))
        .setSize(controlButtonSize)
        .setTextContent("+${MainActivity.SEEK_OFFSET_SMALL_SECONDS}")
        .setContentDescription("Forward 10 seconds")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val seekForwardLargeButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_SEEK_FORWARD_LARGE))
        .setSize(controlButtonSize)
        .setTextContent("+${MainActivity.SEEK_OFFSET_LARGE_SECONDS}")
        .setContentDescription("Forward 30 seconds")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val topButtons = MultiButtonLayout.Builder()
        .addButtonContent(playPauseButton)
        .addButtonContent(stopButton)
        .build()

    val seekButtons = LayoutElementBuilders.Row.Builder()
        .addContent(seekBackLargeButton)
        .addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setWidth(DimensionBuilders.dp(0f))
                .build()
        )
        .addContent(seekBackSmallButton)
        .addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setWidth(DimensionBuilders.dp(0f))
                .build()
        )
        .addContent(seekForwardSmallButton)
        .addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setWidth(DimensionBuilders.dp(0f))
                .build()
        )
        .addContent(seekForwardLargeButton)
        .build()

    val bottomButtons = MultiButtonLayout.Builder()
        .addButtonContent(previousButton)
        .addButtonContent(nextButton)
        .build()

    // On round screens, the usable width is much larger near the center.
    // Shifting content down helps keep the 4 seek buttons on one line.
    val contentTopOffsetDp = controlButtonSize * 0.4f

    val content = LayoutElementBuilders.Column.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.dp(contentTopOffsetDp))
                .build()
        )
        .addContent(topButtons)
        .addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.dp(2f))
                .build()
        )
        .addContent(seekButtons)
        .addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.dp(2f))
                .build()
        )
        .addContent(bottomButtons)
        .build()

    return content
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData({ resources() }) {
    tile(context, false)
}