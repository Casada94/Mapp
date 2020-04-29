package com.example.mapp.entityObjects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
	public Building(String str, double x, double y)
	{
		super(str, x, y);
		availability = true;
	}
	public Building(String name, double x, double y, ArrayList<String> neighbors, ArrayList<String> utilities)
	{
		super(name, x, y, neighbors, utilities);
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

	@Override
	public String toString() {
		return super.toString() + availability;
	}
}
