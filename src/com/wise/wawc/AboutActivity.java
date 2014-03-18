package com.wise.wawc;

import org.json.JSONArray;
import org.json.JSONObject;
import com.wise.pubclas.Constant;
import com.wise.pubclas.GetSystem;
import com.wise.pubclas.NetThread;
import com.wise.pubclas.UpdateManager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class AboutActivity extends Activity{
    private static final int get_version = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Button bt_new_version = (Button)findViewById(R.id.bt_new_version);
        bt_new_version.setOnClickListener(onClickListener);
        ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
        iv_back.setOnClickListener(onClickListener);
    }
    OnClickListener onClickListener = new OnClickListener() {        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_new_version:
                String url = Constant.BaseUrl + "upgrade/android/wiwc";
                new Thread(new NetThread.GetDataThread(handler, url, get_version)).start();
                break;
            }
        }
    };
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case get_version:
                jsonVersion(msg.obj.toString());
                break;
            }
        }        
    };
    private void jsonVersion(String result){
        try {
            double Version = Double.valueOf(GetSystem.GetVersion(AboutActivity.this, "com.wise.wawc"));
            String VersonUrl = new JSONObject(result).getString("app_path");
            String logs = new JSONObject(result).getString("logs");
            JSONArray jsonArray = new JSONObject(result).getJSONArray("logs");
            for (int i = 0; i < jsonArray.length(); i++) {
                 double logVersion = Double.valueOf(jsonArray.getJSONObject(i).getString("version"));
                 if(logVersion > Version){
                     UpdateManager mUpdateManager = new UpdateManager(AboutActivity.this,VersonUrl,logs,Version);
                     mUpdateManager.checkUpdateInfo();
                     break;
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
}
