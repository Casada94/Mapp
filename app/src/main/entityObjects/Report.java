package entityObjects;

import java.sql.Time;
import java.util.Date;

public class Report {
	private boolean isSolved;
	private String reportDetails;
	private Facility facility;
	private Date reportTime;
	private Date solvedTime;
	
	public Report()
	{
		isSolved = false; 
		reportDetails = "";
		facility = null;
		reportTime = new Date();
		solvedTime = null;
	}
	
	public Report(String reportDetails, Facility facility)
	{
		isSolved = false;
		this.reportDetails = reportDetails;
		this.facility = facility;
		reportTime = new Date();
		solvedTime = null;
	}
	
	public boolean isSolved()
	{
		return isSolved;
	}
	public void solve()
	{
		isSolved = true;
		solvedTime = new Date();
	}
	public String getDetails()
	{
		return reportDetails;
	}
	public Facility getFacility()
	{
		return facility;
	}
	public Date getReportTime()
	{
		return reportTime;
	}
	public Date getSolvedTime()
	{
		return solvedTime;
	}
}
