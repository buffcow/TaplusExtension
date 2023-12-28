package hooker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.type.android.AttributeSetClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import config.TaplusConfig.PREF
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import io.github.yangyiyu08.taplusext.R
import kotlin.concurrent.thread


/**
 * Created by qingyu on 2023-02-07.
 */
internal object RecognitionHooker : YukiBaseHooker() {
    override fun onHook() {
        screenLandscapeCtrl()
        deleteSelectedWordsSpace()
        hookRecognitionExpandedTextCard()
    }

    private fun screenLandscapeCtrl() {
        "$packageName.services.TextContentExtensionService".hook {
            injectMember {
                method { name = "isScreenPortrait"; emptyParam() }
                beforeHook { if (PREF.isEnableLandscape(appContext!!)) result = true }
            }
        }
    }

    private fun deleteSelectedWordsSpace() {
        "$packageName.text.adapter.TaplusSegmentAdapter".hook {
            injectMember {
                method { name = "isMatchCharAndNum"; param(StringClass, StringClass) }
                replaceToFalse()
            }.ignoredNoSuchMemberFailure()
        }
    }

    private fun hookRecognitionExpandedTextCard() {
        fun HookParam.addCopyAll() {
            fun closeTaplus(ctx: Context) {
                val event by lazy {
                    newInstance(
                        "$packageName.text.TaplusServiceCancelEvent".toClass(),
                        true,
                        ctx.hashCode(),
                        "copy",
                        "nerwords"
                    )
                }

                val bus by lazy {
                    callStaticMethod(
                        "org.greenrobot.eventbus.EventBus".toClass(),
                        "getDefault"
                    )
                }
                callMethod(bus, "post", event)
            }

            fun copyAll(ctx: Context, words: List<String>) {
                val cm by lazy {
                    ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                }
                thread {
                    words.forEach {
                        cm.setPrimaryClip(ClipData.newPlainText(null, it))
                        Thread.sleep(60)
                    }
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            ctx,
                            moduleAppResources.getString(R.string.toast_copyall_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            instance.current().field {
                name = "mCopy"
            }.cast<TextView>()?.setOnLongClickListener {
                val ctx by lazy { it.context }
                val words = instance.current().field { name = "mSegmentAdapter" }
                    .current()!!.method {
                        emptyParam()
                        name = "getSelectedWordsWithSplit"
                    }.string().split(Regex.fromLiteral("||"))
                copyAll(ctx, words)
                closeTaplus(ctx)
                true
            }
        }

        fun HookParam.addShare() {
            fun shareText(ctx: Context, text: String) {
                val intent by lazy {
                    newInstance(
                        "android.content.Intent".toClass(),
                        "android.intent.action.SEND"
                    )
                }
                callMethod(intent, "setType", "text/plain")
                callMethod(intent, "putExtra", "android.intent.extra.TEXT", text)
                callMethod(intent, "setFlags", Intent.FLAG_ACTIVITY_NEW_TASK)
                callMethod(ctx, "startActivity", intent)
            }

            instance.current().field {
                name = "mSelectAll"
            }.cast<TextView>()?.setOnLongClickListener {
                val ctx by lazy { it.context }
                val selectedNumber = instance.current().method {
                    emptyParam()
                    name = "getSelectedNumber"
                }.invoke<Int>()!!
                val mSegmentAdapter =
                    instance.current().field { name = "mSegmentAdapter" }.current()!!
                if (selectedNumber > 0) {
                    // partial share
                    val word = mSegmentAdapter.method {
                        emptyParam()
                        name = "getSelectedWordsWithSplit"
                    }.string().replace(Regex.fromLiteral("||"), "")
                    shareText(ctx, word)
                } else {
                    // nothing selected, share all
                    val segments = mSegmentAdapter.field { name = "mSegments" }.list<Any>()
                    val word = segments.joinToString(separator = "") {
                        it.current().field { name = "word" }.string()
                    }
                    shareText(ctx, word)
                }
                true
            }
        }

        fun HookParam.removeSpaceForSelectedWords(): String? = instance.current()
            .field { name = "mSegmentAdapter" }.current()!!.method {
                param(StringClass)
                name = "getSelectedWordsWithSplit"
            }.invoke<String>("")

        "$packageName.text.cardview.TaplusRecognitionExpandedTextCard".hook {
            injectMember {
                constructor { param(ContextClass, AttributeSetClass, IntType) }
                afterHook {
                    addCopyAll()
                    addShare()
                }
            }
            injectMember {
                method { name = "getSelectedWordsWithBlank"; emptyParam() }
                replaceAny { removeSpaceForSelectedWords() }
            }

            // always enable: search, copy, translate
            injectMember {
                method { name = "enableTextView" }
                beforeHook {
                    args[1] = true
                }
            }
        }

        "$packageName.text.adapter.TaplusSegmentAdapter".hook {
            injectMember {
                method { name = "getSelectedWords" }
                beforeHook {
                    //if not selected return all, else original
                    if (instance.current().method { name = "getSelectedCount"; emptyParam() }
                            .invoke<Int>() == 0) {
                        val segments = instance.current().field { name = "mSegments" }.list<Any>()
                        result = segments.joinToString(separator = "") {
                            it.current().field { name = "word" }.string()
                        }
                    }
                }
            }
        }

        fun HookParam.removeShopping() {
            instance.current().field {
                name = "mShopping"
            }.cast<TextView>()?.visibility = View.GONE
        }

        "$packageName.text.cardview.TaplusRecognitionExpandedImageCard".hook {
            injectMember {
                constructor { param(ContextClass, AttributeSetClass, IntType) }
                afterHook { if (PREF.isRemoveShooping(args(0).cast<Context>()!!)) removeShopping() }
            }
        }
    }
}