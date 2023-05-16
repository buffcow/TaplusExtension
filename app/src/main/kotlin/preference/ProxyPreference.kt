package preference

import android.content.Context
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import de.robv.android.xposed.XposedHelpers.setObjectField
import hooker.SettingsHoker.toClass


/**
 * Created by qingyu on 2023-05-08.
 */
internal abstract class ProxyPreference(ctx: Context?, pkg: String, name: String) {

    private val mInstance: Any

    init {
        mInstance = newInstance("${pkg}x.preference.${name}Preference".toClass(), ctx, null)
    }

    fun get() = mInstance

    fun setKey(key: CharSequence) {
        "mKey".setField(key)
    }

    fun setOrder(order: Int) {
        "setOrder".callInstanceMethod(order)
    }

    fun setTitle(title: CharSequence) {
        "setTitle".callInstanceMethod(title)
    }

    fun setVisible(visible: Boolean) {
        "setVisible".callInstanceMethod(visible)
    }

    fun getValue(): String? = "getValue".callInstanceMethod() as? String

    private fun String.setField(value: Any?) = setObjectField(mInstance, this, value)

    protected fun String.callInstanceMethod(vararg args: Any?): Any? =
        callMethod(mInstance, this, *args)
}
