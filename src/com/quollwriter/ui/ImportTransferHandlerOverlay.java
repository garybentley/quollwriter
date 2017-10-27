package com.quollwriter.ui;

import java.awt.*;

import java.io.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.Header;

public class ImportTransferHandlerOverlay extends Box
{

	private Header header = null;

    public ImportTransferHandlerOverlay ()
    {

		this (null);

	}

    public ImportTransferHandlerOverlay (String displayText)
    {

		super (BoxLayout.Y_AXIS);

        this.setBackground (UIUtils.getComponentColor ());
		this.setOpaque (true);

		Box bb = new Box (BoxLayout.X_AXIS);

		bb.add (Box.createHorizontalGlue ());

		this.header = UIUtils.createHeader ((displayText != null ? displayText : Environment.getUIString (LanguageStrings.importfile)),
        //"Drop the file to begin the import"),
											Constants.PANEL_TITLE,
											Constants.PROJECT_IMPORT_ICON_NAME,
											null);

        this.header.setFont (this.header.getFont ().deriveFont (UIUtils.getScaledFontSize (20)).deriveFont (Font.PLAIN));
		this.header.setMaximumSize (this.header.getPreferredSize ());
		this.header.setAlignmentX (Component.CENTER_ALIGNMENT);
		this.header.setAlignmentY (Component.CENTER_ALIGNMENT);

		bb.setAlignmentX (Component.CENTER_ALIGNMENT);

		bb.add (this.header);

		this.add (Box.createVerticalGlue ());

		this.add (bb);

		this.add (Box.createVerticalGlue ());

    }

	public void setDisplayText (String t)
	{

		this.header.setTitle (t);
		this.header.setMaximumSize (this.header.getPreferredSize ());
		this.validate ();
		this.repaint ();

	}

    // TODO: Do something with the file
    public void setFile (File f)
    {

    }

}
