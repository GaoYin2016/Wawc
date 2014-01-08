package com.wise.wawc;

import com.wise.pubclas.Config;
import com.wise.pubclas.GetSystem;
import com.wise.pubclas.NetThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 欢迎界面
 * 
 * @author honesty
 */
public class WelcomeActivity extends Activity {
    private static final String TAG = "WelcomeActivity";
    
    private final static int Wait = 1;
    private final static int Get_city = 2;
    private final static int Get_host_city = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        TextView tv_activity_welcom_version = (TextView) findViewById(R.id.tv_activity_welcom_version);
        tv_activity_welcom_version.setText("V"
                + GetSystem.GetVersion(getApplicationContext(),
                        Config.PackageName));
        if (isOffline()) {
            // 没有网络
            setNetworkMethod();
        } else if (isFristLoad()) {
            // 第一次登录，选着城市
            GetCityList();
        }else{
            //跳转到登录界面
            new Thread(new WaitThread()).start();
        }
    }
    String citys = "";
    String hot_citys = "";
    boolean isCity = false;
    boolean isHotCity = false;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case Wait:
                startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                finish();
                break;
            case Get_city:
                isCity = true;
                citys = msg.obj.toString();
                TurnActivity();
                break;
            case Get_host_city:
                isHotCity = true;
                hot_citys = msg.obj.toString();
                TurnActivity();
                break;
            }
        }
    };

    /**
     * 判断网络连接状况，true,没有网络
     */
    private Boolean isOffline() {
        if (!GetSystem.checkNetWorkStatus(getApplicationContext())) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * 打开设置网络界面
     */
    public void setNetworkMethod() {
        new AlertDialog.Builder(WelcomeActivity.this)
                .setTitle(R.string.system_note)
                .setMessage(R.string.network_error)
                .setPositiveButton(R.string.set_network,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(R.string.cancle,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                finish();
                            }
                        }).show();
    }

    class WaitThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(1000);
                Message message = new Message();
                message.what = Wait;
                handler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否是第一次登录
     * 
     * @return
     */
    private boolean isFristLoad() {
        SharedPreferences preferences = getSharedPreferences(
                Config.sharedPreferencesName, Context.MODE_PRIVATE);
        String LocationCity = preferences.getString(Config.LocationCity, "");
        if (LocationCity.equals("")) {
            return true;
        }
        return false;
    }
    private void GetCityList(){
        String url = Config.BaseUrl + "base/city?is_hot=0";
        new Thread(new NetThread.GetDataThread(handler, url, Get_city)).start();
        String url_hot = Config.BaseUrl + "base/city?is_hot=1";
        new Thread(new NetThread.GetDataThread(handler, url_hot, Get_host_city)).start();
    }
    private void TurnActivity(){
        if(isCity && isHotCity){
            if(citys.equals("")||hot_citys.equals("")){
                Toast.makeText(WelcomeActivity.this, "读取数据失败！", Toast.LENGTH_LONG).show();
                finish();
            }else{
                Intent intent = new Intent(WelcomeActivity.this, SelectCityActivity.class);
                intent.putExtra("Citys", citys);
                intent.putExtra("Hot_Citys", hot_citys);
                startActivity(intent);
                finish();
            }            
        }
    }
}