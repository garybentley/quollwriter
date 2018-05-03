package com.quollwriter.importer;

import java.io.*;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.text.TextUtilities;
import com.quollwriter.data.*;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.extractor.*;
import org.apache.poi.hwpf.model.*;
import org.apache.poi.hwpf.usermodel.*;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;

import org.docx4j.wml.Body;


public class MSWordDocumentImporter implements DocumentImporter
{

    public static final String HEADING1 = "Heading1";
    public static final String HEADING_1 = "Heading 1";
    public static final String TITLE = "Title";
    private Project            p = null;
    private StringBuilder      chapterText = new StringBuilder ();
    private NamedObject        n = null;

    public Project convert (InputStream in,
                            String      fileExt)
                     throws Exception
    {

        this.p = new Project ();

        Book b = new Book (this.p,
                           null);

        this.p.addBook (b);

        if (fileExt.equals (Constants.DOC_FILE_EXTENSION))
        {

            this.importFromDOC (in);

        }

        if (fileExt.equals (Constants.DOCX_FILE_EXTENSION))
        {

            this.importFromDOCX (in);

        }

        return this.p;

    }

    private void importFromDOC (InputStream in)
                         throws Exception
    {

        HWPFDocument doc = new HWPFDocument (in);

        PAPBinTable parasT = doc.getParagraphTable ();

        Range r = doc.getRange ();

        List<Object> paras = parasT.getParagraphs ();

        for (int i = 0; i < paras.size (); i++)
        {

            Object o = paras.get (i);

            if (o instanceof PAPX)
            {

                PAPX px = (PAPX) o;

                String style = doc.getStyleSheet ().getStyleDescription (px.getIstd ()).getName ();

                Paragraph para = r.getParagraph (i);

                String text = para.text ();

                this.addItem (style,
                              text);

            }

        }

        this.addLastItem ();

    }

    private void importFromDOCX (InputStream in)
                          throws Exception
    {

        File f = Environment.writeStreamToTempFile (in);
        f.deleteOnExit ();

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load (f);

        MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

        //org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) mp.getContent ();

        //Body body = wmlDocumentEl.getBody ();

        List<Object> bodyChildren = mp.getContent (); //body.getContent (); //getEGBlockLevelElts ();

        for (Object o : bodyChildren)
        {

            if (o instanceof org.docx4j.wml.P)
            {

                if (((org.docx4j.wml.P) o).getPPr () != null)
                {

                    org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P) o).getPPr ();

                    org.docx4j.wml.PPrBase.PStyle style = ppr.getPStyle ();

                    StringWriter sw = new StringWriter ();

                    org.docx4j.TextUtils.extractText (o,
                                                      sw);

                    String text = sw.toString ();
                    String s = null;

                    if (style != null)
                    {

                        s = style.getVal ();

                    }

                    this.addItem (s,
                                  text);

                }

            }

        }

        this.addLastItem ();

    }

    private void addLastItem ()
    {

        if (this.n != null)
        {

            this.createItem ();

        } else
        {

            // Add a single chapter.
            Chapter c = this.p.getBooks ().get (0).createChapterAfter (null,
                                                                       Environment.getDefaultChapterName ());

            c.setText (new StringWithMarkup (chapterText.toString ()));

        }

    }

    private void createItem ()
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

            String ct = chapterText.toString ().trim ();

            // Check the chapterText to see if there is an "Aliases:" prefix to the start
            // of the text.
            int aliasesInd = ct.indexOf ("Aliases: ");

            if (aliasesInd == 0)
            {

                Asset as = (Asset) this.n;

                int lineInd = ct.indexOf (String.valueOf ('\n'));

                String aliases = ct;

                if (lineInd > 0)
                {

                    aliases = ct.substring (0,
                                            lineInd);

                    ct = ct.substring (lineInd).trim ();

                } else {

                    ct = "";

                }

                aliases = aliases.substring ("Aliases: ".length ());

                as.setAliases (aliases);

            }

            if (this.n instanceof QObject)
            {

                int typeInd = ct.indexOf ("Type: ");

                if (typeInd == 0)
                {

                    QObject qo = (QObject) this.n;

                    int lineInd = ct.indexOf (String.valueOf ('\n'));

                    String type = ct;

                    if (lineInd > 0)
                    {

                        type = ct.substring (0,
                                             lineInd);

                        ct = ct.substring (lineInd).trim ();

                    }

                    type = type.substring ("Type: ".length ());

                    qo.setType (type);

                }

            }

            if (this.n instanceof ResearchItem)
            {

                // Get the first line.
                int lineInd = ct.indexOf (String.valueOf ('\n'));

                String url = ct;

                if (lineInd > 0)
                {

                    url = ct.substring (0,
                                        lineInd);

                }

                if ((url.startsWith ("http://"))
                    ||
                    (url.startsWith ("https://"))
                   )
                {

                    ResearchItem ri = (ResearchItem) this.n;

                    ri.setUrl (url.trim ());

                    if (lineInd > 0)
                    {

                        ct = ct.substring (lineInd).trim ();

                    }

                }

            }

            this.n.setDescription (new StringWithMarkup (ct));

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

        text = TextUtilities.sanitizeText (text);

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

            if ((style.equals (HEADING1)) ||
                (style.equals (HEADING_1)))
            {

                if (this.n != null)
                {

                    // Add the object to the project/book.
                    this.createItem ();

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

                    this.n = new Location ();

                    name = text.substring (Environment.getObjectTypeName (Location.OBJECT_TYPE).toLowerCase ().length () + 1).trim ();
                }

                if (text.toLowerCase ().startsWith (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE).toLowerCase () + ":"))
                {

                    this.n = new ResearchItem ();

                    name = text.substring (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE).toLowerCase ().length () + 1).trim ();
                }

                if (text.toLowerCase ().startsWith (Environment.getObjectTypeName (QObject.OBJECT_TYPE).toLowerCase () + ":"))
                {

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
