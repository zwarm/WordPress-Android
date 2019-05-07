package org.wordpress.android.ui.stats.refresh.lists.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import org.wordpress.android.R

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [StatsChartAppWidgetConfigureActivity]
 */
class StatsChartAppWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
            )
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            StatsChartAppWidgetConfigureActivity.deleteTitlePref(
                    context,
                    appWidgetId
            )
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (context != null && appWidgetManager != null) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            Log.d("vojta", "org/wordpress/android/ui/stats/refresh/lists/widget/StatsChartAppWidget.updateAppWidget($appWidgetId)")
            val widgetText = StatsChartAppWidgetConfigureActivity.loadTitlePref(
                    context,
                    appWidgetId
            )
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.stats_chart_app_widget)
            views.setTextViewText(R.id.appwidget_text, widgetText)
            // RemoteViews Service needed to provide adapter for ListView
            val svcIntent = Intent(context, WidgetService::class.java)
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            svcIntent.data = Uri.parse(
                    svcIntent.toUri(Intent.URI_INTENT_SCHEME)
            )
            // setting adapter to listview of the widget
            views.setRemoteAdapter(R.id.list, svcIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

