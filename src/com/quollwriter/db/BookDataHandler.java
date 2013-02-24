package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class BookDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;
    private List<Book>    allBooks = new ArrayList ();
    private boolean       loaded = false;

    public BookDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Book getBook (ResultSet rs,
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
            b.setDescription (rs.getString (ind++));

            b.setLastModified (rs.getTimestamp (ind++));
            b.setDateCreated (rs.getTimestamp (ind++));
            b.setPropertiesAsString (rs.getString (ind++));

            /*
                    if (loadChildObjects)
                    {

                        Connection conn = rs.getStatement ().getConnection ();

                        List chapters = this.objectManager.getObjects (Chapter.class,
                                                                       b,
                                                                       conn,
                                                                       loadChildObjects);

                        for (int i = 0; i < chapters.size (); i++)
                        {

                            b.addChapter ((Chapter) chapters.get (i));

                        }

                    }
             */
            return b;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load book",
                                        e);

        }

    }

    private void loadAllBooks (Connection conn,
                               boolean    loadChildObjects)
                        throws GeneralException
    {

        if (!this.loaded)
        {

            this.loaded = true;

            try
            {

                ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties FROM book_v",
                                                                null,
                                                                conn);

                while (rs.next ())
                {

                    this.addBook (this.getBook (rs,
                                                loadChildObjects));

                }

                try
                {

                    rs.close ();

                } catch (Exception e)
                {
                }

                // Doing it this way so that if the child needs to perform the lookup then
                // we won't go into a loop.
                if (loadChildObjects)
                {

                    for (Book b : this.allBooks)
                    {

                        List chapters = this.objectManager.getObjects (Chapter.class,
                                                                       b,
                                                                       conn,
                                                                       loadChildObjects);

                        for (int i = 0; i < chapters.size (); i++)
                        {

                            b.addChapter ((Chapter) chapters.get (i));

                        }

                    }

                }

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load all books",
                                            e);

            }

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        this.loadAllBooks (conn,
                           loadChildObjects);

        List<Book> ret = new ArrayList (this.allBooks);

/*
        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties FROM book_v WHERE projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getBook (rs,
                                       loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load books for: " +
                                        parent,
                                        e);

        }
*/
        return ret;

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        this.loadAllBooks (conn,
                           loadChildObjects);

        for (Book b : this.allBooks)
        {

            if (b.getKey ().intValue () == key)
            {

                return b;

            }

        }

        return null;
/*
        Book i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties FROM book_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getBook (rs,
                                  loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load book for key: " +
                                        key,
                                        e);

        }

        return i;
*/
    }

    public void updateChapterIndexes (Book       b,
                                      Connection conn)
                               throws GeneralException
    {

        List params = new ArrayList ();
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

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Book b = (Book) d;

        List params = new ArrayList ();
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

        this.addBook (b);

    }

    private void addBook (Book b)
    {

        this.allBooks.add (b);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        Book b = (Book) d;

        // Delete the notes.
        for (Chapter c : b.getChapters ())
        {

            this.objectManager.deleteObject (c,
                                             true,
                                             conn);

        }

        List params = new ArrayList ();
        params.add (b.getKey ());

        this.objectManager.executeStatement ("DELETE FROM book WHERE dbkey = ?",
                                             params,
                                             conn);

        this.allBooks.remove (b);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Book b = (Book) d;

        List params = new ArrayList ();
        params.add (b.getProject ().getBookIndex (b));
        params.add (b.getKey ());

        this.objectManager.executeStatement ("UPDATE book SET index = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
