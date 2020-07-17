package com.doorstep.db.deliveryboyz.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.doorstep.db.deliveryboyz.HistorySingleActivity;
import com.doorstep.db.deliveryboyz.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;
    public TextView time;
    public TextView dest;
    public HistoryViewHolders(View itemView){
        super(itemView);
        itemView.setOnClickListener(this);

        rideId=itemView.findViewById(R.id.rideId);
        time=itemView.findViewById(R.id.time);
        dest=itemView.findViewById(R.id.dest);
    }
    @Override
    public void onClick(View view) {
        Intent intent=new Intent(view.getContext(),HistorySingleActivity.class);
        Bundle b=new Bundle();
        b.putString("rideId",rideId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}
