package preference

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.toClass
import java.lang.reflect.Proxy


/**
 * Created by qingyu on 2023-05-08.
 */
internal class DropDownPreference(ctx: Context?) : ProxyPreference(ctx, "miui", "DropDown") {
    fun setValue(value: String) {
        "setValue".callInstanceMethod(value)
    }

    fun setEntries(entries: Array<String>) {
        "setEntries".callInstanceMethod(entries)
    }

    fun setEntryValues(entryValues: Array<String>) {
        "setEntryValues".callInstanceMethod(entryValues)
    }

    inline fun setOnPreferenceChangeListener(crossinline listener: (value: String) -> Unit) {
        val loader = get().javaClass.classLoader
        val interfaces = arrayOf(
            "androidx.preference.Preference\$OnPreferenceChangeListener".toClass(loader)
        )
        val proxyListener = Proxy.newProxyInstance(loader, interfaces) { _, _, args ->
            listener(args[1] as String)
            true
        }
        "setOnPreferenceChangeListener".callInstanceMethod(proxyListener)
    }
}
