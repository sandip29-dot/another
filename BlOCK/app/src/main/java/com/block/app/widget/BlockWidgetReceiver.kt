package com.block.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * AppWidgetReceiver entry-point for the BlOCK home-screen widget.
 * Declared in AndroidManifest.xml; delegates to [BlockWidget] for content.
 */
class BlockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BlockWidget()
}
