package hooker

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import config.TaplusConfig.ENGINE
import config.TaplusConfig.PREF
import de.robv.android.xposed.XposedHelpers.callMethod
import io.github.yangyiyu08.taplusext.R
import preference.CheckBoxPreference
import preference.DropDownPreference
import preference.EditTextPreference
import preference.PreferenceCategory


/**
 * Created by qingyu on 2023-02-04.
 */
internal object SettingsHoker : YukiBaseHooker() {
    override fun onHook() {
        "$packageName.setting.fragment.MainSettingsFragment".hook {
            injectMember {
                method { name = "initPreferences"; emptyParam() }
                afterHook {
                    val prefScreen by lazy {
                        callMethod(instance, "getPreferenceScreen")
                    }

                    val ctx by lazy {
                        callMethod(instance, "getContext") as Context
                    }

                    val extCategory = createTaplusExtCategory(ctx)
                    // must be added to screen first
                    callMethod(prefScreen, "addPreference", extCategory.get())

                    // then add child prefs to category
                    extCategory.addPrefsToExtCategory(ctx)
                }
            }

            injectMember {
                method { name = "enablePrefConfig"; param(BooleanType) }
                afterHook {
                    val screen = callMethod(instance, "findPreference", PREF.EXT_CATEGORY)
                    val count = callMethod(screen, "getPreferenceCount") as Int
                    for (i in 0 until count) {
                        val pref = callMethod(screen, "getPreference", i)
                        callMethod(pref, "setEnabled", args(0).boolean())
                    }
                }
            }
        }
    }

    private fun createTaplusExtCategory(ctx: Context) = PreferenceCategory(ctx).apply {
        setOrder(1)
        setKey(PREF.EXT_CATEGORY)
        setTitle(moduleAppResources.getString(R.string.app_name))
    }

    private fun PreferenceCategory.addPrefsToExtCategory(ctx: Context) {
        val enableLandscape = CheckBoxPreference(ctx).apply {
            setKey(PREF.ENABLE_LANDSCAPE)
            setTitle(moduleAppResources.getString(R.string.enable_landscape_title))
        }

        val removeShopping = CheckBoxPreference(ctx).apply {
            setKey(PREF.REMOVE_SHOPPING)
            setTitle(moduleAppResources.getString(R.string.remove_shopping_title))
        }

        val customSearch = EditTextPreference(ctx).apply {
            setKey(PREF.CUSTOM_SEARCH)
            setText(PREF.getCustomSearchValue(ctx))
            setVisible(ENGINE.CUSTOM == PREF.getSearchEngineValue(ctx))
            setTitle(moduleAppResources.getString(R.string.custom_search_title))
        }

        val searchEngin = DropDownPreference(ctx).apply {
            setKey(PREF.SEARCH_ENGINE)
            setValue(PREF.getSearchEngineValue(ctx))
            setTitle(moduleAppResources.getString(R.string.search_engine_title))
            setEntries(moduleAppResources.getStringArray(R.array.search_engines))
            setEntryValues(moduleAppResources.getStringArray(R.array.search_engine_values))
            setOnPreferenceChangeListener { customSearch.setVisible(it == ENGINE.CUSTOM) }
        }

        addPreferences(enableLandscape, removeShopping, searchEngin, customSearch)
    }
}
