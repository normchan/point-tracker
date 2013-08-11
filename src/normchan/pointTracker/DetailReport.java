package normchan.pointTracker;

import normchan.pointTracker.model.WeeklyScore;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class DetailReport extends Activity {
	int[] views = {
			R.id.item_adj_date,
			R.id.item_adj_activity,
			R.id.item_adj_reason,
			R.id.item_adj_amount,
			R.id.item_adj_notes
	};
	
	private PointDBHelper dbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.point_details);
        
        WeeklyScore score = (WeeklyScore)getIntent().getSerializableExtra("score"); 
		this.dbHelper = new PointDBHelper(this);
        Cursor cursor = dbHelper.runDetailedReportQuery(score.getScoreId());
        startManagingCursor(cursor);

        ((TextView)findViewById(R.id.childNameValue)).setText(score.getChildName());
        ((TextView)findViewById(R.id.weekOfValue)).setText(score.getEndDate());

        ListView grid = (ListView)findViewById(R.id.detailsGrid);
        grid.setAdapter(new SimpleCursorAdapter(this, R.layout.point_details_line_item, cursor, PointDBHelper.DETAILED_REPORT_QUERY_COLUMNS, views));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null)
			dbHelper.close();

	}
}
