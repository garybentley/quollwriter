package com.quollwriter.ui;

import java.util.*;

public interface TypesHandler
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
    
}