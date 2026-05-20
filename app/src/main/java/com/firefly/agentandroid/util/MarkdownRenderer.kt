package com.firefly.agentandroid.util

import android.text.SpannableStringBuilder
import android.widget.TextView
import coil.ImageLoader
import coil.request.ImageRequest
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

object MarkdownRenderer {

    private var markwon: Markwon? = null

    fun render(textView: TextView, markdown: String) {
        val instance = getMarkwon(textView.context)
        val result = instance.toMarkdown(markdown)
        if (result is SpannableStringBuilder) {
            textView.text = result
        } else {
            instance.setMarkdown(textView, markdown)
        }
    }

    fun getMarkwon(context: android.content.Context): Markwon {
        return markwon ?: createMarkwon(context).also { markwon = it }
    }

    private fun createMarkwon(context: android.content.Context): Markwon {
        val imageLoader = ImageLoader.Builder(context)
            .crossfade(true)
            .build()

        return Markwon.builder(context)
            .usePlugin(CoilImagesPlugin.create(context, imageLoader))
            .usePlugin(TablePlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(HtmlPlugin.create())
            .build()
    }
}
