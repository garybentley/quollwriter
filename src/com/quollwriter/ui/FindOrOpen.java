package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.editors.*;

import org.jdom.*;

/**
 * It is recommended you no longer use this class, it can prevent the JVM shutting
 * down when used for creating a project (a problem I haven't tracked down).
 *
 * The functionality it provides has been superceeded by Landing and UIUtils.showAddNewProject.
 *
 * TODO: This class will be removed in a future release, use at your own risk!
 * @Deprecated
 */
public class FindOrOpen extends PopupWindow //JFrame
{

    public static int SHOW_OPEN = 2;
    public static int SHOW_NEW = 4;

    public static String SHOW_OPEN_WINDOW_TITLE = "Open a {Project}";
    public static String SHOW_NEW_WINDOW_TITLE = "Create a New {Project}";
    public static String SHOW_ALL_WINDOW_TITLE = "Open or create a {Project}";
    
    private class ProjectTableModel extends AbstractTableModel
    {

        private java.util.List projs = null;
        private Project loading = null;

        public ProjectTableModel(java.util.List projs)
        {

            this.projs = projs;

        }

        public void removeProject (Project p)
        {
            
            int r = this.projs.indexOf (p);
            
            this.projs.remove (p);
            
            this.fireTableRowsDeleted (r, r);
            
        }
        
        public void setLoading (Project p)
        {
            
            this.loading = p;
            
        }
        
        public Class getColumnClass (int colInd)
        {
            
            if (colInd == 0)
            {
                
                return ImageIcon.class;
                
            }
            
            if (colInd == 2)
            {
                
                return String.class;
                
            }
            
            return String.class;
            
        }
        
        public int getRowCount ()
        {

            return this.projs.size ();

        }

        public int getColumnCount ()
        {

            return 3;

        }

        public String getColumnName (int colInd)
        {

            if (colInd == 0)
            {

                return "";

            }

            if (colInd == 1)
            {
                
                return "Name";
                
            }
            
            return "Last Edited";

        }

        public Object getValueAt (int row,
                                  int col)
        {

            Project ps = (Project) this.projs.get (row);

            // Check to see if the project is encrypted and/or is valid (i.e. project directory is
            // available).
            if (col == 0)
            {
                
                ImageIcon l = null;
                
                if (!ps.getProjectDirectory ().exists ())
                {
                    
                    // Return a problem icon.
                    l = Environment.getIcon (Constants.ERROR_ICON_NAME,
                                             Constants.ICON_TREE);
                    
                }
                
                if ((l == null)
                    &&
                    (ps.isEncrypted ())
                   )
                {
                    
                    // Return the lock icon.
                    l = Environment.getIcon (Constants.LOCK_ICON_NAME,
                                             Constants.ICON_TREE);
                    
                }
                
                if ((l == null)
                    &&
                    (ps.isEditorProject ())
                   )
                {
                    
                    // Return an editor icon.
                    l = Environment.getIcon (Constants.EDITORS_ICON_NAME,
                                             Constants.ICON_TREE);

                }
                
                if ((l == null)
                    &&
                    (ps.isWarmupsProject ())
                   )
                {
                    
                    // Return an editor icon.
                    l = Environment.getIcon (Constants.WARMUPS_ICON_NAME,
                                             Constants.ICON_TREE);

                }

                /*
                if (ps == this.loading)
                {
Won't animate due to rubber stamping in table renderer.
                    l = Environment.getLoadingIcon ();
                    
                }
                */
                return l;
                
            }
            
            if (col == 1)
            {

                return ps.getName ();

            }

            if (ps.getLastEdited () == null)
            {

                return "N/A";

            }

            return Utils.formatDate (ps.getLastEdited ());

        }

    }

    private String windowTitle = null;
    private int show = -1;
    private JTable projOpenTable = null;

    public JComponent getContentPanel ()
    {

        Box b = new Box (BoxLayout.Y_AXIS);    

        final FindOrOpen _this = this;

        final ActionListener closeAction = new ActionListener ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.close ();
                
            }
            
        };
        
        java.util.List pss = new ArrayList ();

        try
        {

            //pss.addAll (Environment.getAllProjects ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to get all project stubs",
                                  e);

        }

        final java.util.List projs = pss;        
        
        Collections.sort (projs,
                          new ProjectSorter ());
                          
        if (((show & FindOrOpen.SHOW_OPEN) == FindOrOpen.SHOW_OPEN)
            &&
            (projs.size () > 0)
           )
        {

            Header h = UIUtils.createHeader ("Open an existing {Project}",
                                             Constants.POPUP_WINDOW_TITLE);
        
            b.add (h);

            JTextPane help = UIUtils.createHelpTextPane ("Please select one of {Projects} below.  Double click to open.",
                                                         null);
            help.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                         help.getPreferredSize ().height + 10));
            b.add (help);
            
            final JLabel loading = UIUtils.createLoadingLabel ("Opening {project}...");
            
            loading.setBorder (new EmptyBorder (10, 10, 10, 10));            
            
            JPanel panel = new JPanel (new BorderLayout ());
            panel.setOpaque (false);
            panel.setBorder (new EmptyBorder (0, 5, 5, 0));
            panel.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            final ActionListener openProject = new ActionListener ()
            {
              
                public void actionPerformed (ActionEvent ev)
                {
                    
                    final Project p = (Project) ev.getSource ();
                    
                    //loading.setVisible (true);
                    //_this.resize ();
                    
                    UIUtils.doLater (new ActionListener ()
                    {
                    
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            if (_this.handleOpenProject (p))
                            {
        
                                _this.close ();
                                
                                _this.dispose ();
        
                                return;
                                
                            } 
                            
                            //loading.setVisible (false);
                            //_this.resize ();
                            
                        }

                    });
                    
                }
                
            };
            
            this.projOpenTable = new JTable ()
            {
              
                //Implement table cell tool tips.           
                public String getToolTipText(MouseEvent e)
                {
                
                    String tip = null;
                    
                    int rowIndex = this.rowAtPoint (e.getPoint ());
                    
                    if (rowIndex < 0)
                    {
                        
                        return null;
                        
                    }
                    
                    Project p = (Project) projs.get (rowIndex);
    
                    tip = Environment.canOpenProject (p);
                          
                    if (tip != null)
                    {
                        
                        tip = "This {project} cannot be opened for the following reason:<br /><br />" + tip + "<br /><br />Right click to remove this from your list of {projects}.";
                                        
                    }
                    
                    if ((tip == null)                    
                        &&
                        (p.isEncrypted ())
                       )
                    {
                            
                        tip = "This {project} is encrypted and needs a password to access it.";
                            
                    }
                        
                    if ((tip == null)
                        &&
                        (p.isEditorProject ())
                       )
                    {
                        
                        EditorEditor ed = p.getForEditor ();
                        
                        if (ed != null)
                        {
                        
                            String name = ed.getMainName ();                        
                        
                            ed = EditorsEnvironment.getEditorByEmail (ed.getEmail ());
                                                
                            if (ed != null)
                            {
                                
                                name = ed.getShortName ();
                                
                            }
                        
                            tip = String.format ("You are editing this {project} for <b>%s</b>.",
                                                 name);
                            
                        }
                        
                    }

                    if ((tip == null)
                        &&
                        (p.isWarmupsProject ())
                       )
                    {
                        
                        tip = "This is your {warmups} {project}.";
                        
                    }

                    if (tip == null)
                    {
                        
                        return null;
                        
                    }
                    
                    return String.format ("<html>%s</html>",
                                          Environment.replaceObjectNames (tip));
                
                }              
                
            };

            UIUtils.initTable (this.projOpenTable);
            this.projOpenTable.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
            
            this.projOpenTable.addKeyListener (new KeyAdapter ()
                {

                    public void keyPressed (KeyEvent ev)
                    {

                        if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                        {

                            Project p = (Project) projs.get (projOpenTable.getSelectedRow ());

                            openProject.actionPerformed (new ActionEvent (p, 1, "open"));

                            ev.consume ();

                        }

                    }

                });

            // TODO: Change to use a mouseeventhandler.
            projOpenTable.addMouseListener (new MouseAdapter ()
                {

                    public void mouseReleased (MouseEvent ev)
                    {

                        int rowInd = projOpenTable.rowAtPoint (ev.getPoint ());
                        
                        if (rowInd < 0)
                        {
                            
                            return;
                            
                        }
                        
                        projOpenTable.setRowSelectionInterval (rowInd,
                                                               rowInd);
                    
                        final Project p = (Project) projs.get (rowInd);
                        
                        if (p == null)
                        {
                            
                            return;
                            
                        }

                        if (ev.isPopupTrigger ())
                        {

                            JPopupMenu popup = new JPopupMenu ();
            
                            JMenuItem mi = null;
            
                            // If invalid, show remove option.
                            if (Environment.canOpenProject (p) != null)
                            {
                                                                
                                mi = UIUtils.createMenuItem ("Remove",
                                                             Constants.CLOSE_ICON_NAME,
                                                             new ActionListener ()
                                                             {
                                                                                     
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                                                                                                                                                    
                                                                    _this.showRemoveProject (p,
                                                                                             "Remove {project}?",
                                                                                             null,
                                                                                             new ActionListener ()
                                                                                             {
                                                                                                
                                                                                                public void actionPerformed (ActionEvent ev)
                                                                                                {
                                                                                                    
                                                                                                    // Remove the row.
                                                                                                    ((ProjectTableModel) projOpenTable.getModel ()).removeProject (p);
    
                                                                                                }
                                                                                             });
                                                                    
                                                                }
                                                                
                                                             },
                                                             null,
                                                             null);
                                                     
                                popup.add (mi);                                
                                
                            } else {
                            
                                // If ok, show open and show folder
                                mi = UIUtils.createMenuItem ("Open",
                                                             Constants.OPEN_PROJECT_ICON_NAME,
                                                             new ActionListener ()
                                                             {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    if (_this.handleOpenProject (p))
                                                                    {
                                        
                                                                        _this.close ();
                                        
                                                                    }
                                                                                                                                        
                                                                }
                                                                
                                                             },
                                                             null,
                                                             null);
                                                           
                                popup.add (mi);                                

                                mi = UIUtils.createMenuItem ("Show Folder",
                                                             Constants.FOLDER_ICON_NAME,
                                                             new ActionListener ()
                                                             {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                    
                                                                    UIUtils.showFile (null,
                                                                                      p.getProjectDirectory ());
                                                                    
                                                                }
                                                                
                                                             },
                                                             null,
                                                             null);
                                                           
                                popup.add (mi);                                

                            }

                            popup.show ((Component) ev.getSource (),
                                        ev.getPoint ().x,
                                        ev.getPoint ().y);
            
                            return;
                
                        }    
                        
                    }
                
                    public void mouseClicked (MouseEvent ev)
                    {

                        int r = projOpenTable.rowAtPoint (ev.getPoint ());
                        
                        if (r < 0)
                        {
                            
                            return;
                            
                        }
                    
                        Project p = (Project) projs.get (r);
                        
                        if (p == null)
                        {
                            
                            return;
                            
                        }
                    
                        if (ev.getClickCount () == 2)
                        {

                            openProject.actionPerformed (new ActionEvent (p, 1, "open"));

                        }

                    }

                });

            ProjectTableModel pstm = new ProjectTableModel (projs);

            projOpenTable.setModel (pstm);
            projOpenTable.getColumnModel ().getColumn (0).setMinWidth (0);
            projOpenTable.getColumnModel ().getColumn (0).setMaxWidth (24);
            
            projOpenTable.getColumnModel ().getColumn (2).setMaxWidth (80);
            JScrollPane s = UIUtils.createScrollPane (projOpenTable);
            
            projOpenTable.setRowSorter (new TableRowSorter (pstm)
            {
                
                @Override
                public Comparator<?> getComparator (int c)
                {
                    
                    if (c == 2)
                    {
                        
                        return new Comparator<String> ()
                        {
                        
                            @Override 
                            public int compare (String ds1,
                                                String ds2)
                            {
                                
                                // wtf... have to do it this way otherwise we'd have to do a convulted thing with the date column.
                                Date d1 = Environment.parseDate (ds1);
                                
                                Date d2 = Environment.parseDate (ds2);
                                
                                return d1.compareTo (d2);
                                
                            }
                        
                            @Override
                            public boolean equals (Object o)
                            {
                                
                                return o == this;
                                
                            }
                        
                        };
                        
                    }
                    
                    return super.getComparator (c);
                    
                }
                
            });
            
            projOpenTable.setPreferredScrollableViewportSize (new Dimension (-1,
                                                                             projOpenTable.getRowHeight () * 10));

            // Add a warning for the overdue editor projects.
                                                                             
            panel.add (s);

            b.add (panel);
            
            b.add (loading);
            
            final JButton openBut = UIUtils.createButton ("Open",
                                                          new ActionListener ()
                {

                    @Override
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

                        openProject.actionPerformed (new ActionEvent (p, 1, "open"));
                        
                    }

                });

            openBut.setEnabled (false);                
            projOpenTable.getSelectionModel ().addListSelectionListener (new ListSelectionListener ()
            {
                
                public void valueChanged (ListSelectionEvent ev)
                {

                    int sel = projOpenTable.getSelectedRow ();
                
                    if (sel < 0)
                    {
                        
                        return;
                        
                    }
                
                    Project p = (Project) projs.get (sel);

                    openBut.setEnabled ((Environment.canOpenProject (p) == null));
                    
                }
                
            });
            
            JButton cancelBut = new JButton ();
            cancelBut.setText ("Cancel");

            cancelBut.addActionListener (closeAction);

            JButton[] buts = { openBut, cancelBut };

            JPanel bp = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
            bp.setOpaque (false);
            bp.setBorder (UIUtils.createPadding (0, 5, 0, 0));
                                           
            bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            bp.setMaximumSize (bp.getPreferredSize ());
            b.add (bp);

            b.add (Box.createVerticalStrut (20));
            
            h = UIUtils.createHeader ("Is your {Project} not listed?",
                                      Constants.POPUP_WINDOW_TITLE);
        
            b.add (h);

            help = UIUtils.createHelpTextPane ("This can happen if you are using Dropbox or another file syncing service and are syncing the {project} directory but not your projects file.  Use the box below to find your {project}.",
                                               null);
            help.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                         help.getPreferredSize ().height + 10));
            b.add (help);
            
            b.add (Box.createVerticalStrut (5));
            
            final JLabel message = new JLabel ("");
            
            message.setBorder (new EmptyBorder (0,
                                                5,
                                                5,
                                                0));
            message.setVisible (false);
            b.add (message);

            final JButton openBut2 = UIUtils.createButton ("Open");
            openBut2.setEnabled (false);
            
            final FileFinder finder = UIUtils.createFileFind (Environment.getUserQuollWriterDir ().getPath (),
                                                              "Select a Directory",
                                                              JFileChooser.DIRECTORIES_ONLY,
                                                              "Select",
                                                              null);
            finder.setFindButtonToolTip ("Click to find the {project} directory");
                        
            finder.setOnSelectHandler (new ActionAdapter ()
            {
                                                            
                public void actionPerformed (ActionEvent ev)
                {

                    boolean valid = true;
                
                    VetoableActionEvent ve = (VetoableActionEvent) ev;
                
                    File f = finder.getSelectedFile ();
                
                    if (!f.isDirectory ())
                    {
                        
                        valid = false;
                        
                    }
                                      
                    if (valid)
                    {                                                
                    
                        // Look for files names, "projectdb.h2.db" and "___quollwriter_dir.txt"
                        File nf = new File (f.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME);
                        
                        if ((!nf.exists ())
                            ||
                            (!nf.isFile ())
                           )
                        {
                            
                            valid = false;
                            
                        }
                        
                        nf = new File (f.getPath () + "/projectdb.h2.db");
                        
                        if ((!nf.exists ())
                            ||
                            (!nf.isFile ())
                           )
                        {
                            
                            valid = false;
                            
                        }
    
                        if (!valid)
                        {
                            
                            // Show an error
                            message.setText (Environment.replaceObjectNames ("Sorry, that doesn't appear to be a Quoll Writer {project} directory."));
                            message.setForeground (UIUtils.getColor (Constants.ERROR_TEXT_COLOR));
                            message.setIcon (Environment.getIcon (Constants.ERROR_RED_ICON_NAME,
                                                                  Constants.ICON_MENU));
                                                        
                        } else {
                        
                            // See if the project is already in their project list.
                        
                            message.setText (Environment.replaceObjectNames ("That looks like a Quoll Writer {project} directory."));
                            message.setForeground (UIUtils.getColor ("#558631"));
                            message.setIcon (Environment.getIcon ("ok-green",
                                                                  Constants.ICON_MENU));
    
                        }

                        message.setVisible (true);

                        openBut2.setEnabled (valid);
    
                        _this.resize ();
                        
                    }
                                        
                }
                
            });

            finder.setBorder (new EmptyBorder (0,
                                               5,
                                               0,
                                               0));
                        
            b.add (finder);
            b.add (Box.createVerticalStrut (5));
                                                        
            openBut2.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    // See if there is a project file in the directory.
                    final File f = finder.getSelectedFile ();
                                    
                    File nf = new File (f.getPath () + "/project.qwpr");
                    
                    if (nf.exists ())
                    {
                        
                        _this.close ();
                        
                        // Open the file.
                        //Environment.openProject (nf);
                        
                        return;
                        
                    }
                    
                    final String name = WordsCapitalizer.capitalizeEveryWord (f.getName ());
                    
                    TextInputWindow.create (null,
                                            "Confirm name of {project}",
                                            null,
                                            "Please confirm this is the name of your {project}.",
                                            "Open",
                                            name,
                                            new ValueValidator<String> ()
                                            {
                                                
                                                public String isValid (String v)
                                                {
                                                    
                                                    if ((v == null)
                                                        ||
                                                        (v.trim ().length () == 0)
                                                       )
                                                    {
                                                        
                                                        return "Please enter the name of the {project}";
                                                        
                                                    }
                                                                                                        
                                                    return null;
                                                    
                                                }
                                                
                                            },
                                            new ActionListener ()
                                            {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                              
                                                    _this.close ();

                                                    // Open the project.
                                                    Project p = new Project (name);
                                                    p.setProjectDirectory (f);
                                                    
                                                    if (_this.handleOpenProject (p))
                                                    {
                                                        
                                                        try
                                                        {
                                                        
                                                            Environment.addOpenedProject (Environment.getProjectViewer (p));
                                                            
                                                        } catch (Exception e) {
                                                            
                                                            Environment.logError ("Unable to add project to projects file: " +
                                                                                  p.getName (),
                                                                                  e);                                                        
                                                            
                                                        }                                                        
                                                        
                                                    }
                                                    
                                                }
                                                
                                            },
                                            null);
                    
                }

            });

            cancelBut = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                              closeAction);

            buts = new JButton[] { openBut2, cancelBut };

            bp = UIUtils.createButtonBar2 (buts,
                                           Component.LEFT_ALIGNMENT); 
            bp.setOpaque (false);
            bp.setBorder (new EmptyBorder (0,
                                           5,
                                           0,
                                           0));
            bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            bp.setMaximumSize (bp.getPreferredSize ());
            b.add (bp);

        }

        if ((show & FindOrOpen.SHOW_NEW) == FindOrOpen.SHOW_NEW)
        {

            if (((show & FindOrOpen.SHOW_OPEN) == FindOrOpen.SHOW_OPEN)
                &&
                (projs.size () > 0)
               )
            {

                b.add (Box.createVerticalStrut (20));

            }

            Header h = UIUtils.createHeader ("Create a New {Project}",
                                             Constants.POPUP_WINDOW_TITLE);
            h.setBorder (null);
            b.add (h);

            b.add (UIUtils.createHelpTextPane ("To create a new {Project} enter the name below, select the directory it should be saved to and press the Create button.",
                                               null));

            final NewProjectPanel newProjPanel = new NewProjectPanel ();
                                                
            JComponent cp = newProjPanel.createPanel (this,
                                                      closeAction,
                                                      true,
                                                      closeAction,
                                                      true);
            
            cp.setBorder (new EmptyBorder (10, 10, 0, 10));
            
            b.add (cp);

        }

        return b;    
    
    }
    
    public void init ()
    {
        
        super.init ();
        
        this.getHeader ().setVisible (false);
        
        this.resize ();
                
    }
    
    public FindOrOpen(int show)
    {

        super ();

        this.show = show;
        
        String t = null;

        if ((show & FindOrOpen.SHOW_NEW) == FindOrOpen.SHOW_NEW)
        {

            t = FindOrOpen.SHOW_NEW_WINDOW_TITLE;

        }

        if ((show & FindOrOpen.SHOW_OPEN) == FindOrOpen.SHOW_OPEN)
        {

            if (t != null)
            {
                
                t = FindOrOpen.SHOW_ALL_WINDOW_TITLE;
                
            } else {
        
                t = FindOrOpen.SHOW_OPEN_WINDOW_TITLE;
                
            }

        }

        this.windowTitle = t;

    }

    public String getHeaderIconType ()
    {
        
        return null;
        
    }

    public String getWindowTitle ()
    {
        
        return this.windowTitle;
        
    }
    
    public String getHeaderTitle ()
    {
    
        if ((this.show & FindOrOpen.SHOW_OPEN) == FindOrOpen.SHOW_OPEN)
        {
        
            return FindOrOpen.SHOW_OPEN_WINDOW_TITLE;
                
        }

        if ((this.show & FindOrOpen.SHOW_NEW) == FindOrOpen.SHOW_NEW)
        {

            return FindOrOpen.SHOW_NEW_WINDOW_TITLE;

        }
        
        return null;
        
    }
    
    public JButton[] getButtons ()
    {
        
        return null;
        
    }
    
    public String getHelpText ()
    {
        
        return null;
        
    }
    
    private void showRemoveProject (final Project        p,
                                    String         title,
                                    String         message,
                                    final ActionListener onRemove)
    {

        final FindOrOpen _this = this;
    
        if (title == null)
        {
            
            title = "Unable to open {project}";
            
        }
            
        Map<String, ActionListener> buts = new LinkedHashMap ();
        buts.put ("Yes, remove it",
                  new ActionListener ()
                  {
                    
                     public void actionPerformed (ActionEvent ev)
                     {

                        try
                        {
                     
                            //Environment.removeProjectFromProjectsFile (p);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to remove project: " +
                                                  p.getName () +
                                                  " from the project list",
                                                  e);
                
                            UIUtils.showErrorMessage (_this,
                                                      "Unable to remove project, please contact Quoll Writer support for assistance.");
                            
                            return;                            
                            
                        }
                        
                        if (onRemove != null)
                        {
                            
                            onRemove.actionPerformed (ev);
                            
                        }
                        
                     }
                    
                  });
        
        buts.put ("No, keep it",
                  new ActionListener ()
                  {
                    
                     public void actionPerformed (ActionEvent ev)
                     {
                        
                        // Don't do anything...
                        
                     }
                    
                  });

        if (message == null)
        {
            
            message = String.format ("Please confirm you wish to remove {project} <b>%s</b> from your list of {projects}.",
                                     p.getName ());
                  
        }
        
        message = message + "<br /><br />Note: this will <b>only</b> remove the {project} from the list it will not remove any other data.";
        
        UIUtils.createQuestionPopup (this,
                                     title,
                                     Constants.ERROR_ICON_NAME,
                                     message,
                                     buts,
                                     null,
                                     null);
        
    }
    
    private boolean handleOpenProject (Project p)
    {

        String reason = Environment.canOpenProject (p);
        
        if (reason != null)
        {

            this.showRemoveProject (p,
                                    null,
                                    String.format ("Sorry, {project} <b>%s</b> cannot be opened for the following reason:<br /><br /><b>%s</b><br /><br />This can happen if your projects file gets out of sync with your hard drive, for example if you have re-installed your machine or if you are using a file syncing service.<br /><br />Do you want to remove it from your list of projects?",
                                                   p.getName (),
                                                   reason),
                                    null);

            return false;
                                         
        }
    
        try
        {

            // Change the icon to be loading.
            ((ProjectTableModel) this.projOpenTable.getModel ()).setLoading (p);
        
            Environment.openProject (p);

            return true;

        } catch (Exception e)
        {

            // Check for encryption.
            if ((ObjectManager.isEncryptionException (e))
                &&
                (!p.isEncrypted ())
               )
            {
                
                // Try with no credentials.
                try
                {

                    p.setNoCredentials (true);
                    Environment.openProject (p);
                
                    return true;
                
                } catch (Exception ee) {
                
                    p.setNoCredentials (false);
                    
                    // Check for encryption.
                    if (ObjectManager.isEncryptionException (e))
                    {
                    
                        p.setEncrypted (true);
                        
                        this.handleOpenProject (p);
                        
                        return true;
                    
                    }

                    Environment.logError ("Unable to open project: " +
                                          p.getName (),
                                          ee);
        
                    UIUtils.showErrorMessage (this,
                                              "Unable to open project: " +
                                              p.getName ());
                    
                    return false;
                    
                }
                
            }
        
            Environment.logError ("Unable to open project: " +
                                  p.getName (),
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to open project: " +
                                      p.getName ());

        }

        return false;

    }
    
}
