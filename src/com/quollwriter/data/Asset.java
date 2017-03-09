package com.quollwriter.data;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.comparators.*;

public class Asset extends UserConfigurableObject
{

    public Asset (UserConfigurableObjectType objType)
    {

        super (objType);

    }

    @Override
    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet (this.getNotes ());

    }
        
    public static Asset createAsset (UserConfigurableObjectType type)
                              throws GeneralException
    {
        
        if (type.isLegacyObjectType ())
        {
            
            return LegacyAsset.createLegacyAsset (type);
            
        } else {
            
            return new Asset (type);
            
        }
        
    }
    
}
