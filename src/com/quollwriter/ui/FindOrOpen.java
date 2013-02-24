package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class FindOrOpen extends JFrame
{

    public static int SHOW_TITLE = 1;
    public static int SHOW_OPEN = 2;
    public static int SHOW_NEW = 4;
    public static int SHOW_ALL = FindOrOpen.SHOW_TITLE | FindOrOpen.SHOW_OPEN | FindOrOpen.SHOW_NEW;

    public static String SHOW_ALL_WINDOW_TITLE = "Open/Create New Project";
    public static String SHOW_OPEN_WINDOW_TITLE = "Open a Project";
    public static String SHOW_NEW_WINDOW_TITLE = "Create a New Project";

    private class ProjectTableModel extends AbstractTableModel
    {

        private java.util.List projs = null;

        public ProjectTableModel(java.util.List projs)
        {

            this.projs = projs;

        }

        public int getRowCount ()
        {

            return this.projs.size ();

        }

        public int getColumnCount ()
        {

            return 2;

        }

        public String getColumnName (int colInd)
        {

            if (colInd == 0)
            {

                return "Name";

            }

            return "Last Edited";

        }

        public Object getValueAt (int row,
                                  int col)
        {

            Project ps = (Project) this.projs.get (row);

            if (col == 0)
            {

                return ps.getName ();

            }

            if (ps.getLastEdited () == null)
            {

                return "Not yet edited";

            }

            return Utils.formatDate (ps.getLastEdited ());

        }

    }

    private JTextField     nameField = null;
    private JTextField     saveField = null;
    private JCheckBox      encryptField = null;
    private JPasswordField passwordField = null;
    private JPasswordField passwordField2 = null;

    public FindOrOpen(int show)
    {

        super ();

        String t = FindOrOpen.SHOW_ALL_WINDOW_TITLE;

        if (show == FindOrOpen.SHOW_NEW)
        {

            t = FindOrOpen.SHOW_NEW_WINDOW_TITLE;

        }

        if (show == FindOrOpen.SHOW_OPEN)
        {

            t = FindOrOpen.SHOW_OPEN_WINDOW_TITLE;

        }

        UIUtils.setFrameTitle (this,
                               t);

        this.setIconImage (Environment.getWindowIcon ().getImage ());

        this.setMinimumSize (new Dimension (500,
                                            0));

        final FindOrOpen _this = this;

        this.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel (new BorderLayout (),
                                     true);
/*
 * old
        content.setBorder (new CompoundBorder (new EmptyBorder (5,
                                                                5,
                                                                5,
                                                                5),
                                               new LineBorder (new Color (127,
                                                                          127,
                                                                          127),
                                                               1,
                                                               true)));
*/
        final Box b = new Box (BoxLayout.PAGE_AXIS);
        content.add (b);

        b.setOpaque (true);
        b.setBackground (Color.WHITE);
        /*
         * old
        b.setBorder (new EmptyBorder (20,
                                      20,
                                      20,
                                      20));
*/
        b.setBorder (new EmptyBorder (10,
                                      10,
                                      10,
                                      10));

        JLabel title = null;

        if ((show & FindOrOpen.SHOW_TITLE) == FindOrOpen.SHOW_TITLE)
        {

            // Create the header.
            title = new JLabel ("Welcome to " + Constants.QUOLL_WRITER_NAME);
            title.setFont (UIUtils.getHeaderFont ().deriveFont (20f));
            title.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            title.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                  0,
                                                                  1,
                                                                  0,
                                                                  new Color (127,
                                                                             127,
                                                                             127)),
                                                 new EmptyBorder (0,
                                                                  0,
                                                                  3,
                                                                  0)));
            b.add (title);

            String text = "Please select one of the options below.";

            if ((show & FindOrOpen.SHOW_OPEN) != 0)
            {

                b.add (UIUtils.createHelpTextPane (text));

            }

            b.add (Box.createVerticalStrut (10));

        }

        java.util.List pss = new ArrayList ();

        try
        {

            pss.addAll (Environment.getAllProjects ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to get all project stubs",
                                  e);

        }

        final java.util.List projs = pss;

        Collections.sort (projs,
                          new ProjectSorter ());

        if ((show & FindOrOpen.SHOW_OPEN) == FindOrOpen.SHOW_OPEN)
        {

            // Create the "open existing project" section.
            Header h = getHeader ("Open an existing Project",
                                  "open-project");

            b.add (h);

            b.add (UIUtils.createHelpTextPane ("Please select one of Projects/Books below.  Double click to open."));

            JPanel p = new JPanel (new BorderLayout ());
            p.setOpaque (false);
            p.setBorder (new EmptyBorder (0,
                                          10,
                                          5,
                                          10));
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            final JTable projOpenTable = new JTable ();

            projOpenTable.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            projOpenTable.setOpaque (false);
            projOpenTable.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

            projOpenTable.addKeyListener (new KeyAdapter ()
                {

                    public void keyPressed (KeyEvent ev)
                    {

                        if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                        {

                            Project p = (Project) projs.get (projOpenTable.getSelectedRow ());

                            if (_this.handleOpenProject (p))
                            {

                                _this.setVisible (false);
                                _this.dispose ();

                            }

                            ev.consume ();

                        }

                    }

                });

            projOpenTable.addMouseListener (new MouseAdapter ()
                {

                    public void mouseClicked (MouseEvent ev)
                    {

                        if (ev.getClickCount () == 2)
                        {

                            Project p = (Project) projs.get (projOpenTable.rowAtPoint (ev.getPoint ()));

                            if (_this.handleOpenProject (p))
                            {

                                _this.setVisible (false);
                                _this.dispose ();

                            }

                        }

                    }

                });

            ProjectTableModel pstm = new ProjectTableModel (projs);

            projOpenTable.setModel (pstm);

            JScrollPane s = new JScrollPane (projOpenTable);
            s.setBorder (new LineBorder (Environment.getBorderColor (),
                                         1));
            s.setOpaque (false);
            s.getViewport ().setOpaque (false);

            s.setMinimumSize (new Dimension (200,
                                             projOpenTable.getRowHeight () * 10));

            projOpenTable.setPreferredScrollableViewportSize (new Dimension (-1,
                                                                             projOpenTable.getRowHeight () * 10));

            projOpenTable.setFillsViewportHeight (true);

            p.add (s);

            b.add (p);

            b.add (Box.createVerticalStrut (5));

            JButton openBut = new JButton ();
            openBut.setText ("Open Project");

            openBut.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        int sel = projOpenTable.getSelectedRow ();

                        if (sel == -1)
                        {

                            UIUtils.showMessage (_this,
                                                 "Please select a Project.");

                            return;

                        }

                        Project p = (Project) projs.get (sel);

                        if (_this.handleOpenProject (p))
                        {

                            _this.setVisible (false);
                            _this.dispose ();

                        }

                    }

                });

            JButton cancelBut = new JButton ();
            cancelBut.setText ("Cancel");

            cancelBut.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.setVisible (false);
                        _this.dispose ();

                    }

                });

            JButton[] buts = { openBut, cancelBut };

            JPanel bp = ButtonBarFactory.buildLeftAlignedBar (buts);
            bp.setOpaque (false);
            bp.setBorder (new EmptyBorder (0,
                                           10,
                                           0,
                                           10));
            bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            b.add (bp);

            b.add (Box.createVerticalStrut (5));

        }

        if ((show & FindOrOpen.SHOW_NEW) == FindOrOpen.SHOW_NEW)
        {

            if ((show & FindOrOpen.SHOW_OPEN) == FindOrOpen.SHOW_OPEN)
            {

                b.add (Box.createVerticalStrut (20));

            }

            // Create the "new project" section.
            Header h = getHeader ("Create a New Project",
                                  "new-project");

            b.add (h);

            b.add (UIUtils.createHelpTextPane ("To create a new Project enter the name below, select the directory it should be saved to and press the Create button."));

            KeyAdapter k = new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        _this.handleCreateNewProject ();

                    }

                }

            };

            final FormLayout fl = new FormLayout ("right:p, 6px, fill:200px:grow, 2px, p",
                                                  "p, 6px, p, 6px, p, 6px, p, 6px, p");

            final PanelBuilder builder = new PanelBuilder (fl);

            final CellConstraints cc = new CellConstraints ();

            this.nameField = UIUtils.createTextField ();

            builder.addLabel ("Name",
                              cc.xy (1,
                                     1));
            builder.add (this.nameField,
                         cc.xy (3,
                                1));

            builder.addLabel ("Save In",
                              cc.xy (1,
                                     3));

            String defDir = null;

            if (projs.size () > 0)
            {

                Project p = (Project) projs.get (0);

                defDir = p.getProjectDirectory ().getParentFile ().getPath ();

            } else
            {

                File projsDir = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.DEFAULT_PROJECTS_DIR_NAME);

                projsDir.mkdirs ();

                defDir = projsDir.getPath ();

            }

            this.saveField = UIUtils.createTextField ();
            this.saveField.setText (defDir);

            this.nameField.addKeyListener (k);
            this.saveField.addKeyListener (k);

            builder.add (this.saveField,
                         cc.xy (3,
                                3));

            JButton findBut = new JButton (Environment.getIcon ("find",
                                                                Constants.ICON_MENU));

            findBut.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        JFileChooser f = new JFileChooser ();
                        f.setDialogTitle ("Select a Directory");
                        f.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
                        f.setApproveButtonText ("Select");
                        f.setCurrentDirectory (new File (_this.saveField.getText ()));

                        // Need to run: attrib -r "%USERPROFILE%\My Documents" on XP to allow a new directory
                        // to be created in My Documents.

                        if (f.showOpenDialog (_this) == JFileChooser.APPROVE_OPTION)
                        {

                            _this.saveField.setText (f.getSelectedFile ().getPath ());

                        }

                    }

                });

            builder.add (findBut,
                         cc.xy (5,
                                3));

            this.encryptField = new JCheckBox ("Encrypt this project?  You will be prompted for a password.");
            this.encryptField.setBackground (Color.WHITE);

            builder.add (this.encryptField,
                         cc.xyw (3,
                                 5,
                                 2));

            FormLayout pfl = new FormLayout ("right:p, 6px, 100px, 6px, p, 6px, fill:100px",
                                             "6px, p, 6px");

            PanelBuilder pbuilder = new PanelBuilder (pfl);

            this.passwordField = new JPasswordField ();

            pbuilder.addLabel ("Password",
                               cc.xy (1,
                                      2));

            pbuilder.add (this.passwordField,
                          cc.xy (3,
                                 2));

            this.passwordField2 = new JPasswordField ();

            pbuilder.addLabel ("Confirm",
                               cc.xy (5,
                                      2));

            pbuilder.add (this.passwordField2,
                          cc.xy (7,
                                 2));

            final JPanel ppanel = pbuilder.getPanel ();

            ppanel.setVisible (false);
            ppanel.setOpaque (false);

            builder.add (ppanel,
                         cc.xyw (3,
                                 7,
                                 2));

            this.encryptField.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        ppanel.setVisible (_this.encryptField.isSelected ());
                        
                        _this.getContentPane ().setPreferredSize (new Dimension (500,
                                                                                b.getPreferredSize ().height));
                        
                        _this.pack ();
                        _this.repaint ();
                    }

                });

            JButton createBut = new JButton ();
            createBut.setText ("Create");

            createBut.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.handleCreateNewProject ();

                    }

                });

            JButton cancelBut = new JButton ();
            cancelBut.setText ("Cancel");

            cancelBut.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.setVisible (false);
                        _this.dispose ();

                    }

                });

            JButton[] buts = { createBut, cancelBut };

            JPanel bp = ButtonBarFactory.buildRightAlignedBar (buts);
            bp.setOpaque (false);
            builder.add (bp,
                         cc.xy (3,
                                9));

            JPanel fp = builder.getPanel ();
            fp.setBorder (new EmptyBorder (5,
                                           20,
                                           0,
                                           30));
            fp.setOpaque (false);
            fp.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            b.add (fp);

            b.add (Box.createVerticalStrut (20));

        }

        this.getContentPane ().add (content);

        this.setResizable (false);

        this.getContentPane ().setPreferredSize (new Dimension (500,
                                                                this.getContentPane ().getPreferredSize ().height));

        this.pack ();

        UIUtils.setCenterOfScreenLocation (this);

        if (title != null)
        {

            title.setPreferredSize (new Dimension (500,
                                                   title.getPreferredSize ().height));
            title.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 title.getPreferredSize ().height));

        }

    }

    private boolean handleOpenProject (Project p)
    {

        try
        {

            Environment.openProject (p);

            return true;

        } catch (Exception e)
        {

            Environment.logError ("Unable to open project: " +
                                  p.getName (),
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to open project: " +
                                      p.getName ());

        }

        return false;

    }

    private void handleCreateNewProject ()
    {

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            UIUtils.showMessage (this,
                                 "Please provide a Name for the Project.");

            return;

        }

        // See if the project already exists.
        File pf = new File (saveField.getText () + "/" + Utils.sanitizeForFilename (n));

        if (pf.exists ())
        {

            UIUtils.showMessage (this,
                                 "A Project with name: " +
                                 n +
                                 " already exists.");

            return;

        }

        String pwd = null;

        if (this.encryptField.isSelected ())
        {

            // Make sure a password has been provided.
            pwd = new String (this.passwordField.getPassword ()).trim ();

            String pwd2 = new String (this.passwordField2.getPassword ()).trim ();

            if (pwd.equals (""))
            {

                UIUtils.showMessage (this,
                                     "Please provide a password for securing the Project files.");

                return;

            }

            if (pwd2.equals (""))
            {

                UIUtils.showMessage (this,
                                     "Please confirm your password.");

                return;

            }

            if (!pwd.equals (pwd2))
            {

                UIUtils.showMessage (this,
                                     "The passwords do not match.");

                return;

            }

        }

        Project proj = new Project (n);

        AbstractProjectViewer pj = null;

        try
        {

            pj = Environment.getProjectViewerForType (proj);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to create new project: " + proj.getName ());

            return;

        }

        try
        {

            pj.newProject (new File (saveField.getText ()),
                           n,
                           pwd);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to create new project: " + proj.getName ());

            return;

        }

        this.setVisible (false);
        this.dispose ();

    }

    private Header getHeader (String title,
                              String icon)
    {
        
        Icon ic = null;
        
        if (icon != null)
        {
            
            ic = Environment.getIcon (icon,
                                      Constants.ICON_POPUP);

        }
        
        Header h = new Header (title,
                               ic,
                               null);
        h.setFont (UIUtils.getHeaderFont ());
        
        // new
        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (16d)).deriveFont (Font.PLAIN));
        h.setPaintProvider (null);
        h.setTitleColor (UIUtils.getColor ("#333333"));
        h.setIcon (null);
        h.setPadding (null);
        // end new
        
        h.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        return h;
        
        
    }
    
}
