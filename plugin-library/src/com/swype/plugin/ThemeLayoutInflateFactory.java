package com.swype.plugin;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;

import com.swype.plugin.ThemeLayoutAttributeParser.ThemeStyledItem;
import com.swype.plugin.ThemeStyledActivityBase.ThemeStyledViewItem;

final public class ThemeLayoutInflateFactory implements LayoutInflater.Factory
{

    private static final String LOG_TAG = "ThemeLayoutInflateFactory";
    private boolean bUsedInActivity = true;
    
    ArrayList<ThemeStyledViewItem> mThemeViews = new ArrayList<ThemeStyledViewItem>();
    ArrayList<ThemeStyledItem> mThemeItems = new ArrayList<ThemeStyledItem>();
    
    public ThemeLayoutInflateFactory(boolean fromActivity) {
        super();
        bUsedInActivity = fromActivity;
    }
    
    public final View onCreateView(String widgetType, Context paramContext, AttributeSet paramAttributeSet)
    {
        View localView = null;
        boolean bHasPrefix = false;

        if (bUsedInActivity)
        {
            if (widgetType.lastIndexOf('.') == -1)
            {
                if (widgetType.equals("View")) {
                    localView = createView(paramContext, widgetType, "android.view.", paramAttributeSet);
                }
                if (localView == null) {
                    localView = createView(paramContext, widgetType, "android.widget.", paramAttributeSet);
                }
                if (localView == null) {
                    localView = createView(paramContext, widgetType, "android.webkit.", paramAttributeSet);
                }
                bHasPrefix = true;
            }
            else
            {
                localView = createView(paramContext, widgetType, null, paramAttributeSet);
                bHasPrefix = false;
            }
        }
        else {
            String id = paramAttributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "id");
            if (id != null) {
                ThemeStyledItem newItem = new ThemeStyledItem();
                //skip "@"
                newItem.id = Integer.valueOf(id.substring(1));
                newItem.mThemeAttrs = new ArrayList<ThemeAttrAssociation>();
                parseViewAttrSet(paramAttributeSet, newItem.mThemeAttrs);
                if (newItem.mThemeAttrs.size() > 0) {
                    mThemeItems.add(newItem);
                }
            }
            return null;
        }

        ThemeStyledViewItem newItem = new ThemeStyledViewItem();
        newItem.mView = localView;
        newItem.mThemeAttrs = new ArrayList<ThemeAttrAssociation>();
        parseViewAttrSet(paramAttributeSet, newItem.mThemeAttrs);

        if (newItem.mThemeAttrs != null) {
            if (bHasPrefix)
            {
                addDefaultWidgetStryleAttrs(widgetType, newItem.mThemeAttrs);
            }
            else if ((localView instanceof TextView))
            {
                addDefaultWidgetStryleAttrs("TextView", newItem.mThemeAttrs);
                if ((localView instanceof CompoundButton)) {
                    addDefaultWidgetStryleAttrs("CompoundButton", newItem.mThemeAttrs);
                }
            }
            else if ((localView instanceof AbsListView))
            {
                addDefaultWidgetStryleAttrs("AbsListView", newItem.mThemeAttrs);
                if ((localView instanceof ListView)) {
                    addDefaultWidgetStryleAttrs("ListView", newItem.mThemeAttrs);
                }
            }
            else if ((localView instanceof ImageView))
            {
                addDefaultWidgetStryleAttrs("ImageView", newItem.mThemeAttrs);
            }
        }
        if (newItem.mThemeAttrs.size() > 0) {
            mThemeViews.add(newItem);
        }
        return localView;
    }

    private View createView(Context paramContext, String paramName, String paramPrefix, AttributeSet paramAttributeSet)
    {
        try
        {
            return LayoutInflater.from(paramContext).createView(paramName, paramPrefix, paramAttributeSet);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "LayoutInflaterFactory: " + e.toString());
        }
        return null;
    }

    /* parse the attribute set defined for a given view, and find those can be styled by our theme   
     * @param paramAttributeSet: attribute set of the view (in)
     * @param paramArrayList: collection of attributes from the attribute set that will be styled by our theme  
     */
    private void parseViewAttrSet(AttributeSet paramAttributeSet, ArrayList<ThemeAttrAssociation> paramArrayList)
    {
        if (paramArrayList == null) {
            return;
        }
        MainApkResourceBroker localResMgr = MainApkResourceBroker.getInstance();
        for (int j = 0; j < paramAttributeSet.getAttributeCount(); j++)
        {
            String attrResIdStr = paramAttributeSet.getAttributeValue(j);
            String attrName = paramAttributeSet.getAttributeName(j);
            if (attrResIdStr.startsWith("?")) {
                try
                {
                    // skip "?"	
                    int attrResId = Integer.parseInt(attrResIdStr.substring(1));
                    if (localResMgr.mThemeStyleableAttrResIds.contains(Integer.valueOf(attrResId))) {
                        String attrFullName = localResMgr.mContext.getResources().getResourceName(attrResId);
                        //if (!attrFullName.substring(0, 7).equals("android")) {
                            paramArrayList.add(new ThemeAttrAssociation(attrName, attrFullName));
                        //}
                    }
                }
                catch (NumberFormatException localNumberFormatException)
                {
                    Log.e(LOG_TAG, "LayoutInflaterFactory: parse attributeValueReferenceId Exception");
                }
                catch (Resources.NotFoundException localNotFoundException)
                {
                    Log.e(LOG_TAG, "LayoutInflaterFactory: parse attributeValueReferenceName Exception");
                }
            }
        }
    }

    /* default widget style attributes added through ThemeManager::addWidgetDefaultStyleAttr at run-time
     * will also be added to themeable attribute set of the view widge 
     * 
     */
    private void addDefaultWidgetStryleAttrs(String paramWidgetName, ArrayList<ThemeAttrAssociation> paramArrayList)
    {
        if (paramArrayList == null) {
            return;
        }
        MainApkResourceBroker localResMgr = MainApkResourceBroker.getInstance();
        ArrayList<ThemeAttrAssociation> attrSet = (ArrayList<ThemeAttrAssociation>) localResMgr.mWidgetDefaultStyleAttrs.get(paramWidgetName);
        if (attrSet != null)
        {
            Iterator<ThemeAttrAssociation> localItor = attrSet.iterator();
            while (localItor.hasNext()) {
                ThemeAttrAssociation themeAttr = localItor.next();
                if (themeAttr != null) {
                    paramArrayList.add(themeAttr);
                }
            }
        }
    }
}

