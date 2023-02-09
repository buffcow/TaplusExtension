package io.github.yangyiyu08.taplusext

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.UnitType


/**
 * Created by qingyu on 2023-02-04.
 */
internal object UtilsHoker : YukiBaseHooker() {
    override fun onHook() {
        hookAppsUtils()
        hookImageUtil()
        hookCatcherUtil()
    }

    private fun hookAppsUtils() {
        "$packageName.utils.AppsUtils".hook {
            injectMember {
                method { name = "openGlobalSearch"; paramCount = 3 }
                replaceUnit {
                    val context = args(0).cast<Context>()
                    val value = args(1).cast<String>()
                    if (context != null && value != null) {
                        context.startActivity(Intent().apply {
                            TaplusConfig.getSearchEngineUrl(context)?.let {
                                action = Intent.ACTION_VIEW
                                data = Uri.parse(String.format(it, value))
                            } ?: run {
                                action = Intent.ACTION_WEB_SEARCH
                                putExtra(SearchManager.QUERY, value)
                            }
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                }
            }
            injectMember {
                method { name = "getIntentWithBrowser"; param(String::class.java) }
                afterHook {
                    result = result<Intent>()?.apply { data = Uri.parse(args(0).cast<String?>()) }
                }
            }
            injectMember {
                method {
                    name = "openInBrowser"
                    param(ContextClass, String::class.java)
                    returnType = UnitType
                }
                replaceUnit {
                    val context = args(0).cast<Context>()
                    val uriString = args(1).cast<String>()
                    if (context != null && uriString != null) {
                        context.startActivity(Intent().apply {
                            data = Uri.parse(uriString)
                            action = Intent.ACTION_VIEW
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                }
            }
        }
    }

    private fun hookImageUtil() {
        "$packageName.utils.SuperImageUtils".hook {
            injectMember {
                method { name = "isSupportSuperImage"; emptyParam() }
                replaceToTrue()
            }
        }.ignoredHookClassNotFoundFailure() // less than 2.3.6
    }

    private fun hookCatcherUtil() {
        "$packageName.utils.ContentCatcherUtil".hook {
            injectMember {
                method { name = "isCatcherSupportDoublePress"; paramCount = 1 }
                replaceToTrue()
            }.ignoredNoSuchMemberFailure() //less than 2.2.x
        }
    }
}