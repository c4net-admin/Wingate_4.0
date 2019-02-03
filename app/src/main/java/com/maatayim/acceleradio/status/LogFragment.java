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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

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
		ArrayList<Map<String, String>> statusMessages = Prefs.getStatusMessages();
		ValueComparator bvc = new ValueComparator(statusMessages);

		sAdapter = new SimpleAdapter(getActivity(), statusMessages, R.layout.log_item_view ,Prefs.getFrom(), Prefs.getToMessages());
		ListView lvSimple = (ListView) view.findViewById(R.id.statusListView);
		lvSimple.setAdapter(sAdapter);
		lvSimple.setSelection(statusMessages.size()-1);
	}
	
	public static void notifyChanges(){
		if (sAdapter != null){
			sAdapter.notifyDataSetChanged();
		}
	}

	class ValueComparator implements Comparator<String> {
		ArrayList<Map<String, String>> base;

		public ValueComparator(ArrayList<Map<String, String>> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(String a, String b) {
			return base.get(a).compareToIgnoreCase(base.get(b);

		}
	}
}
