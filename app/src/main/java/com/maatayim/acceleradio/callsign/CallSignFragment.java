package com.maatayim.acceleradio.callsign;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;
import com.maatayim.acceleradio.status.LocationItemAdapter;

import java.util.ArrayList;

public class CallSignFragment extends Fragment {
	
	private  CallSignAdapter callSignAdapter;
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.call_sign_fragment, container, false);
		
		initTitle(view);
		initStatusDataLocations(view);
		return view;
	}
	
	
	private void initTitle(View view) {
		((TextView) view.findViewById(R.id.call_sign_name))
		.setText("Name");
		
		((TextView) view.findViewById(R.id.call_sign_serial_number))
		.setText("Mac");
	}


	private void initStatusDataLocations(View view) {

		ArrayList<CallSign> callSigns = Prefs.getInstance().getCallSigns();


		callSignAdapter =
				new CallSignAdapter(callSigns , getActivity());
		
		RecyclerView callSignList =
				(RecyclerView) view.findViewById(R.id.call_sign_list);
		
		callSignList.setHasFixedSize(true);
		
		callSignList.setLayoutManager(
				new LinearLayoutManager(getActivity().getApplicationContext()));
		
		callSignList.setAdapter(callSignAdapter);
	}

		
	
	

}
