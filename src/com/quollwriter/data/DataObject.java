package com.quollwriter.data;

import java.io.*;

import java.lang.ref.*;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gentlyweb.properties.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.events.*;

import org.jdom.*;


public abstract class DataObject
{

    private String       objType = null;
    protected Properties props = new Properties ();
    protected Long       key = null;
    // protected Date lastModified = null;

    private Date                              dateCreated = new Date ();
    protected DataObject                      parent = null;
    private Map<PropertyChangedListener, Map> listeners = new LinkedHashMap ();

    public DataObject(String objType)
    {


        if (objType != null)
        {

            if (objType.equals ("outline-item"))
            {

                objType = OutlineItem.OBJECT_TYPE;

            }

            if (objType.indexOf ("-") != -1)
            {

                throw new IllegalArgumentException ("Object type: " +
                                                    objType +
                                                    " cannot contain the character '-'");

            }

        }

        this.setObjectType (objType);

    }

    public abstract DataObject getObjectForReference (ObjectReference r);

    public Date getDateCreated ()
    {

        if (this.dateCreated == null)
        {

            this.dateCreated = new Date ();

        }

        return this.dateCreated;

    }

    public void setDateCreated (Date d)
    {

        this.dateCreated = d;

    }

    protected void firePropertyChangedEvent (String type,
                                             Object oldValue,
                                             Object newValue)
    {

        if ((oldValue == null) &&
            (newValue == null))
        {

            return;

        }

        boolean changed = false;

        if ((oldValue != null) &&
            (newValue == null))
        {

            changed = true;

        }

        if ((oldValue == null) &&
            (newValue != null))
        {

            changed = true;

        }

        if ((oldValue != null) &&
            (newValue != null))
        {

            if (!oldValue.equals (newValue))
            {

                changed = true;

            }

        }

        if (!changed)
        {

            return;

        }

        PropertyChangedEvent ev = new PropertyChangedEvent (this,
                                                            type,
                                                            oldValue,
                                                            newValue);

        Iterator<PropertyChangedListener> iter = this.listeners.keySet ().iterator ();

        while (iter.hasNext ())
        {

            PropertyChangedListener l = iter.next ();

            Map events = (Map) this.listeners.get (l);

            if ((events == null) ||
                (events.containsKey (ev.getChangeType ())) ||
                (events.size () == 0))
            {

                l.propertyChanged (ev);

            }

        }

    }

    public void removePropertyChangedListener (PropertyChangedListener l)
    {

        this.listeners.remove (l);

    }

    public void addPropertyChangedListener (PropertyChangedListener l,
                                            Map                     events)
    {

        this.listeners.put (l,
                            events);

    }

    public void setParent (DataObject d)
    {

        if (d == null)
        {

            return;

        }

        this.parent = d;

        // Get the properties parent.
        Properties pp = this.props;

        if (pp.getId ().equals (this.objType + "-" + this.key))
        {

            pp = pp.getParentProperties ();

        }

        pp.setParentProperties (d.getProperties ());

    }

    public String getPropertiesAsString ()
                                  throws Exception
    {

        // Conver the properties to a string.
        return JDOMUtils.getElementAsString (this.props.getAsJDOMElement ());

    }

    public void setPropertiesAsString (String p)
                                throws Exception
    {

        if (p == null)
        {

            return;

        }

        // Convert to XML, convert to Properties.
        Element root = JDOMUtils.getStringAsElement (p);

        if (this.props == null)
        {

            throw new IllegalStateException ("The object should have some properties: " +
                                             p);

        }

        Properties pr = this.props;

        this.props = new Properties (root);
        this.props.setId (this.objType + "-" + this.key);
        this.props.setParentProperties (pr);

    }

    void setObjectType (String objType)
    {

        this.objType = objType;

        this.props = new Properties ();
        this.props.setId (this.objType + "-" + this.key);

        this.props.setParentProperties (Environment.getDefaultProperties (this.objType));

    }

    public void setKey (Long k)
    {

        this.key = k;

    }

    public Long getKey ()
    {

        return this.key;

    }

    public void setProperty (String name,
                             String value)
                      throws IOException
    {

        StringProperty p = new StringProperty (name,
                                               value);

        p.setDescription ("N/A");

        this.props.setProperty (name,
                                p);

    }

    public void setProperty (String name,
                             int    value)
                      throws IOException
    {

        IntegerProperty p = new IntegerProperty (name,
                                                 value);

        p.setDescription ("N/A");

        this.props.setProperty (name,
                                p);

    }

    public void setProperty (String  name,
                             boolean value)
                      throws IOException
    {

        BooleanProperty p = new BooleanProperty (name,
                                                 value);

        p.setDescription ("N/A");

        this.props.setProperty (name,
                                p);

    }

    public String getProperty (String name,
                               String defName)
    {

        AbstractProperty ap = this.props.getPropertyObj (name);

        if (ap == null)
        {

            return this.getProperty (defName);

        }

        return this.getProperty (name);

    }

    public String getProperty (String name)
    {

        return this.props.getProperty (name);

    }

    public boolean getPropertyAsBoolean (String name,
                                         String defName)
    {

        AbstractProperty ap = this.props.getPropertyObj (name);

        if (ap == null)
        {

            return this.getPropertyAsBoolean (defName);

        }

        return this.getPropertyAsBoolean (name);

    }

    public boolean getPropertyAsBoolean (String name)
    {

        return this.props.getPropertyAsBoolean (name);

    }

    public int getPropertyAsInt (String name,
                                 String defName)
    {

        AbstractProperty ap = this.props.getPropertyObj (name);

        if (ap == null)
        {

            return this.getPropertyAsInt (defName);

        }

        return this.getPropertyAsInt (name);

    }

    public int getPropertyAsInt (String name)
    {

        return this.props.getPropertyAsInt (name);

    }

    public float getPropertyAsFloat (String name,
                                     String defName)
    {

        AbstractProperty ap = this.props.getPropertyObj (name);

        if (ap == null)
        {

            return this.getPropertyAsFloat (defName);

        }

        return this.props.getPropertyAsFloat (name);

    }

    public float getPropertyAsFloat (String name)
    {

        return this.props.getPropertyAsFloat (name);

    }

    public Properties getProperties ()
    {

        return this.props;

    }

    public String getObjectType ()
    {

        return this.objType;

    }

    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + ((null == this.objType) ? 0 : this.objType.hashCode ());
        hash = (31 * hash) + ((null == this.key) ? 0 : key.hashCode ());

        return hash;

    }

    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof DataObject)))
        {

            return false;

        }

        DataObject d = (DataObject) o;

        if ((d.key == null) ||
            (this.key == null))
        {

            return false;

        }
        if (this.objType.equals ("note"))
        {

        }
        if ((d.key.equals (this.key)) &&
            (d.objType.equals (this.objType)))
        {

            return true;

        }

        return false;

    }

    public ObjectReference getObjectReference ()
    {

        return new ObjectReference (this.getObjectType (),
                                    this.getKey (),
                                    ((this.parent == null) ? null : this.parent.getObjectReference ()));

    }

}
