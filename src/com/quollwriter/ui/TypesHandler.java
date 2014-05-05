package com.quollwriter.ui;

import java.util.*;

import com.quollwriter.data.*;

public interface TypesHandler<E extends DataObject>
{
    
    public boolean removeType (String  type,
                               boolean reload);
    
    public void addType (String  t,
                         boolean reload);

    public boolean renameType (String  oldType,
                               String  newType,
                               boolean reload);    

    public int getUsedInCount (String type);

    public Set<String> getTypes ();
    
    public Set<String> getTypesFromObjects ();

    public Set<E> getObjectsForType (String t);
    
    public Map<String, Set<E>> getObjectsAgainstTypes ();
    
    public boolean typesEditable ();
    
}