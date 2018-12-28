package com.example.mymap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AddressToLatitudeLongitude {
    private String address;//地址
    private double Latitude;//纬度
    private double Longitude;//经度

    public AddressToLatitudeLongitude(String addr_str) {
        this.address = addr_str;
    }

    /**
     * 根据地址得到地理图标
     */
    public void getLatAndLngByAddress() {
        String addr = "";
        String lat = "";
        String lng = "";
        try{
            addr = java.net.URLEncoder.encode(address,"UTF-8");//设置编码
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String url = String.format("http://api.map.baidu.com/geocoder/v2/?"
                +"&mcode=72:2B:6A:60:DC:44:F0:0F:2A:89:12:2A:53:5B:4E:C5:85:DC:B9:2B;com.example.mymap&"
                +"address=%s&ak=LXlkwMMGlu1fTWqxQRSaaixl6XcIjL3c&output=json",addr);
        URL myURL = null;
        URLConnection httpsConn = null;
        //进行转码
        try{
            myURL = new URL(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            httpsConn = (URLConnection)myURL.openConnection();//建立连接
            if (httpsConn != null){
                InputStreamReader insr = new InputStreamReader(
                        httpsConn.getInputStream(),"UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if((data = br.readLine())!=null){
                    System.out.println(data);
                    lat = data.substring(data.indexOf("\"lat\":")+("\"lat\":").length(), data.indexOf("},\"precise\""));
                    lng = data.substring(data.indexOf("\"lng\":")+("\"lng\":").length(), data.indexOf(",\"lat\""));
                }
                insr.close();
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.Latitude = Double.parseDouble(lat);
        this.Longitude = Double.parseDouble(lng);
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {

        return Longitude;
    }
    /*public static void main(String[] args){
        AddressToLatitudeLongitude at = new AddressToLatitudeLongitude("安徽省亳州市亳州一中");
        at.getLatAndLngByAddress();
        System.out.println(at.getLatitude()+" "+at.getLongitude());
    }*/
}
