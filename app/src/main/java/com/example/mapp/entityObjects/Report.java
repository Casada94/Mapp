package com.example.mapp.entityObjects;

import java.sql.Time;
import java.util.Date;

public class Report {
	private boolean isSolved;
	private String description;
	private String reason;
	private String facility;
//	private Date reportTime;
//	private Date solvedTime;

	public Report() {}
//	public Report()
//	{
//		isSolved = false;
//		description = "";
//		reason = "";
//		facility = null;
//		reportTime = new Date();
//		solvedTime = null;
//	}
	
	public Report(String reason, String description, String facility)
	{
		isSolved = false;
		this.description = description;
		this.reason = reason;
		this.facility = facility;
//		reportTime = new Date();
//		solvedTime = null;
	}

	public boolean isSolved()
	{
		return isSolved;
	}
//	public void solve()
//	{
//		isSolved = true;
//		solvedTime = new Date();
//	}
	public String getDescription()
	{
		return description;
	}
	public String getReason() {return reason;}
	public String getFacility()
	{
		return facility;
	}
//	public Date getReportTime()
//	{
//		return reportTime;
//	}
//	public Date getSolvedTime()
//	{
//		return solvedTime;
//	}
}
