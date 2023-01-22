package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class ProjectInfoSorter implements Comparator<ProjectInfo>
{

    public int compare (ProjectInfo o1,
                        ProjectInfo o2)
    {

        Date o1D = (o1.getLastEdited () != null ? o1.getLastEdited () : o1.getDateCreated ());
        Date o2D = (o2.getLastEdited () != null ? o2.getLastEdited () : o2.getDateCreated ());

        if (o2D == null)
        {

            return -1;

        }

        if (o1D == null)
        {

            return 1;

        }

        if (o1D.compareTo (o2D) == 0)
        {
            
            return o1.getName ().compareTo (o2.getName ());
            
        }
        
        return -1 * o1D.compareTo (o2D);

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
