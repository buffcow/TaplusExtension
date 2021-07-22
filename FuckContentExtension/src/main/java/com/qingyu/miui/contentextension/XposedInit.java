package com.qingyu.miui.contentextension;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.*;
public class XposedInit extends XC_MethodReplacement
  implements IXposedHookLoadPackage {
  @Override
  public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam)
    throws Throwable {
    if (!lpparam.packageName.equals("com.miui.contentextension")) { //Hook包名

      return;
    }

    Class<?> cls = lpparam.classLoader.loadClass(
        "com.miui.contentextension.utils.AppsUtils");
    findAndHookMethod(cls, "openInBrowser", Context.class, String.class, this);
    findAndHookMethod(cls, "openGlobalSearch", Context.class, String.class,
      String.class, this);

    findAndHookMethod(cls, "getIntentWithBrowser", String.class,
      new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
          throws Throwable {
          Intent intent = (Intent) param.getResult();
          intent.setData(Uri.parse((String) param.args[0]));
          param.setResult(intent);
        }
      });
  }

  @Override
  protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param)
    throws Throwable {
    Context context = (Context) param.args[0];
    String value = (String) param.args[1];

    switch (param.method.getName()) {
    case "openInBrowser":
      openInBrowser(context, value);

      break;

    case "openGlobalSearch":
      openGlobalSearch(context, value);

      break;
    }

    return null;
  }

  public static void openInBrowser(Context context, String url) {
    if ((context != null) && !TextUtils.isEmpty(url)) {
      Intent intent = new Intent("android.intent.action.VIEW");
      intent.setData(Uri.parse(url));
      context.startActivity(intent);
    }
  }

  private void openGlobalSearch(Context context, String value) {
    try {
      Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
      search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      search.putExtra(SearchManager.QUERY, value);
      context.startActivity(search);
    } catch (Exception e) {
      XposedBridge.log(e);
    }
  }
}
