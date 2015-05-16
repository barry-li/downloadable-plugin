package com.swype.plugin;

public final class ThemeAttrAssociation
{
  // widget attribute that will be themed by styleable attribute, such as "background"
  String attrName; 
  // styleable attribute full name, such as "packagename:attr/textColorPrimary"
  String attrStyleableFullName;
  
  public ThemeAttrAssociation(String attrName, String attrStyleableFullName)
  {
    this.attrName = attrName;
    this.attrStyleableFullName = attrStyleableFullName;
  }
}

