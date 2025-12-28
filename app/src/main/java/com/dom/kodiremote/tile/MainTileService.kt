package com.dom.kodiremote.tile

import android.content.ComponentName
import android.content.Context
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.ActionBuilders
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
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.dom.kodiremote.presentation.MainActivity
import com.dom.kodiremote.kodi.NetworkKodiClient

private const val RESOURCES_VERSION = "0"

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

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
                    "kodi_${MainActivity.TILE_ACTION_NEXT}" -> client.next()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isPlaying = client.isPlaying()
        return tile(requestParams, this, isPlaying)
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
        .addIdToImageMapping("ic_skip_next", ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(com.dom.kodiremote.R.drawable.ic_skip_next)
                .build())
            .build())
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    isPlaying: Boolean
): TileBuilders.Tile {
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context, isPlaying))
                        .build()
                )
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        .build()
}

private fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    isPlaying: Boolean
): LayoutElementBuilders.LayoutElement {

    fun clickableFor(action: String): ModifiersBuilders.Clickable {
        return ModifiersBuilders.Clickable.Builder()
            .setId("kodi_$action")
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
            .build()
    }

    val playPauseButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_PLAY_PAUSE))
        .setIconContent(if (isPlaying) "ic_pause" else "ic_play_arrow")
        .setContentDescription(if (isPlaying) "Pause" else "Play")
        .setButtonColors(ButtonDefaults.PRIMARY_COLORS)
        .build()

    val stopButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_STOP))
        .setIconContent("ic_stop")
        .setContentDescription("Stop")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val nextButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_NEXT))
        .setIconContent("ic_skip_next")
        .setContentDescription("Next")
        .setButtonColors(ButtonDefaults.PRIMARY_COLORS)
        .build()

    val buttons = MultiButtonLayout.Builder()
        .addButtonContent(playPauseButton)
        .addButtonContent(nextButton)
        .addButtonContent(stopButton)
        .build()

    return PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Kodi Remote")
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(argb(Colors.DEFAULT.onSurface))
                .build()
        )
        .setContent(buttons)
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData({ resources() }) {
    tile(it, context, false)
}