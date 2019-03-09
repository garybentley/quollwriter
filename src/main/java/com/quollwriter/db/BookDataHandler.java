package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class BookDataHandler implements DataHandler<Book, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, markup, lastmodified, datecreated, properties, id, version FROM book_v ";
    private ObjectManager objectManager = null;

    public BookDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Book getBook (ResultSet rs,
                          Project   p,
                          boolean   loadChildObjects)
                   throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Book b = new Book ();

            b.setName (rs.getString (ind++));
            b.setKey (key);

            b.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));

            b.setLastModified (rs.getTimestamp (ind++));
            b.setDateCreated (rs.getTimestamp (ind++));
            b.setPropertiesAsString (rs.getString (ind++));
            b.setId (rs.getString (ind++));
            b.setVersion (rs.getString (ind++));

            if (p != null)
            {

                p.addBook (b);

            }

            if (loadChildObjects)
            {

                Connection conn = rs.getStatement ().getConnection ();

                this.objectManager.getObjects (Chapter.class,
                                               b,
                                               conn,
                                               loadChildObjects);

            }

            return b;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load book",
                                        e);

        }

    }

    @Override
    public List<Book> getObjects (Project     parent,
                                  Connection  conn,
                                  boolean     loadChildObjects)
                           throws GeneralException
    {

        List<Book> ret = new ArrayList<> ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE",
                                                            null,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getBook (rs,
                                       parent,
                                       loadChildObjects));

            }

            rs.close ();

            return ret;

        } catch (Exception e) {

            throw new GeneralException ("Unable to get books for project: " +
                                        parent,
                                        e);

        }

    }

    @Override
    public Book getObjectByKey (long       key,
                                Project    proj,
                                Connection conn,
                                boolean    loadChildObjects)
                         throws GeneralException
    {

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + "WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                return this.getBook (rs,
                                     proj,
                                     loadChildObjects);

            }

            return null;

        } catch (Exception e) {

            throw new GeneralException ("Unable to get book for key: " +
                                        key,
                                        e);

        }

    }

    public void updateChapterIndexes (Book       b,
                                      Connection conn)
                               throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (null);
        params.add (null);
        params.add (b.getKey ());

        for (Chapter c : b.getChapters ())
        {

            params.set (0,
                        b.getChapterIndex (c));
            params.set (1,
                        c.getKey ());

            this.objectManager.executeStatement ("UPDATE chapter SET index = ? WHERE dbkey = ? AND bookdbkey = ?",
                                                 params,
                                                 conn);

        }

    }

    @Override
    public void createObject (Book       b,
                              Connection conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (b.getKey ());
        params.add (b.getProject ().getKey ());
        params.add (b.getProject ().getBookIndex (b));

        this.objectManager.executeStatement ("INSERT INTO book (dbkey, projectdbkey, index) VALUES (?, ?, ?)",
                                             params,
                                             conn);

        for (Chapter c : b.getChapters ())
        {

            this.objectManager.saveObject (c,
                                           conn);

        }

    }

    @Override
    public void deleteObject (Book       b,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        // Delete the notes.
        for (Chapter c : b.getChapters ())
        {

            this.objectManager.deleteObject (c,
                                             true,
                                             conn);

        }

        List<Object> params = new ArrayList<> ();
        params.add (b.getKey ());

        this.objectManager.executeStatement ("DELETE FROM book WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (Book       b,
                              Connection conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (b.getProject ().getBookIndex (b));
        params.add (b.getKey ());

        this.objectManager.executeStatement ("UPDATE book SET index = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
