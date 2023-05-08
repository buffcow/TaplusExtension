package hooker

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import config.TaplusConfig.ENGINE
import config.TaplusConfig.PREF
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import io.github.yangyiyu08.taplusext.R
import preference.CheckBoxPreference
import preference.DropDownPreference
import preference.EditTextPreference
import preference.ProxyPreference


/**
 * Created by qingyu on 2023-02-04.
 */
internal object SettingsHoker : YukiBaseHooker() {
    override fun onHook() {
        "$packageName.setting.fragment.MainSettingsFragment".hook {
            injectMember {
                method { name = "initPreferences"; emptyParam() }
                afterHook {
                    val preCategory by lazy {
                        getObjectField(instance, "mCommonSettingsCategory")
                    }

                    fun addPreference(pref: ProxyPreference) {
                        callMethod(preCategory, "addPreference", pref.get())
                    }

                    (callMethod(preCategory, "getContext") as Context).let { ctx ->
                        val searchEnginPref = getSearchEnginPref(ctx)
                        val customSearchPref = getCustomSearchPref(ctx)
                        searchEnginPref.setOnPreferenceChangeListener {
                            customSearchPref.setVisible(it == ENGINE.CUSTOM)
                        }
                        customSearchPref.setVisible(ENGINE.CUSTOM == searchEnginPref.getValue())

                        addPreference(searchEnginPref)
                        addPreference(customSearchPref)

                        addPreference(getEnableLandscapePref(ctx))
                    }
                }
            }

            injectMember {
                method { name = "enablePrefConfig"; param(BooleanType) }
                afterHook {
                    listOf(PREF.SEARCH_ENGINE, PREF.CUSTOM_SEARCH, PREF.ENABLE_LANDSCAPE).forEach {
                        val pref = callMethod(instance, "findPreference", it)
                        callMethod(pref, "setEnabled", args(0).boolean())
                    }
                }
            }
        }
    }

    private fun getSearchEnginPref(ctx: Context) = DropDownPreference(ctx).apply {
        setOrder(2)
        setKey(PREF.SEARCH_ENGINE)
        setValue(PREF.getSearchEngineValue(ctx))
        setTitle(moduleAppResources.getString(R.string.search_engine_title))
        setEntries(moduleAppResources.getStringArray(R.array.search_engines))
        setEntryValues(moduleAppResources.getStringArray(R.array.search_engine_values))
    }

    private fun getCustomSearchPref(ctx: Context) = EditTextPreference(ctx).apply {
        setOrder(3)
        setKey(PREF.CUSTOM_SEARCH)
        setText(PREF.getCustomSearchValue(ctx))
        setTitle(moduleAppResources.getString(R.string.custom_search_title))
    }

    private fun getEnableLandscapePref(ctx: Context) = CheckBoxPreference(ctx).apply {
        setOrder(1)
        setKey(PREF.ENABLE_LANDSCAPE)
        setTitle(moduleAppResources.getString(R.string.enable_landscape_title))
    }
}
