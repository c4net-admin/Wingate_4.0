package com.maatayim.acceleradio.status;

import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static com.maatayim.acceleradio.Prefs.ATTRIBUTE_STATUS_TIME;

public class LogFragment extends Fragment {

    private static SimpleAdapter sAdapter;
    private static boolean isActionDowm;
    private static ArrayList<Map<String, String>> statusMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.log_fragment, container, false);
        initStatusDataMessages(view);
        return view;
    }

    private void initStatusDataMessages(View view) {
        statusMessages = new ArrayList<>(Prefs.getStatusMessages());
        sAdapter = new SimpleAdapter(getActivity(), statusMessages, R.layout.log_item_view, Prefs.getFrom(), Prefs.getToMessages());
        ListView lvSimple = (ListView) view.findViewById(R.id.statusListView);
        lvSimple.setAdapter(sAdapter);
        lvSimple.setSelection(statusMessages.size() - 1);
        lvSimple.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isActionDowm = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    isActionDowm = false;
                }
                return false;
            }
        });

    }

    public static void notifyChanges() {
        // dont notify change is on clicked
        if (isActionDowm) return;

        statusMessages = new ArrayList<>(Prefs.getStatusMessages());
        Collections.sort(statusMessages, new ValueComparator());

        if (sAdapter != null) {
            sAdapter.notifyDataSetChanged();

        }
    }

    static class ValueComparator implements Comparator<Map<String, String>> {


        // Note: this comparator imposes orderings that are inconsistent with
        // equals.

        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            return o1.get(ATTRIBUTE_STATUS_TIME).compareToIgnoreCase(o2.get(ATTRIBUTE_STATUS_TIME));
        }
    }
}
