package io.github.yangyiyu08.taplusext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import kotlin.concurrent.thread


/**
 * Created by qingyu on 2023-02-07.
 */
internal object RecognitionHooker : YukiBaseHooker() {
    override fun onHook() {
        screenLandscapeCtrl()
        deleteSelectedWordsSpace()
        setLongClickToCopySegments()
    }

    private fun screenLandscapeCtrl() {
        "$packageName.services.TextContentExtensionService".hook {
            injectMember {
                method { name = "isScreenPortrait"; emptyParam() }
                beforeHook { if (TaplusConfig.isEnableLandscape(appContext!!)) result = true }
            }
        }
    }

    private fun deleteSelectedWordsSpace() {
        "$packageName.text.adapter.TaplusSegmentAdapter".hook {
            injectMember {
                method { name = "isMatchCharAndNum"; param(String::class.java, String::class.java) }
                replaceToFalse()
            }.ignoredNoSuchMemberFailure()
        }
    }

    private fun setLongClickToCopySegments() {
        "$packageName.text.cardview.TaplusRecognitionExpandedTextCard".hook {
            injectMember {
                constructor { paramCount = 3 }
                afterHook {
                    val closeTaplus = { ctx: Context ->
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

                    val copyAll = { cm: ClipboardManager, words: List<String> ->
                        thread {
                            words.forEach {
                                cm.setPrimaryClip(ClipData.newPlainText(null, it))
                                Thread.sleep(60)
                            }
                        }
                    }

                    field {
                        name = "mCopy"
                    }.get(instance).cast<TextView>()?.setOnLongClickListener { view ->
                        val ctx by lazy { view.context }
                        val words = getObjectField(instance, "mSegmentAdapter").let {
                            it.javaClass.method {
                                name = "getSelectedWordsWithSplit"
                            }.get(it).invoke<String>()?.split(Regex("\\|\\|"))
                        }

                        val cm by lazy { ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
                        words?.let {
                            copyAll(cm, it)
                            Toast.makeText(
                                ctx,
                                moduleAppResources.getString(R.string.toast_copyall_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        closeTaplus(ctx)
                        true
                    }
                }
            }
        }
    }
}