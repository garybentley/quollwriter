package com.quollwriter.ui;

import java.awt.event.*;

import java.util.*;

import java.text.*;

import javax.swing.*;
import javax.swing.border.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;


public class About extends PopupWindow
{

    public About (AbstractProjectViewer v)
    {

        super (v);

    }

    public String getWindowTitle ()
    {

        return "About";

    }

    public String getHeaderTitle ()
    {

        return "About Quoll Writer";

    }

    public String getHeaderIconType ()
    {

        return "about";

    }

    public String getHelpText ()
    {

        return null;

    }

    public void init ()
    {

        super.init ();

    }

    public JComponent getContentPanel ()
    {

        Box b = new Box (BoxLayout.Y_AXIS);

        b.setOpaque (false);
        
        FormLayout pfl = new FormLayout ("5px, right:p, 6px, fill:p:grow",
                                         "6px, p, 6px, p, 6px, p, 10px, p, 6px, p, 6px, p, 6px, p");

        PanelBuilder pbuilder = new PanelBuilder (pfl);

        CellConstraints cc = new CellConstraints ();

        int y = 2;

        pbuilder.addLabel ("Version",
                           cc.xy (2,
                                  y));

        pbuilder.addLabel (Environment.getQuollWriterVersion ().trim (),
                           cc.xy (4,
                                  y));

        y += 2;

        pbuilder.addLabel ("Copyright",
                           cc.xy (2,
                                  y));

        Date d = new Date ();

        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy");

        String year = sdf.format (d);

        pbuilder.addLabel ("\u00A9 2009-" + year + " Gary Bentley",
                           cc.xy (4,
                                  y));

        y += 2;

        pbuilder.addLabel ("Website",
                           cc.xy (2,
                                  y));

        pbuilder.add (UIUtils.createWebsiteLabel (Environment.quollWriterWebsite,
                                                  null,
                                                  false),
                      cc.xy (4,
                             y));

        y += 2;

        String relNotesUrl = Environment.getProperty (Constants.QUOLL_WRITER_RELEASE_NOTES_URL_PROPERTY_NAME);

        relNotesUrl = StringUtils.replaceString (relNotesUrl,
                                                 "[[VERSION]]",
                                                 Environment.getQuollWriterVersion ().trim ().replace ('.',
                                                                                                       '_'));

        pbuilder.add (UIUtils.createWebsiteLabel (relNotesUrl,
                                                  "Release Notes",
                                                  false),
                      cc.xy (4,
                             y));

        y += 2;

        pbuilder.add (UIUtils.createWebsiteLabel (Environment.getProperty (Constants.QUOLL_WRITER_ACKNOWLEDGMENTS_URL_PROPERTY_NAME),
                                                  "Acknowledgments",
                                                  false),
                      cc.xy (4,
                             y));

        y += 2;

        pbuilder.add (UIUtils.createWebsiteLabel (Environment.getProperty (Constants.QUOLL_WRITER_DEV_PLAN_URL_PROPERTY_NAME),
                                                  "Development Plan",
                                                  false),
                      cc.xy (4,
                             y));

        JPanel pan = pbuilder.getPanel ();

        pan.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        pan.setOpaque (false);

        b.add (pan);

        return b;

    }

    public JButton[] getButtons ()
    {

        final About _this = this;

        JButton closeBut = new JButton ();
        closeBut.setText ("Close");

        closeBut.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.close ();

            }

        });
        
        JButton[] buts = new JButton[1];
        buts[0] = closeBut;

        return buts;

    }

}
