package com.quollwriter.ui;

import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class ConvertWarmupToProject extends PopupWindow
{

    private String          text = null;
    private Warmup          warmup = null;
    private NewProjectPanel newProjectPanel = null;

    public ConvertWarmupToProject(AbstractProjectViewer pv,
                                  Warmup                w,
                                  String                text)
    {

        super (pv);

        this.text = text;
        this.warmup = w;

        this.newProjectPanel = new NewProjectPanel ();

    }

    public String getWindowTitle ()
    {

        return "Convert Warm-up to Project";

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return Project.OBJECT_TYPE;

    }

    public String getHelpText ()
    {

        return "To create the new Project enter the name below, select the directory it should be saved to and press the Create button.  Your warm-up text will be added as a chapter to the Project.";

    }

    public JComponent getContentPanel ()
    {
/*
        Box b = new Box (BoxLayout.Y_AXIS);

        JCheckBox cb = new JCheckBox ("Delete the Warm-up once the Project has been created.");
        cb.setOpaque (false);

        b.add (cb);

        b.add (Box.createVerticalStrut (5));
*/
        return this.newProjectPanel.createPanel (this);
               /*
               c.setAlignmentX (Component.LEFT_ALIGNMENT);

               b.add (c);

               return b;
                */
    }

    public JButton[] getButtons ()
    {

        final ConvertWarmupToProject _this = this;

        JButton b = new JButton ("Create");
        JButton c = new JButton ("Cancel");

        b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (!_this.newProjectPanel.checkForm (_this))
                    {

                        return;

                    }

                    ProjectViewer pj = new ProjectViewer ();

                    Project p = new Project (_this.newProjectPanel.getName ());

                    Book b = new Book (p,
                                       p.getName ());

                    // Create a new chapter for the book.
                    Chapter c = new Chapter (b,
                                             Environment.getProperty (Constants.DEFAULT_CHAPTER_NAME_PROPERTY_NAME));

                    b.addChapter (c);

                    c.setName (_this.warmup.getChapter ().getName ());
                    c.setText (_this.text);

                    p.addBook (b);

                    String pwd = _this.newProjectPanel.getPassword ();

                    try
                    {

                        pj.newProject (_this.newProjectPanel.getSaveDirectory (),
                                       p,
                                       pwd);

                        pj.createActionLogEntry (p,
                                                 "Project created from warmup, prompt id: " +
                                                 _this.warmup.getPrompt ().getId () +
                                                 " and chapter: " +
                                                 _this.warmup.getChapter ().getName () +
                                                 " (" +
                                                 _this.warmup.getChapter ().getKey () +
                                                 ")");

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to create new project: " +
                                              p,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to create new project: " + p.getName ());

                        return;

                    }

                    _this.close ();

                }

            });

        c.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[2];
        buts[0] = b;
        buts[1] = c;

        return buts;

    }

}
