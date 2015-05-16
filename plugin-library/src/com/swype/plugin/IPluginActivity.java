package com.swype.plugin;

import android.app.Activity;
import android.os.Bundle;

public interface IPluginActivity {
	public void IOnCreate(Bundle savedInstanceState);

    public void IOnResume();

    public void IOnStart();

    public void IOnPause();

    public void IOnStop();

    public void IOnDestroy();

    public void IOnRestart();

    public void IInit(String path, Activity context, ClassLoader classLoader);
}
