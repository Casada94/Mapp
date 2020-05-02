package com.example.mapp.entityObjects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Building extends point{
	private boolean availability;
	private String name;
	public Building()
	{
		super();
		availability = true;
		this.name = "";
	}
	public Building(String abbr)
	{
		super(abbr);
		availability = true;
		this.name = "";

	}
	public Building(String abbr, double x, double y)
	{
		super(abbr, x, y);
		availability = true;
	}
	public Building(String abbr, double x, double y, ArrayList<String> neighbors, ArrayList<String> utilities)
	{
		super(abbr, x, y, neighbors, utilities);
		availability = true;
		this.name = "";
	}
	public Building(String abbr, double x, double y, ArrayList<String> neighbors, ArrayList<String> utilities, String name)
	{
		super(abbr, x, y, neighbors, utilities);
		availability = true;
		this.name = name;
	}

	public boolean getAvailability()
	{
		return availability;
	}

	public void setAvailability(boolean availability)
	{
		this.availability = availability;
	}

	public String getName() {return name;}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString() {
		return super.toString() + availability;
	}
}
