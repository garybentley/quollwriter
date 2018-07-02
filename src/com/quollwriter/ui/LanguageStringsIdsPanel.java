package com.quollwriter.ui;

import java.util.*;

import java.awt.Component;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.*;

public class LanguageStringsIdsPanel extends BasicQuollPanel<AbstractLanguageStringsEditor>
{

    private String parentId = null;
    private Set<Value> vals = null;
    private Node parent = null;
    private Set<Value> values = null;
    private Box content = null;
    private AbstractLanguageStringsEditor editor = null;

    public LanguageStringsIdsPanel (AbstractLanguageStringsEditor ed,
                                    Node                          parent,
                                    Set<Value>                    values)
    {

        super (ed,
               parent.getNodeId (),
               null);

        this.editor = ed;

        this.parent = parent;
        this.values = values;

        //this.node = this.editor.baseStrings.getNode (id);

        String title = (this.parent.getTitle () != null ? this.parent.getTitle () : this.parent.getNodeId ());

        this.setTitle (String.format ("%s (%s)",
                                      title,
                                      Environment.formatNumber (this.values.size ())));

        this.content = new ScrollableBox (BoxLayout.Y_AXIS);
        this.content.setAlignmentY (Component.TOP_ALIGNMENT);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);

    }

    @Override
    public String getPanelId ()
    {

        return this.parent.getNodeId ();

    }

    @Override
    public boolean isWrapContentInScrollPane ()
    {

        return true;

    }

    public void updatePreviews ()
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsIdBox)
            {

                LanguageStringsIdBox b = (LanguageStringsIdBox) c;

                b.showPreview ();

            }

        }

    }

    @Override
    public JComponent getContent ()
    {

        final LanguageStringsIdsPanel _this = this;

        this.buildForm (this.parent.getNodeId ());

        this.content.add (Box.createVerticalGlue ());

        this.updatePreviews ();

        return this.content;

    }

    public void saveValues ()
                     throws GeneralException
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsIdBox)
            {

                LanguageStringsIdBox b = (LanguageStringsIdBox) c;

                b.saveValue ();

            }

        }

    }

    public String getIdValue (String id)
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsIdBox)
            {

                LanguageStringsIdBox b = (LanguageStringsIdBox) c;

                if (b.getId ().equals (id))
                {

                    return b.getUserValue ();

                }

            }

        }

        return null;

    }

    public int getErrorCount ()
    {

        int c = 0;

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component co = this.content.getComponent (i);

            if (co instanceof LanguageStringsIdBox)
            {

                LanguageStringsIdBox b = (LanguageStringsIdBox) co;

                if (b.hasErrors ())
                {

                    c++;

                }

            }

        }

        return c;

    }

    public int getUserValueCount ()
    {

        int c = 0;

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component co = this.content.getComponent (i);

            if (co instanceof LanguageStringsIdBox)
            {

                LanguageStringsIdBox b = (LanguageStringsIdBox) co;

                if (b.hasUserValue ())
                {

                    c++;

                }

            }

        }

        return c;

    }

    private void createComment (String comment)
    {

        JComponent c = UIUtils.createLabel (comment);
        c.setAlignmentX (LEFT_ALIGNMENT);
        c.setBorder (UIUtils.createPadding (0, 15, 5, 5));

        this.content.add (c);

    }

    private void buildForm (String idPrefix)
    {

        // Check for the section comment.
        if (this.parent.getComment () != null)
        {

            this.createComment (this.parent.getComment ());

        }

        for (Value v : this.values)
        {

            if (v instanceof TextValue)
            {

                this.content.add (new LanguageStringsIdBox ((TextValue) v,
                                             (this.editor.userStrings.containsId (v.getId ()) ? this.editor.userStrings.getTextValue (v.getId ()) : null),
                                             this.editor)); // scount

            }

        }

    }

}
