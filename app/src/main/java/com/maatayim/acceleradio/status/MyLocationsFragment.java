package com.maatayim.acceleradio.status;

import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyLocationsFragment extends Fragment {
	
	private static final String TAG = "200apps.MyLocationsFragment";
	private static LocationItemAdapter locationAdapter;
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.locations_fragment, container, false);
		
		initTitle(view);
		initStatusDataLocations(view);
		Log.d(TAG, "onCreateView "+Prefs.getInstance(getActivity()).getMyStatusLocations());
		return view;
	}
	
	
	private void initTitle(View view) {
		((TextView) view.findViewById(R.id.serial_number))
		.setText(R.string.serial_number_headline);
		
		((TextView) view.findViewById(R.id.name))
		.setText(R.string.name_headline);
		
		((TextView) view.findViewById(R.id.mac))
		.setText(R.string.mac_headline);
		
		((TextView) view.findViewById(R.id.location))
		.setText(R.string.location_headline);
		
		((TextView) view.findViewById(R.id.age))
		.setText(R.string.age_headline);
		
		((TextView) view.findViewById(R.id.connectivity))
		.setVisibility(view.GONE);
		
	}


	private void initStatusDataLocations(View view) {
		
		Log.d("myLocationFragment init", ""+Prefs.getInstance(getActivity()).getMyStatusLocations());
		 locationAdapter =
				new LocationItemAdapter(Prefs.getInstance(getActivity()).getMyStatusLocations(), getActivity());
		
		RecyclerView locationView =
				(RecyclerView) view.findViewById(R.id.locationsListView);
		
		locationView.setHasFixedSize(true);
		
		locationView.setLayoutManager(
				new LinearLayoutManager(getActivity().getApplicationContext()));
		
		locationView.setAdapter(locationAdapter);
	}
	
	public static void notifyChanges(){
		if (locationAdapter != null){
			locationAdapter.notifyDataSetChanged();
			Log.d("notifyChanges", "notified!");
		}
	}
	

}
