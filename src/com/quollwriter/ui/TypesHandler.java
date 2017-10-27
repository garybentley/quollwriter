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

    default boolean renameType (String  oldType,
                                String  newType,
                                boolean reload)
    {

        return false;

    }

    default boolean hasType (String type)
    {

        return false;

    }

    public Set<String> getTypes ();

    default boolean typesEditable ()
    {

        return true;

    }

    public void removePropertyChangedListener (PropertyChangedListener l);

    public void addPropertyChangedListener (PropertyChangedListener l);

}
