package com.example.aibomm.widget

import android.app.PendingIntent
import android.app.ActivityOptions
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.aibomm.MainActivity
import com.example.aibomm.QuickCaptureActivity
import com.example.aibomm.R
import com.example.aibomm.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object WidgetPrefs {
    private const val PREFS = "widget_prefs"

    fun getOpacity(context: Context, appWidgetId: Int): Float {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat("opacity_$appWidgetId", 0.85f)
            .coerceIn(0f, 1f)
    }

    fun setOpacity(context: Context, appWidgetId: Int, opacity: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat("opacity_$appWidgetId", opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun clear(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove("opacity_$appWidgetId")
            .apply()
    }
}

internal object WidgetUpdater {
    fun requestUpdate(context: Context, appWidgetId: Int) {
        val manager = AppWidgetManager.getInstance(context)
        val info = manager.getAppWidgetInfo(appWidgetId)
        val provider = info?.provider ?: return
        val update = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = provider
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        context.sendBroadcast(update)
    }

    fun requestUpdateAll(context: Context, provider: Class<*>) {
        val manager = AppWidgetManager.getInstance(context)
        val providerComponent = ComponentName(context, provider)
        val ids = manager.getAppWidgetIds(providerComponent)
        if (ids.isEmpty()) return
        val update = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = providerComponent
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(update)
    }
}

internal object WidgetRender {
    fun update1x1(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_1x1)
        applyOpacity(context, appWidgetId, views)
        views.setOnClickPendingIntent(R.id.widget_root, pendingQuickCapture(context, appWidgetId, QuickCaptureActivity.MODE_TEXT))
        manager.updateAppWidget(appWidgetId, views)
    }

    fun update2x2(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        pendingResult: BroadcastReceiver.PendingResult? = null
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_2x2)
        applyOpacity(context, appWidgetId, views)
        views.setOnClickPendingIntent(R.id.widget_root, pendingOpenList(context, appWidgetId))
        views.setOnClickPendingIntent(R.id.widget_preview, pendingOpenList(context, appWidgetId))
        views.setOnClickPendingIntent(R.id.widget_add, pendingQuickCapture(context, appWidgetId, QuickCaptureActivity.MODE_TEXT))

        manager.updateAppWidget(appWidgetId, views)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val latest = loadLatestTitle(context)
                withContext(Dispatchers.Main) {
                    val updated = RemoteViews(context.packageName, R.layout.widget_2x2)
                    applyOpacity(context, appWidgetId, updated)
                    updated.setOnClickPendingIntent(R.id.widget_root, pendingOpenList(context, appWidgetId))
                    updated.setOnClickPendingIntent(R.id.widget_preview, pendingOpenList(context, appWidgetId))
                    updated.setOnClickPendingIntent(R.id.widget_add, pendingQuickCapture(context, appWidgetId, QuickCaptureActivity.MODE_TEXT))
                    updated.setTextViewText(R.id.widget_preview, latest.ifBlank { context.getString(R.string.widget_empty_preview) })
                    manager.updateAppWidget(appWidgetId, updated)
                }
            } finally {
                pendingResult?.finish()
            }
        }
    }

    fun update4x1(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_4x1)
        applyOpacity(context, appWidgetId, views)
        views.setOnClickPendingIntent(R.id.widget_input, pendingQuickCapture(context, appWidgetId, QuickCaptureActivity.MODE_TEXT))
        views.setOnClickPendingIntent(R.id.widget_mic, pendingQuickCapture(context, appWidgetId, QuickCaptureActivity.MODE_VOICE))
        views.setOnClickPendingIntent(R.id.widget_image, pendingQuickCapture(context, appWidgetId, QuickCaptureActivity.MODE_IMAGE))
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun applyOpacity(context: Context, appWidgetId: Int, views: RemoteViews) {
        val opacity = WidgetPrefs.getOpacity(context, appWidgetId)
        views.setFloat(R.id.widget_root, "setAlpha", opacity)
    }

    private suspend fun loadLatestTitle(context: Context): String {
        val dao = AppDatabase.getDatabase(context).todoDao()
        return dao.getLatestTitle().orEmpty().take(30)
    }

    private fun pendingOpenList(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val options = runCatching {
            ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
        }.getOrNull()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (options != null) {
            PendingIntent.getActivity(context, requestCode * 10 + 1, intent, flags, options)
        } else {
            PendingIntent.getActivity(context, requestCode * 10 + 1, intent, flags)
        }
    }

    private fun pendingQuickCapture(context: Context, requestCode: Int, mode: String): PendingIntent {
        val intent = Intent(context, QuickCaptureActivity::class.java).apply {
            putExtra(QuickCaptureActivity.EXTRA_MODE, mode)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val options = runCatching {
            ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
        }.getOrNull()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (options != null) {
            PendingIntent.getActivity(context, requestCode * 10 + mode.hashCode(), intent, flags, options)
        } else {
            PendingIntent.getActivity(context, requestCode * 10 + mode.hashCode(), intent, flags)
        }
    }
}

class TodoWidget1x1Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            WidgetRender.update1x1(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        WidgetRender.update1x1(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetPrefs.clear(context, it) }
    }
}

class TodoWidget2x2Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            WidgetRender.update2x2(context, appWidgetManager, appWidgetId, goAsync())
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        WidgetRender.update2x2(context, appWidgetManager, appWidgetId, goAsync())
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetPrefs.clear(context, it) }
    }
}

class TodoWidget4x1Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            WidgetRender.update4x1(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        WidgetRender.update4x1(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetPrefs.clear(context, it) }
    }
}
