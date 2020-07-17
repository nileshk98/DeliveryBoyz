package com.doorstep.db.deliveryboyz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginotp extends AppCompatActivity {
    String m,phonenumber,otp,id;
    EditText fpotp1;
    TextView fpresend;
    ProgressDialog spinner;
    SmsVerifyCatcher smsVerifyCatcher;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        setContentView(R.layout.activity_loginotp);
        spinner=new ProgressDialog(loginotp.this);
        spinner.setMessage("Please Wait...");
        spinner.setCancelable(false);
        Bundle bundle=getIntent().getExtras();
        m=bundle.getString("mob");
        id=bundle.getString("id");
        fpotp1=findViewById(R.id.fpotp1);
        fpresend=findViewById(R.id.fpresend);
        phonenumber = "+91" + m;
        StartFirebaseLogin();
        pt();
        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code
                fpotp1.setText(code);
                spinner.show();
                done();
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        Intent i = new Intent(this, login.class);
        Bundle bundle = new Bundle();
        bundle.putString("id",id);
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }
    public void eotp(View v)
    {
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
        if(fpotp1.getText().toString()=="")
        {
            spinner.dismiss();
            fpotp1.setError("OTP cannot be empty !");
        }
        else {
            otp = fpotp1.getText().toString();
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
    public void resendotp(View v)
    {
        resendVerificationCode(phonenumber,mResendToken);
        fpresend.setTextColor(Color.parseColor("#810C79"));
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
                loginotp.this,        // Activity (for callback binding)
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
                                    editor.putString("id",id);
                                    editor.commit();
                                    Intent i = new Intent(loginotp.this, home.class);
                                    Intent i1=new Intent(loginotp.this,chome.class);
                                    Bundle b=new Bundle();
                                    b.putString("id",id);
                                    b.putString("email","1234");
                                    i.putExtras(b);
                                    i1.putExtras(b);
                                    if(id.equals("Del"))
                                    {
                                        startActivity(i);
                                        spinner.dismiss();
                                        finish();
                                    }
                                    else
                                    {
                                        startActivity(i1);
                                        spinner.dismiss();
                                        finish();
                                    }
                                } else {
                                    spinner.dismiss();
                                    Toast.makeText(loginotp.this,"Incorrect OTP",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(loginotp.this,"verification completed",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Toast.makeText(loginotp.this,"invalid",Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(loginotp.this,"too otp ",Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                mResendToken = forceResendingToken;
                Toast.makeText(loginotp.this,"Code sent",Toast.LENGTH_SHORT).show();
            }
        };
    }
}
