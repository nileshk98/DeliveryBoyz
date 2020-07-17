package com.doorstep.db.deliveryboyz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
        , RoutingListener {
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
boolean qwe=false;
    private FusedLocationProviderClient mFusedLocationClient;

    DatabaseReference ref;
    String id;
    DatabaseReference myRef;
    SupportMapFragment mapFragment;
    private static final int TIME_DELAY = 2000;
    private static long back_pressed;
    private FirebaseAuth mAuth;
    private String customerId = "", destination;
    private LatLng destinationLatLng;
    TextView textView, textView1;
    private LinearLayout mCustomerInfo;
    private CircleImageView mCustomerProfileImage;
    private TextView mCustomerName, mCustomerPhone, mCustomerDestination;
    private Button mRideStatus;
    private int status = 0;
    private Switch mWorkingSwitch;
    private float rideDistance = 0;
    Marker dest;
    View mapView;
    private CircleImageView circleImageView;
    int check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View h = navigationView.getHeaderView(0);
        circleImageView = h.findViewById(R.id.imageView);
        polylines = new ArrayList<>();
        textView = navigationView.getHeaderView(0).findViewById(R.id.textV);
        textView1 = navigationView.getHeaderView(0).findViewById(R.id.textV1);
        mCustomerInfo = findViewById(R.id.customerInfo);
        mCustomerProfileImage = findViewById(R.id.customerProfileImage);
        mCustomerName = findViewById(R.id.customerName);
        mCustomerPhone = findViewById(R.id.customerPhone);
        mCustomerDestination = findViewById(R.id.customerDestination);

        mWorkingSwitch = findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    check = 1;
                    connectDriver();
                } else {
                    check = 0;
                    disconnectDriver();
                }
            }
        });
        mRideStatus = findViewById(R.id.rideStatus);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status) {
                    case 1:
                        mWorkingSwitch.setVisibility(View.GONE);
                        DatabaseReference r = FirebaseDatabase.getInstance().getReference("Register").child(customerId);
                        r.child("picked").setValue(true);
                        qwe=true;
                        getRouteToMarker1(customer,destinationLatLng);
                        status = 2;
                        erasePolyLines();
                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                            pickupMarker.remove();
                            dest = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.custicon)));
                            getRouteToMarker(destinationLatLng);
                            LatLng n = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(n);
                            builder.include(destinationLatLng);
                            LatLngBounds bounds = builder.build();
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                            mMap.animateCamera(cu);
                        }
                        mRideStatus.setText("Complete Delivery");
                        break;
                    case 2:
                        DatabaseReference r1 = FirebaseDatabase.getInstance().getReference("Register").child(customerId);
                        r1.child("picked").removeValue();
                        recordRide();
                        mWorkingSwitch.setVisibility(View.VISIBLE);
                        endRide();
                        break;
                }
            }
        });
        ImageButton call = findViewById(R.id.call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + mCustomerPhone.getText().toString()));
                if (ActivityCompat.checkSelfPermission(home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
            }
        });
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (id.equals("User")) {
            myRef = database.getInstance().getReference("Register").child(userId);
        } else if (id.equals("Del")) {
            myRef = database.getInstance().getReference("Dregister").child(userId);
        }
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String n=dataSnapshot.child("name").getValue(String.class);
                String e=dataSnapshot.child("email").getValue(String.class);
                textView.setText(n);
                textView1.setText(e);
                if(dataSnapshot.child("profileImageUrl").exists()){
                    String mProfileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(circleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        mapView=mapFragment.getView();
        mAuth=FirebaseAuth.getInstance();
        getAssignedCustomer();
    }

    private void getAssignedCustomer() {
        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status=1;
                        customerId=dataSnapshot.getValue().toString();
                        getAssignedCustomerPickupLocation();
                        getAssignedCustomerDestination();
                        getAssignedCustomerInfo();
                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void endRide() {
        mRideStatus.setText("Picked Parcel");
        erasePolyLines();
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(userId).child("customerRequest");
            driverRef.removeValue();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance=0;
        customer=null;
        destinationLatLng=null;
        if(pickupMarker!=null)
        {
            pickupMarker.remove();
        }
        if(dest!=null){
            dest.remove();
        }
        if(assignedCustomerPickupLocationRefListener!=null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        mCustomerDestination.setText("Destination: --");
        mCustomerProfileImage.setImageResource(R.mipmap.account_round);
    }
    private void recordRide()
    {
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(userId).child("history");
        DatabaseReference customerRef=FirebaseDatabase.getInstance().getReference().child("Register").child(customerId).child("history");
        DatabaseReference historyRef=FirebaseDatabase.getInstance().getReference().child("history");
        String requestId=historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);
        /*Location A=new Location("A");
        A.setLatitude(customer.latitude);
        A.setLongitude(customer.longitude);
        Location B=new Location("B");
        B.setLatitude(destinationLatLng.latitude);
        B.setLongitude(destinationLatLng.longitude);
        rideDistance=A.distanceTo(B)/1000;*/
        HashMap map=new HashMap();
        map.put("driver",userId);
        map.put("customer",customerId);
        map.put("rating",0);
        map.put("timestamp",getCurrentTimestamp());
        map.put("destination",destination);
        map.put("location/from/lat",customer.latitude);
        map.put("location/from/lng",customer.longitude);
        map.put("location/to/lat",destinationLatLng.latitude);
        map.put("location/to/lng",destinationLatLng.longitude);
        map.put("distance",rideDistance);
        map.put("price",rideDistance*6);
        map.put("customerPaid",true);
        historyRef.child(requestId).updateChildren(map);
    }

    private Long getCurrentTimestamp() {
        Long timestamp=System.currentTimeMillis()/1000;
        return timestamp;
    }

    private void getAssignedCustomerDestination() {
        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination")!=null){
                        destination=map.get("destination").toString();
                        mCustomerDestination.setText("Destination: "+destination);
                    }
                    else{
                        mCustomerDestination.setText("Destination: --");
                    }

                    Double destinationLat=0.0;
                    Double destinationLng=0.0;
                    if(map.get("destinationLat")!=null)
                    {
                        destinationLat=Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng")!=null)
                    {
                        destinationLng=Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng=new LatLng(destinationLat,destinationLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference("Register").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String n=dataSnapshot.child("name").getValue(String.class);
                String p=dataSnapshot.child("mobile").getValue(String.class);
                mCustomerName.setText(n);
                mCustomerPhone.setText("+91"+p);
                if(dataSnapshot.child("profileImageUrl").exists()){
                    String mProfileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(mCustomerProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
LatLng customer;
    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef=FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener=assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;
                    if(map.get(0)!=null){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickupLatLng=new LatLng(locationLat,locationLng);
                    customer=pickupLatLng;
                    pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.custicon)));
                    getRouteToMarker(pickupLatLng);
                    LatLng n=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    LatLngBounds.Builder builder=new LatLngBounds.Builder();
                    builder.include(n);
                    builder.include(customer);
                    LatLngBounds bounds=builder.build();
                    CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,200);
                    mMap.animateCamera(cu);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getRouteToMarker1(LatLng pickupLatLng,LatLng destinationLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng,destinationLatLng)
                .key("AIzaSyAonRxTarMa2hcOS91-wfc1-4gVUk26lAY")
                .build();
        routing.execute();
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), pickupLatLng)
                .key("AIzaSyAonRxTarMa2hcOS91-wfc1-4gVUk26lAY")
                .build();
        routing.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(q>0){
            if(q==1) {
                setTitle("Delivery Boyz");
            }
            super.onBackPressed();
            q--;
        }else {
            if(back_pressed+TIME_DELAY>System.currentTimeMillis())
            {
                String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                ref=FirebaseDatabase.getInstance().getReference("driversAvailable");
                GeoFire geoFire=new GeoFire(ref);
                geoFire.removeLocation(userId);
                super.onBackPressed();
            }
            else{
                Toast.makeText(this, "Press once again to exit !", Toast.LENGTH_SHORT).show();
            }
            back_pressed=System.currentTimeMillis();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
int q=0;
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment=null;
        if (id == R.id.navbook) {
            Bundle args = new Bundle();
            args.putString("id",this.id);
            fragment = new navbook();
            fragment.setArguments(args);
            q++;
        }  else if (id == R.id.navset) {
            Bundle args = new Bundle();
            args.putString("id",this.id);
            fragment = new navset();
            fragment.setArguments(args);
            q++;
        }
        else if (id==R.id.navout)
        {
            if(check!=0){
           disconnectDriver();}
            mAuth.signOut();
            SharedPreferences prf;
            Intent intent;
            prf = getSharedPreferences("user_details",MODE_PRIVATE);
            intent = new Intent(home.this,select.class);
            SharedPreferences.Editor editor = prf.edit();
            editor.clear();
            editor.commit();
            startActivity(intent);
            finish();
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps_style1));

            if (!success) {
                Log.e("TAG", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("TAG", "Can't find style. Error: ", e);
        }*/
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            View toolbar = ((View) mapView.findViewById(Integer.parseInt("1")).
                    getParent()).findViewById(Integer.parseInt("4"));

            // and next place it, for example, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 50, 460);
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }
    }

    LocationCallback mLocationCallback=new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    if(!customerId.equals("")){
                        //rideDistance+=mLastLocation.distanceTo(location)/1000;
                    }
                    mLastLocation=location;
                    LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                    if(customer==null && destinationLatLng==null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                    }
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable=FirebaseDatabase.getInstance().getReference("driversAvailable");
                    DatabaseReference refWorking=FirebaseDatabase.getInstance().getReference("driversWorking");
                    GeoFire geoFireAvailable=new GeoFire(refAvailable);
                    GeoFire geoFireWorking=new GeoFire(refWorking);
                    switch (customerId){
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));

                            break;
                        default:

                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                            break;
                    }
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Give Permission")
                        .setMessage("Please give Location Permission!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(home.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                .create()
                .show();
            }
            else{
                ActivityCompat.requestPermissions(home.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1: {
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please provide the permisssion for location",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private void connectDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectDriver(){
        if(mFusedLocationClient!=null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref=FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(qwe){
            for (int i = 0; i < route.size(); i++) {
                rideDistance = route.get(i).getDistanceValue() / 1000;
                qwe=false;
            }
        }
        else {
            if (polylines.size() > 0) {
                for (Polyline poly : polylines) {
                    poly.remove();
                }
            }

            polylines = new ArrayList<>();
            //add route(s) to the map.
            for (int i = 0; i < route.size(); i++) {

                //In case of more than 5 alternative routes
                int colorIndex = i % COLORS.length;

                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(getResources().getColor(COLORS[colorIndex]));
                polyOptions.width(10 + i * 3);
                polyOptions.addAll(route.get(i).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylines.add(polyline);

                //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
                if (status == 2) {

                }
            }
        }
    }

    @Override
    public void onRoutingCancelled() {
    }
    private void erasePolyLines(){
        for(Polyline line :polylines){
            line.remove();
        }
        polylines.clear();
    }
}
