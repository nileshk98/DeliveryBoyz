package com.doorstep.db.deliveryboyz;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class phoneUpdate extends AppCompatActivity {
String id;
EditText phone;
Button ok;
DatabaseReference ref;
int x=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_update);
        setTitle("Update Mobile Number");
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        phone= findViewById(R.id.ph);
        ok=findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String ph=phone.getText().toString();
                if(phone.getText().toString().equals("")){
                    phone.setError("Please fill Mobile no.!");
                }
                else if(phone.getText().toString().length()!=10){
                    phone.setError("Please Enter valid mobile no.!");
                }
                else{
                    if(id.equals("User")) {
                        ref = FirebaseDatabase.getInstance().getReference("Register");
                    }else{
                        ref=FirebaseDatabase.getInstance().getReference("Dregister");
                    }
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        int x=0;
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot item:dataSnapshot.getChildren()){
                                String value=item.child("mobile").getValue(String.class);
                                if(value.equals(ph)){
                                    x=1;
                                }
                            }
                            if(x==1)
                            {
                                phone.setError("Mobile no. already exists");
                            }
                            else {
                                Intent i = new Intent(phoneUpdate.this, updateotp.class);
                                Bundle b = new Bundle();
                                b.putString("mob", phone.getText().toString());
                                b.putString("id", id);
                                i.putExtras(b);
                                startActivity(i);
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }
}
