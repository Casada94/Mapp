package com.example.mapp.entityObjects;

public class Building extends point{
	private boolean availability;
	public Building()
	{
		super();
		availability = true;
	}
	public Building(String str)
	{
		super(str);
		availability = true;
	}

	public boolean getAvailability()
	{
		return availability;
	}

	public void setAvailability(boolean availability)
	{
		this.availability = availability;
	}
}
