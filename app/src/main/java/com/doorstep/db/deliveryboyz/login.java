package com.doorstep.db.deliveryboyz;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;

public class login extends AppCompatActivity {
    DatabaseReference myRef;
    EditText email;
    ProgressDialog spinner;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=getIntent().getExtras();
        id=bundle.getString("id");
        setContentView(R.layout.activity_login);
        spinner=new ProgressDialog(login.this);
        spinner.setMessage("Please Wait...");
        spinner.setCancelable(false);
        InternetAvailabilityChecker.init(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(id.equals("User"))
        {
            myRef = database.getInstance().getReference("Register");
        }
        else if(id.equals("Del"))
        {
            myRef = database.getInstance().getReference("Dregister");
        }
        email=findViewById(R.id.email1);
    }
    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this,select.class));
        finish();
    }
    public void click(View v) {
        spinner.show();
        if(email.getText().toString().equals(""))
        {
            spinner.dismiss();
            email.setError("Please enter mobile no.");
        }
        else if(email.getText().length()<10||email.getText().length()>10)
        {
            spinner.dismiss();
            email.setError("Please enter valid mobile no.");
        }
        else {
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                int x = 0, y = 0;
                String e = email.getText().toString();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String value2 = item.child("mobile").getValue(String.class);
                        if (value2.equals(e)) {
                            x = 1;
                        }
                    }
                    if (x == 1) {
                        Intent i = new Intent(login.this, loginotp.class);
                        Bundle b=new Bundle();
                        b.putString("mob",email.getText().toString());
                        b.putString("id",id);
                        i.putExtras(b);
                        startActivity(i);
                        finish();
                    } else {
                        spinner.dismiss();
                        email.setError("Mobile number does not exist!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
    public void click1(View v) {
            Intent i = new Intent(this, registration.class);
            Bundle b=new Bundle();
            b.putString("id",id);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
}
