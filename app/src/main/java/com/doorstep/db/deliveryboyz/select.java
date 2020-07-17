package com.doorstep.db.deliveryboyz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class select extends AppCompatActivity {
Button user,del;
    private static final int TIME_DELAY=2000;
    private static long back_pressed;
    ProgressDialog spinner;
    public static final int MULTIPLE_PERMISSIONS = 10;
    private FirebaseAuth mAuth;
    SharedPreferences pref;
    String[] permissions= new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CALL_PHONE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        mAuth=FirebaseAuth.getInstance();
        if (checkPermissions())
        {

        }
        spinner=new ProgressDialog(this);
        spinner.setMessage("Please Wait...");
        spinner.setCancelable(false);
        user=findViewById(R.id.user);
        del=findViewById(R.id.del);
        startService(new Intent(this,onAppKilled.class));
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.show();
                Intent i=new Intent(select.this,login.class);
                Bundle bundle=new Bundle();
                bundle.putString("id","User");
                i.putExtras(bundle);
                startActivity(i);
                spinner.dismiss();
                finish();
            }
        });
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.show();
                Intent i=new Intent(select.this,login.class);
                Bundle bundle=new Bundle();
                bundle.putString("id","Del");
                i.putExtras(bundle);
                startActivity(i);
                spinner.dismiss();
                finish();
            }
        });
    }
    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissionsList) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;

                        }

                    }
                    // Show permissionsDenied
                }
                return;
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        pref = getSharedPreferences("user_details",MODE_PRIVATE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Bundle b = new Bundle();
            b.putString("id", pref.getString("id",null));
            if(pref.getString("id",null).equals("Del")) {
                Intent intent = new Intent(select.this, home.class);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
            else
            {
                Intent intent = new Intent(select.this, chome.class);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
            return;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    public void onBackPressed()
    {
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
