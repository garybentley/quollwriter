package com.quollwriter.text;

import java.util.*;

public class IssueSorter implements Comparator<Issue>
{

    public int compare (Issue o1,
                        Issue o2)
    {

        int o1p = o1.getStartPosition ().getOffset ();
        int o2p = o2.getStartPosition ().getOffset ();
        
        if (o1p > o2p)
        {
            
            return 1;
            
        }

        if (o1p < o2p)
        {
            
            return -1;
            
        }

        o1p = o1.getStartIssuePosition ();
        o2p = o2.getStartIssuePosition ();
        
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
