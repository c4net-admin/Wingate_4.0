package com.maatayim.acceleradio.callsign;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maatayim.acceleradio.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CallSignAdapter extends RecyclerView.Adapter<CallSignAdapter.MyViewHolder> {


    private ArrayList<CallSign> callSigns;

    public CallSignAdapter(ArrayList<CallSign> callSigns, FragmentActivity activity) {
        this.callSigns = callSigns;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_sign_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CallSign callSign = callSigns.get(position);
        holder.name.setText(callSign.getName());
        holder.mac.setText(callSign.getMac());

    }

    @Override
    public int getItemCount() {
        return callSigns.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView name;
        TextView mac;
        public MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.call_sign_name);
            mac = v.findViewById(R.id.call_sign_serial_number);
        }
    }
}
