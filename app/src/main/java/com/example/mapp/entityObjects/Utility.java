package com.example.mapp.entityObjects;

import java.util.ArrayList;

public class Utility extends Facility{
	enum Type
	{
		VENDING_MACHINE, BATHROOM, WATER_FOUNTAIN;
	}
	private Type type;
	private ArrayList<String> contents;

	public Utility(Type type)
	{
		super();
		this.type = type;
		contents = new ArrayList<String>();
	}
	public void addContent(String content)
	{
		contents.add(content);
	}
	public ArrayList<String> getContents()
	{
		return contents;
	}
	public Type getType()
	{
		return type;
	}
}
