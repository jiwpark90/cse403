package com.example.budgetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.budgetmanager.Budget.Duration;

import org.joda.time.LocalDate;

/**
 * Creates a template activity for the {@link AddBudgetActivity} and
 * {@link EditBudgetActivity} activities
 *
 * @author Chris brucec5
 */
public abstract class AbstractBudgetEditorActivity extends UBudgetActivity {
	private static final String TAG = "AbstractBudgetEditorActivity";

	protected final int DOLLAR_IN_CENTS = 100;

	// Text field for entering the Budget name
	protected EditText mBudgetNameView;
	protected String mBudgetName;

	// Number field for entering the Budget amount
	protected EditText mBudgetAmountView;
	protected String mBudgetAmount;

	// Enables the user to pick the start date of the Budget
	protected DatePicker mBudgetDateView;

	// Enable the user to pick a number of different durations for the Budget
	protected Spinner mBudgetDurationView;

	// Whether or not this Budget should recur after one cycle
	protected CheckBox mRecurringView;

	// Submit button
	protected Button mAddButtonView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// set theme based on current preferences
		Utilities.setActivityTheme(this, getApplicationContext());

		super.onCreate(savedInstanceState);

		// inflate view
		setContentView(R.layout.activity_budget_editor);

		mBudgetNameView = (EditText) findViewById(R.id.budget_name);
		mBudgetAmountView = (EditText) findViewById(R.id.budget_amount);
		// set currency filter
		mBudgetAmountView.setFilters(new InputFilter[] { new CurrencyInputFilter() });
		mBudgetDateView = (DatePicker) findViewById(R.id.budget_date);
		mRecurringView = (CheckBox) findViewById(R.id.budget_recur);
		mAddButtonView = (Button) findViewById(R.id.create_budget_button);

		// Sets up the duration dropdown
		mBudgetDurationView = (Spinner) findViewById(R.id.budget_duration);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.duration_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mBudgetDurationView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		String action = getIntent().getAction();
		if (action == null || !action.equals("Already created")) {
			// don't restart if action is present
			Intent intent = new Intent(this, AddBudgetActivity.class);
			startActivity(intent);
			finish();
		} else {
			// remove the unique action so the next time onResume
			// call will force restart
			getIntent().setAction(null);
		}
	}

	/**
	 * Perform common validations of fields. Sets the errors on the fields
	 * accordingly.
	 *
	 * @return true if all fields are good, false if there exists an error.
	 */
	public boolean commonValidations() {
		// check input validity
		boolean ok = true;
		View focusView = null;
		double amount = 0.0;

		mBudgetAmountView.setError(null);
		mBudgetNameView.setError(null);

		mBudgetAmount = mBudgetAmountView.getText().toString();
		mBudgetName = mBudgetNameView.getText().toString();

		// checks whether amount is not empty
		if (mBudgetAmount.isEmpty()) {
			mBudgetAmountView.setError(getString(R.string.error_invalid_amount));
			focusView = mBudgetAmountView;
			ok = false;
		} else {
			// Only attempt to parse the amount if it's non-empty.
			amount = Double.parseDouble(mBudgetAmountView.getText().toString());
		}

		// checks whether the amount is non-zero
		if (ok && amount == 0.0) {
			mBudgetAmountView.setError(getString(R.string.error_zero_amount));
			mBudgetAmountView.requestFocus();
			focusView = mBudgetAmountView;
			ok = false;
		} else {
			Log.i(TAG, "Amount was " + amount);
		}

		// Checks whether the name is empty.
		if (mBudgetName.isEmpty()) {
			mBudgetNameView.setError(getString(R.string.error_invalid_budget_name));
			focusView = mBudgetNameView;
			ok = false;
		}

		// Focus the view that has problems, if problems exist.
		if (!ok) {
			focusView.requestFocus();
		}

		return ok;
	}

	/**
	 * Creates a <code>Budget</code> based on the contents of the input fields.
	 *
	 * @return a <code>Budget</code> with values specified by the input fields.
	 */
	protected Budget createBudget() {
		String name = mBudgetNameView.getText().toString();

		// Multiply by 100 in order to convert the amount to cents for storage
		String amountText = mBudgetAmountView.getText().toString();
		int amount =  (int) Math.round(Double.parseDouble(amountText) * DOLLAR_IN_CENTS);
		boolean recur = mRecurringView.isChecked();

		LocalDate startDate = new LocalDate(mBudgetDateView.getYear(),
				mBudgetDateView.getMonth() + 1, mBudgetDateView.getDayOfMonth());

		Duration duration;
		int index = mBudgetDurationView.getSelectedItemPosition();
		switch(index) {
		case 0:
			duration = Duration.DAY;
			break;
		case 1:
			duration = Duration.WEEK;
			break;
		case 2:
			duration = Duration.FORTNIGHT;
			break;
		case 3:
			duration = Duration.MONTH;
			break;
		case 4:
			duration = Duration.YEAR;
			break;
		default:
			throw new IllegalArgumentException("Invaid duration argument");
		}

		return new Budget(name, amount, recur,
				startDate, duration);
	}

	protected boolean nameIsUnique() {
		boolean isUnique = true;
		for (Budget budget : Budget.getBudgets()) {
			if (budget.getName().equals(mBudgetName)) {
				mBudgetNameView.setError(getString(
						R.string.error_name_already_exists));
				mBudgetNameView.requestFocus();
				isUnique = false;
				break;
			}
		}

		return isUnique;
	}

	/**
	 * Resets the add budget view.
	 *
	 * @param view The reference to the clear button.
	 */
	public void clearBudget(View view) {
		mBudgetAmountView.setError(null);
		mBudgetNameView.setError(null);

		// clear the EditText fields
		mBudgetAmountView.setText("");
		mBudgetNameView.setText("");

		// get current time
		LocalDate now = LocalDate.now();
		// update the DatePicker
		mBudgetDateView.updateDate(now.getYear(),
				now.getMonthOfYear() - 1,
				now.getDayOfMonth());

		mRecurringView.setChecked(false);

		mBudgetDurationView.setSelection(0);
	}
}
