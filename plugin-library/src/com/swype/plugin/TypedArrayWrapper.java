package com.swype.plugin;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.util.TypedValue;

public final class TypedArrayWrapper {

    TypedArray delegateTypedArray;
    SparseArray<TypedValue> tValsStyledByApk;

    public TypedArrayWrapper(TypedArray innerTypedArray, SparseArray<TypedValue> innerArray) {
        delegateTypedArray = innerTypedArray;
        tValsStyledByApk = innerArray;
    }


    public TypedArray getRawTypedArray() {
        return delegateTypedArray;
    }
    
    public int length() {
        return delegateTypedArray.length();
    }
    
    public int getIndexCount () {
        return delegateTypedArray.getIndexCount();
    }
    
    public int getIndex (int at) {
        return delegateTypedArray.getIndex(at);
    }
    
    public int getResourceId (int index, int defValue) {
        return delegateTypedArray.getResourceId(index, defValue);
    }

    /**
     * Return the Resources object this array was loaded from.
     */
    public Resources getResources() {
        return delegateTypedArray.getResources();
    }

    /**
     * Retrieve the styled string value for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return CharSequence holding string data.  May be styled.  Returns
     *         null if the attribute is not defined.
     */
    public CharSequence getText(int index) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return ThemeLoader.getInstance().getThemedText(tVal);
            }
        }

        return delegateTypedArray.getText(index);
    }

    /**
     * Retrieve the string value for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return String holding string data.  Any styling information is
     * removed.  Returns null if the attribute is not defined.
     */

    public String getString(int index) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                CharSequence txt = ThemeLoader.getInstance().getThemedText(tVal);
                return (txt != null)?txt.toString():null;
            }
        }
        return delegateTypedArray.getString(index);
    }

    /**
     * Retrieve the boolean value for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined.
     *
     * @return Attribute boolean value, or defValue if not defined.
     */
    public boolean getBoolean(int index, boolean defValue) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return ThemeLoader.getInstance().getThemedBoolean(tVal);
            }
        }

        return delegateTypedArray.getBoolean(index, defValue);
    }

    /**
     * Retrieve the integer value for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined.
     *
     * @return Attribute int value, or defValue if not defined.
     */
    public int getInt(int index, int defValue) {
        return delegateTypedArray.getInt(index, defValue);
    }

    /**
     * Retrieve the float value for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Attribute float value, or defValue if not defined..
     */
    public float getFloat(int index, float defValue) {
        return delegateTypedArray.getFloat(index, defValue);
    }

    /**
     * Retrieve the color value for the attribute at <var>index</var>.  If
     * the attribute references a color resource holding a complex
     * {@link android.content.res.ColorStateList}, then the default color from
     * the set is returned.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute color value, or defValue if not defined.
     */
    public int getColor(int index, int defValue) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return ThemeLoader.getInstance().getThemedColor(tVal);
            }
        }
        return delegateTypedArray.getColor(index, defValue);
    }

    /**
     * Retrieve the ColorStateList for the attribute at <var>index</var>.
     * The value may be either a single solid color or a reference to
     * a color or complex {@link android.content.res.ColorStateList} description.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return ColorStateList for the attribute, or null if not defined.
     */
    public ColorStateList getColorStateList(int index) {
        return delegateTypedArray.getColorStateList(index);
    }

    /**
     * Retrieve the integer value for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute integer value, or defValue if not defined.
     */
    public int getInteger(int index, int defValue) {
        return delegateTypedArray.getInteger(index, defValue);
    }

    /**
     * Retrieve a dimensional unit attribute at <var>index</var>.  Unit
     * conversions are based on the current {@link DisplayMetrics}
     * associated with the resources this {@link TypedArray} object
     * came from.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute dimension value multiplied by the appropriate
     * metric, or defValue if not defined.
     *
     * @see #getDimensionPixelOffset
     * @see #getDimensionPixelSize
     */
    public float getDimension(int index, float defValue) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return ThemeLoader.getInstance().getThemedDimension(tVal);
            }
        }
        return delegateTypedArray.getDimension(index, defValue);
    }

    /**
     * Retrieve a dimensional unit attribute at <var>index</var> for use
     * as an offset in raw pixels.  This is the same as
     * {@link #getDimension}, except the returned value is converted to
     * integer pixels for you.  An offset conversion involves simply
     * truncating the base value to an integer.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute dimension value multiplied by the appropriate
     * metric and truncated to integer pixels, or defValue if not defined.
     *
     * @see #getDimension
     * @see #getDimensionPixelSize
     */
    public int getDimensionPixelOffset(int index, int defValue) {
        return delegateTypedArray.getDimensionPixelOffset(index, defValue);
    }

    /**
     * Retrieve a dimensional unit attribute at <var>index</var> for use
     * as a size in raw pixels.  This is the same as
     * {@link #getDimension}, except the returned value is converted to
     * integer pixels for use as a size.  A size conversion involves
     * rounding the base value, and ensuring that a non-zero base value
     * is at least one pixel in size.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute dimension value multiplied by the appropriate
     * metric and truncated to integer pixels, or defValue if not defined.
     *
     * @see #getDimension
     * @see #getDimensionPixelOffset
     */
    public int getDimensionPixelSize(int index, int defValue) {
        return delegateTypedArray.getDimensionPixelSize(index, defValue);
    }

    /**
     * Special version of {@link #getDimensionPixelSize} for retrieving
     * {@link android.view.ViewGroup}'s layout_width and layout_height
     * attributes.  This is only here for performance reasons; applications
     * should use {@link #getDimensionPixelSize}.
     *
     * @param index Index of the attribute to retrieve.
     * @param name Textual name of attribute for error reporting.
     *
     * @return Attribute dimension value multiplied by the appropriate
     * metric and truncated to integer pixels.
     */
    public int getLayoutDimension(int index, String name) {
        return delegateTypedArray.getLayoutDimension(index, name);
    }

    public int getLayoutDimension(int index, int defValue) {
        return delegateTypedArray.getLayoutDimension(index, defValue);
    }

    /**
     * Retrieve a fractional unit attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param base The base value of this fraction.  In other words, a
     *             standard fraction is multiplied by this value.
     * @param pbase The parent base value of this fraction.  In other
     *             words, a parent fraction (nn%p) is multiplied by this
     *             value.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute fractional value multiplied by the appropriate
     * base value, or defValue if not defined.
     */
    public float getFraction(int index, int base, int pbase, float defValue) {
        return delegateTypedArray.getFraction(index, base, pbase, defValue);
    }


    public int getResourceIdDefinedInApk(int index, int defValue) {
        TypedValue tVal = tValsStyledByApk.get(index);
        if (tVal != null) {
            return tVal.resourceId;
        }

        return defValue;
    }

    /**
     * Retrieve the resource identifier defined in main apk for the attribute at
     * <var>index</var>.  Note that attribute resource as resolved when
     * the overall {@link TypedArray} object is retrieved.  As a
     * result, this function will return the resource identifier of the
     * final resource value that was found, <em>not</em> necessarily the
     * original resource that was specified by the attribute.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute resource identifier, or defValue if not defined.
     */
    public int getResourceIdDefinedInMain(int index, int defValue) {
        return delegateTypedArray.getResourceId(index, defValue);
    }

    /**
     * Retrieve the Drawable for the attribute at <var>index</var>.  This
     * gets the resource ID of the selected attribute, and uses
     * {@link Resources#getDrawable Resources.getDrawable} of the owning
     * Resources object to retrieve its Drawable.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Drawable for the attribute, or null if not defined.
     */
    public Drawable getDrawable(int index) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return ThemeLoader.getInstance().getThemedDrawable(tVal);
            }
        }
        return delegateTypedArray.getDrawable(index);
    }


    /**
     * Retrieve the CharSequence[] for the attribute at <var>index</var>.
     * This gets the resource ID of the selected attribute, and uses
     * {@link Resources#getTextArray Resources.getTextArray} of the owning
     * Resources object to retrieve its String[].
     *
     * @param index Index of attribute to retrieve.
     *
     * @return CharSequence[] for the attribute, or null if not defined.
     */
    public CharSequence[] getTextArray(int index) {

        return delegateTypedArray.getTextArray(index);
    }

    /**
     * Retrieve the raw TypedValue for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param outValue TypedValue object in which to place the attribute's
     *                 data.
     *
     * @return Returns true if the value was retrieved, else false.
     */
    public boolean getValue(int index, TypedValue outValue) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                outValue.setTo(tVal);
                return true;
            }
        }
        return delegateTypedArray.getValue(index, outValue);
    }

    /**
     * Determines whether the styable attribute at <var>index</var> will be
     * styled by theme apk.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return True if the typed value is from theme apk, false otherwise.
     */
    public boolean IsValueFromApk(int index) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether there is an attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return True if the attribute has a value, false otherwise.
     */
    public boolean hasValue(int index) {
        if (tValsStyledByApk != null) {
            TypedValue tVal = tValsStyledByApk.get(index);
            if (tVal != null) {
                return true;
            }
        }
        return delegateTypedArray.hasValue(index);
    }

    /**
     * Retrieve the raw TypedValue for the attribute at <var>index</var>
     * and return a temporary object holding its data.  This object is only
     * valid until the next call on to {@link TypedArray}.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Returns a TypedValue object if the attribute is defined,
     *         containing its data; otherwise returns null.  (You will not
     *         receive a TypedValue whose type is TYPE_NULL.)
     */
    public TypedValue peekValue(int index) {
        return delegateTypedArray.peekValue(index);
    }

    /**
     * Returns a message about the parser state suitable for printing error messages.
     */
    public String getPositionDescription() {
        return delegateTypedArray.getPositionDescription();
    }

    /**
     * Give back a previously retrieved StyledAttributes, for later re-use.
     */
    public void recycle() {
        if (tValsStyledByApk != null) {
            tValsStyledByApk.clear();
            tValsStyledByApk = null;
        }
        delegateTypedArray.recycle();
    }


    public String toString() {
        return delegateTypedArray.toString();
    }

}
