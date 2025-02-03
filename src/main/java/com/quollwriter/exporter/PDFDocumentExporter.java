package com.quollwriter.exporter;

import java.io.*;

import java.text.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import org.docx4j.*;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;

import org.docx4j.wml.*;
import org.docx4j.fonts.*;

public class PDFDocumentExporter extends MSWordDocXDocumentExporter
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

        FileOutputStream out = new FileOutputStream (new File (this.settings.outputDirectory.getPath () + "/" + name + Constants.PDF_FILE_EXTENSION));

        wordMLPackage.setFontMapper (new IdentityPlusMapper (), true);

        Docx4J.toPDF (wordMLPackage,
                      out);

    }

}
