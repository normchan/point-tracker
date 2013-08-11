package normchan.pointTracker.model;

import java.util.Date;

public class Adjustment {
	private Date date;
	private WeeklyScore score;
	private int amount;
	private String activity;
	private String reason;
	private String notes;
	private int newPointTotal;
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public WeeklyScore getScore() {
		return score;
	}
	public void setScore(WeeklyScore score) {
		this.score = score;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getNewPointTotal() {
		return newPointTotal;
	}
	public void setNewPointTotal(int newPointTotal) {
		this.newPointTotal = newPointTotal;
	}
}
