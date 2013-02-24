package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public interface DataHandler
{

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException;

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException;

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException;

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException;

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException;

}
