package com.quollwriter.exporter;

import java.awt.Color;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import nl.siegmann.epublib.epub.*;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Resource;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.text.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class EPUBDocumentExporter extends AbstractDocumentExporter
{

    // private ExportSettings settings = null;
    private ZipOutputStream zout = null;

    private JTextField author = null;
    private JTextField id = null;

    public EPUBDocumentExporter()
    {

    }

    public String getStartStage ()
    {

        return "select-items";

    }

    public WizardStep getStage (String stage)
    {

        final EPUBDocumentExporter _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("select-items"))
        {

            ws.title = getUIString (exportproject,stages,selectitems,title);
            //"Select the items you wish to export";

            ws.helpText = getUIString (exportproject,stages,selectitems,text);

            //ws.title = "Select the items you wish to export";

            //ws.helpText = "Select the items you wish to export, if you select any {chapters} then any associated {notes} and {outlineitems} will also be exported.  {Locations}, {characters}, {objects} and {researchitems} will be added as appendices.";

            this.initItemsTree (null);

            JScrollPane sp = new JScrollPane (this.itemsTree);

            sp.setOpaque (false);
            sp.getViewport ().setOpaque (false);
            sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            sp.setBorder (new LineBorder (new Color (127,
                                                     127,
                                                     127),
                                          1));

            ws.panel = sp;

        }

        if (stage.equals ("details"))
        {

            ws.title = getUIString (exportproject,stages,bookdetails,title);
            //"Enter the details about the book";
            ws.helpText = getUIString (exportproject,stages,bookdetails,text);
            //"You should provide either the ISBN of your book or a unique url for the book as the ID.";

            FormLayout fl = new FormLayout ("10px, right:p, 6px, 200px, fill:10px",
                                            "p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.author = UIUtils.createTextField ();

            builder.addLabel (getUIString (exportproject,stages,bookdetails,labels,authorname),
            //"Author Name",
                              cc.xy (2,
                                     1));
            builder.add (this.author,
                         cc.xy (4,
                                1));

            this.author.setText (this.proj.getProperty (Constants.AUTHOR_NAME_PROPERTY_NAME));

            builder.addLabel (getUIString (exportproject,stages,bookdetails,labels, LanguageStrings.id),
                            //"ID (ISBN/URL)",
                              cc.xy (2,
                                     3));

            this.id = UIUtils.createTextField ();

            builder.add (this.id,
                         cc.xy (4,
                                3));

            this.id.setText (this.proj.getProperty (Constants.BOOK_ID_PROPERTY_NAME));

            ws.panel = builder.getPanel ();

        }

        return ws;

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "select-items";

        }

        if (currStage.equals ("select-items"))
        {

            return "details";

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("details"))
        {

            return "select-items";

        }

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

    public void exportProject (File dir)
                        throws GeneralException
    {

        try
        {

            Project p = ExportUtils.getSelectedItems (this.itemsTree,
                                                      this.proj);

            // Create new Book
            nl.siegmann.epublib.domain.Book book = new nl.siegmann.epublib.domain.Book ();

            // Set the title
            book.getMetadata ().addTitle (this.proj.getName ());

            // Add an Author
            book.getMetadata ().addAuthor (new Author (this.author.getText ()));
            book.getMetadata ().setLanguage (this.getLanguageCode (this.proj));

            // Set cover image
            //book.getMetadata().setCoverImage(new Resource(Simple1.class.getResourceAsStream("/book1/test_cover.png"), "cover.png"));

            String css = Utils.getResourceFileAsString ("/data/export/epub/css-template.xml");

            css = StringUtils.replaceString (css,
                                             "[[FONT_NAME]]",
                                             UserProperties.get (Constants.EDITOR_FONT_PROPERTY_NAME));
            css = StringUtils.replaceString (css,
                                             "[[FONT_SIZE]]",
                                             UserProperties.getAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME) + "pt");
            css = StringUtils.replaceString (css,
                                             "[[LINE_SPACING]]",
                                             (100 * UserProperties.getAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME)) + "%");
            css = StringUtils.replaceString (css,
                                             "[[ALIGN]]",
                                             UserProperties.get (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME).toLowerCase ());

            String indent = "0px";

            if (UserProperties.getAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME))
            {

                indent = "5em";

            }

            css = StringUtils.replaceString (css,
                                             "[[INDENT]]",
                                             indent);

            book.getResources ().add (new Resource (new ByteArrayInputStream (css.getBytes ("utf-8")),
                                                    "main.css"));

            Book b = p.getBook (0);

            String cTemp = Utils.getResourceFileAsString ("/data/export/epub/chapter-template.xml");

            List<Chapter> chapters = b.getChapters ();

            int count = 0;

            for (Chapter c : chapters)
            {

                count++;

                String chapterText = StringUtils.replaceString (cTemp,
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

                chapterText = StringUtils.replaceString (chapterText,
                                                         "[[CONTENT]]",
                                                         ct.toString ());

                book.addSection (c.getName (),
                                 new Resource (new ByteArrayInputStream (chapterText.getBytes ("utf-8")),
                                               "chapter" + count + ".html"));

            }

            // TODO Make a constant/property...
            String appendixTemp = Utils.getResourceFileAsString ("/data/export/epub/appendix-template.xml");

            Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

            // Capital A.
            //int apxChar = 65;

            char apxChar = 'A';

            for (UserConfigurableObjectType type : assetTypes)
            {

                Set<Asset> as = p.getAssets (type);

                if ((as == null)
                    ||
                    (as.size () == 0)
                   )
                {

                    continue;

                }

                String apxC = Character.toString (apxChar);

                apxChar = (char) ((int) apxChar + 1);

                String cid = String.format ("appendix-%s-%s",
                                            apxC,
                                            type.getObjectTypeNamePlural ().replace (" ", "-"));

                String title = String.format (getUIString (exportproject,sectiontitles,appendix),
                                            //"Appendix %s - %s",
                                              apxC,
                                              type.getObjectTypeNamePlural ());

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (as,
                                                                   book));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ("utf-8")),
                                               cid + ".html"));

            }
/*
            // Get the locations.
            List<Location> locs = p.getLocations ();

            if (locs.size () > 0)
            {

                String cid = "appendix-b-locations";

                String title = "Appendix B - Locations";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (locs));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));

            }

            // Get the objects.
            List<QObject> objs = p.getQObjects ();

            if (objs.size () > 0)
            {

                String cid = "appendix-c-items";

                String title = "Appendix C - Items";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (objs));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));

            }

            // Get the research items.
            List<ResearchItem> res = p.getResearchItems ();

            if (res.size () > 0)
            {

                String cid = "appendix-d-research";

                String title = "Appendix D - Research";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (res));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));

            }
               */
            // Create EpubWriter
            EpubWriter epubWriter = new EpubWriter ();

            // Write the Book as Epub
            epubWriter.write (book, new FileOutputStream (new File (dir.getPath () + "/" + this.sanitizeName (this.proj.getName ()) + Constants.EPUB_FILE_EXTENSION)));

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

                Object val = h.getFieldValue ();

                if (val == null)
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

                        epubBook.getResources ().add (new Resource (new ByteArrayInputStream (UIUtils.getImageBytes (UIUtils.getImage (f))),
                                                                    f.getName ()));

                    } catch (Exception e) {

                        Environment.logError ("Unable to get image file: " +
                                              f,
                                              e);

                        continue;

                    }

                    buf.append ("<p><img src=\"");
                    buf.append (f.getName ());
                    buf.append (" /></p>");

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
