package com.maatayim.acceleradio.status;

import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class LogFragment extends Fragment {
	
	private static SimpleAdapter sAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.log_fragment, container, false);
		initStatusDataMessages(view);
		return view;
	}
	
	private void initStatusDataMessages(View view) {
		sAdapter = new SimpleAdapter(getActivity(), Prefs.getStatusMessages(), R.layout.log_item_view ,Prefs.getFrom(), Prefs.getToMessages());
		ListView lvSimple = (ListView) view.findViewById(R.id.statusListView);
		lvSimple.setAdapter(sAdapter);
		lvSimple.setSelection(Prefs.getStatusMessages().size()-1);
	}
	
	public static void notifyChanges(){
		if (sAdapter != null){
			sAdapter.notifyDataSetChanged();
		}
	}
}
