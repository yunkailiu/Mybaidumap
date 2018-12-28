package com.example.mymap;

import android.content.Context;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;//地图控件
    private BaiduMap mBaiduMap;//百度地图对象
    private Context context;
    //实现定位相关数据类型
    private LocationClient mLocationClient;//定位服务客户对象
    private MyLocationListener myLocationListener;//重写的监听类
    private boolean isFirstIn = true;
    private double mLatitude;//存储自己的纬度
    private double mLongitude;//存储自己的经度
    private float myCurrentX;

    private BitmapDescriptor myIconLocation1;//当前位置的箭头图标
    private MyOrientationListener myOrientationListener;//方向感应器类对象
    private MyLocationConfiguration.LocationMode locationMode;//定位图层显示方式
    private LinearLayout myLinearLayout2; //地址搜索区域

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        setContentView(R.layout.activity_main);
        this.context = this;
        initView();
        //初始化定位
        initLocation();
    }

    private void initLocation() {
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;
        //客户端的定位服务
        mLocationClient = new LocationClient(this);
        myLocationListener = new MyLocationListener();
        //注册监听器
        mLocationClient.registerLocationListener(myLocationListener);
        //设置定位参数
        LocationClientOption option = new LocationClientOption();
        //设置坐标类型
        option.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        option.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        option.setOpenGps(true);
        //设置扫描间隔为1秒
        option.setScanSpan(1000);
        //传入设置好的信息
        mLocationClient.setLocOption(option);

        //初始化图标,BitmapDescriptorFactory是bitmap描述信息工厂类
        myIconLocation1 = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
        //配置定义的图层,使之生效
        MyLocationConfiguration configuration = new MyLocationConfiguration(locationMode,true,myIconLocation1);
        mBaiduMap.setMyLocationConfiguration(configuration);

        myOrientationListener = new MyOrientationListener(context);
        //接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                myCurrentX = x;
            }
        });
    }

    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //设置地图放大比例
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
    }

    /**
     * 创建菜单操作
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            /**
             * 返回自己所在位置
             */
            case R.id.menu_item_mylocation:
                getLocationByLL(mLatitude,mLongitude);
            break;
            /**
             * 根据地址名前往所在的位置
             */
            case R.id.menu_item_sitesearch:
                myLinearLayout2 = (LinearLayout)findViewById(R.id.linearLayout2);
                //显示地址搜索区域
                myLinearLayout2.setVisibility(View.VISIBLE);
                final EditText myEditText_site = (EditText) findViewById(R.id.editText_site);
                Button button_site = (Button) findViewById(R.id.button_sitesearch);

                button_site.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String site_str = myEditText_site.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AddressToLatitudeLongitude at = new AddressToLatitudeLongitude(site_str);
                                at.getLatAndLngByAddress();
                                Looper.prepare();
                                getLocationByLL(at.getLatitude(),at.getLongitude());
                                Looper.loop();
                            }
                        }).start();
                        //隐藏前面地址输入区域
                        myLinearLayout2.setVisibility(View.GONE);
                        //隐藏输入法键盘
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 根据经纬度返回当前位置
     * @param la
     * @param lg
     */
    private void getLocationByLL(double la, double lg) {
        LatLng latLng = new LatLng(la,lg);
        //描述地图状态将要发生的变化,通过当前经纬度来使地图显示到该位置
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);
        getLatAndLng(la,lg);
    }

    /**
     * 返回当前位置的经纬度,并以闪现消息的方式显示
     */
    private void getLatAndLng(double la, double lg){
        String latAndlng = "经度："+ String.valueOf(lg) + "\n" + "纬度：" + String.valueOf(la);
        Toast.makeText(context,latAndlng,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {

        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }
        myOrientationListener.start();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        myOrientationListener.stop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            MyLocationData data = new MyLocationData.Builder()//
                    .direction(myCurrentX)//
                    .accuracy(location.getRadius())//
                    .latitude(mLatitude)//
                    .longitude(mLongitude).build();
            mBaiduMap.setMyLocationData(data);
            if (isFirstIn){
                /*LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);*/
                getLocationByLL(mLatitude,mLongitude);
                isFirstIn = false;
                //Toast.makeText(context,location.getAddrStr(),Toast.LENGTH_LONG).show();
            }
        }
    }
}
