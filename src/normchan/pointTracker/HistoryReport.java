package normchan.pointTracker;

import normchan.pointTracker.model.WeeklyScore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryReport extends Activity {
	int[] views = {
			R.id.childNameValue,
			R.id.weekOfValue,
			R.id.pointsValue
	};

	private PointDBHelper dbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.point_history);
        
        String name = getIntent().getStringExtra("childName");
		this.dbHelper = new PointDBHelper(this);
        Cursor cursor = dbHelper.runHistoryReportQuery(name);
        startManagingCursor(cursor);

        ListView list = (ListView)findViewById(R.id.historyList);
        list.setAdapter(new SimpleCursorAdapter(this, R.layout.point_history_line_item, cursor, PointDBHelper.HISTORY_REPORT_QUERY_COLUMNS, views));
        
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	String child = ((TextView) view.findViewById(R.id.childNameValue)).getText().toString();
            	String week = ((TextView) view.findViewById(R.id.weekOfValue)).getText().toString();
	            WeeklyScore score = dbHelper.getScore(child, week);
				startActivity(new Intent(HistoryReport.this, DetailReport.class).putExtra("score", score));
            }
        });
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null)
			dbHelper.close();
	}
}
