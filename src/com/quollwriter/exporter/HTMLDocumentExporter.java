package com.quollwriter.exporter;

import java.io.*;

import java.text.*;

import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

import org.docx4j.convert.out.html.*;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;


public class HTMLDocumentExporter extends MSWordDocXDocumentExporter
{

    // public static final String

    public static final String HEADING1 = "h1";
    public static final String TITLE = "title";
    public static final String P = "p";

    private JTree itemsTree = null;

    // Options:
    //
    // Export all chapters as a single page
    // Export as a single zip file

    protected void save (WordprocessingMLPackage wordMLPackage,
                         String                  name)
                  throws Exception
    {

        name = name.replace ('/',
                             '_');

        name = name.replace ('\\',
                             '_');

        name = Utils.sanitizeForFilename (name);                             
                             
        HtmlExporterNG export = new HtmlExporterNG ();

        export.setWmlPackage (wordMLPackage);

        FileOutputStream out = new FileOutputStream (new File (this.settings.outputDirectory.getPath () + "/" + name + Constants.HTML_FILE_EXTENSION));

        export.output (new javax.xml.transform.stream.StreamResult (out));

        out.flush ();
        out.close ();

    }
}
