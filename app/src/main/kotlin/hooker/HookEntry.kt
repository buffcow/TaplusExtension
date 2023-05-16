package hooker

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.yangyiyu08.taplusext.BuildConfig


//
// Created by yangyiyu08 on 2022-11-03.
//
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog { isDebug = BuildConfig.DEBUG; tag = "TaplusExtension" }
    }

    override fun onHook() = encase {
        loadApp("com.miui.contentextension") {
            loadHooker(UtilsHoker)
            loadHooker(SettingsHoker)
            loadHooker(RecognitionHooker)
        }
    }
}