package com.quollwriter.ui.fx;

import java.util.*;

public class State
{

    public static final String DEF_SEP = "|";

    public enum Key
    {

        scrollpanev,
        open;

    }

    private Map<String, String> state = new LinkedHashMap<> ();

    public State ()
    {

    }

    public State (String s)
    {

        StringTokenizer t = new StringTokenizer (s,
                                                 String.valueOf ('\n'));

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            StringTokenizer tt = new StringTokenizer (tok,
                                                      "=");

            while (tt.hasMoreTokens ())
            {

                if (tt.countTokens () == 2)
                {

                    String name = tt.nextToken ().trim ();
                    String value = tt.nextToken ().trim ();

                    this.state.put (name,
                                    value);

                }

            }

        }

    }

    public List<String> getItems (Key name)
    {

        return this.getItems (name.name (),
                              DEF_SEP);

    }

    public List<String> getItems (String name)
    {

        return this.getItems (name,
                              DEF_SEP);

    }

    public List<String> getItems (String name,
                                  String sep)
    {

        String v = this.get (name);

        if (v == null)
        {

            return new ArrayList<> ();

        }

        return Arrays.asList (v.split (sep));

    }

    public int getAsInt (String name)
    {

        return this.getAsInt (name,
                              0);

    }

    public int getAsInt (String name,
                         int    def)
    {

        try
        {

            return Integer.parseInt (this.get (name));

        } catch (Exception e) {

            return def;

        }

    }

    public int getAsInt (Key name)
    {

        return this.getAsInt (name,
                              0);

    }

    public int getAsInt (Key name,
                         int def)
    {

        return this.getAsInt (name.name (),
                              def);

    }

    public boolean getAsBoolean (Key name)
    {

        return this.getAsBoolean (name.name ());

    }

    public boolean getAsBoolean (Key     name,
                                 boolean def)
    {

        return this.getAsBoolean (name.toString (),
                                  def);

    }

    public boolean getAsBoolean (String name)
    {

        return this.getAsBoolean (name,
                                  false);

    }

    public boolean getAsBoolean (String  name,
                                 boolean def)
    {

        try
        {

            return Boolean.parseBoolean (this.get (name));

        } catch (Exception e) {

            return def;

        }

    }

    public String get (String name)
    {

        return this.state.get (name);

    }

    public void set (Key                name,
                     Collection<String> values)
    {

        this.set (name.name (),
                  values);

    }

    public void set (String             name,
                     Collection<String> values)
    {

        this.set (name,
                  values,
                  DEF_SEP);

    }

    public void set (String             name,
                     Collection<String> values,
                     String             sep)
    {

        this.set (name,
                  String.join (sep, values));

    }

    public void set (Key     name,
                     Boolean value)
    {

        this.set (name.toString (),
                  value);

    }

    public void set (String  name,
                     Boolean value)
    {

        this.set (name,
                  value.toString ());

    }

    public void set (Key   name,
                     String value)
    {

        this.state.put (name.toString (),
                        value);

    }

    public void set (Key    name,
                     Double value)
    {

        this.set (name.toString (),
                  value);

    }

    public void set (String name,
                     Double value)
    {

        this.state.put (name,
                        Double.toString (value));

    }

    public void set (Key     name,
                     Integer value)
    {

        this.set (name.toString (),
                  value);

    }

    public void set (String  name,
                     Integer value)
    {

        this.state.put (name,
                        Integer.toString (value));

    }

    public void set (String name,
                     String value)
    {

        this.state.put (name,
                        value);

    }

    public String asString ()
    {

        StringBuilder b = new StringBuilder ();

        for (String k : this.state.keySet ())
        {

            // TODO Improve
            String v = this.state.get (k);

            b.append (k);
            b.append ("=");
            b.append (v);
            b.append ('\n');

        }

        return b.toString ();

    }

}
