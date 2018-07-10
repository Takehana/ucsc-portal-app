package com.example.avision.ucsc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class campus extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText mSearchText;

    private String[][] loco_list;
    private ListView mSearchList;
    private ArrayAdapter<String> adapter;

    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //bar
        mSearchText = (EditText) findViewById(R.id.input_search);
        mSearchList = (ListView) findViewById(R.id.search_content);

        init();

        search();
        selectItem();

    }

    @Override
    public void onBackPressed() {
        if (!adapter.isEmpty()) {
            adapter.clear();
            //mSearchList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    private void init() {
        //set up data base
        ArrayList<String> location_list = new ArrayList<>();
        location_list.addAll(Arrays.asList(getResources().getStringArray((R.array.location_list))));

        loco_list = new String[location_list.size()][3];

        int index = 0;
        for (String entry : location_list) {
            String[] parts = entry.split("%");
            for(int i = 0; i < 3; i++){
                loco_list[index][i] = parts[i];
            }
            index++;
        }

        //set up adapter
        ArrayList<String> temp = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                campus.this, android.R.layout.simple_list_item_1, temp);
        mSearchList.setAdapter(adapter);

        //set up key config
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        configure_keyboard();
    }

    private void configure_keyboard() {
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    imm.hideSoftInputFromInputMethod(campus.this.getCurrentFocus().getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    private void search() {
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String input) {
        ArrayList<String> result = new ArrayList<>();
        if (input.length() >= 2) {
            for (String[] entry : loco_list) {
                if (entry[0].toLowerCase().contains(input.toLowerCase())) {
                    result.add(entry[0]);
                }
            }
        }
        adapter.clear();
        adapter.addAll(result);
        adapter.notifyDataSetChanged();
        //mSearchList.setAdapter(adapter);
    }

    private void selectItem() {
        mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String[] str = new String[3];
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < loco_list.length; i++) {
                    if (loco_list[i][0] == adapter.getItem(position)) {
                        str[0] = loco_list[i][0];
                        str[1] = loco_list[i][1];
                        str[2] = loco_list[i][2];
                        markMap(str[0], Double.parseDouble(str[1]), Double.parseDouble(str[2]));
                    }
                }
                Toast.makeText(campus.this, str[0] + ": " + str[1] + ", " + str[2], Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markMap(String name, double lat, double lng) {
        mMap.clear();
        LatLng marker = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(marker).title(name));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 18.2f));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng ucsc = new LatLng(36.9954221,-122.0635989);
        mMap.addMarker(new MarkerOptions().position(ucsc).title("ucsc"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ucsc));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ucsc, 14.5f));

        mMap.setMinZoomPreference(14.0f);


        LatLngBounds campusBound = new LatLngBounds(
                new LatLng(36.978098, -122.027028), new LatLng(37.0021413,-122.0577561));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ucsc, 14.5f));
        mMap.setLatLngBoundsForCameraTarget(campusBound);

    }
}
