package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class NoteSorter implements Comparator<Note>
{

    public int compare (Note o1,
                        Note o2)
    {

        return o1.getPosition () - o2.getPosition ();

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
