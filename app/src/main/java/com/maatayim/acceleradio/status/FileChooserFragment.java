package com.maatayim.acceleradio.status;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.maatayim.acceleradio.R;
import com.maatayim.acceleradio.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileChooserFragment extends DialogFragment{

	private OnFileSelectedListener mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Create the AlertDialog object and return it
		final FileAdapter adapter = new FileAdapter(getActivity(), new ArrayList<ListEntry>());             
		adapter.getFiles();     
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing here to prevent dismiss after click    
			}
		};
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setAdapter(adapter, clickListener)
		.setNegativeButton("abort", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		builder.setTitle("choose file");
		final AlertDialog theDialog = builder.show();
		theDialog.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path;
				path = adapter.getItem(position).name;
				mCallback.onFileSelected(path);
				theDialog.dismiss();
			}

		});
		return theDialog;
	}

	private boolean isBaseDir(String dir) {
		File folder = new File(dir);
		if (!folder.exists()){
			folder = new File("/");
			if (!folder.exists()){
			}
		}
		File baseDir = new File("/");
		if (folder.equals(baseDir)){
			return true;
		}else{
			return false;
		}
	}

	// Container Activity must implement this interface
	public interface OnFileSelectedListener {
		public void onFileSelected(String file);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try { 
			mCallback = (OnFileSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	class ListEntry {
		public String name;
		public Drawable item ;

		public ListEntry(String name, Drawable item) {
			this.name = name;
			this.item = item;
		}
	}

	class FileAdapter extends ArrayAdapter<ListEntry>{

		//show only files with the suffix FILE_SUFFIX, use "*" to show all files;
		private static final String FILE_SUFFIX = ".gil";

		public FileAdapter(Context context, ArrayList<ListEntry> fileEntry) {
			super(context, R.layout.filechooser_list_item,fileEntry);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			ListEntry entry = getItem(position);    
			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.filechooser_list_item, parent, false);
			}
			// Lookup view for data population
			TextView filechooserEntry = (TextView) convertView.findViewById(R.id.filechooser_entry);
			// Populate the data into the template view using the data object
			filechooserEntry.setText(entry.name);
			filechooserEntry.setCompoundDrawablesWithIntrinsicBounds(entry.item, null, null, null);
			// Return the completed view to render on screen
			return convertView;
		}   

		private FileAdapter getFiles() {
			SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getActivity());
			File Root = FileUtils.getRootDir();
			String baseDir = Root + File.separator + SettingsFragment.EXPORTED_MAPS_DIRECTORY;
			ArrayList<File> files = getFilesInDir(baseDir);

			if (!isBaseDir(options.getString("BaseDir", ""))){
			}

			for (File file : files){            
				if (file.isDirectory()){
				}else{
					if (file.getName().endsWith(FILE_SUFFIX)||FILE_SUFFIX.equals("*")){
						this.add(new ListEntry(file.getName(),getResources().getDrawable(R.drawable.ic_file)));
					}
				}   
			}
			return this;
		}

		private ArrayList<File> getFilesInDir(String dir) {
			File folder = new File(dir);
			if (!folder.exists()){
				folder = new File("/");
				if (!folder.exists()){
				}
			}
			ArrayList<File> fileList;
			if (folder.listFiles()!=null){
				fileList = new ArrayList<File>(Arrays.asList(folder.listFiles()));
			}else{
				fileList = new ArrayList<File>();
			}
			return fileList;
		}
	}
}
