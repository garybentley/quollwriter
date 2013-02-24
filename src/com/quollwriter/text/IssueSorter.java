package com.quollwriter.text;

import java.util.*;

public class IssueSorter implements Comparator<Issue>
{

    public int compare (Issue o1,
                        Issue o2)
    {

        int o1p = o1.getSentenceStartPosition ().getOffset ();
        int o2p = o2.getSentenceStartPosition ().getOffset ();
        
        if (o1p > o2p)
        {
            
            return 1;
            
        }

        if (o1p < o2p)
        {
            
            return -1;
            
        }

        o1p = o1.getStartWordPosition ();
        o2p = o2.getStartWordPosition ();
        
        if (o1p > o2p)
        {
            
            return 1;
            
        }

        if (o1p < o2p)
        {
            
            return -1;
            
        }

        return 0;

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
