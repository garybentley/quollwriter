package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.*;

import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.tree.*;

import com.toedter.calendar.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;

public class SendProjectWizard extends Wizard<AbstractProjectViewer>
{

    private EditorEditor editor = null;
    private ActionListener onSend = null;
    private ProjectVersion projectVersion = null;

    private TextArea notes = null;
    private JDateChooser date = null;
    private JLabel error = null;
    private JTree chapterTree = null;
    private Box sendBox = null;
    private boolean firstSend = false;
    private JTextField newVerName = null;
    private JLabel newVerNameError = null;
    private JRadioButton createVersion = null;
    private JRadioButton selectDetails = null;

    public SendProjectWizard (AbstractProjectViewer viewer,
                              EditorEditor          ed,
                              ActionListener        onSend)
                       throws Exception
    {

        super (viewer);
        this.editor = ed;
        this.onSend = onSend;

        final SendProjectWizard _this = this;

        this.firstSend = !EditorsEnvironment.hasUserSentAProjectBefore ();
        this.firstSend = true;

        this.newVerNameError = UIUtils.createErrorLabel ("Please enter a name for the version.");

        this.newVerName = new JTextField ();

        this.newVerName.addKeyListener (new KeyAdapter ()
        {

            public void keyPressed (KeyEvent ev)
            {

                _this.enableButton ("next",
                                    false);

                if (_this.newVerName.getText ().trim ().length () > 0)
                {

                    _this.enableButton ("next",
                                        true);

                }

            }

        });
    }

    public String getFirstHelpText ()
    {

        return String.format ("Send your {project} to <b>%s</b>.",
                              this.editor.getShortName ());

    }

    public int getContentPreferredHeight ()
    {

        return 350;

    }

    public boolean handleFinish ()
    {

        Set<Chapter> chapters = new LinkedHashSet ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.chapterTree.getModel ().getRoot ();

        UIUtils.getSelectedObjects (root,
                                    chapters);

        String err = null;

        if (chapters.size () == 0)
        {

            // Show the error.
            err = "Please select at least 1 {chapter}.";

        }

        for (Chapter c : chapters)
        {

            AbstractEditorPanel qp = this.viewer.getEditorForChapter (c);

            if (qp != null)
            {

                if (qp.hasUnsavedChanges ())
                {

                    err = "One of the selected {chapters} has unsaved changes, please save your work before sending the {project}.";

                    break;

                }

            }

        }

        if (this.notes.getText ().trim ().length () > 5000)
        {

            err = "Notes can be a maximum of 5000 characters.";

        }

        if (err != null)
        {

            this.error.setText (err);
            this.error.setVisible (true);

            UIUtils.resizeParent (this);

            return false;

        }

        this.error.setVisible (false);

        // Show the sending.
        this.sendBox.setVisible (true);

        UIUtils.resizeParent (this);

        String n = this.notes.getText ().trim ();

        ProjectVersion pv = new ProjectVersion ();
        pv.setName (this.newVerName.getText ().trim ());
        pv.setDueDate (this.date.getDate ());
        pv.setDescription (new StringWithMarkup (n));

        // Need to snapshot the chapters.
        Set<Chapter> nchapters = null;

        try
        {

            nchapters = this.viewer.snapshotChapters (chapters,
                                                      pv);

        } catch (Exception e) {

            Environment.logError ("Unable to snapshot chapters: " +
                                  chapters,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to send new project to {editor}, please contact Quoll Writer support for assistance.");

            return false;

        }

        NewProjectMessage mess = new NewProjectMessage (this.viewer.getProject (),
                                                        nchapters,
                                                        pv,
                                                        this.editor,
                                                        this.editor.getEditorStatus () == EditorEditor.EditorStatus.pending ? EditorsEnvironment.getUserAccount () : null);

        // Since we are sending the message we have dealt with it.
        mess.setDealtWith (true);

        final SendProjectWizard _this = this;

        EditorsEnvironment.sendMessageToEditor (mess,
                                                // On send.
                                                new ActionListener ()
                                                {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        ProjectEditor pe = new ProjectEditor (_this.viewer.getProject (),
                                                                                              _this.editor);

                                                        pe.setStatus (ProjectEditor.Status.invited);
                                                        pe.setStatusMessage ("{Project} sent: " + Environment.formatDate (new Date ()));

                                                        // Add the editor to the list of editors
                                                        // for the project.  A little dangerous to do it here
                                                        // since it's not in the same transaction as the message.
                                                        // TODO: Maybe have the message have a "side-effect" or "after save"
                                                        // TODO: that will add the editor to the project in the same transaction.
                                                        try
                                                        {

                                                            EditorsEnvironment.addProjectEditor (pe);

                                                            _this.viewer.getProject ().addProjectEditor (pe);

                                                        } catch (Exception e) {

                                                            // Goddamn it!
                                                            // Nothing worse than having to show an error and success at the same time.
                                                            Environment.logError ("Unable to add editor: " +
                                                                                  _this.editor +
                                                                                  " to project: " +
                                                                                  _this.viewer.getProject (),
                                                                                  e);

                                                            UIUtils.showErrorMessage (_this.viewer,
                                                                                      "Unable to add {editor} " + _this.editor.getMainName () + " to the {project}.  Please contact Quoll Writer support for assistance.");

                                                        }

                                                        UIUtils.showMessage ((PopupsSupported) _this.viewer,
                                                                             "Your {project} has been sent",
                                                                             String.format ("Your {project} <b>%s</b> has been sent to <b>%s</b>",
                                                                                            _this.viewer.getProject ().getName (),
                                                                                            _this.editor.getMainName ()));

                                                        UIUtils.closePopupParent (_this);

                                                    }

                                                },
                                                // On cancel of login.
                                                new ActionListener ()
                                                {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        UIUtils.closePopupParent (_this);

                                                    }

                                                },
                                                null);

        return false;

    }

    public void handleCancel ()
    {

        final SendProjectWizard _this = this;

        // Check to see if we are sending to a pending editor.
        if (this.editor.isPending ())
        {

            InviteMessage invite = new InviteMessage (EditorsEnvironment.getUserAccount ());

            invite.setEditor (this.editor);

            // Send an invite.
            EditorsEnvironment.sendMessageToEditor (invite,
                                                    new ActionListener ()
                                                    {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            /*
                                                            TODO
                                                            AbstractViewer viewer = Environment.getFocusedViewer ();

                                                            UIUtils.showMessage ((PopupsSupported) viewer,
                                                                                 "Invite sent",
                                                                                 String.format ("An invite has been sent to: <b>%s</b>.",
                                                                                                _this.editor.getEmail ()),
                                                                                 "Ok, got it",
                                                                                 null);
                                                            */
                                                        }

                                                    },
                                                    null,
                                                    null);

        }

    }

    @Override
    public String getNextButtonLabel (String currStage)
    {

        if (currStage.equals ("details"))
        {

            return "Send";

        }

        return super.getNextButtonLabel (currStage);

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "choose";

        }

        if (currStage.equals ("newversionname"))
        {

            if (this.firstSend)
            {

                return "first.selectchapters";

            }

            return "details";

        }

        if (currStage.equals ("first"))
        {

            if (this.createVersion.isSelected ())
            {

                return "newversionname";

            }

            return "details";

        }

        if (currStage.equals ("first.selectchapters"))
        {

            return "first.notes";

        }

        if (currStage.equals ("first.notes"))
        {

            return "first.create";

        }

        if (currStage.equals ("first.create"))
        {

            return "details";

        }

        if (currStage.equals ("choose"))
        {

            return "details";

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("details"))
        {

            if (this.selectDetails.isSelected ())
            {

                if (this.firstSend)
                {

                    return "first";

                }

                return "choose";

            }

            if (this.createVersion.isSelected ())
            {

                return "newversionname";

            }

            return "choose";

        }

        if (currStage.equals ("first.selectchapters"))
        {

            return "newversionname";

        }

        if (currStage.equals ("first.notes"))
        {

            return "first.selectchapters";

        }

        if (currStage.equals ("newversionname"))
        {

            if (this.firstSend)
            {

                return "first";

            }

            return "choose";

        }

        if (currStage.equals ("details"))
        {

            return "choose";

        }

        return null;

    }

    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {

        if (newStage.equals ("first"))
        {

            this.enableButton ("next",
                               false);

        }

        if (newStage.equals ("newversionname"))
        {

            this.newVerName.grabFocus ();

            this.enableButton ("next",
                               false);

        }

        if (newStage.equals ("first"))
        {

            return true;

        }

        if (newStage.equals ("choose"))
        {

            return true;

        }

        if (oldStage != null)
        {

            if (oldStage.equals ("newversionname"))
            {

                this.newVerNameError.setVisible (false);

                if (this.newVerName.getText ().length () == 0)
                {

                    this.newVerNameError.setText ("Please enter a name for the version.");

                    this.newVerNameError.setVisible (true);

                    return false;

                } else {

                    try
                    {

                        if (this.viewer.getProjectVersion (this.newVerName.getText ().trim ()) != null)
                        {

                            this.newVerNameError.setText ("Already have a version with that name.");

                            this.newVerNameError.setVisible (true);

                            return false;

                        }

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project version with name: " +
                                              this.newVerName.getText (),
                                              e);

                        UIUtils.showErrorMessage (this.viewer,
                                                  "Unable to check the version name.");

                        return false;

                    }

                }

            }

        }

        return true;

    }

    public String getStartStage ()
    {

        if (this.firstSend)
        {

            return "first";

        }

        //return "choose";

    return "first";

    }

    public WizardStep getStage (String stage)
    {

        final SendProjectWizard _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("first.notes"))
        {

            ws.title = "Finally, add some notes";

            FormLayout fl = new FormLayout ("5px, fill:410px:grow, 10px",
                                            "p, 10px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            JTextPane desc = UIUtils.createHelpTextPane ("The final step in creating a version is adding some notes about what you would like the {editor} to do.  It's a description of what you would like them to focus on while editing and areas they should pay attention to (or not).<br /><br />Leave this bit blank if you want to give individual instructions for each {editor}.",
                                                         this.viewer);
            desc.setBorder (null);

            int row = 1;

            builder.add (desc,
                         cc.xy (2,
                                row));

            row += 2;

            this.notes = new TextArea (null,
                                       4,
                                       5000);

            builder.add (notes,
                         cc.xy (2,
                                row));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setAlignmentY (JComponent.TOP_ALIGNMENT);

            ws.panel = p;

        }

        if (stage.equals ("first.selectchapters"))
        {

            ws.title = "Select the {chapters}";

            FormLayout fl = new FormLayout ("5px, fill:410px:grow, 10px",
                                            "p, 10px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            JTextPane desc = UIUtils.createHelpTextPane ("Select the {chapters} that you would like to be part of the version.  But don't worry, the version only acts as a template for what you send.  You can always change what you actually send to {an editor} when you send it.",
                                                         this.viewer);
            desc.setBorder (null);

            int row = 1;

            builder.add (desc,
                         cc.xy (2,
                                row));

            row += 2;

            this.chapterTree = UIUtils.createSelectableTree ();

            Project proj = this.viewer.getProject ();

            final DefaultMutableTreeNode root = UIUtils.createTreeNode (proj,
                                                                        null,
                                                                        null,
                                                                        true);

            if (proj.getBooks ().size () == 1)
            {

                Book b = (Book) proj.getBooks ().get (0);

                DefaultMutableTreeNode broot = UIUtils.createTreeNode (b,
                                                                       null,
                                                                       null,
                                                                       true);

                //root.add (broot);

                // Get the chapters.
                List<Chapter> chaps = b.getChapters ();

                for (Chapter c : chaps)
                {

                    DefaultMutableTreeNode node = UIUtils.createTreeNode (c,
                                                                          null,
                                                                          null,
                                                                          true);

                    SelectableDataObject s = (SelectableDataObject) node.getUserObject ();

                    s.selected = true;

                    if (node == null)
                    {

                        continue;

                    }

                    root.add (node);

                }

            } else
            {

                List<Book> books = proj.getBooks ();

                for (int i = 0; i < books.size (); i++)
                {

                    Book b = books.get (i);

                    DefaultMutableTreeNode node = UIUtils.createTree (b,
                                                                      null,
                                                                      null,
                                                                      true);

                    if (node == null)
                    {

                        continue;

                    }

                    root.add (node);

                }

            }

            this.chapterTree.setModel (new DefaultTreeModel (root));

            UIUtils.expandAllNodesWithChildren (this.chapterTree);

            JComponent t = this.chapterTree;

            if (this.chapterTree.getPreferredSize ().height > 300)
            {

                JScrollPane sp = UIUtils.createScrollPane (this.chapterTree);
                sp.setOpaque (false);
                //sp.setBorder (null);

                sp.setPreferredSize (new Dimension (200,
                                                    300));

                t = sp;

            }

            builder.add (t,
                         cc.xy (2,
                                row));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setAlignmentY (JComponent.TOP_ALIGNMENT);

            ws.panel = p;

        }

        if (stage.equals ("newversionname"))
        {

            ws.title = "Select a name for the version";

            int row = 1;

            FormLayout fl = new FormLayout ("5px, fill:410px:grow, 10px",
                                            "p, 10px, p, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            JTextPane desc = UIUtils.createHelpTextPane ("Version names should be meanginful to you, for example <b>1st Draft</b>, <b>2nd Draft</b>.  You can also create special versions just for your {editor}, for example <b>Jason first edit</b>.  The only restriction is that the name must be unique across all versions you create for this {project}.<br /><br />Enter the name of the version in the box below.",
                                                         this.viewer);
            desc.setBorder (null);

            builder.add (desc,
                         cc.xy (2,
                                row));

            row += 2;

            this.newVerNameError.setVisible (false);
            this.newVerNameError.setBorder (UIUtils.createPadding (0, 0, 5, 5));

            UIUtils.addDoActionOnReturnPressed (this.newVerName,
                                                new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (_this.firstSend)
                    {

                        _this.showStage ("first.selectchapters");

                        return;

                    }

                    _this.showStage ("details");

                }

            });

            this.newVerName.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    _this.newVerNameError.setVisible (false);

                    _this.enableButton ("next",
                                        true);

                }

            });

            builder.add (this.newVerNameError,
                         cc.xy (2,
                                row));

            row++;

            builder.add (this.newVerName,
                         cc.xy (2,
                                row));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setAlignmentY (JComponent.TOP_ALIGNMENT);

            ws.panel = p;

            this.enableButton ("next",
                               false);

        }

        if (stage.equals ("first"))
        {

            ws.title = "Sending your first project";

            int row = 1;

            FormLayout fl = new FormLayout ("5px, fill:410px:grow, 10px",
                                            "p, 10px, p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            JTextPane desc = UIUtils.createHelpTextPane ("It is recommended that you send a <i>version</i> of your {project}.  A version is just a snapshot of your {chapters} with a name you provide.  It acts as a template or shortcut for what you send to {editors}.<br /><br />For example you might have a <b>1st Draft</b> version or a <b>Ready for edit</b> version and so on.  The name is a label that helps you remember what you sent and makes any comments you receive from {editors} more meaningful.<br /><br />A version helps ensure you send the same thing to your {editors} (although you can tweak what you send).<br /><br />So, pick an option below to continue.",
                                                         this.viewer);
            desc.setBorder (null);

            builder.add (desc,
                         cc.xy (2,
                                row));

            row += 2;

            this.createVersion = new JRadioButton ("Create a version");

            this.createVersion.addActionListener (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showStage ("newversionname");

                    UIUtils.doLater (new ActionListener ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.newVerName.grabFocus ();

                        }

                    });

                }

            });

            this.createVersion.setOpaque (false);

            this.selectDetails = new JRadioButton (Environment.replaceObjectNames ("I'll select the {chapters} to send"));

            this.selectDetails.addActionListener (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showStage ("details");

                }

            });

            this.selectDetails.setOpaque (false);

            ButtonGroup bg = new ButtonGroup ();

            bg.add (this.createVersion);
            bg.add (this.selectDetails);

            builder.add (this.createVersion,
                         cc.xy (2,
                                row));

            row += 2;

            builder.add (this.selectDetails,
                         cc.xy (2,
                                row));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setAlignmentY (JComponent.TOP_ALIGNMENT);

            ws.panel = p;

            this.enableButton ("next",
                               false);

        }

        if (stage.equals ("choose"))
        {

            ws.title = "Select what you would like to do";

            FormLayout fl = new FormLayout ("10px, p, 10px",
                                            "p, 6px, p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.createVersion = new JRadioButton ("Create a new version");

            this.createVersion.setOpaque (false);

            JRadioButton b2 = new JRadioButton (Environment.replaceObjectNames ("Select the {chapters} to send"));

            b2.setOpaque (false);

            ButtonGroup bg = new ButtonGroup ();

            bg.add (this.createVersion);
            bg.add (b2);

            int row = 1;

            builder.add (this.createVersion,
                         cc.xy (2,
                                row));

            row += 2;

            builder.add (b2,
                         cc.xy (2,
                                row));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            ws.panel = p;

        }

        if (stage.equals ("details"))
        {

            Box content = new Box (BoxLayout.Y_AXIS);

            ws.title = "Select what you would like to send";
            ws.alwaysRefreshPanel = true;

            JTextPane desc = UIUtils.createHelpTextPane ("You can select a date indicating when you would like the editing to be completed, but be reasonable, {editors} have lives too!",
                                                         this.viewer);
            desc.setBorder (null);

            desc.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            content.add (desc);

            this.error = UIUtils.createErrorLabel ("");
            this.error.setBorder (UIUtils.createPadding (10, 10, 0, 10));
            this.error.setVisible (false);

            content.add (this.error);



            String verName = (this.projectVersion != null ? this.projectVersion.getName () : this.newVerName.getText ().trim ());

            boolean hasVersion = verName.length () > 0;

            String verRow = "";

            if (this.createVersion.isSelected ())
            {

                verRow = "p, 6px,";

            }

            FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                            "10px, " + verRow + " top:p, 6px, p, 6px, top:min(150px;p), 0px");

            fl.setHonorsVisibility (true);
            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            int row = 2;

            if (hasVersion)
            {

                builder.addLabel (Environment.replaceObjectNames ("Version"),
                                  cc.xy (2,
                                         row));

                builder.addLabel (verName,
                                  cc.xy (4,
                                         row));

                row += 2;

            }

            builder.addLabel (Environment.replaceObjectNames ("Notes"),
                              cc.xy (2,
                                     row));

            this.notes = new TextArea (Environment.replaceObjectNames ("Tell your {editor} about your {project}/book/story here.  Also add instructions/hints on what you are wanting them to do, what to look at specifically and so on."),
                                                 4,
                                                 5000);

            builder.add (notes,
                         cc.xy (4,
                                row));

            row += 2;

            builder.addLabel (Environment.replaceObjectNames ("Due by"),
                              cc.xy (2,
                                     row));

            Calendar cal = new GregorianCalendar ();
            cal.add (Calendar.DATE,
                     7);

            this.date = new JDateChooser (cal.getTime ());

            this.date.setMinSelectableDate (new Date ());
            this.date.getCalendarButton ().setMargin (new java.awt.Insets (3, 3, 3, 3));
            this.date.setIcon (Environment.getIcon ("date",
                                                    16));

            Box calBox = new Box (BoxLayout.X_AXIS);
            calBox.add (this.date);
            this.date.setMaximumSize (this.date.getPreferredSize ());
            builder.add (calBox,
                         cc.xy (4,
                                row));

            row += 2;

            builder.addLabel (Environment.replaceObjectNames ("{Chapters}"),
                              cc.xy (2,
                                     row));

            this.chapterTree = UIUtils.createSelectableTree ();

            Project p = this.viewer.getProject ();

            final DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                                        null,
                                                                        null,
                                                                        true);

            if (p.getBooks ().size () == 1)
            {

                Book b = (Book) p.getBooks ().get (0);

                DefaultMutableTreeNode broot = UIUtils.createTreeNode (b,
                                                                       null,
                                                                       null,
                                                                       true);

                //root.add (broot);

                // Get the chapters.
                List<Chapter> chaps = b.getChapters ();

                for (Chapter c : chaps)
                {

                    DefaultMutableTreeNode node = UIUtils.createTreeNode (c,
                                                                          null,
                                                                          null,
                                                                          true);

                    SelectableDataObject s = (SelectableDataObject) node.getUserObject ();

                    s.selected = true;

                    if (node == null)
                    {

                        continue;

                    }

                    root.add (node);

                }

            } else
            {

                List<Book> books = p.getBooks ();

                for (int i = 0; i < books.size (); i++)
                {

                    Book b = books.get (i);

                    DefaultMutableTreeNode node = UIUtils.createTree (b,
                                                                      null,
                                                                      null,
                                                                      true);

                    if (node == null)
                    {

                        continue;

                    }

                    root.add (node);

                }

            }

            this.chapterTree.setModel (new DefaultTreeModel (root));

            UIUtils.expandAllNodesWithChildren (this.chapterTree);

            JComponent t = this.chapterTree;

            if (this.chapterTree.getPreferredSize ().height > 200)
            {

                JScrollPane sp = UIUtils.createScrollPane (this.chapterTree);
                sp.setOpaque (false);
                //sp.setBorder (null);

                sp.setPreferredSize (new Dimension (200,
                                                    170));

                t = sp;

            }

            builder.add (t,
                         cc.xy (4,
                                row));

            row++;

            JLabel sending = UIUtils.createLoadingLabel ("Sending {project}");
            sending.setText (Environment.replaceObjectNames ("Sending {project}..."));
            sending.setVisible (true);

            this.sendBox = new Box (BoxLayout.X_AXIS);
            this.sendBox.setBorder (UIUtils.createPadding (10, 0, 0, 0));
            this.sendBox.add (sending);
            this.sendBox.setVisible (false);

            builder.add (this.sendBox,
                         cc.xy (4,
                                row));

            JPanel panel = builder.getPanel ();
            panel.setOpaque (false);
            panel.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            content.add (panel);

            ws.panel = content;

            UIUtils.doLater (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    //_this.notes.getTextArea ().getCaret ().setDot (0);
                    //_this.notes.grabFocus ();

                    UIUtils.resizeParent (_this);

                }

            });

        }

        return ws;

    }

}
