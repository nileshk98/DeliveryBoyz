package com.doorstep.db.deliveryboyz;

import android.Manifest;
import android.app.Activity;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class chome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,RoutingListener {
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    String id;
    double price,dbpay;
    boolean qwe=false;
    DatabaseReference myRef;
    SupportMapFragment mapFragment;
    private static final int TIME_DELAY=2000;
    private static long back_pressed;
    private Button mRequest;
    private LatLng pickupLocation;
    private FirebaseAuth mAuth;
    private Boolean requestBol=false;
    TextView textView,textView1;
    private Marker pickupMarker;
    private String destination;
    private LinearLayout mDriverInfo;
    private CircleImageView mDriverProfileImage;
    private CircleImageView imageView;
    private TextView mDriverName,mDriverPhone,mDriverBike;
    private RatingBar mRatingBar;
    private LatLng destinationLatLng;
    int q=0;
    private FusedLocationProviderClient mFusedLocationClient;
    View mapView;
    boolean paysuccess=false;
    double temp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Register").child(uid).child("dbpay").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                        dbpay = Double.valueOf(dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        textView=navigationView.getHeaderView(0).findViewById(R.id.text);
        textView1=navigationView.getHeaderView(0).findViewById(R.id.text1);
        mRequest=findViewById(R.id.request);
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        mDriverInfo=findViewById(R.id.driverInfo);
        mDriverProfileImage=findViewById(R.id.driverProfileImage);
        mDriverName=findViewById(R.id.driverName);
        mDriverPhone=findViewById(R.id.driverPhone);
        mDriverBike=findViewById(R.id.driverBike);
        mRatingBar=findViewById(R.id.ratingBar);
        polylines = new ArrayList<>();
        View h=navigationView.getHeaderView(0);
        imageView=h.findViewById(R.id.imageView);
        ImageButton call = findViewById(R.id.call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + mDriverPhone.getText().toString()));
                if (ActivityCompat.checkSelfPermission(chome.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(requestBol){
                    if(paysuccess) {
                        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference("Register").child(uid).child("dbpay").setValue(String.valueOf(temp+dbpay));
                        dbpay=temp+dbpay;
                        paysuccess=false;
                    }
                    endRide();
                }else{
                    requestBol=true;
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                    pickupLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.custicon)));

                    mRequest.setText("Getting your deliverer....");

                    getClosestDriver();
                }
            }
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                    String mProfileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    Glide.with(getApplication()).load(mProfileImageUrl).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
            mapFragment.getMapAsync(this);
            mapView=mapFragment.getView();
        destinationLatLng=new LatLng(0.0,0.0);
        mAuth=FirebaseAuth.getInstance();
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                qwe=true;
                getRouteToMarker(place.getLatLng());
                erasePolyLines();
                mRequest.setVisibility(View.VISIBLE);
                destination=place.getName().toString();
                destinationLatLng=place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
        picked();
    }
    int w=0;
    Marker dest;
    private void picked(){
        final String q=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference r= FirebaseDatabase.getInstance().getReference("Register").child(q).child("picked");
        r.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    erasePolyLines();
                    if(pickupMarker!=null) {
                        w=1;
                        pickupMarker.remove();
                        FirebaseDatabase.getInstance().getReference("Register").child(q).child("dbpay").setValue(String.valueOf(dbpay));

                    }
                    dest=mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.custicon)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private int radius=1;
    private Boolean driverFound=false;
    private String driverFoundID;

    boolean b=false;
    GeoQuery geoQuery;
    private void getClosestDriver() {
    DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("driversAvailable");
    GeoFire geoFire = new GeoFire(driverLocation);

    geoQuery=geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
    geoQuery.removeAllListeners();

    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if(!driverFound && requestBol) {
                driverFound = true;
                driverFoundID=key;
                if(price>dbpay){
                    if(dbpay==0.0d){
                        payPalPayment(price);
                        temp=price;
                    }
                    else{
                        temp=price;
                        payPalPayment(price-dbpay);
                        dbpay=0.0;
                    }
                }else{
                    dbpay=dbpay-price;
                    temp=price;
                    paysuccess=true;
                }

                DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(driverFoundID).child("customerRequest");
                String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap map=new HashMap();
                map.put("customerRideId",customerId);
                map.put("destination",destination);
                map.put("destinationLat",destinationLatLng.latitude);
                map.put("destinationLng",destinationLatLng.longitude);
                driverRef.updateChildren(map);
                getDriverLocation();
                getDriverInfo();
                getHasRideEnded();
                mRequest.setText("Looking for nearby Deliverer...");
            }
        }

        @Override
        public void onKeyExited(String key) {

        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {
           if(!driverFound)
           {
               if(radius>=15){
                   endRide();
                   Toast.makeText(chome.this, "No Drivers Found!", Toast.LENGTH_SHORT).show();
               }
               else {
                   radius++;
                   getClosestDriver();
               }
           }
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    });
    }
    private int PAYPAL_REQUEST_CODE=1;
    private static PayPalConfiguration config=new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);
    private void payPalPayment(double p) {
        PayPalPayment payment=new PayPalPayment(new BigDecimal(p),"USD","Delivery Boyz",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent=new Intent(this,PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);

        startActivityForResult(intent,PAYPAL_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PAYPAL_REQUEST_CODE){
            if(resultCode==Activity.RESULT_OK){
                PaymentConfirmation confirm=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm!=null){
                    try{
                        JSONObject jsonObj=new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse=jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){
                            paysuccess=true;
                            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                endRide();
                Toast.makeText(this, "Payment Unsuccessful please Try again Later!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();
    }
    private void getDriverInfo() {
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference("Dregister").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String n=dataSnapshot.child("name").getValue(String.class);
                String p=dataSnapshot.child("mobile").getValue(String.class);
                mDriverName.setText(n);
                mDriverPhone.setText("+91"+p);
                mDriverBike.setText("Bike");
                int ratingSum=0;
                int ratingsTotal=0;
                float ratingsAvg=0;
                for(DataSnapshot child:dataSnapshot.child("rating").getChildren()){
                    if(Integer.valueOf(child.getValue().toString())>0) {
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                }
                if(ratingsTotal!=0){
                    ratingsAvg=ratingSum/ratingsTotal;
                    mRatingBar.setRating(ratingsAvg);
                }
                if(dataSnapshot.child("profileImageUrl").exists()){
                    String mProfileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(mDriverProfileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded() {
        driveHasEndedRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener=driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    //erasePolyLines();
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void endRide() {
        erasePolyLines();
        w=0;
        requestBol=false;
        geoQuery.removeAllListeners();
        if(driverLocationRef!=null) {
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }
        if(driveHasEndedRef!=null) {
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        }
        if(driverFoundID!=null)
        {
            DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Dregister").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID=null;
        }
        driverFound=false;
        radius=1;
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker!=null)
        {
            pickupMarker.remove();
        }
        if(mDriverMarker!=null)
        {
            mDriverMarker.remove();
        }
        if(dest!=null) {
            dest.remove();
        }
        mRequest.setClickable(true);
        mRequest.setText("Make Delivery request");
        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverBike.setText("");
        mDriverProfileImage.setImageResource(R.mipmap.account_round);
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation() {
        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener=driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;
                    mRequest.setText("Deliverer Found!");
                    if(map.get(0)!=null){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng=new LatLng(locationLat,locationLng);
                    if(mDriverMarker !=null){
                        mDriverMarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2=new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);


                    mDriverMarker=mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Deliverer").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bikeicon)));
                    if(w==1){
                        mRequest.setClickable(false);
                        mRequest.setText("Parcel picked!");
                        getRouteToMarker1(driverLatLng,destinationLatLng);
                        final LatLngBounds.Builder builder=new LatLngBounds.Builder();
                        builder.include(driverLatLng);
                        builder.include(destinationLatLng);
                        LatLngBounds bounds=builder.build();
                        CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,200);
                        mMap.animateCamera(cu);
                    }
                    else if(w==0) {
                        if(distance<80)
                        {
                            mRequest.setText("Deliverer reached!");
                        }
                        else{
                            mRequest.setText("Cancel Request");
                        }
                        getRouteToMarker(driverLatLng);
                        LatLng n = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(n);
                        builder.include(driverLatLng);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                        mMap.animateCamera(cu);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(q>0){
            if(q==1) {
                setTitle("Delivery Boyz");
            }
            super.onBackPressed();
            q--;
        }
        else {
            if(back_pressed+TIME_DELAY>System.currentTimeMillis())
            {
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
        getMenuInflater().inflate(R.menu.chome, menu);
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
        } else if (id == R.id.navpay) {
            fragment = new navPay();
            q++;
        } else if (id == R.id.navref) {

        } else if (id == R.id.navset) {
            Bundle args = new Bundle();
            args.putString("id",this.id);
            fragment = new navset();
            fragment.setArguments(args);
            q++;
        }
        else if (id==R.id.navout)
        {
            mAuth.signOut();
            SharedPreferences prf;
            Intent intent;
            prf = getSharedPreferences("user_details",MODE_PRIVATE);
            intent = new Intent(chome.this,select.class);
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
        mMap=googleMap;
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 300, 50, 0);
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }
    LatLng latLng;
    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    if(!getDriversAroundStarted) {
                        getDriversAround();
                    }
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(chome.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(chome.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getDriversAround(){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        getDriversAroundStarted=true;
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 6000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bikeicon)));
                mDriverMarker.setTag(key);

                markers.add(mDriverMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                        markers.remove(markerIt);
                        return;
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private void getRouteToMarker1(LatLng driverLatLng,LatLng destLatlng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(driverLatLng, destLatlng)
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
                price=(route.get(i).getDistanceValue()/1000)*6;
                qwe=false;
            }
        }else {
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
