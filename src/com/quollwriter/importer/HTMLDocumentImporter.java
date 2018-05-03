package com.quollwriter.importer;

import java.io.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.extractor.*;
import org.apache.poi.hwpf.model.*;
import org.apache.poi.hwpf.usermodel.*;

import org.docx4j.openpackaging.parts.WordprocessingML.*;


public class HTMLDocumentImporter implements DocumentImporter
{

    public static final String HEADING1 = "h1";
    public static final String TITLE = "title";
    private Project            p = null;
    private StringBuilder      chapterText = new StringBuilder ();
    private NamedObject        n = null;

    public Project convert (InputStream in,
                            String      fileExt)
                     throws Exception
    {

        this.p = new Project ();

        return p;

    }

    private void addLastItem ()
    {

        if (this.n != null)
        {

            if (this.n instanceof Chapter)
            {

                Chapter c = (Chapter) this.n;

                this.p.getBooks ().get (0).addChapter (c);
                c.setText (new StringWithMarkup (chapterText.toString ()));

            }

            if (this.n instanceof Asset)
            {

                this.p.addAsset ((Asset) this.n);
                this.n.setDescription (new StringWithMarkup (chapterText.toString ()));

            }

        } else
        {

            // Add a single chapter.
            Chapter c = this.p.getBooks ().get (0).createChapterAfter (null,
                                                                       Environment.getDefaultChapterName ());

            c.setText (new StringWithMarkup (chapterText.toString ()));

        }

    }

    private void addItem (String style,
                          String text)
                   throws GeneralException
    {

        if (text.trim ().length () == 0)
        {

            return;

        }

        if (style != null)
        {

            String name = null;

            if (style.equals (TITLE))
            {

                if (this.p.getName () == null)
                {

                    this.p.setName (text);
                    this.p.getBooks ().get (0).setName (text);

                }

                return;

            }

            if (style.equals (HEADING1))
            {

                if (this.n != null)
                {

                    // Add the object to the project/book.
                    if (this.n instanceof Chapter)
                    {

                        Chapter c = (Chapter) this.n;

                        this.p.getBooks ().get (0).addChapter (c);
                        c.setText (new StringWithMarkup (chapterText.toString ()));

                    }

                    if (this.n instanceof Asset)
                    {

                        this.p.addAsset ((Asset) this.n);
                        this.n.setDescription (new StringWithMarkup (chapterText.toString ()));

                    }

                    this.chapterText = new StringBuilder ();

                    this.n = null;

                }

                // Work out what to create.
                if (text.toLowerCase ().startsWith (Environment.getObjectTypeName (QCharacter.OBJECT_TYPE).toLowerCase () + ":"))
                {

                    // Create a character.
                    this.n = new QCharacter ();

                    name = text.substring (Environment.getObjectTypeName (QCharacter.OBJECT_TYPE).toLowerCase ().length () + 1).trim ();
                }

                if (text.toLowerCase ().startsWith (Environment.getObjectTypeName (Location.OBJECT_TYPE).toLowerCase () + ":"))
                {

                    // Create a character.
                    this.n = new Location ();

                    name = text.substring (Environment.getObjectTypeName (Location.OBJECT_TYPE).toLowerCase ().length () + 1).trim ();
                }

                if (text.toLowerCase ().startsWith (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE).toLowerCase () + ":"))
                {

                    // Create a character.
                    this.n = new ResearchItem ();

                    name = text.substring (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE).toLowerCase ().length () + 1).trim ();
                }

                if (text.toLowerCase ().startsWith (Environment.getObjectTypeName (QObject.OBJECT_TYPE).toLowerCase () + ":"))
                {

                    // Create a character.
                    this.n = new QObject ();

                    name = text.substring (Environment.getObjectTypeName (QObject.OBJECT_TYPE).toLowerCase ().length () + 1).trim ();

                }

                if (this.n == null)
                {

                    this.n = new Chapter (this.p.getBooks ().get (0),
                                          text);

                    name = text.trim ();

                }

                this.n.setName (name);

                return;

            }

        }

        // Assume chapter text.
        if (chapterText.length () > 0)
        {

            chapterText.append ("\n\n");

        }

        chapterText.append (text);

    }

}
