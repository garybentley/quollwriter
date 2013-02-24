package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class IdeaTypeComparator implements Comparator<Idea>
{

    public static final String SORT_BY_RATING = "rating";
    public static final String SORT_BY_DATE = "lastModified";
    public static final String SORT_BY_TEXT = "description";

    private String sortBy = null;
    
    public IdeaTypeComparator (String sortBy)
    {
        
        this.sortBy = sortBy;

        if (this.sortBy == null)
        {
            
            this.sortBy = SORT_BY_RATING;
            
        }
        
    }
    
    public int compare (Idea i1,
                        Idea i2)
    {

        if (this.sortBy.equals (SORT_BY_RATING))
        {
            
            return i2.getRating () - i1.getRating ();
            
        }

        if (this.sortBy.equals (SORT_BY_TEXT))
        {
            
            return i1.getDescription ().compareTo (i2.getDescription ());
            
        }

        return (int) (i2.getLastModified ().getTime () - i1.getLastModified ().getTime ());

    }

    public boolean equals (Object o)
    {

        return this == o;

    }
    
    
}
