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

private const val RESOURCES_VERSION = "0"

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources(requestParams)

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ) = tile(requestParams, this)
}

private fun resources(
    requestParams: RequestBuilders.ResourcesRequest
): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context))
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
): LayoutElementBuilders.LayoutElement {
    val activityComponentName = ComponentName(context, MainActivity::class.java)

    fun clickableFor(action: String): ModifiersBuilders.Clickable {
        val extras = mapOf(
            MainActivity.EXTRA_TILE_ACTION to ActionBuilders.stringExtra(action)
        )
        return ModifiersBuilders.Clickable.Builder()
            .setId("kodi_$action")
            .setOnClick(ActionBuilders.launchAction(activityComponentName, extras))
            .build()
    }

    val playPauseButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_PLAY_PAUSE))
        .setTextContent("⏯")
        .setContentDescription("Play/Pause")
        .setButtonColors(ButtonDefaults.PRIMARY_COLORS)
        .build()

    val stopButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_STOP))
        .setTextContent("⏹")
        .setContentDescription("Stop")
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    val nextButton = Button.Builder(context, clickableFor(MainActivity.TILE_ACTION_NEXT))
        .setTextContent("⏭")
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
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context)
}