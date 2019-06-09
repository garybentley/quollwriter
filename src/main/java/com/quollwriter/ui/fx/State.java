package com.quollwriter.ui.fx;

import java.util.*;

import com.quollwriter.*;

public class State
{

    public static final String DEF_SEP = "|";

    public enum Key
    {

        scrollpanev,
        open;

    }

    private Map<String, Object> state = new LinkedHashMap<> ();

    public State ()
    {

    }

    public State (String s)
    {

        if (s == null)
        {

            return;

        }

        // Try json first.
        try
        {

            Map v = (Map) JSONDecoder.decode (s);

            Iterator it = v.keySet ().iterator ();

            while (it.hasNext ())
            {

                String k = it.next ().toString ();
                Object val = v.get (k);

                this.state.put (k, val);

            }

        } catch (Exception e) {

            // Try the legacy method.
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

                    } else {

                        break;

                    }

                }

            }

        }

    }

    public Integer getAsInt (String name)
    {

        return this.getAsInt (name,
                              null);

    }

    public Integer getAsInt (Key     name,
                             Integer def)
    {

        return this.getAsInt (name.name (),
                              def);

    }

    public Integer getAsInt (Key name)
    {

        return this.getAsInt (name,
                              null);

    }

    public Integer getAsInt (String  name,
                             Integer def)
    {

        Number n = this.getAsNumber (name,
                                     def);

        if (n != null)
        {

            return n.intValue ();

        }

        return def;

    }

    public Number getAsNumber (Key    name,
                               Number def)
    {

        try
        {

            Number n = this.getAs (name.name (),
                                   Number.class);

            if (n == null)
            {

                return def;

            }

            return n;

        } catch (Exception e) {

            return def;

        }

    }

    public Number getAsNumber (String name,
                               Number def)
    {

        return this.getAs (name,
                           Number.class);

    }

    public Boolean getAsBoolean (Key name)
    {

        return this.getAsBoolean (name.name ());

    }

    public Boolean getAsBoolean (Key     name,
                                 boolean def)
    {

        return this.getAsBoolean (name.toString (),
                                  def);

    }

    public Boolean getAsBoolean (String name)
    {

        return this.getAsBoolean (name,
                                  false);

    }

    public Boolean getAsBoolean (String  name,
                                 boolean def)
    {

        try
        {

            Boolean b = this.getAs (name,
                                    Boolean.class);

            if (b == null)
            {

                return def;

            }

            return b;

        } catch (IllegalStateException e) {

            return def;

        }

    }

    public <T> T getAs (Key      name,
                        Class<T> expect)
    {

        return this.getAs (name.name (),
                           expect);

    }

    public <T> T getAs (String   name,
                        Class<T> expect)
    {

        return this.checkGet (name,
                              expect);

    }

    public String getAsString (String name)
    {

        return this.getAs (name,
                           String.class);

    }

    public State getAsState (String name)
    {

        return this.getAs (name,
                           State.class);

    }

    public <T> Set<T> getAsSet (String name,
                                Class  itemExpect)
    {

        Object v = this.get (name);

        if (v == null)
        {

            return null;

        }

        if (!Collection.class.isAssignableFrom (v.getClass ()))
        {

            throw new IllegalArgumentException ("Unable to convert item data for: " + name + " to set of type: " + itemExpect.getName ());

        }

        Collection c = (Collection) v;

        Set<T> items = new LinkedHashSet<> ();

        for (Object o : c)
        {

            if (!itemExpect.isAssignableFrom (o.getClass ()))
            {

                throw new IllegalArgumentException ("Unable to convert collection item: " + o.getClass ().getName () + " to type: " + itemExpect.getName ());

            }

            items.add ((T) o);

        }

        return items;

    }

    private <T> T checkGet (String   name,
                            Class<T> expect)
    {

        Object v = this.get (name);

        if (v == null)
        {

            return null;

        }

        if (!(expect.isAssignableFrom (v.getClass ())))
        {

            throw new IllegalStateException ("Value for name: " + name + ", is type: " + v.getClass ().getName ());

        }

        return expect.cast (v);

    }

    public Object get (String name)
    {

        return this.state.get (name);

    }

    public void set (Key    name,
                     Object value)
    {

        this.set (name.toString (),
                  value);

    }

    public void set (String name,
                     Object value)
    {

        this.state.put (name,
                        value);

    }

    public String toString ()
    {

        return this.getClass ().getName () + "(" + this.state + ")";

    }

    public String asString ()
                     throws GeneralException
    {

        return JSONEncoder.encode (this.state);

    }

}
