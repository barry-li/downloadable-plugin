package com.swype.plugin;

import android.view.View;
import java.util.ArrayList;

final class ThemeStyledViewItem
{
    /* each ThemeItem associates the view object to be theme styled and the set of mThemeAttrs which
     * defines the set of theme styled attributes
     */
    View mView;
    ArrayList<ThemeAttrAssociation> mThemeAttrs = new ArrayList<ThemeAttrAssociation>();
}

