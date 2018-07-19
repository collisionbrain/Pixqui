package com.libre.mixtli.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.libre.mixtli.Manifest;
import com.libre.mixtli.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapMaskFragment extends Fragment implements LocationListener{
    private MapView mMapView;
    private double longitude;
    private double latitude;
    private Context contexto;
    private  IMapController mapController;
    private LocationManager locationManager;
    private Location location;
    //private MyItemizedOverlay myItemizedOverlay = null;

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.map, null);
        contexto=this.getActivity().getBaseContext();
        mMapView = new MapView(inflater.getContext());
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(19.3804234,-99.0167972);
        mapController = mMapView.getController();
        mapController.setZoom(16);
        mapController.setCenter(startPoint);
        mapController.animateTo(startPoint);
        return mMapView;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    public void onPause(){
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setLocationInMap(location.getLatitude(),location.getLongitude());
        if (mMapView != null) {
            mMapView.onResume();
        }
       getView().setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    return true;

                }
                return false;
            }
        } );
    }
    @Override
    public void onLocationChanged(Location location) {
        if( !this.location.equals(location)){
            this.location=location;
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void setLocationInMap(double latitude,double longitude){
        GeoPoint point = new GeoPoint(latitude, longitude);
        mapController = mMapView.getController();
        mapController.setZoom(16);
        mapController.setCenter(point);
        mapController.animateTo(point);
    }
}
