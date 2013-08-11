package normchan.pointTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import normchan.pointTracker.model.WeeklyScore;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PointTracker extends Activity {
	private static final String TAG = PointTracker.class.getName();
	
	private static final int UPDATE_POINTS_REQUEST = 0;
	
	private PointDBHelper dbHelper = null;
	private String[] names;
	private String week;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        ((TextView)findViewById(R.id.weekOfValue)).setText(SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(cal.getTime()));
        this.week = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        
        this.names = getResources().getStringArray(R.array.children_names);
        this.dbHelper = new PointDBHelper(this, names);
    	dbHelper.initializeWeeklyScores(week);

        ListView listView = (ListView)findViewById(R.id.pointListView);
    	populateList(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	String itemText = ((TextView) view).getText().toString();
	            WeeklyScore score = dbHelper.getScore(itemText.split(":")[0], week);
	            startActivityForResult(new Intent(PointTracker.this, PointUpdater.class).putExtra("score", score), UPDATE_POINTS_REQUEST);
            }
        });
        
        registerForContextMenu(listView);
    }
    
    private void populateList(ListView listView) {
        String[] currentScores = names.clone();
        for (int i=0; i < currentScores.length; i++) {
        	int score = dbHelper.getScore(currentScores[i], week).getPoints();
        	currentScores[i] += ": "+score;
        }
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currentScores));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.historyItem:
			startActivity(new Intent(PointTracker.this, HistoryReport.class));
    		break;
    	case R.id.applyMultipleItem:
			startActivityForResult(new Intent(PointTracker.this, ChildSelector.class).putExtra("endDate", this.week), UPDATE_POINTS_REQUEST);
    		break;
		default:
			return super.onOptionsItemSelected(item);
    	}

    	return true;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		  AdapterView.AdapterContextMenuInfo menuInfo = null;
		  switch (item.getItemId()) {
		  case R.id.detailsItem:
			  menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			  WeeklyScore score = dbHelper.getScore(names[menuInfo.position], week);
			  startActivity(new Intent(PointTracker.this, DetailReport.class).putExtra("score", score));
			  return true;
		  case R.id.historyItem:
			  menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			  startActivity(new Intent(PointTracker.this, HistoryReport.class).putExtra("childName", names[menuInfo.position]));
			  return true;
		  default:
			  return super.onContextItemSelected(item);
		  }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult called in main activity.");
		if (requestCode == UPDATE_POINTS_REQUEST) {
	        ListView listView = (ListView)findViewById(R.id.pointListView);
	    	populateList(listView);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null)
			dbHelper.close();

	}
}