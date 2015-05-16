package com.swype.plugin;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import java.io.InputStream;
import java.util.HashMap;

final class ThemeResourceAccessor
{
    private String mApkPkgName;
    private HashMap<String, Integer> mAttrName2Values = new HashMap<String, Integer>();
    private Resources mResources;
    private Resources.Theme mTheme;
    private int[] mThemeAttrs;
    private TypedArray mThemeAttrValues;

    public ThemeResourceAccessor(Resources.Theme currentContextTheme, Resources currentContextResources, int[] themeTemplateAttrs, String packageName, int defStyle)
    {
        this.mTheme = currentContextTheme;
        this.mResources = currentContextResources;
        this.mThemeAttrs = themeTemplateAttrs;
        this.mApkPkgName = packageName;
        this.mThemeAttrValues = currentContextTheme.obtainStyledAttributes(defStyle, themeTemplateAttrs);
    }

    public InputStream openRawResource(int id) throws NotFoundException {
        return mResources.openRawResource(id);
    }
    
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) {
        mResources.getValue(id, outValue, resolveRefs);
    }
    
    public Drawable getDrawable(int id) {
        return mResources.getDrawable(id);
    }

    final TypedValue getAttrTypedValue(int paramInt, boolean bReturnNew)
    {
        if (mThemeAttrValues != null)
        {
            int i = paramInt - mThemeAttrs[0];
            if (i < 0 || i >= mThemeAttrs.length ) {
                return null;
            }
            if (!mThemeAttrValues.hasValue(i)) {
                return null;
            }
            if (bReturnNew) {
                TypedValue localNewTypedValue = new TypedValue();
                mThemeAttrValues.getValue(i, localNewTypedValue);
                return localNewTypedValue;
            }
            return mThemeAttrValues.peekValue(i);
        }

        TypedValue localTypedValue = new TypedValue();
        if ((mTheme.resolveAttribute(paramInt, localTypedValue, true))) {
            return localTypedValue;
        }
        return null;
    }

    final TypedValue obtainAttrTypedValue(String attrName)
    {
        String str = attrName;
        Integer localInteger;
        if ((localInteger = (Integer)mAttrName2Values.get(str)) == null)
        {
            if (str.substring(0, 13).equals("android:attr/"))
            {
                localInteger = Integer.valueOf(mResources.getIdentifier(str, null, null));
            }
            else
            {
                localInteger = Integer.valueOf(mResources.getIdentifier(str.substring(str.indexOf("/") + 1), "attr", mApkPkgName));
            }
            mAttrName2Values.put(str, localInteger);
        }

        int i;
        if ((i = localInteger.intValue()) == 0) {
            return null;
        }
        return getAttrTypedValue(i, true);
    }
    final TypedValue getAttrTypedValue(String attrName)
    {
        String str = attrName;
        Integer localInteger;
        if ((localInteger = (Integer)mAttrName2Values.get(str)) == null)
        {
            if (str.substring(0, 13).equals("android:attr/"))
            {
                localInteger = Integer.valueOf(mResources.getIdentifier(str, null, null));
            }
            else
            {
                localInteger = Integer.valueOf(mResources.getIdentifier(str.substring(str.indexOf("/") + 1), "attr", mApkPkgName));
            }
            mAttrName2Values.put(str, localInteger);
        }

        int i;
        if ((i = localInteger.intValue()) == 0) {
            return null;
        }
        return getAttrTypedValue(i, false);
    }

    final CharSequence getText(String attrName)
    {
        return getText(getAttrTypedValue(attrName));
    }

    final static CharSequence getText(TypedValue paramTypedValue)
    {
        if (paramTypedValue == null) {
            return null;
        }
        if (paramTypedValue.type == TypedValue.TYPE_STRING)
        {
            return paramTypedValue.string;
        }
        return paramTypedValue.coerceToString();
    }

    final float getDimension(String attrName)
    {
        return getDimension(getAttrTypedValue(attrName));
    }

    final float getDimension(TypedValue paramTypedValue)
    {
        if (paramTypedValue == null) {
            return 0.0f;
        }
        if (paramTypedValue.type == TypedValue.TYPE_DIMENSION)
        {
            return paramTypedValue.getDimension(mResources.getDisplayMetrics());
        } 
        else if (paramTypedValue.type == TypedValue.TYPE_FRACTION)
        {
            return getFraction(paramTypedValue, 1, 1);
        }
        return 0.0f;
    }

    final float getFraction(TypedValue paramTypedValue, float base, float pbase)
    {
        if (paramTypedValue == null) {
            return 0.0f;
        }
        if (paramTypedValue.type == TypedValue.TYPE_FRACTION)
        {
            return paramTypedValue.getFraction(base, pbase);
        }
        return 0.0f;
    }

    final int getBoolean(String attrName)
    {
        return getBoolean(getAttrTypedValue(attrName));
    }

    final static int getBoolean(TypedValue paramTypedValue)
    {
        if (paramTypedValue == null) {
            return -1;
        }
        if (paramTypedValue.type >= TypedValue.TYPE_FIRST_INT
                && paramTypedValue.type <= TypedValue.TYPE_LAST_INT)
        {
            return (paramTypedValue.data != 0)?1:0;
        }
        return -1;
    }

    final int getColor(String attrName)
    {
        return getColor(getAttrTypedValue(attrName));
    }

    final int getColor(TypedValue paramTypedValue)
    {
        if (paramTypedValue == null) {
            return -1;
        }
        switch (paramTypedValue.type)
        {
        case TypedValue.TYPE_STRING:
            return mResources.getColor(paramTypedValue.resourceId);
        case TypedValue.TYPE_INT_COLOR_ARGB8: 
        case TypedValue.TYPE_INT_COLOR_RGB8: 
        case TypedValue.TYPE_INT_COLOR_ARGB4: 
        case TypedValue.TYPE_INT_COLOR_RGB4: 
            return paramTypedValue.data;
        }
        return -1;
    }

    final Drawable getDrawable(String attrName)
    {
        return getDrawable(getAttrTypedValue(attrName));
    }

    final Drawable getDrawable(TypedValue paramTypedValue)
    {
        if (paramTypedValue == null) {
            return null;
        }
        switch (paramTypedValue.type)
        {
        case TypedValue.TYPE_STRING: 
            return mResources.getDrawable(paramTypedValue.resourceId);
        case TypedValue.TYPE_INT_COLOR_ARGB8: 
        case TypedValue.TYPE_INT_COLOR_RGB8: 
        case TypedValue.TYPE_INT_COLOR_ARGB4: 
        case TypedValue.TYPE_INT_COLOR_RGB4: 
            return new ColorDrawable(paramTypedValue.data);
        }
        return null;
    }
    
    final ColorStateList getColorStateList(String attrName)
    {
        return getColorStateList(getAttrTypedValue(attrName));
    }
    
    final ColorStateList getColorStateList(TypedValue paramTypedValue)
    {
        if (paramTypedValue == null) {
            return null;
        }
        if  (paramTypedValue.type == TypedValue.TYPE_STRING)
        { 
            return mResources.getColorStateList(paramTypedValue.resourceId);
        }
        return null;
    }

    final void release()
    {
        mAttrName2Values.clear();
        mThemeAttrValues.recycle();
        mThemeAttrs = null;
        mResources = null;
    }
}
