package com.maatayim.acceleradio.chat;

import com.maatayim.acceleradio.R;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MessageHolder extends RecyclerView.ViewHolder{

	TextView ownerName;
	TextView message;
	public MessageHolder(View itemView) {
		super(itemView);
		ownerName = (TextView) itemView.findViewById(R.id.owner_name);
		message = (TextView) itemView.findViewById(R.id.message);
	}

}
