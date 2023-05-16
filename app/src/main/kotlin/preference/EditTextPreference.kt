package preference

import android.content.Context


/**
 * Created by qingyu on 2023-05-08.
 */
internal class EditTextPreference(ctx: Context?) : ProxyPreference(ctx, "android", "EditText") {
    fun setText(text: String?) {
        text?.let { "setText".callInstanceMethod(it) }
    }
}
