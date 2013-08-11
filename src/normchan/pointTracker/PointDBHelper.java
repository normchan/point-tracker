package normchan.pointTracker;

import java.text.SimpleDateFormat;

import normchan.pointTracker.model.Adjustment;
import normchan.pointTracker.model.WeeklyScore;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PointDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "points";
    private static final String CHILDREN_TABLE_CREATE = "CREATE TABLE children (id integer primary key autoincrement, name text not null unique)";
    private static final String SCORES_TABLE_CREATE = "create table scores (id integer primary key autoincrement, " +
    	"child_id not null references children(id), end_date integer not null, point_total integer not null default 100)"; 
    private static final String SCORES_TABLE_QUERY = "select id from children c where not exists (select 1 from scores "+
    	"where child_id = c.id and end_date = ?)";
    private static final String ADJUSTMENTS_TABLE_CREATE = "create table adjustments (id integer primary key autoincrement, " +
    	"score_id not null references scores(id), date integer not null, amount integer not null, activity text, " +
    	"reason text not null, notes text)";
    private static final String CHILD_SCORE_ID_QUERY = "select s.id, s.child_id, c.name, s.point_total from children c, scores s " +
    	"where c.id = s.child_id and c.name = ? and s.end_date = ?";

    private static final String DETAILED_REPORT_QUERY = "select a.id as _id, strftime('%Y-%m-%d %H:%M', a.date) as date, a.activity, a.reason, a.amount, a.notes " +
    	"from children c, scores s, adjustments a where c.id = s.child_id and s.id = a.score_id and s.id = ? order by date asc";
    static final String[] DETAILED_REPORT_QUERY_COLUMNS = { 
    	"date",
    	"activity",
    	"reason",
    	"amount",
    	"notes"};
    
    private static final String HISTORY_REPORT_BY_CHILD_QUERY = "select s.id as _id, c.name, s.end_date, s.point_total " +
		"from children c, scores s where c.id = s.child_id and c.name = ? order by end_date desc, c.id asc";
    private static final String HISTORY_REPORT_QUERY = "select s.id as _id, c.name, s.end_date, s.point_total " +
		"from children c, scores s where c.id = s.child_id order by end_date desc, c.id asc";
	static final String[] HISTORY_REPORT_QUERY_COLUMNS = { 
		"name",
		"end_date",
		"point_total"};

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String[] names;
    private final SQLiteDatabase database;
    
    public PointDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.names = null;
		this.database = getWritableDatabase();
    }

    public PointDBHelper(Context context, String[] names) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.names = names;
		this.database = getWritableDatabase();
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(CHILDREN_TABLE_CREATE);
        db.execSQL(SCORES_TABLE_CREATE);
        db.execSQL(ADJUSTMENTS_TABLE_CREATE);
        if (this.names != null) {
            for (String name : this.names) {
            	ContentValues vals = new ContentValues();
            	vals.put("name", name);
            	db.insert("children", null, vals);
            }
        }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void initializeWeeklyScores(String week) {
		String[] args = new String[1];
		args[0] = week;
		Cursor cursor = database.rawQuery(SCORES_TABLE_QUERY, args);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
            	ContentValues vals = new ContentValues();
            	vals.put("end_date", week);
            	vals.put("child_id", cursor.getString(0));
            	database.insert("scores", null, vals);
				cursor.moveToNext();
			}
		}
		
		cursor.close();
	}
	
	public WeeklyScore getScore(String childName, String week) {
		String[] args = new String[2];
		args[0] = childName;
		args[1] = week;
		Cursor cursor = database.rawQuery(CHILD_SCORE_ID_QUERY, args);
		cursor.moveToFirst();
		
		WeeklyScore score = new WeeklyScore();
		score.setScoreId(cursor.getInt(0));
		score.setChildId(cursor.getInt(1));
		score.setChildName(cursor.getString(2));
		score.setEndDate(week);
		score.setPoints(cursor.getInt(3));
		cursor.close();
		
		return score;
	}
	
	public void addAdjustment(Adjustment adjustment) {
    	ContentValues vals = new ContentValues();
    	vals.put("score_id", adjustment.getScore().getScoreId());
    	vals.put("date", dateFormat.format(adjustment.getDate()));
    	vals.put("amount", adjustment.getAmount());
    	vals.put("activity", adjustment.getActivity());
    	vals.put("reason", adjustment.getReason());
    	vals.put("notes", adjustment.getNotes());
    	if (database.insert("adjustments", null, vals) == -1)
    		throw new RuntimeException("failed to created adjustment");
    	
    	vals = new ContentValues();
    	vals.put("point_total", adjustment.getNewPointTotal());
    	database.update("scores", vals, "id = ?", new String[]{new Integer(adjustment.getScore().getScoreId()).toString()});
	}
	
	public Cursor runDetailedReportQuery(int scoreId) {
		String[] args = new String[1];
		args[0] = new Integer(scoreId).toString();
		return database.rawQuery(DETAILED_REPORT_QUERY, args);
	}
	
	public Cursor runHistoryReportQuery(String childName) {
		if (childName == null) {
			return database.rawQuery(HISTORY_REPORT_QUERY, null);
		} else {
			String[] args = new String[1];
			args[0] = childName;
			return database.rawQuery(HISTORY_REPORT_BY_CHILD_QUERY, args);
		}
	}
}
