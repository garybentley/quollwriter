package com.quollwriter.ui;

import java.util.*;

import javax.swing.event.*;

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

    public boolean hasType (String type);
                               
    public int getUsedInCount (String type);

    public Set<String> getTypes ();
    
    public Set<String> getTypesFromObjects ();

    public Set<E> getObjectsForType (String t);
    
    public Map<String, Set<E>> getObjectsAgainstTypes ();
    
    public boolean typesEditable ();
    
    public void removeChangeListener (ChangeListener l);
    
    public void addChangeListener (ChangeListener l);
    
}