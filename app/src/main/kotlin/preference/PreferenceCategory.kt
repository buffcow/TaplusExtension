package preference

import android.content.Context


/**
 * Created by qingyu on 2023-05-16.
 */
internal class PreferenceCategory(ctx: Context?) : ProxyPreference(
    ctx, "android", "Preference", "Category"
) {
    fun setOrder(order: Int) {
        "setOrder".callInstanceMethod(order)
    }

    fun <T : ProxyPreference> addPreferences(vararg pref: T) {
        pref.forEach {
            "addPreference".callInstanceMethod(it.get())
        }
    }
}