package com.doorstep.db.deliveryboyz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class otp extends AppCompatActivity implements View.OnClickListener {
String cc,n,m,e,id,otp,phonenumber,id1;
EditText otp1;
Button sub;
TextView resend,te;
ProgressDialog spinner;
    SmsVerifyCatcher smsVerifyCatcher;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;
    DatabaseReference myRef;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    SharedPreferences pref;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        setContentView(R.layout.activity_otp);
        spinner=new ProgressDialog(otp.this);
        spinner.setMessage("Please Wait...");
        spinner.setCancelable(false);
        Bundle bundle=getIntent().getExtras();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        cc=bundle.getString("cc");
        n=bundle.getString("n");
        m=bundle.getString("m");
        e=bundle.getString("e");
        id=bundle.getString("id");
        id1=bundle.getString("id1");
        if(id1.equals("User"))
        {
            myRef = database.getInstance().getReference("Register");
        }
        else
        {
            myRef = database.getInstance().getReference("Dregister");
        }
        te=findViewById(R.id.te);
        resend=findViewById(R.id.resend);
        otp1=findViewById(R.id.otp1);
        sub=findViewById(R.id.sub);
        sub.setOnClickListener(this);
        phonenumber = cc + m;
        StartFirebaseLogin();
       pt();
       te.setText("OTP has been sent to "+phonenumber);
        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code
                otp1.setText(code);
                spinner.show();//set code in edit text
                done();
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        Intent i = new Intent(this, registration.class);
        Bundle b=new Bundle();
        b.putString("id",id1);
        i.putExtras(b);
        startActivity(i);
        finish();
    }
    @Override
    public void onClick(View view) {
        spinner.show();
        done();
    }
    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }
    public void done()
    {
        if(otp1.getText().toString()=="")
        {
            spinner.dismiss();
            otp1.setError("OTP cannot be empty !");
        }
        else {
            otp = otp1.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
            SigninWithPhone(credential);
        }
    }
    private String parseCode(String message) {
        Pattern p = Pattern.compile("(|^)\\d{6}");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }
    public void wr(View v)
    {
        Intent i = new Intent(this, login.class);
        Bundle b=new Bundle();
        b.putString("id",id1);
        i.putExtras(b);
        startActivity(i);
    }
    public void resendotp(View v)
    {
        resendVerificationCode(phonenumber,mResendToken);
        resend.setTextColor(Color.parseColor("#810C79"));
    }
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallback,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
public void pt()
{
    PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phonenumber,                     // Phone number to verify
            60,                           // Timeout duration
            TimeUnit.SECONDS,                // Unit of timeout
            otp.this,        // Activity (for callback binding)
            mCallback);             // OnVerificationStateChangedCallbacks
}
    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("id",id1);
                            editor.commit();
                            String id=FirebaseAuth.getInstance().getCurrentUser().getUid();
                            myRef.child(id).child("name").setValue(n);
                            myRef.child(id).child("mobile").setValue(m);
                            myRef.child(id).child("email").setValue(e);
                            myRef.child(id).child("ccp").setValue(cc);
                            Toast.makeText(otp.this, "Succesfully registered", Toast.LENGTH_SHORT).show();
                            Bundle b=new Bundle();
                            b.putString("id",id1);
                            if(id1.equals("Del")) {
                                Intent i = new Intent(otp.this, home.class);
                                i.putExtras(b);
                                startActivity(i);
                                finish();
                            }
                            else
                            {
                                Intent i = new Intent(otp.this, chome.class);
                                i.putExtras(b);
                                startActivity(i);
                                finish();
                            }

                        } else {
                            Toast.makeText(otp.this,"Incorrect OTP",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                SigninWithPhone(phoneAuthCredential);
                Toast.makeText(otp.this,"verification completed",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Toast.makeText(otp.this,"invalid",Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(otp.this,"too otp ",Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                mResendToken = forceResendingToken;
                Toast.makeText(otp.this,"Code sent",Toast.LENGTH_SHORT).show();
            }
        };
    }
    }

