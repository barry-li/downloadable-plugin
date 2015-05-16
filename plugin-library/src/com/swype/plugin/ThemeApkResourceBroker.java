package com.swype.plugin;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class ThemeApkResourceBroker
{
    ThemeApkInfo mApkInfo;
    boolean mHasInited = false;
    ThemeResourceAccessor mResourceAccessor;

    private static ThemeApkResourceBroker sThemeApkResBroker;

    public static ThemeApkResourceBroker getInstance()
    {
        if(sThemeApkResBroker == null ){
            sThemeApkResBroker = new ThemeApkResourceBroker();
        }
        return sThemeApkResBroker;
    }

    public static void releaseInstance()
    {
        if(sThemeApkResBroker != null ){
            sThemeApkResBroker = null;
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean setThemeFromApk(ThemeApkInfo parama)
    {
        if ((parama == null) || (parama.equals(mApkInfo))) {
            return false;
        }
        Resources themeApkRes = parama.apkResources;
        ThemeMetaData apkThemeInfo = parama.themeMetaData;
        if (apkThemeInfo == null) {
            return false;
        }

        if (apkThemeInfo.themeStyleName == null) {
            return false;
        }

        int[] attrs = null;
        try
        {
            DexClassLoader classLoader = new DexClassLoader(parama.apkPath + "/" + parama.apkName, parama.dexDir, null, ClassLoader.getSystemClassLoader());
            Class<?> rClass = classLoader.loadClass(parama.apkPackageName + ".R$styleable");
            attrs = (int[])((rClass.getField(apkThemeInfo.themeStyleableName)).get(null));
        }
        catch (Exception e)
        {
            Log.e("ThemeApkResourceBroker", "ThemeApkResourceBroker: " + e.toString());
            return false;
        }
        mApkInfo = parama;
        Resources.Theme newTheme = themeApkRes.newTheme();
        int j = themeApkRes.getIdentifier(apkThemeInfo.themeStyleName, "style", parama.apkPackageName);
        newTheme.applyStyle(j, true);
        if (mResourceAccessor != null) {
            mResourceAccessor.release();
        }
        j = themeApkRes.getIdentifier("SwypeThemeDefaults", "style", parama.apkPackageName);
        mResourceAccessor = new ThemeResourceAccessor(newTheme, themeApkRes, attrs, mApkInfo.apkPackageName, j);
        mHasInited = true;
        return true;
    }
    
    final void release()
    {
        mResourceAccessor.release();
        mResourceAccessor = null;
        mApkInfo = null;
        mHasInited = false;
    }
}

