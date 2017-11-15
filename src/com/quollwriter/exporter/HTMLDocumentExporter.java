package com.quollwriter.exporter;

import java.io.*;

import java.util.*;

import com.quollwriter.*;

import org.docx4j.*;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;

public class HTMLDocumentExporter extends MSWordDocXDocumentExporter
{

    protected void save (WordprocessingMLPackage wordMLPackage,
                         String                  name)
                  throws Exception
    {

        name = name.replace ('/',
                             '_');

        name = name.replace ('\\',
                             '_');

        name = Utils.sanitizeForFilename (name);

        FileOutputStream out = new FileOutputStream (new File (this.settings.outputDirectory.getPath () + "/" + name + Constants.HTML_FILE_EXTENSION));

        Docx4J.toHTML (wordMLPackage,
                       null,
                       null,
                       out);

    }
}
