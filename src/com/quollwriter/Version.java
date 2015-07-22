package com.quollwriter;

import java.util.*;

/**
 * Models the version numbers, format is: [version.version.version...]b[version]
 * where each version is an integer.  The b[version] is optional and indicates a beta version.
 *
 * Examples:
 *
 *  2.2
 *  2.3b1
 *  2.4b1
 *  2.4
 *  2.2.1b1
 *  2.3.9
 *
 * A value of 2.3 is considered to be "newer" than 2.3b1 and 2.3b2 is newer than 2.3b1.
 */
public class Version implements Comparable<Version>
{
    
    private String _version = null;
    private int version = 0;
    private int betaVersion = 0;
    
    public Version (String v)
    {
        
        if (v == null)
        {
            
            throw new IllegalArgumentException ("Version must be specified");
            
        }
        
        this._version = v.trim ();
        
        String ver = this._version;
        
        int bind = v.indexOf ("b");
        
        if (bind > 0)
        {
            
            // Get the version then the beta.
            ver = v.substring (0, bind);
            
            String bver = v.substring (bind + 1);
            
            this.betaVersion = Integer.parseInt (bver);     
            
        }
        
        this.version = this.getVersionAsInt (ver);
        
    }
    
    @Override
    public int compareTo (Version v)
    {
        
        if (v == null)
        {
            
            return 1;
            
        }
        
        if (this.isSame (v))
        {

            return 0;
            
        }

        if (this.isNewer (v))
        {

            return -1;
            
        }
        
        return 1;
        
    }
    
    public String getVersion ()
    {
        
        return this._version;
        
    }
    
    public String toString ()
    {
        
        return this._version;
        
    }
    
    private String expandVersion (String v)
    {

        char[] chs = v.toCharArray ();
    
        int c = 0;
    
        for (int i = 0; i < chs.length; i++)
        {
        
            if (chs[i] == '.')
            {
                
                c++;
                
            }
            
        }
        
        if (c < 2)
        {
            
            for (int i = c; i < 2; i++)
            {
                
                v += ".0";
                
            }
            
        }

        return v;
        
    }    
    
    private List<Integer> getVersionParts (String v)
    {
        
        if (v == null)
        {
            
            v = "0";
            
        }
        
        v = this.expandVersion (v);
        
        StringTokenizer t = new StringTokenizer (v,
                                                 ".");
        
        List<Integer> parts = new ArrayList ();
        
        while (t.hasMoreTokens ())
        {
            
            parts.add (Integer.parseInt (t.nextToken ()));
            
        }
        
        return parts;
        
    }
    
    private int getVersionAsInt (String version)
    {

        if (version == null)
        {
            
            return -1;
            
        }
        
        List<Integer> parts = this.getVersionParts (version);
        
        int mult = 100;
        
        int t = 0;
        
        for (int i = 0; i < parts.size (); i++)
        {
            
            int p = parts.get (i);
            
            t += p * mult;
                        
            mult /= 10;
            
        }
        
        return t;
                
    }
        
    public boolean isBeta ()
    {
        
        return this.betaVersion > 0;
        
    }
    
    public boolean isSame (Version other)
    {
        
        return this.equals (other);
        
    }
    
    /**
     * Is the passed in version newer than this one.
     *
     * @param other The version to check.
     * @return <code>true</code> if <b>other</b> is newer than this version.
     */
    public boolean isNewer (Version other)
    {
        
        if (other == null)
        {
            
            return false;
            
        }
        
        if (other.version == this.version)
        {

            // i.e. 2.3 is NOT newer than 2.3b1
            if (this.betaVersion == 0)
            {
                
                return false;
                
            }
            
            // 2.3 IS newer than 2.3b1
            if ((other.betaVersion == 0)
                ||
                (other.betaVersion > this.betaVersion)
               )
            {
                
                return true;
                
            }
                        
        }

        if (other.version > this.version)
        {
                        
            return true;
            
        }
                        
        return false;
        
    }
 
    @Override
    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + this.version;
        hash = (31 * hash) + this.betaVersion;

        return hash;

    }

    @Override
    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof Version)))
        {

            return false;

        }

        Version v = (Version) o;

        return (v.version == this.version)
                &&
                (v.betaVersion == this.betaVersion);

    }
    
}