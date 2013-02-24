package com.quollwriter.ui;

import java.awt.BorderLayout;

import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class ChapterDetailsEditPanel extends DetailsEditPanel
{

    private JTextArea   goalsEdit = null;
    private JPanel      goals = null;
    private JScrollPane goalsSP = null;

    public ChapterDetailsEditPanel(Chapter       c,
                                   ProjectViewer pv)
    {

        super (c,
               pv);

        this.goalsEdit = UIUtils.createTextArea (5);

        this.goals = new JPanel (new BorderLayout ());
        this.goals.setOpaque (false);

        this.goalsSP = new JScrollPane (this.goals);
        this.goalsSP.setOpaque (false);
        this.goalsSP.getViewport ().setOpaque (false);
        this.goalsSP.setBorder (null);

    }

    public String getEditHelpText ()
    {

        return "Enter each goal on a separate line.";

    }

    public void fillForSave ()
    {

        Chapter c = (Chapter) this.object;

        String g = this.goalsEdit.getText ().trim ();
        
        if (g.length () == 0)
        {
            
            g = null;
            
        }

        c.setGoals (g);

    }

    public void fillForEdit ()
    {

        Chapter chapter = (Chapter) this.object;

        if (chapter.getGoals () != null)
        {

            this.goalsEdit.setText (chapter.getGoals ().trim ());

        } else
        {

            this.goalsEdit.setText ("");

        }

    }

    public boolean canSave ()
    {

        return true;

    }

    public List<FormItem> getExtraEditItems ()
    {

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem ("Goals",
                                 this.goalsEdit,
                                 "fill:60dlu"));

        return items;

    }

    public List<FormItem> getExtraViewItems ()
    {

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem ("Goals",
                                 this.goalsSP));

        return items;

    }

    public void refreshViewPanel ()
    {

        this.goals.removeAll ();

        Chapter c = (Chapter) this.object;

        String gs = c.getGoals ();

        if (gs == null)
        {
        
            return;

        }

        StringBuilder layoutRows = new StringBuilder ();

        StringTokenizer t = new StringTokenizer (gs,
                                                 String.valueOf ('\n'));

        while (t.hasMoreTokens ())
        {

            if (layoutRows.length () > 0)
            {

                layoutRows.append (",");

            }

            layoutRows.append ("top:p, 6px");

            t.nextToken ();

        }

        FormLayout fl = new FormLayout ("3px, pref, 5px, fill:pref:grow, 3px",
                                        layoutRows.toString ());

        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        t = new StringTokenizer (gs,
                                 String.valueOf ('\n'));

        int r = 1;

        while (t.hasMoreTokens ())
        {

            pb.add (new JLabel (Environment.getIcon ("bullet",
                                                     Constants.ICON_MENU)),
                    cc.xy (2,
                           r));

            JTextArea ta = UIUtils.createTextArea (-1);
            ta.setMargin (null);
            ta.setText (t.nextToken ().trim ());
            ta.setEditable (false);
            ta.setOpaque (false);

            pb.add (ta,
                    cc.xy (4,
                           r));

            r += 2;

        }

        JPanel p = pb.getPanel ();

        p.setOpaque (false);

        this.goals.add (p,
                        BorderLayout.CENTER);

    }

}
