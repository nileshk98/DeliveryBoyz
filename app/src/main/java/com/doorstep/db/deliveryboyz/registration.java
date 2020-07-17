package com.doorstep.db.deliveryboyz;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class registration extends AppCompatActivity {
    EditText name,mobile,email;
    DatabaseReference myRef;
    String emailpattern;
    TextView ccp;
    ProgressDialog spinner;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Bundle bundle=getIntent().getExtras();
        id=bundle.getString("id");
        spinner=new ProgressDialog(registration.this);
        spinner.setMessage("Please Wait...");
        spinner.setCancelable(false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(id.equals("User"))
        {
            myRef = database.getInstance().getReference("Register");
        }
        else if(id.equals("Del"))
            {
            myRef = database.getInstance().getReference("Dregister");
        }
        name=findViewById(R.id.name1);
        mobile=findViewById(R.id.mobile1);
        email=findViewById(R.id.email1);
        ccp=findViewById(R.id.ccp);
    }
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
     if(keyCode==KeyEvent.KEYCODE_BACK){
         Intent i = new Intent(this, login.class);
         Bundle b=new Bundle();
         b.putString("id",id);
         i.putExtras(b);
         startActivity(i);
         finish();
         return true;
     }
     return super.onKeyDown(keyCode,event);
    }
    public void register(View v)
    {
        spinner.show();
        int q=0;
        emailpattern="[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(email.getText().toString().matches(emailpattern))
        {
            q=1;
        }
        if(name.getText().toString().equals(""))
        {
            spinner.dismiss();
            name.setError("This field can't be empty");
        }
        else if(mobile.getText().toString().equals(""))
        {
            spinner.dismiss();
            mobile.setError("This field can't be empty");
        }
        else if(mobile.getText().toString().length()!=10)
        {
            spinner.dismiss();
            mobile.setError("Please enter valid mobile no.");
        }
        else if(email.getText().toString().equals(""))
        {
            spinner.dismiss();
            email.setError("This field can't be empty");
        }
        else if(q!=1)
        {
            spinner.dismiss();
            email.setError("Please enter valid email address");
        }
        else {
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                int x;
                String m = mobile.getText().toString();
                String e = email.getText().toString();
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        String value = item.child("mobile").getValue(String.class);
                        String value1 = item.child("email").getValue(String.class);
                        if(value.equals(m))
                        {
                            x=1;
                        }
                        else if(value1.equals(e))
                        {
                            x=2;
                        }
                    }
                    if(x==1)
                    {
                        spinner.dismiss();
                        mobile.setError("Mobile number already exists! Please Sign in instead.");
                    }
                    else if(x==2)
                    {
                        spinner.dismiss();
                        email.setError("Email id already exists! Please Sign in instead.");
                    }
                    else
                    {
                        setvalue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("nn", "Failed to read value.", error.toException());
                }
            });
            }
    }

    private void setvalue() {
        String cc= (String) ccp.getText();
        String n = name.getText().toString();
        String m = mobile.getText().toString();
        String e = email.getText().toString();
        String id = myRef.push().getKey();
        Intent i=new Intent(this,otp.class);
        Bundle bundle=new Bundle();
        bundle.putString("cc",cc);
        bundle.putString("n",n);
        bundle.putString("m",m);
        bundle.putString("e",e);
        bundle.putString("id",id);
        bundle.putString("id1",this.id);
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }
}
