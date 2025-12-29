package com.example.aibomm.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.aibomm.R

class WidgetOpacityConfigureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setResult(Activity.RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        setContentView(R.layout.widget_configure)

        val value = findViewById<TextView>(R.id.value)
        val seek = findViewById<SeekBar>(R.id.seek)
        val done = findViewById<Button>(R.id.done)

        val initial = (WidgetPrefs.getOpacity(this, appWidgetId) * 100).toInt().coerceIn(0, 100)
        seek.progress = initial
        value.text = "$initial%"

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                value.text = "${progress.coerceIn(0, 100)}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        done.setOnClickListener {
            val opacity = (seek.progress.coerceIn(0, 100) / 100f)
            WidgetPrefs.setOpacity(this, appWidgetId, opacity)
            WidgetUpdater.requestUpdate(this, appWidgetId)

            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}

