package com.qingyu.micsm

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit


//
// Created by yangyiyu08 on 2022-11-03.
//
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            isDebug = false
            tag = "FuckContentExtension"
        }
    }

    override fun onHook() = encase {
        loadApp("com.miui.contentextension") {
            "$packageName.utils.AppsUtils".hook { hookUtils() }
        }
    }

    private fun YukiMemberHookCreator.hookUtils() {
        injectMember {
            method {
                name = "getIntentWithBrowser"
                param(String::class.java)
            }
            afterHook {
                result = result<Intent>()?.apply {
                    data = Uri.parse(args(0).cast())
                }
            }
        }

        injectMember {
            method {
                name = "openInBrowser"
                returnType = UnitType
                param(ContextClass, String::class.java)
            }
            replaceAny {
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

        injectMember {
            method {
                name = "openGlobalSearch"
                returnType = UnitType
                param(ContextClass, String::class.java, String::class.java)
            }
            replaceAny {
                val context = args(0).cast<Context>()
                val value = args(1).cast<String>()
                if (context != null && value != null) {
                    context.startActivity(Intent().apply {
                        action = Intent.ACTION_WEB_SEARCH
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(SearchManager.QUERY, value)
                    })
                }
            }
        }
    }
}