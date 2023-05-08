package hooker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.StringClass
import config.TaplusConfig
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
                beforeHook { if (TaplusConfig.PREF.isEnableLandscape(appContext!!)) result = true }
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
        "$packageName.text.cardview.TaplusRecognitionExpandedTextCard".hook {
            injectMember {
                constructor { paramCount = 3 }
                afterHook {
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

                    field {
                        name = "mCopy"
                    }.get(instance).cast<TextView>()?.setOnLongClickListener {
                        val ctx by lazy { it.context }
                        val words = field { name = "mSegmentAdapter" }.get(instance)
                            .current()!!.method {
                                emptyParam()
                                name = "getSelectedWordsWithSplit"
                            }.string().split(Regex.fromLiteral("||"))
                        copyAll(ctx, words)
                        closeTaplus(ctx)
                        true
                    }
                }
            }

            injectMember {
                method { name = "getSelectedWordsWithBlank"; emptyParam() }
                replaceAny {
                    field { name = "mSegmentAdapter" }.get(instance).current()!!.method {
                        param(StringClass)
                        name = "getSelectedWordsWithSplit"
                    }.call("")
                }
            }
        }
    }
}