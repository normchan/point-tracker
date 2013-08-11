package normchan.pointTracker;

import java.util.Date;

import normchan.pointTracker.model.Adjustment;
import normchan.pointTracker.model.WeeklyScore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PointUpdater extends Activity {
	private final static int ID_DIALOG_CANCEL_CONFIRMATION = 0;
	
	private WeeklyScore score = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.update_points);
        
        this.score = (WeeklyScore)getIntent().getSerializableExtra("score"); 
        ((TextView)findViewById(R.id.weekOfValue)).setText(score.getEndDate());
        ((TextView)findViewById(R.id.childNameValue)).setText(score.getChildName());
        ((TextView)findViewById(R.id.pointsValue)).setText(new Integer(score.getPoints()).toString());

        Spinner spinner = (Spinner)findViewById(R.id.adjustmentSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.adjustments, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        spinner = (Spinner)findViewById(R.id.reasonSpinner);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.reasons, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        ((Button)findViewById(R.id.updateButton)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Adjustment adjustment = new Adjustment();
				String errors = validateForm(adjustment);
				if (errors != null) {
					Toast.makeText(PointUpdater.this, "Update failed: "+errors, Toast.LENGTH_LONG).show();
				} else {
					adjustPoints(adjustment);
					setResult(RESULT_OK);
					finish();
				}
			}
        	
        });

        ((Button)findViewById(R.id.cancelButton)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(ID_DIALOG_CANCEL_CONFIRMATION);
			}
        	
        });
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case ID_DIALOG_CANCEL_CONFIRMATION:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Are you sure you want to cancel? Any changes will be lost.")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   setResult(RESULT_OK);
	    	        	   PointUpdater.this.finish();
	    	           }
	    	       })
	    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   dialog.cancel();
	    	           }
	    	       });
	    	dialog = builder.create();
	    	break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

	private String validateForm(Adjustment adjustment) {
		StringBuffer errors = new StringBuffer();
		adjustment.setDate(new Date());
		if (this.score == null) {
			errors.append("Weekly Score record is null.  ");
		} else {
			adjustment.setScore(this.score);
		}
		String amount = ((Spinner)findViewById(R.id.adjustmentSpinner)).getSelectedItem().toString();
		if (amount.equals("--Select One--")) {
			errors.append("Select a valid adjustment amount.  ");
		} else {
			if (amount.startsWith("+")) {
				amount = amount.substring(1);
			}
			adjustment.setAmount(new Integer(amount).intValue());
		}
		String reason = ((Spinner)findViewById(R.id.reasonSpinner)).getSelectedItem().toString();
		if (reason.equals("--Select One--")) {
			errors.append("Select a valid reason.  ");
		} else {
			adjustment.setReason(reason);
		}
		String activity = ((EditText)findViewById(R.id.activityField)).getText().toString();
		if (activity != null && activity.trim().length() > 0) {
			adjustment.setActivity(activity.trim());
		}
		String notes = ((EditText)findViewById(R.id.notesField)).getText().toString();
		if (notes != null && notes.trim().length() > 0) {
			adjustment.setNotes(notes.trim());
		}
		return errors.length() == 0 ? null : errors.toString();
	}
	
	private void adjustPoints(Adjustment adjustment) {
		adjustment.setNewPointTotal(this.score.getPoints()+adjustment.getAmount());
		PointDBHelper dbHelper = new PointDBHelper(this);
		dbHelper.addAdjustment(adjustment);
		dbHelper.close();
	}
}
