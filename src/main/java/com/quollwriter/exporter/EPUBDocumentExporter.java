package com.quollwriter.exporter;

import java.io.*;
import java.nio.file.*;

import java.text.*;

import java.util.*;
import java.util.zip.*;

import nl.siegmann.epublib.epub.*;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Resource;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EPUBDocumentExporter extends AbstractDocumentExporter
{

    public static final String DETAILS_STAGE = "details";

    // private ExportSettings settings = null;
    private ZipOutputStream zout = null;

    //private JTextField author = null;
    //private JTextField id = null;

    private QuollTextField author2 = null;
    private QuollTextField id2 = null;

    public EPUBDocumentExporter()
    {

    }

    public String getStartStage ()
    {

        return DETAILS_STAGE;

    }

    @Override
    public Wizard.Step getStage (String stage)
    {

        final EPUBDocumentExporter _this = this;

        com.quollwriter.ui.fx.components.Wizard.Step ws = new com.quollwriter.ui.fx.components.Wizard.Step ();

        if (DETAILS_STAGE.equals (stage))
        {

            ws.title = getUILanguageStringProperty (exportproject,stages,bookdetails,title);

            this.author2 = QuollTextField.builder ()
                .build ();

            this.author2.setText (this.proj.getProperty (Constants.AUTHOR_NAME_PROPERTY_NAME));

            this.id2 = QuollTextField.builder ()
                .build ();

            this.id2.setText (this.proj.getProperty (Constants.BOOK_ID_PROPERTY_NAME));

            Form f = Form.builder ()
                .description (exportproject,stages,bookdetails,text)
                .item (getUILanguageStringProperty (exportproject,stages,bookdetails,labels,authorname),
                       this.author2)
                .item (getUILanguageStringProperty (exportproject,stages,bookdetails,labels,LanguageStrings.id),
                       this.id2)
                .build ();

            ws.content = f;
            ws.content.getStyleClass ().add (DETAILS_STAGE);

        }

        return ws;

    }

    public String getNextStage (String currStage)
    {

        if (DETAILS_STAGE.equals (currStage))
        {

            return null;

        }

        return DETAILS_STAGE;

    }

    public String getPreviousStage (String currStage)
    {

        return null;

    }

    private void addEntry (String name,
                           String content)
                    throws Exception
    {

        byte[] bytes = content.getBytes ("utf-8");

        ZipEntry ze = new ZipEntry (name);

        if (name.equals ("mimetype"))
        {

            this.zout.setLevel (ZipEntry.STORED);

        } else
        {

            this.zout.setLevel (ZipEntry.DEFLATED);

        }

        this.zout.putNextEntry (ze);

        this.zout.write (bytes,
                         0,
                         bytes.length);

    }

    private String sanitizeName (String name)
    {

        name = Utils.sanitizeForFilename (name);

        return name;

    }

    public void exportProject (Path             dir,
                               Set<NamedObject> itemsToExport)
                        throws GeneralException
    {

        try
        {

            if (this.author2.getText () != null)
            {

                this.proj.setProperty (Constants.AUTHOR_NAME_PROPERTY_NAME,
                                       this.author2.getText ().trim ());

            }

            if (this.id2.getText () != null)
            {

                this.proj.setProperty (Constants.BOOK_ID_PROPERTY_NAME,
                                       this.id2.getText ().trim ());

            }

            // Create new Book
            nl.siegmann.epublib.domain.Book book = new nl.siegmann.epublib.domain.Book ();

            // Set the title
            book.getMetadata ().addTitle (this.proj.getName ());

            // Add an Author
            book.getMetadata ().addAuthor (new Author (this.author2.getText ()));
            book.getMetadata ().setLanguage (this.getLanguageCode (this.proj));

            // Set cover image
            //book.getMetadata().setCoverImage(new Resource(Simple1.class.getResourceAsStream("/book1/test_cover.png"), "cover.png"));

            String css = Utils.getResourceFileAsString ("/data/export/epub/css-template.xml");

            css = Utils.replaceString (css,
                                             "[[FONT_NAME]]",
                                             UserProperties.get (Constants.EDITOR_FONT_PROPERTY_NAME));
            css = Utils.replaceString (css,
                                             "[[FONT_SIZE]]",
                                             UserProperties.getAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME) + "pt");
            css = Utils.replaceString (css,
                                             "[[LINE_SPACING]]",
                                             (100 * UserProperties.getAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME)) + "%");
            css = Utils.replaceString (css,
                                             "[[ALIGN]]",
                                             UserProperties.get (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME).toLowerCase ());

            String indent = "0px";

            if (UserProperties.getAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME))
            {

                indent = "5em";

            }

            css = Utils.replaceString (css,
                                             "[[INDENT]]",
                                             indent);

            book.getResources ().add (new Resource (new ByteArrayInputStream (css.getBytes ("utf-8")),
                                                    "main.css"));

            if (itemsToExport.size () > 0)
            {

                String cTemp = Utils.getResourceFileAsString ("/data/export/epub/chapter-template.xml");

                int count = 0;

                for (NamedObject n : itemsToExport)
                {

                    if (!(n instanceof Chapter))
                    {

                        continue;

                    }

                    Chapter c = (Chapter) n;

                    count++;

                    String chapterText = Utils.replaceString (cTemp,
                                                                    "[[TITLE]]",
                                                                    c.getName ());

                    StringWithMarkup v = c.getText ();

                    String t = (v != null ? v.getText () : null);

                    TextIterator iter = new TextIterator (t);

                    Markup m = (c != null ? v.getMarkup () : null);

                    StringBuilder ct = new StringBuilder ();

                    for (Paragraph para : iter.getParagraphs ())
                    {

                        ct.append (String.format ("<p>%s</p>",
                                                  para.markupAsHTML (m)));

                    }

                    chapterText = Utils.replaceString (chapterText,
                                                             "[[CONTENT]]",
                                                             ct.toString ());

                    book.addSection (c.getName (),
                                     new Resource (new ByteArrayInputStream (chapterText.getBytes ("utf-8")),
                                                   "chapter" + count + ".html"));

                }

            }

            // TODO Make a constant/property...
            String appendixTemp = Utils.getResourceFileAsString ("/data/export/epub/appendix-template.xml");

            // Capital A.
            //int apxChar = 65;

            char apxChar = 'A';

            Map<UserConfigurableObjectType, Set<Asset>> assets = new LinkedHashMap<> ();

            for (NamedObject n : itemsToExport)
            {

                if (!(n instanceof Asset))
                {

                    continue;

                }

                Asset a = (Asset) n;

                Set<Asset> assts = assets.get (a.getUserConfigurableObjectType ());

                if (assts == null)
                {

                    assts = new LinkedHashSet<> ();
                    assets.put (a.getUserConfigurableObjectType (),
                                assts);

                }

                assts.add (a);

            }

            for (UserConfigurableObjectType type : assets.keySet ())
            {

                Set<Asset> as = assets.get (type);

                String apxC = Character.toString (apxChar);

                apxChar = (char) ((int) apxChar + 1);

                String cid = String.format ("appendix-%s-%s",
                                            apxC,
                                            type.getObjectTypeNamePlural ().replace (" ", "-"));

                String title = String.format (getUIString (exportproject,sectiontitles,appendix),
                                            //"Appendix %s - %s",
                                              apxC,
                                              type.getObjectTypeNamePlural ());

                String t = Utils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = Utils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (as,
                                                                   book));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ("utf-8")),
                                               cid + ".html"));

            }

            // Create EpubWriter
            EpubWriter epubWriter = new EpubWriter ();

            // Write the Book as Epub
            epubWriter.write (book, Files.newOutputStream (dir.resolve (this.sanitizeName (this.proj.getName ()) + Constants.EPUB_FILE_EXTENSION)));

        } catch (Exception e) {

            throw new GeneralException ("Unable to export project: " +
                                        this.proj,
                                        e);

        }

    }

    private String getAssetsPage (Set<Asset>                      assets,
                                  nl.siegmann.epublib.domain.Book epubBook)
    {

        StringBuilder buf = new StringBuilder ();

        for (Asset a : assets)
        {

            buf.append ("<h2>");
            buf.append (a.getName ());
            buf.append ("</h2>");

            // Get the handlers.
            Set<UserConfigurableObjectFieldViewEditHandler> handlers = a.getViewEditHandlers (null);

            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {

                // TODO: This has GOT to change.
                if (h instanceof ObjectNameUserConfigurableObjectFieldViewEditHandler)
                {

                    continue;

                }

                Object val = null;

                try
                {

                    val = h.getFieldValue ();

                } catch (Exception e) {

                    Environment.logError ("Unable to get field value: " + h,
                                          e);

                }

                if ((val == null)
                    ||
                    ((val instanceof StringWithMarkup) && (!((StringWithMarkup) val).hasText ()))
                   )
                {

                    continue;

                }

                buf.append ("<h3>");
                buf.append (h.getTypeField ().getFormName ());
                buf.append ("</h3>");

                if (h instanceof ImageUserConfigurableObjectFieldViewEditHandler)
                {

                    File f = this.proj.getFile (val.toString ());

                    if (!f.exists ())
                    {

                        continue;

                    }

                    try
                    {

                        epubBook.getResources ().add (new Resource (new ByteArrayInputStream (UIUtils.getImageBytes (UIUtils.getImage (f.toPath ()))),
                                                                    f.getName ()));

                    } catch (Exception e) {

                        Environment.logError ("Unable to get image file: " +
                                              f,
                                              e);

                        continue;

                    }

                    buf.append ("<p><img src=\"");
                    buf.append (f.getName ());
                    buf.append ("\" /></p>");

                    continue;

                }

                StringWithMarkup sm = null;

                if (val instanceof String)
                {

                    sm = new StringWithMarkup ((String) val);

                }

                if (val instanceof Date)
                {

                    sm = new StringWithMarkup (Environment.formatDate ((Date) val));

                }

                if (val instanceof Number)
                {

                    sm = new StringWithMarkup (Environment.formatNumber ((Double) val));

                }

                if (val instanceof Set)
                {

                    sm = new StringWithMarkup (Utils.joinStrings ((Set<String>) val, null));

                }

                if (val instanceof StringWithMarkup)
                {

                    sm = (StringWithMarkup) val;

                }

                if (sm == null)
                {

                    continue;

                }

                TextIterator iter = new TextIterator (sm.getText ());

                for (Paragraph p : iter.getParagraphs ())
                {

                    buf.append ("<p>");

                    this.addParagraph (buf,
                                       p,
                                       sm.getMarkup ());

                    buf.append ("</p>");

                }

            }

        }

        return buf.toString ();

    }

    private void addParagraph (StringBuilder buf,
                               Paragraph     para,
                               Markup        markup)
    {

        Markup pm = new Markup (markup,
                                para.getAllTextStartOffset (),
                                para.getAllTextEndOffset ());

        pm.shiftBy (-1 * para.getAllTextStartOffset ());

        // Get the markup items.
        List<Markup.MarkupItem> items = pm.items;

        String ptext = para.getText ();
        int textLength = ptext.length ();

        if (items.size () > 0)
        {

            Markup.MarkupItem last = null;

            int start = -1;
            int end = -1;

            int lastEnd = 0;

            // Check the first item.
            Markup.MarkupItem fitem = items.get (0);

            if (fitem.start > 0)
            {

                start = 0;
                end = fitem.start;

                if (end > textLength)
                {

                    end = textLength;

                }

                // Add the start text.
                buf.append (ptext.substring (0, end));

                lastEnd = end;

            }

            for (Markup.MarkupItem item : items)
            {

                start = item.start;

                end = item.end;

                if (start > lastEnd)
                {

                    // Add some normal text.
                    buf.append (ptext.substring (lastEnd, Math.min(item.start, textLength)));

                }

                if ((end >= textLength)
                    ||
                    (end == -1)
                   )
                {

                    end = textLength;

                }

                StringBuilder cl = new StringBuilder ();

                if (item.bold)
                {

                    cl.append (" b");

                }

                if (item.italic)
                {

                    cl.append (" i");

                }

                if (item.underline)
                {

                    cl.append (" u");

                }

                buf.append ("<span class=\"");
                buf.append (cl);
                buf.append ("\">");
                buf.append (ptext.substring (start, end));
                buf.append ("</span>");

                lastEnd = end;

            }

            // Check the last item.
            Markup.MarkupItem litem = items.get (items.size () - 1);

            // Does it end before the end of the text, if so then add normal text.
            if (litem.end < textLength)
            {

                // Add the last text.
                buf.append (ptext.substring (litem.end));

            }

        } else {

            buf.append (ptext);

        }

    }

    private String getLanguageCode (Project p)
    {

        // Ref: http://docwiki.embarcadero.com/RADStudio/Tokyo/en/Language_Culture_Names,_Codes,_and_ISO_Values
        // The first part is the language, the second part is the culture.  Not sure how this affects how
        // the document content will behave though.
        return p.getLanguageCodeForSpellCheckLanguage ();

    }

}
