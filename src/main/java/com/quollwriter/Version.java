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
    private int betaVersion = 0;
    private List<Integer> parts = new ArrayList<> ();

    public Version (String v)
    {

        if (v == null)
        {

            throw new IllegalArgumentException ("Version must be specified");

        }

        this._version = v.trim ();

        String ver = this._version;

        int bind = this._version.indexOf ("b");

        if (bind > 0)
        {

            // Get the version then the beta.
            ver = this._version.substring (0, bind);

            String bver = this._version.substring (bind + 1);

            this.betaVersion = Integer.parseInt (bver);

        }

        this.getVersionParts (ver);

        //this.version = this.getVersionAsInt (ver);

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

    public String getVersionNoBeta ()
    {

        int bind = this._version.indexOf ("b");

        if (bind > 0)
        {

            return  this._version.substring (0, bind);

        }

        return this._version;
        
    }

    public String getVersion ()
    {

        return this._version;

    }

    public String toString ()
    {

        return this._version;

    }

    private List<Integer> getVersionParts (String v)
    {

        if (v == null)
        {

            v = "0";

        }

        //v = this.expandVersion (v);

        StringTokenizer t = new StringTokenizer (v,
                                                 ".");

        //List<Integer> parts = new ArrayList ();

        while (t.hasMoreTokens ())
        {

            this.parts.add (Integer.parseInt (t.nextToken ()));

        }

        return this.parts;

    }

    public boolean isBeta ()
    {

        return this.betaVersion > 0;

    }

    public boolean isSame (Version other)
    {

        return this.equals (other);

    }

    private int getPart (int i)
    {

        if (i < this.parts.size ())
        {

            return this.parts.get (i);

        }

        return -1;

    }

    private int getPartsCount ()
    {

        return this.parts.size ();

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

        for (int i = 0; i < other.getPartsCount (); i++)
        {

            int op = other.getPart (i);
            int tp = this.getPart (i);

            if (op > tp)
            {

                return true;

            }

            if (op < tp)
            {

                return false;

            }

        }

        if (other.betaVersion > 0)
        {

            return other.betaVersion > this.betaVersion;

        }

        return false;

    }

    @Override
    public int hashCode ()
    {

        return this._version.hashCode ();

    }

    @Override
    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof Version)))
        {

            return false;

        }

        Version v = (Version) o;

        return this._version.equals (v._version);

    }

    public boolean equalsIgnoreBeta (Version v)
    {

        if (v == null)
        {

            return false;

        }

        for (int i = 0; i < v.getPartsCount (); i++)
        {

            int op = v.getPart (i);
            int tp = this.getPart (i);

            if (op != tp)
            {

                return false;

            }

        }

        return true;

    }

}
