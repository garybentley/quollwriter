package com.quollwriter.ui;

import com.quollwriter.*;

import com.quollwriter.data.*;


public interface DragActionHandler
{

    public boolean performAction (int         removeRow,
                                  NamedObject removeObject,
                                  int         insertRow,
                                  NamedObject insertObject)
                           throws GeneralException;

}
