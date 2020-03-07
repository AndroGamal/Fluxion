package com.example.andro.fluxion;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ListActivity {
    ArrayList n;
    List<ScanResult> a;
    WifiManager c;
    public static String h;
    static String key;
    Process process;
    NotificationManager k;
    Intent name;
    static int i = 0;
    SharedPreferences.Editor wifi;
    SharedPreferences wifi_read;
    Timer t;
    TimerTask tt;
    Handler hender;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        h = a.get(position).BSSID;
        key = a.get(position).SSID;
        c.setWifiEnabled(true);
        k.notify(i, new NotificationCompat.Builder(this).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(key).setContentText(h).build());
        i++;
        d.setEnabled(true);
        Toast.makeText(this, h, Toast.LENGTH_LONG).show();
        super.onListItemClick(l, v, position, id);
    }

    Button s, d;
    ListView q;
    Switch swi;

    void scan() {
        try {
            n.clear();
            c.startScan();
            a = c.getScanResults();
            for (ScanResult scanResult : a) {
                n.add(scanResult.SSID);
            }
            setListAdapter(new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, n));
        } catch (Exception e) {
            Toast.makeText(this, "Please wait ...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        t = new Timer();
        hender = new Handler();
        n = new ArrayList();
        c = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        q = findViewById(android.R.id.list);
        k = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        s = findViewById(R.id.s);
        d = findViewById(R.id.d);
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            e.printStackTrace();
        }
        swi = findViewById(R.id.switch1);
        wifi_read = getSharedPreferences("wifiname", MODE_MULTI_PROCESS);
        name = new Intent(MainActivity.this, wifiService.class);
        wifi = getSharedPreferences("wifiname", MODE_MULTI_PROCESS).edit();
        d.setEnabled(false);
        swi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swi.setChecked(c.isWifiEnabled());
                try {
                    while ((boolean) wifiService.isenable.invoke(c)) {
                        wifiService.setmethod.invoke(c, wifiService.myConfig, false);
                    }
                } catch (Exception e) {
                }
                c.setWifiEnabled(!c.isWifiEnabled());
                n.clear();
                setListAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_expandable_list_item_1, n));
            }
        });
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifi.putString("name", key);
                wifi.putBoolean("run", true);
                wifi.apply();
                stopService(name);
                try {
                    process.getOutputStream().write(("echo " + h.toUpperCase() + " > efs/wifi/.mac.info\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new AlertDialog.Builder(MainActivity.this).setMessage("start attack wifi").setTitle("Attack")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startService(name);
                            }
                        }).setNegativeButton("No", null).create().show();

            }
        });
        tt = new TimerTask() {
            @Override
            public void run() {
                if (swi.isChecked() != c.isWifiEnabled())
                    hender.post(new Runnable() {
                        @Override
                        public void run() {
                            swi.setChecked(c.isWifiEnabled());
                            swi.setEnabled(true);
                        }
                    });
            }
        };

        t.schedule(tt, 0, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        t.cancel();
        try {
            process.getOutputStream().write("iptables -t nat -F\n".getBytes());
            process.getOutputStream().write("iptables -t mangle -F\n".getBytes());
            process.getOutputStream().write("iptables -F\n".getBytes());
            process.getOutputStream().write("ip link set dev br0 down\n".getBytes());
            process.getOutputStream().write("brctl delif br0 wlan0\n".getBytes());
            process.getOutputStream().write("brctl delbr br0\n".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopService(name);
        super.onDestroy();

    }
}
