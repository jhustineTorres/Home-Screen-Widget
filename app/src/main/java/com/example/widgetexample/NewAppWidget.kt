package com.example.widgetexample

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews

const val WIDGET_SYNC = "WIDGET_SYNC"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [NewAppWidgetConfigureActivity]
 */
class NewAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        //this is where we receive an intent broadcast
        val action = intent!!.action ?: ""

        if(context != null && action == "increase") {
            //update preferences value
            val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            prefs.edit().putString(
                "widgetText",
                ((prefs.getString("widgetText", "0") ?: "0").toInt() + 1).toString()
            ).apply()

            //update widgets after operation
            updateWidgets(context)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        if (appWidgetManager != null) {
            if (context != null) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

// update all widgets
private fun updateWidgets(context: Context) {
    val manager = AppWidgetManager.getInstance(context)
    val ids = manager.getAppWidgetIds(ComponentName(context, NewAppWidget::class.java))
    //update every widget
    ids.forEach { id -> updateAppWidget(context, manager, id) }
}

//this is where we create suck an intent
private fun pendingIntent(
    context: Context?,
    action: String
): PendingIntent?{
    val intent = Intent(context, NewAppWidget::class.java)
    intent.action = action

    //return the pending intent
    return PendingIntent.getBroadcast(
        context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}

 internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    //get the widget text from shared preferences
    val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    val widgetText = prefs.getString("widgetText", "0")
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)
    views.setTextViewText(R.id.tv_random, java.util.Random().nextInt().toString())

    //launch a pending intent to increase the value saved in shared preferences
    views.setOnClickPendingIntent(R.id.iv_sync, pendingIntent(context, "increase"))

    //views.setOnClickPendingIntent(R.id.iv_sync, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

