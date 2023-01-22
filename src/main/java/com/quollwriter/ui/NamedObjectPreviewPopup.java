package com.quollwriter.ui;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Rectangle;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.components.ActionAdapter;

public class NamedObjectPreviewPopup extends HideablePopup<AbstractProjectViewer>
{

    private NamedObject obj = null;

    public NamedObjectPreviewPopup (AbstractProjectViewer viewer)
    {

        super (viewer);

    }

    /**
     * Not supported, call show with NamedObject instead.
     */
    @Override
    public void show (final int         showDelay,
                      final int         hideDelay,
                      final Point       po,
                      final ActionListener onHide)
    {

        throw new UnsupportedOperationException ("Not supported, use show(NamedObject,int,int,Point,ActionListener) instead.");

    }

    public void show (final NamedObject obj,
                      final int         showDelay,
                      final int         hideDelay,
                      final Point       po,
                      final ActionListener onHide)
    {

        if (obj == null)
        {

            return;

        }

        this.obj = obj;

        super.show (showDelay,
                    hideDelay,
                    po,
                    onHide);

    }

    @Override
    public JComponent getContent ()
    {

        if (this.obj == null)
        {

            throw new IllegalStateException ("No object set.");

        }

        // TODO: Make this nicer later.
        if (this.obj instanceof Chapter)
        {

            Chapter c = (Chapter) this.obj;

            JComponent t = UIUtils.getChapterInfoPreview (c,
                                                          null,
                                                          this.viewer);

            if (t == null)
            {

                // May be a fake chapter, return null.
                return null;

            }

            t.setSize (new Dimension (300,
                                      Short.MAX_VALUE));

            return t;

        } else {

            String firstLine = Environment.getUIString (LanguageStrings.project,
                                                        LanguageStrings.sidebar,
                                                        LanguageStrings.objectpreview,
                                                        LanguageStrings.assets,
                                                        LanguageStrings.novalue);
                                                        //"<b><i>No description.</i></b>";

            String t = (obj.getDescription () != null ? obj.getDescription ().getText () : null);

            if ((t != null)
                &&
                (t.length () > 0)
               )
            {

                firstLine = new Paragraph (t, 0).getFirstSentence ().getText ();

            }

            JEditorPane desc = UIUtils.createHelpTextPane (firstLine,
                                                           null);

            FormLayout fl = new FormLayout ("380px",
                                            "p");

            PanelBuilder pb = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            pb.add (desc, cc.xy (1, 1));

            desc.setAlignmentX (Component.LEFT_ALIGNMENT);

            JPanel p = pb.getPanel ();
            p.setOpaque (true);
            p.setBackground (UIUtils.getComponentColor ());

            return p;

        }

    }

}
