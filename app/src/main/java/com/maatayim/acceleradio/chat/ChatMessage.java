package com.maatayim.acceleradio.chat;

public class ChatMessage {

	private String ownerName;
	
	private String message;
	
	private boolean isMine;
	
	private long date;

	public ChatMessage(String ownerName, String message, boolean isMine, long date) {
		super();
		this.ownerName = ownerName;
		this.message = message;
		this.isMine = isMine;
		this.date = date;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isMine() {
		return isMine;
	}

	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	public String toString(){
		return String.valueOf(date) + ":" + String.valueOf(isMine) + ":" + ownerName + ":" + message;
	}
	
	
}
