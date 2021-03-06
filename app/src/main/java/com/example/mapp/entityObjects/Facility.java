package com.example.mapp.entityObjects;

import java.sql.Time;
import java.time.LocalTime;

public abstract class Facility{
	private String name;
	private boolean availability;
	private LocalTime openTime;
	private LocalTime closeTime;
	
	public String getName()
	{
		return name;
	}
	public boolean getAvailability()
	{
		return availability;
	}
	public LocalTime getOpenTime()
	{
		return openTime;
	}
	public LocalTime getCloseTime()
	{
		return closeTime;
	}
	public boolean isOpen()
	{
		boolean ret = false;
		if(availability)
		{
			LocalTime now = LocalTime.now();
			if(now.isAfter(openTime) && now.isBefore(closeTime))
				ret = true;
		}
		return ret;
	}
	public Report report(String Reason, String reportDetails)
	{
		return new Report(Reason, reportDetails, this.getName());
	}
}
