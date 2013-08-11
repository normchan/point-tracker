package normchan.pointTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import normchan.pointTracker.model.Adjustment;
import normchan.pointTracker.model.WeeklyScore;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ChildSelector extends Activity {
	private static final String TAG = ChildSelector.class.getName();
	private String endDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.child_selector);

        this.endDate = (String)getIntent().getSerializableExtra("endDate"); 

        Spinner spinner = (Spinner)findViewById(R.id.reasonSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.parental_misbehaviors, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        
        String[] names = getResources().getStringArray(R.array.children_names);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
        final ListView childrenList = (ListView) findViewById(R.id.childrenList);
        childrenList.setAdapter(adapter);
//		for (int i = 0; i < adapter.getCount(); i++) {
//			CheckedTextView item = (CheckedTextView)childrenList.getChildAt(i);
//			Log.d(TAG, "Item at position "+i+" is "+item);
//			item.setChecked(true);
//		}
        
        childrenList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "onItemClick called with view "+arg1+" position "+arg2+" and id "+arg3);
				((Checkable)arg1).toggle();
			}
			
		});

        ((Button)findViewById(R.id.okButton)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String reason = ((Spinner)findViewById(R.id.reasonSpinner)).getSelectedItem().toString();
				if (reason.equals("--Select One--")) {
					Toast.makeText(ChildSelector.this, "Select a valid reason", Toast.LENGTH_LONG).show();
				} else {
					int scoreValue = 0;
					if (reason.equals("Yelling")) {
						scoreValue = 10;
					} else if (reason.equals("Talking with mouth full")) {
						scoreValue = 5;
					}
					
					PointDBHelper dbHelper = new PointDBHelper(ChildSelector.this);
					for (int i = 0; i < adapter.getCount(); i++) {
						CheckedTextView item = (CheckedTextView)childrenList.getChildAt(i);
						if (scoreValue > 0 && item.isChecked()) {
							String name = adapter.getItem(i);
							Log.d(TAG, "Adjusting score for "+name);
							WeeklyScore score = dbHelper.getScore(name, ChildSelector.this.endDate);
			    			Adjustment adjustment = new Adjustment();
			    			adjustment.setDate(new Date());
			    			adjustment.setScore(score);
			    			adjustment.setAmount(scoreValue);
			    			adjustment.setReason("Caught Mom/Dad "+reason);
			    			adjustment.setNewPointTotal(score.getPoints()+adjustment.getAmount());
			    			dbHelper.addAdjustment(adjustment);
						}
					}

					setResult(RESULT_OK);
					finish();
				}
			}
        	
        });
	}

}
