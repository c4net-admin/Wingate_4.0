package com.maatayim.acceleradio.chat;

import java.util.ArrayList;

import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ChatFragment extends Fragment {
	
	private OnSmsSent smsSender;
	
	protected static final String TAG = "200apps.ChatFragment";
	private RecyclerView recyclerView;
	public static LinearLayoutManager layoutManager;
	
	
	public static MessagesAdapter adapter;
	private EditText messagesEditText;
	private ImageView send;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view  = inflater.inflate(R.layout.chat_fragment, container, false);
		initRecycleView(view);
		
		initEditText(view);
		return view;
	}

	private void initEditText(View view) {
		messagesEditText = (EditText) view.findViewById(R.id.message_edit_text);
		send = (ImageView) view.findViewById(R.id.message_send);
	
		messagesEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE){
					addNewMineMessage(messagesEditText.getText().toString());
					messagesEditText.setText("");
				}
				
				return false;
			}
		});
		
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addNewMineMessage(messagesEditText.getText().toString());
				messagesEditText.setText("");
			}
		});
		
	}

	protected void addNewMineMessage(String message) {
//		ChatMessage newMessage = new ChatMessage("", message, true, new Date().getTime()); 
//		adapter.add(newMessage);
//		layoutManager.scrollToPosition(adapter.getItemCount() - 1);
		smsSender.onSmsSent(message);
	}

	private void initRecycleView(View view) {
		
			recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

			// use this setting to improve performance if you know that changes
			// in content do not change the layout size of the RecyclerView
			recyclerView.setHasFixedSize(true);

			// use a linear layout manager
			layoutManager = new LinearLayoutManager(getActivity());
			
			recyclerView.setLayoutManager(layoutManager);			

			
			// specify an adapter (see also next example)
			adapter = new MessagesAdapter(Prefs.messages);



			recyclerView.setAdapter(adapter);
			recyclerView.setItemAnimator(new DefaultItemAnimator());


			layoutManager.scrollToPosition(Prefs.messages.size()-1);
		
	}

	

	@Override
	public void onAttach(Activity a) {
	    super.onAttach(a);
	    smsSender = (OnSmsSent) a;
	}
	
	public interface OnSmsSent {
	    public void onSmsSent(String data);
	}

}
