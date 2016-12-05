package com.quollwriter.ui;

import com.quollwriter.*;

import com.quollwriter.data.*;


public interface DragActionHandler<E>
{

    public boolean handleMove (int fromRow,
                               int toRow,
                               E   object)
                        throws GeneralException;

    public boolean performAction (int         removeRow,
                                  NamedObject removeObject,
                                  int         insertRow,
                                  NamedObject insertObject)
                           throws GeneralException;

}
