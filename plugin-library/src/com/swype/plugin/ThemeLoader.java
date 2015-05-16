package com.swype.plugin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class ThemeLoader
{

    private static ThemeLoader sThemeLoader;	
    private HashSet<IThemeChangeListener> mThemeChangeListeners = new HashSet<IThemeChangeListener>();
    private int[] themeTemplateAttrs;
    private int defStyle;
    private boolean bThemeApkLoaded = false;
    private Stack<ThemeLayoutAttributeParser> mParsers = new Stack<ThemeLayoutAttributeParser>();

    private static final String LOG_TAG = "ThemeLoader";

    public static ThemeLoader getInstance()
    {
        if(sThemeLoader == null ){
            sThemeLoader = new ThemeLoader();
        }
        return sThemeLoader;
    }

    private void notifyThemeChange()
    {
        Iterator<IThemeChangeListener> localIterator = mThemeChangeListeners .iterator();
        while (localIterator.hasNext())
        {
            ((IThemeChangeListener)localIterator.next()).onThemeChange();
        }
    }

    final void registerThemeChangeListener(IThemeChangeListener listener)
    {
        mThemeChangeListeners.add(listener);
    }

    final void unregisterThemeChangeListener(IThemeChangeListener listener)
    {
        mThemeChangeListeners.remove(listener);
    }

    
    public void setLayoutInflaterFactory(LayoutInflater inflater){
        if (bThemeApkLoaded) {
            ThemeLayoutAttributeParser parser = new ThemeLayoutAttributeParser();
            parser.setFactory(inflater);
            mParsers.push(parser);
        }
    }
    
    public void applyTheme(View view){
        if (bThemeApkLoaded) {
            ThemeLayoutAttributeParser parser = null;
            try {
                parser = mParsers.pop();
            } catch (EmptyStackException e) {
                
            }
            if (parser != null) {
                parser.applyThemeFromCurrentThemeApk(view);
            }
            parser = null;
        }
    }
    
    
    /* If widget property defines styleable attribute, specifyWidgetDefaultStyleAttr will add entry to
     * the theme manager so that it can apply style to the attribute at run-time  
     * For example: 
     * themeMgr.specifyStyleAttrToWidgetAttr("Button", "background", R.attr.bgcolor);
     * themeMgr.specifyStyleAttrToWidgetAttr("Button", "textColor", R.attr.textColor);
     * will add an entry to the map:
     * {button, {ThemeAttr("background","package:attr/bg_btn)",R.attr.bgcolor), 
     *           ThemeAttr("textColor","package:attr/textColorPrimary",R.attr.textColorPrimary)}}
     */
    public void specifyStyleAttrToWidgetAttr(String widgetTypeName, String attrName, int attrValueRId)
    {
        MainApkResourceBroker localResMgr = MainApkResourceBroker.getInstance();
        if (localResMgr.mContext == null) {
            throw new RuntimeException("Theme Manager has not been initialized.");
        }
        ArrayList<ThemeAttrAssociation> localArrayList;
        if ((localArrayList = (ArrayList<ThemeAttrAssociation>)localResMgr.mWidgetDefaultStyleAttrs.get(widgetTypeName)) == null)
        {
            localArrayList = new ArrayList<ThemeAttrAssociation>();
            localResMgr.mWidgetDefaultStyleAttrs.put(widgetTypeName, localArrayList);
        }
        String fullAttrName = localResMgr.mContext.getResources().getResourceName(attrValueRId);
        localArrayList.add(new ThemeAttrAssociation(attrName, fullAttrName));
    }

    private TypedValue getThemeTypedValueFromMain(int attrValueResId) {
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            // retrieve styled boolean attribute value from main apk using attribute id defined in main apk R.java
            return MainApkResourceBroker.getInstance().mResourceAccessor.getAttrTypedValue(attrValueResId, false);
        }
        return null;
    }
    
    public TypedValue getThemeTypedValueFromApk(int attrValueResId) {
        TypedValue tVal;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                // retrieve styled boolean attribute value from theme apk using attribute name defined in the main apk
                if (attrName != null && (tVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getAttrTypedValue(attrName)) != null) {
                    return tVal;
                }
            }
        }
        return null;
    }

    public void getValueFromApk(int id, TypedValue outValue, boolean resolveRefs) {
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                ThemeApkResourceBroker.getInstance().mResourceAccessor.getValue(id, outValue, resolveRefs);
            }
        }
        
    }
    /*
     * @param attrValueResId attribute id defined as R.attr.xxx
     */
    public boolean getThemedBoolean(int attrValueResId)
    {
        int bVal;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                // retrieve styled boolean attribute value from theme apk using attribute name defined in the main apk
                if ((bVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getBoolean(attrName)) != -1) {
                    return bVal > 0;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled boolean attribute value from main apk using attribute id defined in main apk R.java
            return ThemeResourceAccessor.getBoolean(accessor.getAttrTypedValue(attrValueResId, false)) > 0;
        }
        return false;
    }

    public boolean getThemedBoolean(TypedValue tVal) {
        int bVal; 
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            // retrieve text attribute value from theme apk using attribute name defined in the main apk
            if ((bVal = ThemeResourceAccessor.getBoolean(tVal)) != -1) {
                return bVal > 0;
            }
        }
        return false;
    }

    /*
     * @param attrValueResId attribute id defined as R.attr.xxx
     */
    public int getThemedColor(int attrValueResId)
    {
        int colorVal = -1;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                // retrieve styled color attribute value from theme apk using attribute name defined in the main apk
                if ((colorVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getColor(attrName)) != -1) {
                    return colorVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled color attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getColor(accessor.getAttrTypedValue(attrValueResId, false));
        }
        return colorVal;
    }

    public int getThemedColor(TypedValue tVal)
    {
        int colorVal = -1;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                // retrieve styled color attribute value from theme apk using attribute name defined in the main apk
                if ((colorVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getColor(tVal)) != -1) {
                    return colorVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled color attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getColor(tVal);
        }
        return colorVal;
    }

    public ColorStateList getThemedColorStateList(int attrValueResId)
    {
        ColorStateList colorVal = null;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                // retrieve styled color attribute value from theme apk using attribute name defined in the main apk
                if ((colorVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getColorStateList(attrName)) != null) {
                    return colorVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled color attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getColorStateList(accessor.getAttrTypedValue(attrValueResId, false));
        }
        return colorVal;
    }
    
    public ColorStateList getThemedColorStateList(TypedValue tVal)
    {
        ColorStateList colorVal = null;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                // retrieve styled color attribute value from theme apk using attribute name defined in the main apk
                if ((colorVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getColorStateList(tVal)) != null) {
                    return colorVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled color attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getColorStateList(tVal);
        }
        return colorVal;
    }
    public Drawable getThemedDrawable(int attrValueResId)
    {
        Drawable dVal = null; 
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                // retrieve styled drawable attribute value from theme apk using attribute name defined in the main apk
                if ((dVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getDrawable(attrName)) != null) {
                    return dVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled drawable attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getDrawable(accessor.getAttrTypedValue(attrValueResId, false));
        }
        return dVal;
    }

    public Drawable getThemedDrawable(TypedValue tVal) {
        Drawable dVal = null; 
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                // retrieve styled drawable attribute value from theme apk using attribute name defined in the main apk
                if ((dVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getDrawable(tVal)) != null) {
                    return dVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled drawable attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getDrawable(tVal);
        }
        return dVal;
    }

    /*
     * @param attrValueResId attribute id defined as R.attr.xxx
     */
    public CharSequence getThemedText(int attrValueResId)
    {
        CharSequence dVal = null;
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                // retrieve styled text attribute value from theme apk using attribute name defined in the main apk
                if ((dVal = ThemeApkResourceBroker.getInstance().mResourceAccessor.getText(attrName)) != null) {
                    return dVal;
                }
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled text attribute value from main apk using attribute id defined in main apk R.java
            return ThemeResourceAccessor.getText(accessor.getAttrTypedValue(attrValueResId, false));
        }
        return null;
    }

    public CharSequence getThemedText(TypedValue tVal) {
        CharSequence dVal = null; 
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            // retrieve text attribute value from theme apk using attribute name defined in the main apk
            if ((dVal = ThemeResourceAccessor.getText(tVal)) != null) {
                return dVal;
            }
        }
        return dVal;
    }

    public float getThemedDimension(int attrValueResId)
    {
        float fVal = 0.0f; 
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                String attrName = MainApkResourceBroker.getInstance().mContext.getResources().getResourceName(attrValueResId);
                return ThemeApkResourceBroker.getInstance().mResourceAccessor.getDimension(attrName);
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled drawable attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getDimension(accessor.getAttrTypedValue(attrValueResId, false));
        }
        return fVal;
    }

    public float getThemedDimension(TypedValue tVal) {
        float fVal = 0.0f; 
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                // retrieve styled drawable attribute value from theme apk using attribute name defined in the main apk
                return ThemeApkResourceBroker.getInstance().mResourceAccessor.getDimension(tVal);
            }
            ThemeResourceAccessor accessor = MainApkResourceBroker.getInstance().mResourceAccessor;
            // retrieve styled drawable attribute value from main apk using attribute id defined in main apk R.java
            return accessor.getDimension(tVal);
        }
        return fVal;
    }

    public TypedArrayWrapper obtainStyledAttributes(Context context, AttributeSet mainAttrsSet, int[] attrs, int defStyleAttr, int defStyleRes,
            int defXmlAttr, String defStyleAttrTag, String defStyleResTag) {
        SparseArray<TypedValue> typedValsInApk = null;
        TypedArray taMain = context.obtainStyledAttributes(mainAttrsSet,  
                attrs, defStyleAttr, defStyleRes); 
        if (bThemeApkLoaded) {
            if (MainApkResourceBroker.getInstance().mHasInited && ThemeApkResourceBroker.getInstance().mHasInited) {
                SparseIntArray attrsMap = new SparseIntArray();
                // build a map from attribute rId to index in attribute stylable 
                for (int i = 0; i < attrs.length; i++) {
                    attrsMap.put(attrs[i], i);
                }
                
                typedValsInApk = new SparseArray<TypedValue>();
                
                /* Context::obtainStyledAttributes retrieves the styled attribute values the main apk.
                 * Need to check loaded theme apk for all attributes and overload the TypedValue 
                 */
                
                // 1. Check any attribute values in the given attributeSet
                if (mainAttrsSet != null) {
                    obtainTypedValuesFromThemeApk(attrsMap, mainAttrsSet, typedValsInApk);     
                }
                // 2. the default style specified by defStyleAttr 
                if (defStyleAttr != 0){
                    AttributeSet attrSetDefAttr = loadDefValXMLAsAttributeSet(context, defXmlAttr, defStyleAttrTag);
                    obtainTypedValuesFromThemeApk(attrsMap, attrSetDefAttr, typedValsInApk);
                } 
                // 3. The default style specified by defStyleRes, such as SwypeReference
                if (defStyleRes != 0){
                    AttributeSet attrSetDefRes = loadDefValXMLAsAttributeSet(context, defXmlAttr, defStyleResTag);
                    obtainTypedValuesFromThemeApk(attrsMap, attrSetDefRes, typedValsInApk);
                } 
                
                MainApkResourceBroker localResMgr = MainApkResourceBroker.getInstance();
                ThemeResourceAccessor accessor = ThemeApkResourceBroker.getInstance().mResourceAccessor;
                // 4. check the base value in the theme
                for(int i = 0; i < attrsMap.size(); i++) {
                    int attrResId = attrsMap.keyAt(i);
                    if (localResMgr.mThemeStyleableAttrResIds.contains(Integer.valueOf(attrResId))) {  
                        //                        String partName = attrFullName.substring(attrFullName.indexOf("/") + 1);
                        //                        if (partName.equalsIgnoreCase("btnKeyboardPopupKeyNormal")) {
                        //                            Log.d("Barry", "here");
                        //                        }
                        int attr_index = attrsMap.valueAt(i);
                        if( attr_index >= 0 ) {
                            String attrFullName = localResMgr.mContext.getResources().getResourceName(attrResId);
                            TypedValue tValFoundInApk = accessor.obtainAttrTypedValue(attrFullName);
                            if (tValFoundInApk != null) {
                                typedValsInApk.put(attr_index, tValFoundInApk);
                            }
                        }      
                    }
                }
            }
        }
        return new TypedArrayWrapper(taMain, typedValsInApk);
    }

    private AttributeSet loadDefValXMLAsAttributeSet(Context context, int xmlRId, String Deftag) {
        XmlResourceParser parser = context.getResources().getXml(xmlRId);
        int state = 0;
        do {
            try {
                state = parser.next();
            } catch (XmlPullParserException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }       
            if (state == XmlPullParser.START_TAG) {
                String str = parser.getName();
                if (str.equals(Deftag)) {
                    return Xml.asAttributeSet(parser);
                }
            }
        } while(state != XmlPullParser.END_DOCUMENT);

        return null;
    }

    /*
     * @param attrsMap: maps attribute rid to index in attribute styable
     * @param attrSet: AttributeSet defined in xml, either layout or style
     * @param typedValsInApk: output param. Holds TypedValue defined in theme apk 
     */
    private void obtainTypedValuesFromThemeApk(SparseIntArray attrsMap, AttributeSet attrSet, SparseArray<TypedValue> typedValsInApk) {
        int count = attrSet.getAttributeCount();
        MainApkResourceBroker localResMgr = MainApkResourceBroker.getInstance();
        ThemeResourceAccessor accessor = ThemeApkResourceBroker.getInstance().mResourceAccessor;
        for (int j = 0; j < count; j++)
        {
            if (attrsMap.size() == 0) {
                break;
            }
            /* Example: <com.nuance.swype.input.KeyboardViewEx
             *             ns:keyBackground="?btnKeyboardPopupKeyNormal"
             *             .../>
             *   attrRIdInSet: R.attr.keyBackground 
             *   attrValueIdStr: ?R.attr.btnKeyboardPopupKeyNormal
             *   attrResId: R.attr.btnKeyboardPopupKeyNormal
             *   attrFullName: "res/attr/btnKeyboardPopupKeyNormal"
             *   attr_index: R.attr.KeyboardViewEx_keyBackground     
             */             
            int attrRIdInSet = attrSet.getAttributeNameResource(j);
            Log.d(LOG_TAG, "checking:" + attrSet.getAttributeName(j));
            int attr_index = attrsMap.get(attrRIdInSet, -1);
            if( attr_index >= 0 ) {
                attrsMap.delete(attrRIdInSet);
            }
            String attrValueIdStr = attrSet.getAttributeValue(j);
            if (attrValueIdStr.startsWith("?")) {
                try
                {
                    // skip "?"	
                    int attrResId = Integer.parseInt(attrValueIdStr.substring(1));
                    if (localResMgr.mThemeStyleableAttrResIds.contains(Integer.valueOf(attrResId))) {                       
                        //                        String partName = attrFullName.substring(attrFullName.indexOf("/") + 1);
                        //                        if (partName.equalsIgnoreCase("btnKeyboardPopupKeyNormal")) {
                        //                            Log.d("Barry", "here");
                        //                        }
                        if( attr_index >= 0 ) {
                            String attrFullName = localResMgr.mContext.getResources().getResourceName(attrResId);
                            TypedValue tValFoundInApk = accessor.obtainAttrTypedValue(attrFullName);
                            if (tValFoundInApk != null) {
                                typedValsInApk.put(attr_index, tValFoundInApk);
                            }
                        }      
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
    
    private boolean initializeMainResourceBorker(Context themedContext) {
        MainApkResourceBroker localMgr = MainApkResourceBroker.getInstance();
        localMgr.mContext = themedContext;
        
        if (!localMgr.mHasInited) {
            for (int attr : themeTemplateAttrs) {
                localMgr.mThemeStyleableAttrResIds.add(Integer.valueOf(attr));
            }
        } else if (localMgr.mResourceAccessor != null) {
            localMgr.mResourceAccessor.release();
        }
        
        localMgr.mResourceAccessor = new ThemeResourceAccessor(themedContext.getTheme(), themedContext.getResources(), 
                themeTemplateAttrs, localMgr.mContext.getApplicationInfo().packageName, defStyle);
        localMgr.mHasInited = true;
        
        return true;
    }

    public boolean buildResourceIdMap(AttributeSet paramAttributeSet, SparseIntArray paramArrayList)
    {
        if (paramArrayList == null || !bThemeApkLoaded || !ThemeApkResourceBroker.getInstance().mHasInited) {
            paramArrayList.clear();
            return false;
        }
        MainApkResourceBroker localResMgr = MainApkResourceBroker.getInstance();
        for (int j = 0; j < paramAttributeSet.getAttributeCount(); j++)
        {
            String attrResIdStr = paramAttributeSet.getAttributeValue(j);
            if (attrResIdStr.startsWith("?")) {
                try
                {
                    // skip "?" 
                    int attrResId = Integer.parseInt(attrResIdStr.substring(1));
                    if (localResMgr.mThemeStyleableAttrResIds.contains(Integer.valueOf(attrResId))) {
                            TypedValue t = getThemeTypedValueFromMain(attrResId);
                            String attrFullName = localResMgr.mContext.getResources().getResourceName(attrResId);
                            String partName = attrFullName.substring(attrFullName.indexOf("/") + 1);
                            if (partName.equals("btnKeyboardPopupKeyNormal")) {
                                Log.d("Barry", partName);
                            }
                            int attrThemedValueRId = t.resourceId;
                            TypedValue tVal = getThemeTypedValueFromApk(attrResId);
                            if (tVal != null && tVal.resourceId != 0) {
                               paramArrayList.put(attrThemedValueRId, tVal.resourceId);
                            }
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
        return true;
    }
    
    public InputStream openRawResourceInThemeApk(int id) throws NotFoundException {
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                return ThemeApkResourceBroker.getInstance().mResourceAccessor.openRawResource(id);
            }
        }
        return null;
    }
    
    public Drawable getDrawableInThemeApk(int id) throws NotFoundException {
        if (MainApkResourceBroker.getInstance().mHasInited)
        {
            if (ThemeApkResourceBroker.getInstance().mHasInited)
            {
                return ThemeApkResourceBroker.getInstance().mResourceAccessor.getDrawable(id);
            }
        }
        return null;
    }


    /*
     * @param themeStyleableAttrs: theme template defined as styleable in attrs.xml
     * @param defStyle: default style 
     */
    public boolean init(int[] themeStyleableAttrs, int defStyle)
    {
        if ((themeStyleableAttrs == null)) {
            return false;
        }
        this.defStyle = defStyle;
        themeTemplateAttrs = themeStyleableAttrs;
        
        return true;
    }
    
//    public boolean loadBuiltinTheme(int residTheme)
//    {
//        MainApkResourceBroker localMgr;
//        localMgr = MainApkResourceBroker.getInstance();
//        Context localContext = localMgr.mContext;
//        if (localContext == null) {
//            return false;
//        }
//        if (ThemeApkResourceBroker.getInstance().mHasInited) {
//            ThemeApkResourceBroker.getInstance().mHasInited = false;
//            ThemeApkResourceBroker.getInstance().mResourceAccessor.release();
//            ThemeApkResourceBroker.releaseInstance();
//        }
//
//        if (localMgr.mResourceAccessor != null) {
//            localMgr.mResourceAccessor.release();
//            localMgr.mHasInited = false;
//        }
//        Resources.Theme newTheme = localMgr.mContext.getResources().newTheme();
//        newTheme.applyStyle(residTheme, true);
//        localMgr.mResourceAccessor = new ThemeResourceAccessor(newTheme, localMgr.mContext.getResources(), 
//                themeTemplateAttrs, localMgr.mContext.getApplicationInfo().packageName, defStyle);
//        localMgr.mHasInited = true;
//        notifyThemeChange();
//
//        return true;
//    }

    public boolean loadThemeApkFile(Context themedMainContext, String themeApkPath)
    {
        boolean bVal = false;
       
        if (themedMainContext == null) {
            return bVal;
        }
        
        if (!bThemeApkLoaded) {
            initializeMainResourceBorker(themedMainContext);
        }
        
        if (ThemeApkResourceBroker.getInstance().mHasInited) {
            ThemeApkResourceBroker.getInstance().release();
        }
        

        ThemeApkInfo currentThemeApkInfo = ThemeApkInfo.fromStaticApkFile(themedMainContext, themeApkPath);
        bVal = ThemeApkResourceBroker.getInstance().setThemeFromApk(currentThemeApkInfo);
        bThemeApkLoaded = true;
        if (bVal) {
            notifyThemeChange();
        } else {
            clear();
        }
        return bVal;
    }
    
    public void clear() {
        if (!bThemeApkLoaded) {
            return;
        }
        
        if (ThemeApkResourceBroker.getInstance().mHasInited) {
            ThemeApkResourceBroker.getInstance().release();
            ThemeApkResourceBroker.releaseInstance();
        }
        
        if (MainApkResourceBroker.getInstance().mHasInited) {
            MainApkResourceBroker.getInstance().release();
            MainApkResourceBroker.releaseInstance();
        }
        
        mParsers.clear();
        
        bThemeApkLoaded = false;
    }
    
    public boolean isUsingThemeApk() {
        return bThemeApkLoaded;
    }
}

