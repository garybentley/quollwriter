package com.quollwriter.data;

import java.util.*;

public class DateRange implements Comparable<DateRange>
{
    
    private Date from = null;
    private Date to = null;
    
    public DateRange (Date from,
                      Date to)
    {

        this.from = from;
        this.to = to;
        
        if ((this.from != null)
            &&
            (this.to != null)
           )
        {
            
            if (this.from.after (this.to))
            {
                
                throw new IllegalArgumentException ("From: " +
                                                    from +
                                                    " is after to: " +
                                                    to);
                
            }
            
        }
        
        if ((this.from == null)
            &&
            (this.to == null)
           )
        {
            
            throw new IllegalArgumentException ("From and to are null");
            
        }
        
    }
    
    private long getCompareValue ()
    {
        
        Date d = (this.from != null ? this.from : this.to);
        
        if (d == null)
        {
            
            return 0;
            
        } else {
            
            return d.getTime ();
            
        }
        
    }
    
    public boolean contains (Date d)
    {
        
        if (this.from != null)
        {
            
            return this.from.before (d);
            
        }

        if (this.to != null)
        {
            
            return this.to.after (d);
            
        }
        
        return false;
        
    }
    
    private long getOtherCompareValue ()
    {
        
        Date d = null;
        
        if (this.from != null)
        {
            
            d = this.to;
            
        }
        
        if (d == null)
        {
            
            return 0;
            
        }
        
        return d.getTime ();
        
    }
    
    public int compareTo (DateRange d)
    {
                
        long thisD = this.getCompareValue ();
        long thatD = d.getCompareValue ();
                
        long ret = thisD - thatD;
        
        if (ret == 0)
        {
            
            thisD = this.getOtherCompareValue ();
            thatD = this.getOtherCompareValue ();
            
            ret = thisD - thatD;
            
        }
        
        if (ret > 0)
        {
            
            return 1;
            
        } else {
            
            return -1;
            
        }

    }
    
    public String toString ()
    {
        
        return "from: " + this.from + " / to: " + this.to;
        
    }
    
}