package com.maatayim.acceleradio.chat;

import java.util.List;



import com.maatayim.acceleradio.R;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MessagesAdapter extends RecyclerView.Adapter<MessageHolder>{
	
	private static final int VIEW_TYPE_MINE = 0;
	private static final int VIEW_TYPE_THEIR = 1;
	private List<ChatMessage> messages;
	
	public MessagesAdapter(List<ChatMessage> messages){
		this.messages = messages;
	}
	
	@Override
	public int getItemCount() {
		
		return messages.size();
	}
	@Override
	public void onBindViewHolder(MessageHolder holder, int position) {
		
		holder.ownerName.setText(messages.get(position).getOwnerName());
		holder.message.setText(messages.get(position).getMessage());
		
	}
	@Override
	public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		int viewRes = viewType == VIEW_TYPE_MINE?
				R.layout.mine_message :
				R.layout.their_message;
		 View v = LayoutInflater.from(parent.getContext())
                 .inflate(viewRes, parent, false);
		 
		return new MessageHolder(v);
	}
	
	@Override
	public int getItemViewType(int position) {
		return messages.get(position).isMine()?
				VIEW_TYPE_MINE:
					VIEW_TYPE_THEIR;
	}
	
	public void add(ChatMessage message){
		messages.add(message);
		notifyItemInserted(messages.size() - 1);
	}

}
