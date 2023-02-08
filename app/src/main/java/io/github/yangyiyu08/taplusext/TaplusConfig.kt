package io.github.yangyiyu08.taplusext

import android.content.Context
import java.util.Locale


/**
 * Created by qingyu on 2023-02-04.
 */
internal object TaplusConfig {
    private val zh
        get() = Locale.getDefault().language.lowercase().contains("zh")

    const val KEY_CUSTOM = "custom"
    private const val KEY_DEFAULT = "default"

    const val PREF_SEARCH_ENGINE = "search_engine_pref"
    const val PREF_CUSTOM_SEARCH = "custom_search_pref"

    private const val URL_BING = "https://www.bing.com/search?q=%s"
    private const val URL_BAIDU = "https://www.baidu.com/s?wd=%s"
    private const val URL_SOGOU = "https://www.sogou.com/web?query=%s"
    private const val URL_GOOGLE = "https://www.google.com/search?q=%s"
    private const val URL_EXAMPLE = "https://example.com/?q=%s"

    val SEARCH_ENGINE_DATA = linkedMapOf(
        KEY_DEFAULT to Pair(if (zh) "默认" else "Default", null),
        "baidu" to Pair(if (zh) "百度" else "Baidu", URL_BAIDU),
        "sogou" to Pair(if (zh) "搜狗" else "Sogou", URL_SOGOU),
        "bing" to Pair(if (zh) "必应" else "Bing", URL_BING),
        "google" to Pair(if (zh) "谷歌" else "Google", URL_GOOGLE),
        KEY_CUSTOM to Pair(if (zh) "自定义" else "Custom", URL_EXAMPLE),
    )

    fun getSearchEngineUrl(context: Context): String? {
        val k = context.getPref().getString(PREF_SEARCH_ENGINE, KEY_DEFAULT)
        val c by lazy { getCustomSearchUrl(context) }
        return (if (KEY_CUSTOM == k) c else SEARCH_ENGINE_DATA[k]?.second)?.takeIf {
            it.isNotBlank() && it.contains("%s") && it.startsWith("http")
        }
    }

    fun getCustomSearchUrl(context: Context): String? {
        return context.getPref().getString(PREF_CUSTOM_SEARCH, URL_EXAMPLE)
    }

    private fun Context.getPref() = lazy {
        getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
    }.value
}