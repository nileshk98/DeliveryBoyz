package com.doorstep.db.deliveryboyz;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.doorstep.db.deliveryboyz.historyRecyclerView.HistoryAdapter;
import com.doorstep.db.deliveryboyz.historyRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


//import java.text.DateFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class navbook extends Fragment {
    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    private TextView mBalance;

    private Double Balance=0.0,cutBalance=0.0;
    String userId,id;

    private Button mPayout;
String uid;
    private EditText mPayoutEmail;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navbook, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle b = getArguments();
        id = b.getString("id");
        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Dregister").child(uid).child("haveBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                cutBalance=Double.valueOf(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mBalance=getView().findViewById(R.id.balance);
        mPayout=getView().findViewById(R.id.payout);
        mPayoutEmail=getView().findViewById(R.id.payoutEmail);

        mHistoryRecyclerView=getView().findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager=new LinearLayoutManager(getContext());
        ((LinearLayoutManager) mHistoryLayoutManager).setReverseLayout(true);
        ((LinearLayoutManager) mHistoryLayoutManager).setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter=new HistoryAdapter(getDataSetHistory(),getContext());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();
        if(id.equals("Del")){
            mBalance.setVisibility(View.VISIBLE);
            mPayout.setVisibility(View.VISIBLE);
            mPayoutEmail.setVisibility(View.VISIBLE);
        }
        //you can set the title for your toolbar here for different fragments different titles
        if(id.equals("User")) {
            getActivity().setTitle("Your Bookings");
        }else{
            getActivity().setTitle("Delivery History");
        }
        mPayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Balance-cutBalance==0.0d){
                    Toast.makeText(getContext(), "Insufficient Balance!", Toast.LENGTH_SHORT).show();
                }else {
                    payoutRequest();
                }
            }
        });
    }


    private ArrayList resultsHistory=new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase=null;
        if(id.equals("User")) {
            userHistoryDatabase=FirebaseDatabase.getInstance().getReference().child("Register").child(userId).child("history");
        }
        else{
            userHistoryDatabase=FirebaseDatabase.getInstance().getReference().child("Dregister").child(userId).child("history");
        }
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history: dataSnapshot.getChildren()){
                        if(history.getValue().toString().equals("true"))
                        FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase=FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId=dataSnapshot.getKey();
                    String destination=dataSnapshot.child("destination").getValue().toString();
                    Long timestamp=0L;
                    Double ridePrice=0.0;

                        if(dataSnapshot.child("timestamp").getValue()!=null){
                            timestamp=Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                        }

                    if(dataSnapshot.child("customerPaid").getValue()!=null&&dataSnapshot.child("driverPaidOut").getValue()==null){
                        if(dataSnapshot.child("distance").getValue()!=null){
                            ridePrice=Double.valueOf(dataSnapshot.child("price").getValue().toString());
                            Balance+=ridePrice;
                            mBalance.setText("Balance: ₹"+String.valueOf(Balance-cutBalance));
                        }
                    }
                    HistoryObject obj=new HistoryObject(rideId,getDate(timestamp),destination);
                    resultsHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date=DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();
        return date;
    }

    public static final MediaType MEDIA_TYPE=MediaType.parse("application/json");
    ProgressDialog progress;
    private void payoutRequest() {
        progress=new ProgressDialog(getContext());
        progress.setTitle("Processing your payout");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();

        final OkHttpClient client=new OkHttpClient();
        JSONObject postData=new JSONObject();

        try {
            postData.put("uid",FirebaseAuth.getInstance().getUid());
            postData.put("email",mPayoutEmail.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body=RequestBody.create(MEDIA_TYPE,postData.toString());

        final Request request=new Request.Builder()
                .url("https://us-central1-delivery-boyz-321b7.cloudfunctions.net/payout")
                .post(body)
                .addHeader("Context-Type","application/json")
                .addHeader("cache-control","no-cache")
                .addHeader("Authorization","Your Token")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode=response.code();
                if(response.isSuccessful()){
                    switch(responseCode){
                        case 200:
                            progress.dismiss();
                            Snackbar.make(getView().findViewById(R.id.layout),"Payout Successful",Snackbar.LENGTH_LONG).show();
                            FirebaseDatabase.getInstance().getReference("Dregister").child(uid).child("haveBalance")
                                    .setValue(String.valueOf(cutBalance+(Balance-cutBalance)));
                            mBalance.setText("Balance : ₹0.0");
                            break;
                        case 500:
                            Snackbar.make(getView().findViewById(R.id.layout),"Error: Could not complete Payout",Snackbar.LENGTH_LONG).show();
                            break;
                        default:
                            Snackbar.make(getView().findViewById(R.id.layout),"Error: Could not complete Payout",Snackbar.LENGTH_LONG).show();
                            break;
                    }
                }
                progress.dismiss();
            }
        });
    }
}
