package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

// TODO: ? Make generic here with <T extends DataObject>, <T extends NamedObject> - the first being the type of object managed, the second being the parent passed to getObjects.
// TODO: Have an annotation for implementations.
public interface DataHandler<D extends DataObject, P extends DataObject>
{

    public void createObject (D          d,
                              Connection conn)
                       throws GeneralException;

    public void deleteObject (D          d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException;

    public void updateObject (D          d,
                              Connection conn)
                       throws GeneralException;

    // Changed from <? extends NamedObject>
    public List<D> getObjects (P           parent,
                               Connection  conn,
                               boolean     loadChildObjects)
                        throws GeneralException;

    public D getObjectByKey (long       key,
                             P          parent,
                             Connection conn,
                             boolean    loadChildObjects)
                      throws GeneralException;

}
