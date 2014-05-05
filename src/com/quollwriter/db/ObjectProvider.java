package com.quollwriter.db;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.data.*;

public interface ObjectProvider<E extends DataObject>
{
    
    public Set<E> getAll ();
    
    public E getByKey (Long key);
    
    public void save (E      obj)
                      throws GeneralException;
                      
    public void saveAll (List<E> objs)
                         throws  GeneralException;
    
}