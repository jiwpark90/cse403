package com.example.budgetmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.budgetmanager.api.ApiInterface;
import com.example.budgetmanager.preference.SettingsActivity;
import com.example.budgetmanager.preference.SettingsFragment;

/**
 * Activity which displays list of budgets screen to the user,
 * showing summaries of the budgets.
 *
 * @author Chi Ho coldstar96
 */

public class SummaryActivity extends Activity {
	private final String TAG = "SummaryActivity";

	// UI reference
	private ListView listView;

	private BudgetSummaryAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// set theme based on current preferences
		Utilities.setActivityTheme(this, getApplicationContext());

		super.onCreate(savedInstanceState);

		// inflate view
		setContentView(R.layout.activity_summary);

		// set adapter
		adapter = new BudgetSummaryAdapter(this, R.layout.list_budget_layout,
				Budget.getBudgets());
		Log.d(TAG, "Made the adapter!");

		// set up Entry Logs screen
		listView = (ListView) findViewById(R.id.budget_list);
		listView.setAdapter(adapter);
		Log.d(TAG, "added the adapter!");

		adapter.sort(new BudgetSummaryAdapter.BudgetActiveComparator());
		adapter.notifyDataSetChanged();

		// trick to prevent infinite looping when onResume() is called
		getIntent().setAction("Already created");
	}

	@Override
	protected void onResume() {
		super.onResume();

		String action = getIntent().getAction();
		if (action == null || !action.equals("Already created")) {
			// don't restart if action is present
			Intent intent = new Intent(this, SummaryActivity.class);
			startActivity(intent);
			finish();
		} else {
			// remove the unique action so the next time onResume
			// call will force restart
			getIntent().setAction(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.items, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
		case R.id.menu_settings:
			// take the user to the Settings screen
			Intent settingsIntent = new Intent(SummaryActivity.this,
					SettingsActivity.class);

			// these extras allow SettingsActivity to skip the 'headers'
			// layer, which is unnecessary since we have very few settings
			settingsIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
					SettingsFragment.class.getName());
			settingsIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);

			startActivity(settingsIntent);

			return false;

		case R.id.menu_signout:
			// sign the user out
			// sign the user out
			ApiInterface.getInstance().logOut();
			Intent logOut = new Intent(SummaryActivity.this, LoginActivity.class);
			// Clear the back stack so when you press the back button you will exit the app
			logOut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			// Goes to the login page
			startActivity(logOut);
			return false;
		}

		return true;
	}
}
