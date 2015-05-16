package com.swype.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;

import java.io.File;

public class ProxyPluginActivity extends Activity{
	IPluginActivity mPluginActivity;
    String mPluginApkFilePath;
    String mLaunchActivity;
    private String mPluginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            return;
        }
        mPluginName = bundle.getString("plugin_name");
        mLaunchActivity = bundle.getString("launch_activity");
        File pluginFile = PluginUtils.getInstallPath(ProxyPluginActivity.this, mPluginName);
        if(!pluginFile.exists()){
            return;
        }
        mPluginApkFilePath = pluginFile.getAbsolutePath();
        try {
            initPlugin();
            super.onCreate(savedInstanceState);
            mPluginActivity.IOnCreate(savedInstanceState);
        } catch (Exception e) {
            mPluginActivity = null;
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPluginActivity != null){
            mPluginActivity.IOnResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mPluginActivity != null) {
            mPluginActivity.IOnStart();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(mPluginActivity != null) {
            mPluginActivity.IOnRestart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mPluginActivity != null) {
            mPluginActivity.IOnStop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPluginActivity != null) {
            mPluginActivity.IOnPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPluginActivity != null) {
            mPluginActivity.IOnDestroy();
        }
    }

    private void initPlugin() throws Exception {
        PackageInfo packageInfo = PluginUtils.getPackgeInfo(this, mPluginApkFilePath);

        if (mLaunchActivity == null || mLaunchActivity.length() == 0) {
            mLaunchActivity = packageInfo.activities[0].name;
        }

        ClassLoader classLoader = PluginUtils.getClassLoader(this, mPluginName, mPluginApkFilePath);

        if (mLaunchActivity == null || mLaunchActivity.length() == 0) {
            if (packageInfo == null || (packageInfo.activities == null) || (packageInfo.activities.length == 0)) {
                throw new ClassNotFoundException("Launch Activity not found");
            }
            mLaunchActivity = packageInfo.activities[0].name;
        }
        Class<?> mClassLaunchActivity = classLoader.loadClass(mLaunchActivity);

        getIntent().setExtrasClassLoader(classLoader);
        mPluginActivity = (IPluginActivity) mClassLaunchActivity.newInstance();
        mPluginActivity.IInit(mPluginApkFilePath, this, classLoader);
    }


    protected Class<? extends ProxyPluginActivity> getProxyActivity(String pluginActivityName) {
        return getClass();
    }

    protected  Class<? extends ProxyPluginService> getProxyService(String pluginServiceName){
        return ProxyPluginService.class;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        boolean pluginActivity = intent.getBooleanExtra("is_in_plugin", false);
        if (pluginActivity) {
            String launchActivity = null;
            ComponentName componentName = intent.getComponent();
            if(null != componentName) {
                launchActivity = componentName.getClassName();
            }
            intent.putExtra("is_in_plugin", false);
            if (launchActivity != null && launchActivity.length() > 0) {
                Intent pluginIntent = new Intent(this, getProxyActivity(launchActivity));

                pluginIntent.putExtra("plugin_name", mPluginName);
                pluginIntent.putExtra("plugin_path", mPluginApkFilePath);
                pluginIntent.putExtra("launch_activity", launchActivity);
                startActivityForResult(pluginIntent, requestCode);
            }
        } else {
			super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        boolean pluginService = service.getBooleanExtra("is_in_plugin", false);
        if (pluginService) {
            String serviceName = null;
            ComponentName componentName = service.getComponent();
            if (null != componentName) {
                serviceName = componentName.getClassName();
            }
            Intent intent = new Intent(this, getProxyService(serviceName));
            intent.putExtra("is_in_plugin", false);
            intent.putExtra("plugin_name", mPluginName);
            intent.putExtra("launch_service", serviceName);
            return super.bindService(intent, conn, flags);
        }else{
            return super.bindService(service, conn, flags);
        }
    }
}
