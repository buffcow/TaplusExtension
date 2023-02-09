package io.github.yangyiyu08.taplusext

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import de.robv.android.xposed.XposedHelpers.setObjectField
import io.github.yangyiyu08.taplusext.TaplusConfig.KEY_CUSTOM


/**
 * Created by qingyu on 2023-02-04.
 */
internal object SettingsHoker : YukiBaseHooker() {
    private val prefProxyMap = mutableMapOf<String, PreferenceProxy?>()

    override fun onHook() {
        "$packageName.setting.fragment.MainSettingsFragment".hook {
            injectMember {
                method { name = "initPreferences"; emptyParam() }
                afterHook {
                    val preCategory = getObjectField(instance, "mCommonSettingsCategory")
                    (callMethod(preCategory, "getContext") as Context).let {
                        addSearchEnginPref(it, preCategory, instance)
                        addCustomSearchPref(it, preCategory)
                    }
                }
            }

            injectMember {
                method { name = "enablePrefConfig"; param(BooleanType) }
                afterHook { prefProxyMap.forEach { it.value?.setEnabled(args(0).boolean()) } }
            }

            injectMember {
                method { name = "onPreferenceChange"; paramCount = 2 }
                afterHook {
                    val key = callMethod(args(0).any(), "getKey")
                    if (TaplusConfig.PREF_SEARCH_ENGINE == key) {
                        val value = args(1).any() as String
                        prefProxyMap[TaplusConfig.PREF_CUSTOM_SEARCH]?.let {
                            (it as EditTextPreference).setVisible(KEY_CUSTOM == value)
                        }
                    }
                }
            }
        }
    }

    private fun addSearchEnginPref(ctx: Context, preCategory: Any, instance: Any) {
        val searchEnginPref = DropDownPreference(ctx).apply {
            setOrder(2)
            setKey(TaplusConfig.PREF_SEARCH_ENGINE)
            setOnPreferenceChangeListener(instance)
            setEntryValues(TaplusConfig.SEARCH_ENGINE_DATA.keys.toTypedArray())
            setTitle(moduleAppResources.getString(R.string.search_engine_title))
            setEntries(TaplusConfig.SEARCH_ENGINE_DATA.let {
                Array(it.size) { i -> it.values.elementAt(i).first }
            })
        }

        prefProxyMap[TaplusConfig.PREF_SEARCH_ENGINE] = searchEnginPref
        callMethod(preCategory, "addPreference", searchEnginPref.get())
    }

    private fun addCustomSearchPref(ctx: Context, preCategory: Any) {
        val customSearchPref = EditTextPreference(ctx).apply {
            setOrder(3)
            setKey(TaplusConfig.PREF_CUSTOM_SEARCH)
            setText(TaplusConfig.getCustomSearchUrl(ctx))
            setTitle(moduleAppResources.getString(R.string.custom_search_title))
        }

        val seachEnginePref = prefProxyMap[TaplusConfig.PREF_SEARCH_ENGINE] as DropDownPreference?
        customSearchPref.setVisible(KEY_CUSTOM == seachEnginePref?.getValue())

        prefProxyMap[TaplusConfig.PREF_CUSTOM_SEARCH] = customSearchPref
        callMethod(preCategory, "addPreference", customSearchPref.get())
    }

    private class DropDownPreference(ctx: Context?) : PreferenceProxy(ctx, "miui", "DropDown") {
        fun getValue(): String? {
            return callMethod(mInstance, "getValue") as String?
        }

        fun setEntries(entries: Array<String>) {
            callMethod(mInstance, "setEntries", entries)
        }

        fun setEntryValues(entryValues: Array<CharSequence>) {
            callMethod(mInstance, "setEntryValues", entryValues)
        }

        fun setOnPreferenceChangeListener(listener: Any?) {
            callMethod(mInstance, "setOnPreferenceChangeListener", listener)
        }
    }

    private class EditTextPreference(ctx: Context?) : PreferenceProxy(ctx, "android", "EditText") {
        fun setText(text: String?) {
            text?.let { callMethod(mInstance, "setText", it) }
        }
    }

    private open class PreferenceProxy(ctx: Context?, pkg: String, name: String) {
        protected val mInstance: Any

        init {
            mInstance = newInstance("${pkg}x.preference.${name}Preference".toClass(), ctx, null)
        }

        fun get(): Any {
            return mInstance
        }

        fun setKey(key: CharSequence) {
            setObjectField(mInstance, "mKey", key)
        }

        fun setOrder(order: Int) {
            callMethod(mInstance, "setOrder", order)
        }

        fun setTitle(title: CharSequence) {
            callMethod(mInstance, "setTitle", title)
        }

        fun setEnabled(enabled: Boolean) {
            callMethod(mInstance, "setEnabled", enabled)
        }

        fun setVisible(visible: Boolean) {
            callMethod(mInstance, "setVisible", visible)
        }
    }
}