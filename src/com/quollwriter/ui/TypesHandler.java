package com.quollwriter.ui;

import java.util.*;

import javax.swing.event.*;

import com.quollwriter.data.*;
import com.quollwriter.events.*;

public interface TypesHandler
{
    
    public boolean removeType (String  type,
                               boolean reload);
    
    public void addType (String  t,
                         boolean reload);

    public boolean renameType (String  oldType,
                               String  newType,
                               boolean reload);    

    public boolean hasType (String type);
                               
    public Set<String> getTypes ();
        
    public boolean typesEditable ();
    
    public void removePropertyChangedListener (PropertyChangedListener l);
    
    public void addPropertyChangedListener (PropertyChangedListener l);
    
}