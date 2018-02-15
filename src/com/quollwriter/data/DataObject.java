package com.quollwriter.data;

import java.io.*;

import java.lang.ref.*;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

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
    private   String id = null;
    private   String version = null;
    private boolean latest = true;
    // protected Date lastModified = null;

    private Date                              dateCreated = new Date ();
    protected DataObject                      parent = null;
    private Map<PropertyChangedListener, Object> listeners = null;

    // Just used in the maps above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();

    public DataObject(String objType)
    {

        // We use a synchronized weak hash map here so that we don't have to worry about all the
        // references since they will be transient compared to the potential lifespan of the object.

        // Where possible listeners should de-register as normal but this just ensure that objects
        // that don't have a controlled pre-defined lifecycle.
        this.listeners = Collections.synchronizedMap (new WeakHashMap ());

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

    @Override
    public String toString ()
    {

        return Environment.formatObjectToStringProperties (this);

    }

    protected void addToStringProperties (Map<String, Object> props,
                                          String              name,
                                          Object              value)
    {

        if (props.containsKey (name))
        {

            throw new IllegalArgumentException ("Already have a toString property with name: " +
                                                name +
                                                ", props: " +
                                                props);

        }

        props.put (name,
                   value);

    }

    public void fillToStringProperties (Map<String, Object> props)
    {

        this.addToStringProperties (props,
                                    "objType",
                                    this.objType);
        this.addToStringProperties (props,
                                    "key",
                                    this.key);
        this.addToStringProperties (props,
                                    "id",
                                    this.id);
        this.addToStringProperties (props,
                                    "version",
                                    this.version);
        this.addToStringProperties (props,
                                    "latest",
                                    this.latest);
        this.addToStringProperties (props,
                                    "dateCreated",
                                    this.dateCreated);
        this.addToStringProperties (props,
                                    "parent",
                                    this.parent);

    }

    public abstract DataObject getObjectForReference (ObjectReference r);

    public void setLatest (boolean v)
    {

        this.latest = v;

    }

    public boolean isLatest ()
    {

        return this.latest;

    }

    public void setVersion (String v)
    {

        this.version = v;

    }

    public String getVersion ()
    {

        return this.version;

    }

    public void setId (String id)
    {

        if ((this.id != null)
            &&
            (id != null)
           )
        {

            throw new IllegalStateException ("Once the id for an object is set it cannot be set again.");

        }

        this.id = id;

    }

    public String getId ()
    {

        return this.id;

    }

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

    protected synchronized void firePropertyChangedEvent (String type,
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

        Object o = new Object ();

        Set<PropertyChangedListener> ls = null;

        // Get a copy of the current valid listeners.
        synchronized (this.listeners)
        {

            ls = new HashSet (this.listeners.keySet ());

        }

        PropertyChangedEvent ev = new PropertyChangedEvent (this,
                                                            type,
                                                            oldValue,
                                                            newValue);

        for (PropertyChangedListener l : ls)
        {

            if (l == null)
            {

                continue;

            }

            l.propertyChanged (ev);

        }

    }

    public void removePropertyChangedListener (PropertyChangedListener l)
    {

        this.listeners.remove (l);

    }

    /**
     * Adds a property changed listener to the object.
     *
     * Warning!  The listeners use a weak hash map structure so it is up to the caller to ensure that
     * the listener has the relevant lifespan to be called.
     * For example, this will cause the listener to be removed:
     *
     * <code>
     * // Don't do this, the reference isn't strong and the inner object will be garbage collected once
     * // it falls out of scope.
     * myObj.addPropertyChangedListener (new PropertyChangedListener ()
     * {
     *
     *    public void propertyChanged (PropertyChangedEvent ev) {}
     *
     * });
     * </code>
     *
     * Instead ensure that the listener is tied to the transient object (with the variable lifespan) instead, i.e.
     * use "implements PropertyChangedListener" or keep a reference to the listener, thus:
     *
     * <code>
     * this.propListener = new PropertyChangedListener ()
     * {
     *
     *    public void propertyChanged (PropertyChangedEvent ev) {}
     *
     * };
     * </code>
     *
     * Note: this makes sense anyway since otherwise it would lead to a memory leak and potentially dangling references.
     *
     * For clarity, the weak map structure is used because the caller may not have a well defined lifespan/lifecycle, that
     * is you may not be able to remove the listener at the time the enclosing class falls out of use.  Consider a box that
     * displays information about this DataObject and listens for changes to the object and then updates as necessary.  If
     * a standard map (with strong references) is used then it forces the box to have a well defined lifecycle that is managed by the class using
     * the box, the user of the box would need to be aware of the internals of the box and its behaviour.  This is a huge
     * burden and not always possible since java does not guarantee that an object is ever "finalized" unless it is no longer
     * references, but this would never happen because of the strong reference to the listener in the map.
     *
     * @param l The listener.
     */
    public void addPropertyChangedListener (PropertyChangedListener l)
    {

        this.listeners.put (l,
                            this.listenerFillObj);

    }

    public DataObject getParent ()
    {

        return this.parent;

    }

    public void setParent (DataObject d)
    {

        if (d == null)
        {

            this.parent = null;

            //this.props.setParentProperties (null);

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

    public void removeProperty (String name)
    {

        this.props.removeProperty (name);

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

    @Override
    public int hashCode ()
    {

        if (this.key == null)
        {

            return super.hashCode ();

        }

        int hash = 7;
        hash = (31 * hash) + ((null == this.objType) ? 0 : this.objType.hashCode ());
        hash = (31 * hash) + ((null == this.key) ? 0 : key.hashCode ());

        return hash;

    }

    @Override
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

            return d.hashCode () == this.hashCode ();

            //return false;

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
