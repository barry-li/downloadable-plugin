package com.swype.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;

public class ThemeStyledActivityBase extends Activity implements IThemeChangeListener
{
    
    public static class ThemeStyledViewItem
    {
        /* each ThemeItem associates the view object to be theme styled and the set of mThemeAttrs which
         * defines the set of theme styled attributes
         */
        View mView;
        ArrayList<ThemeAttrAssociation> mThemeAttrs = new ArrayList<ThemeAttrAssociation>();
    }

    private static final String LOG_TAG = "ThemeStyledActivity";
    private ThemeLayoutInflateFactory mLayoutInflateFactory = new ThemeLayoutInflateFactory(true);
    private boolean mFirstStart = true;
    private boolean mResponseOnThemeChangeEvent = false;

    @Override
    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        LayoutInflater.from(this).setFactory(mLayoutInflateFactory);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mResponseOnThemeChangeEvent)
        {
            ThemeLoader.getInstance().unregisterThemeChangeListener(this);
            mLayoutInflateFactory.mThemeViews.clear();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (mFirstStart)
        {
            applyCurrentLoadedTheme();
            mFirstStart = false;
            if (mResponseOnThemeChangeEvent)
            {
                ThemeLoader.getInstance().registerThemeChangeListener(this);
                return;
            }
            mLayoutInflateFactory.mThemeViews.clear();
        }
    }

    @SuppressLint("NewApi")
    private void applyCurrentLoadedTheme()
    {
        ThemeResourceAccessor resAccessor = null;
        boolean bThemeApkCurrentLoaded = false;
        if (!ThemeApkResourceBroker.getInstance().mHasInited) {
            resAccessor = MainApkResourceBroker.getInstance().mResourceAccessor;
        } else {
            resAccessor = ThemeApkResourceBroker.getInstance().mResourceAccessor;
            bThemeApkCurrentLoaded = true;
        }

        Iterator<ThemeStyledViewItem> itemIterator = mLayoutInflateFactory.mThemeViews.iterator();

        while (itemIterator.hasNext())
        {
            ThemeStyledViewItem localItem = itemIterator.next();
            Iterator<ThemeAttrAssociation> localAttrItor = localItem.mThemeAttrs.iterator();
            while (localAttrItor.hasNext())
            {
                ThemeAttrAssociation currentAttr = (ThemeAttrAssociation)localAttrItor.next();
                TypedValue tVal = resAccessor.getAttrTypedValue(currentAttr.attrStyleableFullName);
                if (tVal == null && bThemeApkCurrentLoaded) {
                    resAccessor = MainApkResourceBroker.getInstance().mResourceAccessor;
                    tVal = resAccessor.getAttrTypedValue(currentAttr.attrStyleableFullName);
                }
                if (tVal != null)
                {
                    Log.d(LOG_TAG, "Apply theme to:" + localItem.mView.getClass().getSimpleName());

                    if ("background".equals(currentAttr.attrName))
                    {
                        localItem.mView.setBackground(resAccessor.getDrawable(tVal));
                    }
                    else
                    {
                        Drawable localDrawable;
                        if ((localItem.mView instanceof TextView))
                        {
                            if ("textColor".equals(currentAttr.attrName))
                            {
                                ((TextView)localItem.mView).setTextColor(tVal.data);
                            }
                            else
                            {
                                float f;
                                if ("textSize".equals(currentAttr.attrName))
                                {
                                    f = tVal.getDimension(ThemeApkResourceBroker.getInstance().mApkInfo.apkResources.getDisplayMetrics());
                                    ((TextView)localItem.mView).setTextSize(f);
                                } 
                                else if (((localItem.mView instanceof CompoundButton)) && ("button".equals(currentAttr.attrName)))
                                {
                                    localDrawable = resAccessor.getDrawable(tVal);
                                    ((CompoundButton)localItem.mView).setButtonDrawable(localDrawable);
                                }
                            }
                        }
                        else if ((localItem.mView instanceof AbsListView))
                        {
                            if ("listSelector".equals(currentAttr.attrName))
                            {
                                localDrawable = resAccessor.getDrawable(tVal);
                                ((AbsListView)localItem.mView).setSelector(localDrawable);
                            }
                            else if (((localItem.mView instanceof ListView)) && ("divider".equals(currentAttr.attrName)))
                            {
                                localDrawable = resAccessor.getDrawable(tVal);
                                ((ListView)localItem.mView).setDivider(localDrawable);
                            }
                        }
                        else if (((localItem.mView instanceof ImageView)) && ("src".equals(currentAttr.attrName)))
                        {
                            localDrawable = resAccessor.getDrawable(tVal);
                            ((ImageView)localItem.mView).setImageDrawable(localDrawable);
                        }
                    }
                }
            }
        }
    }

    protected void enableCallbackOnThemeChange(boolean bEnable)
    {
        if (bEnable)
        {
            if (!mFirstStart) {
                return;
            }
        }
        else if (!mFirstStart && mResponseOnThemeChangeEvent)
        {
            ThemeLoader.getInstance().registerThemeChangeListener(this);
            mLayoutInflateFactory.mThemeViews.clear();
        }
        mResponseOnThemeChangeEvent = bEnable;
    }

    public void onThemeChange()
    {
        applyCurrentLoadedTheme();
    }

    /* Register a view object with Theme Manager to apply the styled attribute next time 
     * a theme apk is loaded 
     * @param view: the view object that the styled attribute will be applied onto
     * @param attrName: the view property attribute name, such as "background", "textColor"
     * @param attrValueRId: the theme stylable attribute resource id, such as "R.attr.keyboardBackground"
     */
    public void specifyThemeAttrForViewObject(View viewObj, String attrName, int attrValueRId)
    {
        ThemeStyledViewItem newItem = new ThemeStyledViewItem();
        newItem.mView = viewObj;
        String attrFullName = getResources().getResourceName(attrValueRId);
        newItem.mThemeAttrs = new ArrayList<ThemeAttrAssociation>();
        newItem.mThemeAttrs.add(new ThemeAttrAssociation(attrName, attrFullName));
        mLayoutInflateFactory.mThemeViews.add(newItem);
    }
}

