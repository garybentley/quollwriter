package com.quollwriter.data;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.comparators.*;


public abstract class Asset extends NamedObject
{

    public static Map<String, Class> supportedAssetTypes = new LinkedHashMap ();

    static
    {

        Map m = Asset.supportedAssetTypes;

        m.put (QCharacter.OBJECT_TYPE,
               QCharacter.class);
        m.put (Location.OBJECT_TYPE,
               Location.class);
        m.put (QObject.OBJECT_TYPE,
               QObject.class);
        m.put (ResearchItem.OBJECT_TYPE,
               ResearchItem.class);

    }

    private Project proj = null;

    public Asset(String objType,
                 String name)
    {

        super (objType);

        this.name = name;

    }

    public Asset(String objType)
    {

        super (objType);

    }

    public static Asset createSubType (String objectType)
                                throws GeneralException
    {

        Class cl = Asset.supportedAssetTypes.get (objectType);

        if (cl == null)
        {

            return null;

        }

        try
        {

            return (Asset) cl.newInstance ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create new instance of: " +
                                        cl.getName () +
                                        " for object type: " +
                                        objectType,
                                        e);

        }

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet (this.getNotes ());

    }

    public Project getProject ()
    {

        return this.proj;

    }

    public static Class getAssetClass (String objType)
    {
        
        return Asset.supportedAssetTypes.get (objType);
        
    }
    
    public void setProject (Project p)
    {

        this.proj = p;
        
        this.setParent (this.proj);

    }

}
