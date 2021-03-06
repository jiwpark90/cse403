package com.example.budgetmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.budgetmanager.api.ApiCallback;
import com.example.budgetmanager.api.ApiInterface;

/**
 * Activity which allows users to add entries.
 *
 * @author Ji jiwpark90
 * @author Graham grahamb5
 */
public class AddEntryActivity extends AbstractEntryEditorActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.title_activity_add_entry));
		mAddButtonView.setText(getString(R.string.entry_activity_button_add));
	}

	/**
	 * Adds a new <code>Entry</code> to the specified <code>Budget</code>.
	 *
	 * @param view The reference to the add button.
	 */
	@Override
	public void addEntry(View view) {
		final Entry newEntry = createEntry();

		if (newEntry == null) {
			// do nothing until add Budget activity is up
			// or the user fixes their errors.
			return;
		}

		ApiInterface.getInstance().create(newEntry, new ApiCallback<Long>() {
			@Override
			public void onSuccess(Long result) {
				Budget b = newEntry.getBudget();
				b.addEntry(newEntry);
				// clear the fields if the add was successful.
				// passes a null since the method doesn't need
				// a reference to a view object to work.
				AddEntryActivity.this.clearEntry(null);
				// goto logs screen
				finish();
			}

			@Override
			public void onFailure(String errorMessage) {
				// if the request fails, do nothing (the toast is for testing purposes)
				Toast.makeText(AddEntryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
				mAddButtonView.setClickable(true);
			}
		});
	}

	// Helper method to create the new <code>Entry</code> object to be added.
	@Override
	protected Entry createEntry() {
		return super.createEntry();
	}
}
