package com.quollwriter.uistrings;

public interface RefValueProvider
{

    public String getString (String id);

    public String getRawText (String id);

    public int getSCount (String id);

    public boolean isIdValid (String id);

}
