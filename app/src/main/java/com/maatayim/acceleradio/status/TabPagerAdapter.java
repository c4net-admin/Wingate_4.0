package com.maatayim.acceleradio.status;


import com.maatayim.acceleradio.R;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;



public class TabPagerAdapter extends FragmentPagerAdapter {


	private Fragment[] fragments;

	private Context context;

	public static final int LOG_FRAGMENT_INDEX = 0;

	public static final int LOCATIONS1_FRAGMENT_INDEX = 1;

	public static final int LOCATIONS2_FRAGMENT_INDEX = 2;
	
	public static final int SETTINGS_FRAGMENT_INDEX = 3;

	public static final int TABS = 4;
	
	static final int[] TAB_TITLES = {
		R.string.log,
		R.string.locations1,
		R.string.locations2,
		R.string.settings
	};





	public TabPagerAdapter(FragmentManager fm, Context context) {	
		super(fm);
		this.context = context;
		initFragments();
	}

	private void initFragments() {

		fragments = new Fragment[TABS];

		fragments[LOG_FRAGMENT_INDEX] = new LogFragment();
		fragments[LOCATIONS1_FRAGMENT_INDEX] = new MyLocationsFragment();
		fragments[LOCATIONS2_FRAGMENT_INDEX] = new TheirLocationsFragment();
		fragments[SETTINGS_FRAGMENT_INDEX] = new SettingsFragment();



	}

	/**
	 * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
	 * <p>
	 * Here we return the value returned from {@link SamplePagerItem#createFragment()}.
	 */
	@Override
	public Fragment getItem(int i) {
		return fragments[i];
	}

	@Override
	public int getCount() {
		return TABS;
	}
	// BEGIN_INCLUDE (pageradapter_getpagetitle)
	/**
	 * Return the title of the item at {@code position}. This is important as what this method
	 * returns is what is displayed in the {@link SlidingTabLayout}.
	 * <p>
	 * Here we return the value returned from {@link SamplePagerItem#getTitle()}.
	 */
	@Override
	public CharSequence getPageTitle(int position) {
		return context.getString(TAB_TITLES[position]);
	}
	// END_INCLUDE (pageradapter_getpagetitle)

}
