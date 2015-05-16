package com.swype.plugin;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

final class MainApkResourceBroker
{
    final HashMap<String, ArrayList<ThemeAttrAssociation>> mWidgetDefaultStyleAttrs = new HashMap<String, ArrayList<ThemeAttrAssociation>>();
    // Store the resource ids of all theme styleable attributes in theme template
    final HashSet<Integer> mThemeStyleableAttrResIds = new HashSet<Integer>();

    Context mContext;
    ThemeResourceAccessor mResourceAccessor;

    boolean mHasInited = false;

    private static MainApkResourceBroker sLocalResBroker;

    public static MainApkResourceBroker getInstance()
    {
        if(sLocalResBroker == null ){
            sLocalResBroker = new MainApkResourceBroker();
        }
        return sLocalResBroker;
    }

    public static void releaseInstance()
    {
        if(sLocalResBroker != null ){
            sLocalResBroker = null;
        }
    }
    
    final void release()
    {
        mResourceAccessor.release();
        mResourceAccessor = null;
        mWidgetDefaultStyleAttrs.clear();
        mThemeStyleableAttrResIds.clear();
        mContext = null;
        mHasInited = false;
    }

}
