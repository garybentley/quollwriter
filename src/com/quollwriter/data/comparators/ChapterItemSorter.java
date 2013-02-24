package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class ChapterItemSorter implements Comparator<ChapterItem>
{

    public int compare (ChapterItem o1,
                        ChapterItem o2)
    {

        if (o1.getKey () == null)
        {
            
            return 1;
            
        }

        if (o2.getKey () == null)
        {
            
            return 1;
            
        }
    
        if (o1.getPosition () == o2.getPosition ())
        {
            
            return (int) (o1.getKey () - o2.getKey ());
            
        }
    
        return o1.getPosition () - o2.getPosition ();

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
