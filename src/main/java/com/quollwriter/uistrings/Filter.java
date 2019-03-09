package com.quollwriter.uistrings;

public interface Filter<E extends Node>
{

    public boolean accept (E n);

}
