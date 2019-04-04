package com.maatayim.acceleradio.status;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;

class LocationItemHolder extends RecyclerView.ViewHolder{
	
	TextView serialNumber;
	TextView name;
	TextView mac;
	TextView location;
	TextView age;
	TextView connectivity;

	public LocationItemHolder(View itemView) {
		super(itemView);
		serialNumber = (TextView) itemView.findViewById(R.id.serial_number);
		name = (TextView) itemView.findViewById(R.id.name);
		mac = (TextView) itemView.findViewById(R.id.mac);
		location = (TextView) itemView.findViewById(R.id.location);
		age = (TextView) itemView.findViewById(R.id.age);
		connectivity = (TextView) itemView.findViewById(R.id.connectivity);
	}
}
	
	

public class LocationItemAdapter extends RecyclerView.Adapter<LocationItemHolder> {
	
	private Context context;
	
	private ArrayList<Map<String, String>> itemsList;
	
	public LocationItemAdapter(ArrayList<Map<String, String>> list, Context context) {
		Log.d("LocationItemAdapter", "LocationItemAdapter");
		Log.d("constructor,", ""+list);
		itemsList = list;
		this.context = context;
	}

	@Override
	public int getItemCount() {
		Log.d("LocationItemAdapter", "getItemCount" + itemsList.size() + " instance " + itemsList);

		return itemsList.size();
	}


	@Override
	public void onBindViewHolder(LocationItemHolder holder, int position) {
		Log.d("LocationItemAdapter", "onBindViewHolder");
		String[] data = itemsList.get(position).get(Prefs.ATTRIBUTE_STATUS_TEXT).split(",");
		holder.serialNumber.setText(String.valueOf(position));
		holder.name.setText(itemsList.get(position).get(Prefs.ATTRIBUTE_MARKER_NAME));
		holder.mac.setText(data[2] + ":" + data[3]);
		holder.location.setText(data[5] + "," + data[6]);
		holder.age.setText(itemsList.get(position).get(Prefs.ATTRIBUTE_AGE));
		holder.connectivity.setText(data[8]);
		if(data[8].equals("-")) holder.connectivity.setVisibility(View.GONE);
		
	}

	@Override
	public LocationItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Log.d("LocationItemAdapter", "onCreateViewHolder");

		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.locations_item_view, parent, false);
		return new LocationItemHolder(v);
	}

	

}
