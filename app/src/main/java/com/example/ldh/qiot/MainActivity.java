package com.example.ldh.qiot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener,
        MapView.POIItemEventListener, MapView.MapViewEventListener{

    JsonTransfer http;
    String no[];
    String location[];
    int cnt[];
    String name[];
    String location2[];
    String ds[];
    String date[];
    String lat[];
    String lng[];
    int remain[];
    int QTY[];
    int cost[];
    double percentarray[];
    int predictarray[];

    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout rlBottomSheet;
    private MapView mapView;
    private MapPOIItem mCustomBmMarker;
    private MapReverseGeoCoder mReverseGeoCoder = null;

    //private static MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(37.537229, 127.005515);
    //private static MapPoint DEFAULT_MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.4020737, 127.1086766);
    private static MapPoint mapPoint;
    private static MapPoint DEFAULT_MARKER_POINT;

    TextView current;
    TextView locationName;
    TextView costValue;
    TextView timeValue;
    TextView percent;
    TextView pot;
    TextView predict;

    Button call;
    Button nevi;

    double X = 0, Y = 0;

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationName = (TextView) findViewById(R.id.location_value);
        costValue = (TextView) findViewById(R.id.cost_value);
        timeValue = (TextView) findViewById(R.id.time_value);
        percent = (TextView) findViewById(R.id.percent_value);
        pot = (TextView) findViewById(R.id.pot_value);
        predict = (TextView) findViewById(R.id.predict_value);
        call = (Button) findViewById(R.id.call);
        nevi = (Button) findViewById(R.id.nebi);

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tel = "tel:010-6368-5402";
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tel));
                startActivity(myIntent);
            }
        });

        //공공데이터 저장 배열
        no = new String[160];
        location = new String[160];
        cnt = new int[160];
        name = new String[160];
        location2 = new String[160];
        ds = new String[160];
        date = new String[160];
        lat = new String[160];
        lng = new String[160];
        remain = new int[160];
        QTY = new int[160];
        cost = new int[160];
        percentarray = new double[160];
        predictarray = new int[160];

        //가격 임의조정
        cost[0] = 3000;

        current = (TextView)findViewById(R.id.location);
        rlBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(rlBottomSheet);

        http = new JsonTransfer();
        http.execute("http://qiotpjt.azurewebsites.net/getParkingMainInfo");

        mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setCurrentLocationEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setMapViewEventListener(this);

        while(true) {
            if (http.check) {
                doJson(http.getJson());
                //마커 표시하기
                for (int i = 0; i < 160; i++) {
                    createCustomBitmapMarker(mapView, lat[i], lng[i], i);
                    showAll();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(http.check){
            doJson(http.getJson());
            //마커 표시하기
            for(int i = 0; i < 160; i++) {
                createCustomBitmapMarker(mapView, lat[i], lng[i], i);
                showAll();
            }
        }
    }

    void doJson(String getvalue){
        try{
            JSONArray jsonArray = new JSONArray(getvalue);
            Log.d("size", " " + getvalue.length());
            for (int i = 0 ; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                no[i] = jsonObject.getString("PKPL_ADM_NO");
                location[i] = jsonObject.getString("PKPL_NM");
                cnt[i] = jsonObject.getInt("PRK_DVS_CNT");
                name[i] = jsonObject.getString("PKPL_TP");
                location2[i] = jsonObject.getString("LCT_LNO_ADR");
                ds[i] = jsonObject.getString("LLC_DS");
                date[i] = jsonObject.getString("DAT_STND_DD");
                lat[i] = jsonObject.getString("PKPL_LAT");
                lng[i] = jsonObject.getString("PKPL_LNG");
                remain[i] = jsonObject.getInt("REMAIN_QTY");
                QTY[i] = jsonObject.getInt("QTY");
                percentarray[i] = Math.round(((double)QTY[i] / (double)cnt[i]) * 100);
                predictarray[i] = jsonObject.getInt("PRENUM");
                cost[i] = 3000;

                Log.d("test", no[i] + " " + location[i] + " " + cnt[i] + " " + name[i] + " " + location2[i] +
                        ds[i] + " " + date[i] + " " + lat[i] + " " + lng[i]);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void createCustomBitmapMarker(MapView mapView, String latitude, String longitude, int i) {
        mCustomBmMarker = new MapPOIItem();
        //비율표시
        double ratio = Math.round(((double)QTY[i] / (double)cnt[i]) * 100);
        String ratioOutput = Double.toString(ratio);
        //시간 및 이용시간 표시

        mapPoint = MapPoint.mapPointWithGeoCoord(Double.parseDouble(latitude), Double.parseDouble(longitude));
        mCustomBmMarker.setItemName("점유율 " + ratioOutput + "%");
        mCustomBmMarker.setTag(Integer.parseInt(no[i]));
        mCustomBmMarker.setMapPoint(mapPoint);
        mCustomBmMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

        if(ratio >= 90)
            mCustomBmMarker.setCustomImageResourceId(R.drawable.map_pin_red);
        else if(ratio <90 && ratio >= 50)
            mCustomBmMarker.setCustomImageResourceId(R.drawable.map_pin_yellow);
        else if(ratio <50 && ratio >= 10)
            mCustomBmMarker.setCustomImageResourceId(R.drawable.map_pin_blue);
        else
            mCustomBmMarker.setCustomImageResourceId(R.drawable.map_pin_green);
        mCustomBmMarker.setCustomImageAutoscale(false);
        mCustomBmMarker.setCustomImageAnchor(0.5f, 1.0f);

        mapView.addPOIItem(mCustomBmMarker);
        mapView.selectPOIItem(mCustomBmMarker, false);
        //mapView.setMapCenterPoint(mapPoint, false);
    }

    private void showAll() {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        mapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        int padding = 20;
        float minZoomLevel = 3;
        float maxZoomLevel = 5;
        //MapPointBounds bounds = new MapPointBounds(mapPoint, mapPoint);
        //mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(bounds, padding, minZoomLevel, maxZoomLevel));
    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        //현재위치 lat, lgt값 가져오기
        mapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        DEFAULT_MARKER_POINT = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        /*초기세팅
        String lat = "35.907882";
        String longitude = "128.612808"; */
        /*마커 표시하기
        if(!flag) {
            createCustomBitmapMarker(mapView, lat, longitude, 0);
            showAll();
        }
        flag = true; //첫 시작시 한번만 위치를 출력시켜주게 설정*/
        //현재위치 주소로 표시
        mReverseGeoCoder = new MapReverseGeoCoder("58dcd2fe8a6074640ed8c077c7b4dc76", mapView.getMapCenterPoint(), MainActivity.this, MainActivity.this);
        mReverseGeoCoder.startFindingAddress();
        X = mapPointGeo.latitude;
        Y = mapPointGeo.longitude;
        Log.i("userlocation", String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        http = new JsonTransfer();
        http.execute("http://qiotpjt.azurewebsites.net/getParkingMainInfo");
        //DB로부터 가져온 데이터 배열에저장
        if(http.check){
            doJson(http.getJson());
        }
        //마커 표시하기
        for(int i = 0; i < 160; i++) {
            createCustomBitmapMarker(mapView, lat[i], lng[i], i);
            showAll();
        }
        Log.d("item", "" + mapPOIItem.getTag());
        int tag = 0;
        for(int i = 0; i < 160; i++){
            if(no[i].equals(Integer.toString(mapPOIItem.getTag()))){
                locationName.setText(location2[i]);
                costValue.setText("" + cost[i] + "원");
                pot.setText("" + remain[i] + "");
                percent.setText("" + percentarray[i] + "%");
                timeValue.setText("9시 30분 ~ 12시 30분");
                predict.setText("" + predictarray[i] + "%");
                tag = i;
                break;
            }
        }

        //네비게이션 기능
        final int finalTag = tag;
        nevi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuffer result = new StringBuffer("daummaps://route?sp=");
                result.append(X); result.append(","); result.append(Y); /* 시작점 */
                result.append("&ep="); result.append(lat[finalTag]); result.append(","); result.append(lng[finalTag]); /* 도착점 */
                result.append("&by=CAR");
                Uri uri = Uri.parse(result.toString());
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //Toast.makeText(this, "Clicked " + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    //현재 주소값  출력
    private void onFinishReverseGeoCoding(String result) {
        current.setText("현재 위치 : " + result);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    //화면 클릭 이벤트
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        Log.d("tab", "click");
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
}
