package com.doorstep.db.deliveryboyz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

public class navPay extends Fragment {
    TextView balance;
    EditText amount;
    Button ok;
    int status=1;
    Float bal=0.0f;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navpay, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("DB Wallet");
        balance=getView().findViewById(R.id.balance);
        amount=getView().findViewById(R.id.amount);
        ok=getView().findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status==1){
                    ok.setText("OK");
                    amount.setVisibility(View.VISIBLE);
                    status=2;
                }else if(status==2){
                    if(amount.getText().toString().equals("")||amount.getText().toString().equals("0")){
                        Toast.makeText(getContext(), "Please Enter Valid Amount", Toast.LENGTH_SHORT).show();;
                    }
                    else{
                        payPalPayment();
                    }
                }
            }
        });
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Register").child(uid).child("dbpay").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    balance.setText("₹"+dataSnapshot.getValue(String.class));
                    bal=Float.valueOf(dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private int PAYPAL_REQUEST_CODE=1;
    private static PayPalConfiguration config=new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);
    private void payPalPayment() {
        PayPalPayment payment=new PayPalPayment(new BigDecimal(amount.getText().toString()),"USD","Delivery Boyz",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent=new Intent(getContext(),PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);

        startActivityForResult(intent,PAYPAL_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PAYPAL_REQUEST_CODE){
            if(resultCode==Activity.RESULT_OK){
                PaymentConfirmation confirm=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm!=null){
                    try{
                        JSONObject jsonObj=new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse=jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){
                            Toast.makeText(getContext(), "Balance added Successfuly!", Toast.LENGTH_SHORT).show();
                            amount.setVisibility(View.GONE);
                            ok.setText("Add Money");
                            status=1;
                            String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseDatabase.getInstance().getReference("Register").child(uid).child("dbpay")
                                    .setValue(String.valueOf(bal+Float.valueOf(amount.getText().toString())));
                            amount.setText("");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                Toast.makeText(getContext(), "Payment Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
