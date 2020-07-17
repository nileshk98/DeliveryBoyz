package com.doorstep.db.deliveryboyz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class emailUpdate extends AppCompatActivity {
private EditText email,cemail;
private Button submit;
String id;
DatabaseReference ref;
String emailpattern;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Update Email Id");
        setContentView(R.layout.activity_email_update);
        email=findViewById(R.id.em);
        cemail=findViewById(R.id.em1);
        submit=findViewById(R.id.sub);
        emailpattern="[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String em=email.getText().toString();
                String em1=cemail.getText().toString();
                if(!em.equals(em1)){
                    cemail.setError("Email address doesnt matches");
                }
                else if(em.equals("")||em1.equals("")){
                    email.setError("Please Fill values");
                    cemail.setError("Please Fill values");
                }
                else if(!em.matches(emailpattern)){
                    email.setError("Enter valid Email Id!");
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
                            String value = item.child("email").getValue(String.class);
                            if(value.equals(em))
                            {
                                x=1;
                            }
                        }
                        if(x==1)
                        {
                            email.setError("Email id already exists");
                        }
                        else
                        {
                            ref.child(uid).child("email").setValue(em);
                            Toast.makeText(emailUpdate.this, "Email id updated successfuly!", Toast.LENGTH_SHORT).show();
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
