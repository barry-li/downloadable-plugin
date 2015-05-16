package com.swype.plugin;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.io.File;


public final class ThemeApkInfo
{
    private static final String LOG_TAG = "ThemeApkInfo";

    String apkPath;
    String apkName;
    String dexDir;
    private String apkVersionName;
    private int apkVersionCode;
    String apkPackageName;
    private String apkAppName;
    Resources apkResources;
    Drawable apkIcon;
    Context apkContext;
    ThemeMetaData themeMetaData;

    public final int hashCode()
    {
        int i = 31 + (apkAppName == null ? 0 : apkAppName.hashCode());
        i = i * 31 + (apkPackageName == null ? 0 : apkPackageName.hashCode());
        return i = i * 31 + (apkVersionName == null ? 0 : apkVersionName.hashCode());
    }

    public final boolean equals(Object paramObject)
    {
        if (this == paramObject) {
            return true;
        }
        if (paramObject == null) {
            return false;
        }
        if (getClass() != paramObject.getClass()) {
            return false;
        }
        ThemeApkInfo inApkInfo = (ThemeApkInfo)paramObject;

        if (apkAppName == null)
        {
            if (inApkInfo.apkAppName != null) {
                return false;
            }
        }
        else if (!apkAppName.equals(inApkInfo.apkAppName)) {
            return false;
        }

        if (apkPackageName == null)
        {
            if (inApkInfo.apkPackageName != null) {
                return false;
            }
        }
        else if (!apkPackageName.equals(inApkInfo.apkPackageName)) {
            return false;
        }

        if (apkVersionName == null)
        {
            if (inApkInfo.apkVersionName != null) {
                return false;
            }
        }
        else if (!apkVersionName.equals(inApkInfo.apkVersionName)) {
            return false;
        }

        if (apkVersionCode != inApkInfo.apkVersionCode) {
            return false;
        }
        return true;
    }

    /*
     * @param paramPackageName: package name defined in the AndroidManifest.xml of the theme pack
     */
//    static ThemeApkInfo fromInstalledApk(Context mainContext, String paramPackageName) {
//        Context reloadContext;
//        PackageInfo packInfo;
//        ApplicationInfo appInfo;
//        PackageManager packageManager;
//        try {
//            packageManager = mainContext.getPackageManager();
//            packInfo = packageManager.getPackageInfo(paramPackageName, PackageManager.GET_ACTIVITIES);
//            reloadContext = mainContext.createPackageContext(packInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
//            appInfo = packInfo.applicationInfo;
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        try {
//            ThemeApkInfo themeApkInfo = new ThemeApkInfo();
//            themeApkInfo.apkName = "";
//            themeApkInfo.apkPath = appInfo.publicSourceDir;
//            themeApkInfo.apkResources = reloadContext.getResources();
//
//            if (appInfo.icon != 0) {
//                themeApkInfo.apkIcon = themeApkInfo.apkResources.getDrawable(appInfo.icon);
//            }
//            File dexOutputDir = mainContext.getDir("dex", 0);
//            themeApkInfo.dexDir = dexOutputDir.getAbsolutePath();
//            //themeApkInfo.apkContext = reloadContext;
//            themeApkInfo.apkAppName = packageManager.getApplicationLabel(appInfo).toString();
//            themeApkInfo.apkPackageName = appInfo.packageName;
//            themeApkInfo.apkVersionName = packInfo.versionName;
//            themeApkInfo.apkVersionCode = packInfo.versionCode;
//
//            return themeApkInfo;
//        }
//        catch (Exception localException)
//        {
//            Log.e(LOG_TAG, "ApkInfo: " + localException.toString());
//        }
//
//
//        return null;
//    }

    public static ThemeApkInfo fromStaticApkFile(Context mainContext, String paramApkPath)
    {
        File apkFile;
        if ((!(apkFile = new File(paramApkPath)).exists()) || (!paramApkPath.toLowerCase().endsWith(".apk")))
        {
            Log.d(LOG_TAG, "ApkInfo: file path is not correct");
            return null;
        }

        try
        {
            ThemeApkInfo themeApkInfo = new ThemeApkInfo();
            
            PackageInfo mInfo = mainContext.getPackageManager().getPackageArchiveInfo(paramApkPath, PackageManager.GET_ACTIVITIES); 
            if (mInfo != null)
            {
                themeApkInfo.apkVersionName = mInfo.versionName;
                themeApkInfo.apkVersionCode = mInfo.versionCode;
            }
            
            ApplicationInfo appInfo = mInfo.applicationInfo;
            appInfo.sourceDir = paramApkPath;
            appInfo.publicSourceDir = paramApkPath;
            Resources apkResources = mainContext.getPackageManager().getResourcesForApplication(appInfo);

            File dexOutputDir = mainContext.getDir("dex", 0);
            themeApkInfo.dexDir = dexOutputDir.getAbsolutePath();
            themeApkInfo.apkPath = apkFile.getParent();
            themeApkInfo.apkName = apkFile.getName();
            themeApkInfo.apkResources = apkResources;  
            themeApkInfo.apkContext = null;

            if (appInfo.icon != 0) {
                themeApkInfo.apkIcon = apkResources.getDrawable(appInfo.icon);
            }
            if (appInfo.labelRes != 0)
            {
                themeApkInfo.apkAppName = apkResources.getText(appInfo.labelRes).toString();
            }
            else
            {
                themeApkInfo.apkAppName = themeApkInfo.apkName.substring(0, themeApkInfo.apkName.lastIndexOf("."));
            }
            themeApkInfo.apkPackageName = appInfo.packageName;

            themeApkInfo.themeMetaData = readThemeMetaDataFromFile(themeApkInfo.apkResources, themeApkInfo.apkPackageName);
            return themeApkInfo;
        }
        catch (Exception localException)
        {
            Log.e(LOG_TAG, "ApkInfo: " + localException.toString());
        }
        return null;
    }

    private static ThemeMetaData readThemeMetaDataFromFile(Resources themeApkRes, String packageName) {
        int resid = themeApkRes.getIdentifier("theme_metadata", "xml", packageName);
        ThemeMetaData themeMetaData = null;
        XmlResourceParser localXmlResourceParser = null;
        try
        {
            localXmlResourceParser = themeApkRes.getXml(resid);
            themeMetaData = new ThemeMetaData(localXmlResourceParser);
        } catch (Exception localException1) {
            // Log?
        } finally {
            if (localXmlResourceParser != null) {
                localXmlResourceParser.close();
            }
        }

        return themeMetaData;
    }

    public ThemeMetaData getMetadata() {
        return themeMetaData;
    }

    public Resources getResources() {
        return apkResources;
    }
}

