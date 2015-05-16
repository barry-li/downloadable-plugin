package com.swype.plugin;

import java.util.ArrayList;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ThemeLayoutAttributeParser {

    private static final String LOG_TAG = "ThemeLayoutAttributeParser";

    private ThemeLayoutInflateFactory mLayoutInflateFactory;

    //private static ThemeLayoutAttributeParser sLocalAttrParser;

    public static class ThemeStyledItem
    {
        /* each ThemeItem associates the view object to be theme styled and the set of mThemeAttrs which
         * defines the set of theme styled attributes
         */
        int id;
        ArrayList<ThemeAttrAssociation> mThemeAttrs = new ArrayList<ThemeAttrAssociation>();
    }

//    public static ThemeLayoutAttributeParser getInstance()
//    {
//        if(sLocalAttrParser == null ){
//            sLocalAttrParser = new ThemeLayoutAttributeParser();
//        }
//        return sLocalAttrParser;
//    }

    public ThemeLayoutAttributeParser(){
        mLayoutInflateFactory = new ThemeLayoutInflateFactory(false);
    }

    public void setFactory(LayoutInflater inflater){
        clear();
        inflater.setFactory(mLayoutInflateFactory);
    }

    private void clear() {
        mLayoutInflateFactory.mThemeItems.clear();
    }

    @SuppressLint("NewApi") public void applyThemeFromCurrentThemeApk(View view)
    {
        ThemeResourceAccessor resAccessor = null;
        boolean bThemeApkCurrentLoaded = false;
        if (!ThemeApkResourceBroker.getInstance().mHasInited) {
            return;
        } else {
            resAccessor = ThemeApkResourceBroker.getInstance().mResourceAccessor;
            bThemeApkCurrentLoaded = true;
        }

        Iterator<ThemeStyledItem> itemIterator = mLayoutInflateFactory.mThemeItems.iterator();

        while (itemIterator.hasNext())
        {
            ThemeStyledItem localItem = itemIterator.next();
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
                    View localView = view.findViewById(localItem.id);
                    if (localView == null) {
                        continue;
                    }
                    Log.d(LOG_TAG, "Apply theme to:" + localView.getClass().getSimpleName());

                    if ("background".equals(currentAttr.attrName))
                    {
                        localView.setBackground(resAccessor.getDrawable(tVal));
                    }
                    else
                    {
                        Drawable localDrawable;
                        if ((localView instanceof TextView))
                        {
                            if ("textColor".equals(currentAttr.attrName))
                            {
                                ((TextView)localView).setTextColor(tVal.data);
                            }
                            else
                            {
                                float f;
                                if ("textSize".equals(currentAttr.attrName))
                                {
                                    f = tVal.getDimension(ThemeApkResourceBroker.getInstance().mApkInfo.apkResources.getDisplayMetrics());
                                    ((TextView)localView).setTextSize(f);
                                } 
                                else if (((localView instanceof CompoundButton)) && ("button".equals(currentAttr.attrName)))
                                {
                                    localDrawable = resAccessor.getDrawable(tVal);
                                    ((CompoundButton)localView).setButtonDrawable(localDrawable);
                                }
                            }
                        }
                        else if ((localView instanceof AbsListView))
                        {
                            if ("listSelector".equals(currentAttr.attrName))
                            {
                                localDrawable = resAccessor.getDrawable(tVal);
                                ((AbsListView)localView).setSelector(localDrawable);
                            }
                            else if (((localView instanceof ListView)) && ("divider".equals(currentAttr.attrName)))
                            {
                                localDrawable = resAccessor.getDrawable(tVal);
                                ((ListView)localView).setDivider(localDrawable);
                            }
                        }
                        else if (((localView instanceof ImageView)) && ("src".equals(currentAttr.attrName)))
                        {
                            localDrawable = resAccessor.getDrawable(tVal);
                            ((ImageView)localView).setImageDrawable(localDrawable);
                        }
                    }
                }
            }
        }
        clear();
    }
}