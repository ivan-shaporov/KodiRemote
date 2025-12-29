package com.dom.kodiremote.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.dom.kodiremote.R
import com.dom.kodiremote.presentation.MainActivity

/**
 * Provides a static Play/Pause button that toggles Kodi playback.
 *
 * This data source intentionally does not reflect player state.
 */
class MainComplicationService : SuspendingComplicationDataSourceService() {

	override fun getPreviewData(type: ComplicationType): ComplicationData? {
		return when (type) {
			ComplicationType.SMALL_IMAGE -> createSmallImageComplicationData()
			ComplicationType.SHORT_TEXT -> createShortTextComplicationData()
			ComplicationType.LONG_TEXT -> createLongTextComplicationData()
			else -> null
		}
	}

	override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
		return when (request.complicationType) {
			ComplicationType.SMALL_IMAGE -> createSmallImageComplicationData()
			ComplicationType.SHORT_TEXT -> createShortTextComplicationData()
			ComplicationType.LONG_TEXT -> createLongTextComplicationData()
			else -> throw IllegalArgumentException("Unknown complication type")
		}
	}

	private fun createTapAction(): PendingIntent {
		val intent = Intent(this, MainActivity::class.java).apply {
			putExtra(MainActivity.EXTRA_TILE_ACTION, MainActivity.TILE_ACTION_PLAY_PAUSE)
		}
		return PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
	}

	private fun createSmallImageComplicationData(): ComplicationData {
		return SmallImageComplicationData.Builder(
			smallImage = SmallImage.Builder(
				image = Icon.createWithResource(this, R.drawable.ic_play_arrow),
				type = SmallImageType.ICON
			).build(),
			contentDescription = PlainComplicationText.Builder("Play/Pause").build()
		)
			.setTapAction(createTapAction())
			.build()
	}

	private fun createShortTextComplicationData(): ComplicationData {
		return ShortTextComplicationData.Builder(
			text = PlainComplicationText.Builder("Kodi").build(),
			contentDescription = PlainComplicationText.Builder("Play/Pause").build()
		)
			.setMonochromaticImage(
				MonochromaticImage.Builder(
					Icon.createWithResource(this, R.drawable.ic_play_arrow)
				).build()
			)
			.setTapAction(createTapAction())
			.build()
	}

	private fun createLongTextComplicationData(): ComplicationData {
		return LongTextComplicationData.Builder(
			text = PlainComplicationText.Builder("Kodi Play/Pause").build(),
			contentDescription = PlainComplicationText.Builder("Play/Pause").build()
		)
			.setMonochromaticImage(
				MonochromaticImage.Builder(
					Icon.createWithResource(this, R.drawable.ic_play_arrow)
				).build()
			)
			.setTapAction(createTapAction())
			.build()
	}
}