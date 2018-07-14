package com.quollwriter.ui;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.concurrent.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.text.*;

public class LanguageStringsImageIdBox extends Box
{

    private AbstractLanguageStringsEditor editor = null;
    private ImageSelector userValue = null;
    private ImageValue baseValue = null;
    private ImageValue stringsValue = null;

    private JTextPane errors = null;
    private Box errorsWrapper = null;
    private JLabel errorsLabel = null;

    public LanguageStringsImageIdBox (final ImageValue                    baseValue,
                                      final ImageValue                    stringsValue,
                                      final AbstractLanguageStringsEditor editor)
    {

        super (BoxLayout.Y_AXIS);

        final LanguageStringsImageIdBox _this = this;

        this.editor = editor;
        this.baseValue = baseValue;
        this.stringsValue = stringsValue;

        Header h = UIUtils.createHeader (BaseStrings.toId (this.baseValue.getId ()),
                                         Constants.SUB_PANEL_TITLE);

        h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));
        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (h);

        String comment = this.baseValue.getComment ();

        Box b = new Box (BoxLayout.Y_AXIS);
        FormLayout   fl = new FormLayout ("right:60px, 5px, min(150px;p):grow",
                                          (comment != null ? "top:p, 6px," : "") + "top:p, 6px, top:p:grow, 6px, top:p, top:p, top:p");
        fl.setHonorsVisibility (true);
        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int r = 1;

        if (comment != null)
        {

            pb.addLabel ("<html><i>Comment</i></html>",
                         cc.xy (1, r));

            pb.addLabel ("<html>" + comment + "</html>",
                         cc.xy (3, r));

            r += 2;

        }

        pb.add (UIUtils.createClickableLabel ("View English Image Online",
                                              Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                   Constants.ICON_MENU),
                                              Environment.getQuollWriterWebsite () + this.baseValue.getUrl ()),
                cc.xy (3, r));

        r += 2;

        pb.addLabel ("<html><i>Your Image</i></html>",
                     cc.xy (1, r));

        this.userValue = new ImageSelector ((this.stringsValue != null ? this.stringsValue.getImageFile () : null),
                                            UIUtils.imageFileFilter,
                                            new Dimension (150,
                                                           150));
        this.userValue.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.userValue.setBorder (UIUtils.createLineBorder ());

        Box _b = new Box (BoxLayout.X_AXIS);
        _b.add (this.userValue);
        _b.add (Box.createHorizontalGlue ());

        pb.add (_b,
                cc.xy (3, r));

        r += 2;

        // Needed to prevent the performance hit
        this.errorsWrapper = new Box (BoxLayout.Y_AXIS);

        this.errorsLabel = UIUtils.createErrorLabel ("Errors");
        this.errorsLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
        this.errorsLabel.setVisible (false);
        this.errorsLabel.setIcon (null);
        this.errorsLabel.setFocusable (false);

        pb.add (this.errorsLabel,
                cc.xy (1, r));
        pb.add (this.errorsWrapper,
                cc.xy (3, r));

        r += 1;

        JPanel p = pb.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        p.setBorder (UIUtils.createPadding (5, 5, 0, 0));

        this.add (p);

        this.setBorder (UIUtils.createPadding (0, 10, 20, 10));
        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setAlignmentY (Component.TOP_ALIGNMENT);

    }

    public void saveValue ()
                    throws GeneralException
    {

        File uv = this.getUserValue ();

        if (uv != null)
        {

            if (this.stringsValue == null)
            {

                this.stringsValue = this.editor.userStrings.insertImageValue (this.baseValue.getId ());

            }

            this.stringsValue.setImageFile (uv);

        } else {

            this.editor.userStrings.removeNode (this.baseValue.getId ());

        }

    }

    public String getId ()
    {

        return BaseStrings.toId (this.baseValue.getId ());

    }

    public boolean hasUserValue ()
    {

        return this.getUserValue () != null;

    }

    public File getUserValue ()
    {

        return this.userValue.getFile ();

    }

    public boolean hasErrors ()
    {

        File s = this.getUserValue ();

        if (s == null)
        {

            return false;

        }

        return false;


    }

    public boolean showErrors (boolean requireUserValue)
    {

        File s = this.getUserValue ();

        if ((s == null)
            &&
            (!requireUserValue)
           )
        {

            this.errorsLabel.setVisible (false);
            this.errorsWrapper.setVisible (false);

            return false;

        }

        Set<String> errs = null;

        if (s == null)
        {

            errs = new LinkedHashSet<> ();

            errs.add ("Cannot show a preview, no value provided.");

        } else {

        }

        Node root = this.baseValue.getRoot ();

        this.editor.updateSideBar (this.baseValue);

        if (errs.size () > 0)
        {

            if (this.errors == null)
            {

                this.errors = UIUtils.createHelpTextPane ("",
                                                          this.editor);
                this.errors.setBorder (UIUtils.createPadding (6, 0, 0, 0));
                this.errors.setFocusable (false);
                this.errorsWrapper.add (this.errors);

            }

            StringBuilder b = new StringBuilder ();

            for (String e : errs)
            {

                if (b.length () > 0)
                {

                    b.append ("<br />");

                }

                b.append ("- " + e);

            }

            this.errors.setText ("<span class='error'>" + b.toString () + "</span>");
            this.errorsLabel.setVisible (true);
            this.errorsWrapper.setVisible (true);

            this.editor.updateSideBar (this.baseValue);

            return true;

        } else {

            this.errorsLabel.setVisible (false);
            this.errorsWrapper.setVisible (false);

        }

        return false;

    }

    public void showPreview ()
    {

        if (this.showErrors (false))
        {

            return;

        }

        File s = this.getUserValue ();

        if (s == null)
        {

            this.editor.updateSideBar (this.baseValue);

            return;

        }

        this.editor.updateSideBar (this.baseValue);

        this.validate ();
        this.repaint ();

    }

    @Override
    public Dimension getMaximumSize ()
    {

        return new Dimension (Short.MAX_VALUE,
                              this.getPreferredSize ().height);

    }

}
