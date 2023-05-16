package config

import android.content.Context


/**
 * Created by qingyu on 2023-02-04.
 */
internal object TaplusConfig {
    object PREF {
        const val EXT_CATEGORY = "pref_ext_category"
        const val SEARCH_ENGINE = "pref_search_engine"
        const val CUSTOM_SEARCH = "pref_custom_search"
        const val REMOVE_SHOPPING = "pref_remove_shopping"
        const val ENABLE_LANDSCAPE = "pref_enable_landscape"

        fun isEnableLandscape(context: Context): Boolean {
            return context.getPref().getBoolean(ENABLE_LANDSCAPE, false)
        }

        fun isRemoveShooping(context: Context): Boolean {
            return context.getPref().getBoolean(REMOVE_SHOPPING, false)
        }

        fun getSearchEngineValue(context: Context): String {
            return context.getPref().getString(SEARCH_ENGINE, ENGINE.DEFAULT)!!
        }

        fun getCustomSearchValue(context: Context): String? {
            return context.getPref().getString(CUSTOM_SEARCH, ENGINE.CUSTOM_URL_EXAMPLE)
        }

        fun getSearchEngineUrl(context: Context): String? {
            return when (val engine = getSearchEngineValue(context)) {
                ENGINE.DEFAULT -> null

                ENGINE.CUSTOM -> getCustomSearchValue(context)?.takeIf {
                    it.isNotBlank() && it.contains("%s") && it.startsWith("http")
                }

                else -> engine
            }
        }

        private fun Context.getPref() = lazy {
            getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        }.value
    }

    object ENGINE {
        const val CUSTOM = "custom"
        const val DEFAULT = "default"
        const val CUSTOM_URL_EXAMPLE = "https://example.com/s?q=%s"
    }
}