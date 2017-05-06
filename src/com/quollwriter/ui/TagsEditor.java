package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.events.*;

public class TagsEditor extends Box implements ProjectEventListener
{

    private DefaultTableModel typeModel = null;
    private AbstractViewer viewer = null;
    private JLabel error = null;
    
    public TagsEditor (AbstractViewer pv)
    {

        super (BoxLayout.Y_AXIS);
        
        this.viewer = pv;

        Environment.addUserProjectEventListener (this);
                
    }

    @Override
    public void eventOccurred (ProjectEvent ev)
    {
        
        if (ev.getType ().equals (ProjectEvent.TAG))
        {

            this.reloadTypes ();
        
        }        
        
    }
    
    public void init ()
    {

        final TagsEditor _this = this;

        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setOpaque (true);
        this.setBackground (null);
        this.add (Box.createVerticalStrut (5));

        this.add (UIUtils.createBoldSubHeader (String.format ("New %s",
                                                              this.getTypesName ()),
                                               null));

        JTextPane tp = UIUtils.createHelpTextPane (String.format ("Enter the new %s to add below, separate the %s with commas or semi-colons.",
                                                                  this.getTypesName ().toLowerCase (),
                                                                  this.getTypesName ().toLowerCase ()),
                                                   this.viewer);

        tp.setBorder (UIUtils.createPadding (5,
                                       5,
                                       0,
                                       5));

        this.add (tp);
        
        final JTextField newTypes = UIUtils.createTextField ();
        newTypes.setAlignmentX (Component.LEFT_ALIGNMENT);

        final JTable typeTable = UIUtils.createTable ();
        typeTable.setTableHeader (null);
        typeTable.setToolTipText ("Double click to edit, press Enter when done.");
        typeTable.setDefaultRenderer (Object.class,
                                      new DefaultTableCellRenderer ()
        {
            
            @Override
            public Component getTableCellRendererComponent (JTable table,
                                                            Object value,
                                                            boolean isSelected,
                                                            boolean hasFocus,
                                                            int     row,
                                                            int     column)
            {
                
                super.getTableCellRendererComponent (table,
                                                     value,
                                                     isSelected,
                                                     hasFocus,
                                                     row,
                                                     column);
                
                this.setText (((Tag) value).getName ());
                
                this.setBorder (UIUtils.createPadding (0, 3, 0, 3));
                
                return this;
                
            }
            
        });
        
        typeTable.setDefaultEditor (Object.class,
                                    new DefaultCellEditor (new JTextField ())
        {
            
            private Tag tag = null;
            
            @Override
            public int getClickCountToStart ()
            {
                
                return 2;
                
            }
           
            @Override
            public Component getTableCellEditorComponent (JTable  table,
                                                          Object  value,
                                                          boolean isSelected,
                                                          int     row,
                                                          int     column)        
            {
                
                this.tag = (Tag) value;
                
                JTextField t = (JTextField) this.getComponent ();
                
                t.setText (this.tag.getName ().trim ());
                
                t.setBorder (UIUtils.createPadding (0, 3, 0, 3));
                
                return t;
                
            }
            
            @Override
            public boolean stopCellEditing ()
            {
                      
                _this.error.setVisible (false);
                
                UIUtils.resizeParent (_this);
                              
                String newName = ((JTextField) this.getComponent ()).getText ().trim ();
                           
                if (newName.length () == 0)
                {
                    
                    return _this.showError ("Tag must have a value!");
                           
                }
                
                try
                {
                
                    Tag ot = Environment.getTagByName (newName);
                              
                    // See if we have another tag with that name.
                    if ((ot != null)
                        &&
                        (ot != this.tag)
                       )
                    {
                        
                        return _this.showError (String.format ("Already have a tag called <b>%s</b>.",
                                                               ot.getName ()));
                                                    
                    }
        
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get tag for name: " + newName,
                                          e);
                    
                    return _this.showError ("Unable to check tag.");
                    
                }

                this.tag.setName (newName);                
                
                try
                {
                    
                    Environment.saveTag (this.tag);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update tag: " +
                                          this.tag,
                                          e);
                    
                    return _this.showError ("Unable to update tag.");
                    
                }
                
                _this.error.setVisible (false);
                
                UIUtils.resizeParent (_this.error);

                return super.stopCellEditing ();
                
            }
            
            @Override
            public void cancelCellEditing ()
            {
                
                _this.error.setVisible (false);
                
                UIUtils.resizeParent (_this.error);
                
                super.cancelCellEditing ();
                
            }
            
            @Override
            public Object getCellEditorValue ()
            {
                            
                return this.tag;
                
            }
                                    
        });

        typeTable.addMouseListener (new MouseEventHandler ()
        {
                        
            @Override
            public void fillPopup (final JPopupMenu m,
                                   final MouseEvent ev)
            {
                
                final int rowInd = typeTable.rowAtPoint (ev.getPoint ());
                        
                if (rowInd < 0)
                {
                    
                    return;
                    
                }
                        
                typeTable.setRowSelectionInterval (rowInd,
                                                   rowInd);

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               new ActionListener ()
                                               {
                                                
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        typeTable.editCellAt (rowInd,
                                                                              0);
                                                        
                                                    }
                                                
                                               }));
                
            }
            
        });
        
        this.typeModel = (DefaultTableModel) typeTable.getModel ();

        this.reloadTypes ();

        Box fb = new Box (BoxLayout.Y_AXIS);
        fb.setAlignmentX (Component.LEFT_ALIGNMENT);
        fb.add (newTypes);
                
        fb.add (Box.createVerticalStrut (5));
        fb.setBorder (UIUtils.createPadding (5,
                                       5,
                                       20,
                                       5));

        final JButton add = new JButton ("Add");

        JButton[] buts = new JButton[] { add };
        
        final ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                String n = newTypes.getText ();

                StringTokenizer t = new StringTokenizer (n,
                                                         ",;");

                while (t.hasMoreTokens ())
                {

                    String w = t.nextToken ().trim ();

                    if (w.length () == 0)
                    {
                        
                        continue;
                        
                    }
                    
                    try
                    {
                    
                        if (Environment.getTagByName (w) != null)
                        {
                            
                            continue;
                            
                        }

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get tag for name: " + w,
                                              e);
                        
                        continue;
                        
                    }
                    
                    DefaultTableModel m = (DefaultTableModel) typeTable.getModel ();

                    Tag tag = new Tag ();
                    tag.setName (w);
                    
                    try
                    {
                        
                        Environment.saveTag (tag);
                        
                    } catch (Exception e) {

                        Environment.logError ("Unable to add tag: " +
                                              tag,
                                              e);
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to add tag.");
                        
                        return;
                        
                    }
                                                
                    Vector r = new Vector ();
                    r.add (tag);
                    m.insertRow (0,
                                 r);
                                 
                }

                newTypes.setText ("");

            }

        };

        add.addActionListener (aa);

        UIUtils.addDoActionOnReturnPressed (newTypes,
                                            aa);

        fb.add (UIUtils.createButtonBar2 (buts, Component.LEFT_ALIGNMENT));
        
        this.add (fb);
        
        this.add (UIUtils.createBoldSubHeader (Environment.replaceObjectNames (this.getTypesName ()),
                                               null));

        fb = new Box (BoxLayout.Y_AXIS);
        fb.setAlignmentX (Component.LEFT_ALIGNMENT);
        fb.setBorder (UIUtils.createPadding (5,
                                       5,
                                       0,
                                       5));                                            
                                            
        if (this.getNewTypeHelp () != null)
        {
                                            
            tp = UIUtils.createHelpTextPane (Environment.replaceObjectNames (this.getNewTypeHelp ()),
                                             this.viewer);
            tp.setBorder (null);
            fb.add (tp);
            fb.add (Box.createVerticalStrut (10));

        }
        
        final JScrollPane ppsp = UIUtils.createScrollPane (typeTable);

        typeTable.setPreferredScrollableViewportSize (new Dimension (-1,
                                                                     (typeTable.getRowHeight () + 3) * 5));

        this.error = UIUtils.createErrorLabel ("Please enter a value.");
        this.error.setVisible (false);
        
        this.error.setBorder (UIUtils.createPadding (5, 0, 5, 5));
        
        fb.add (this.error);
                                                                     
        fb.add (ppsp);

        final JButton remove = new JButton ("Remove Selected");

        buts = new JButton[] { remove };
        
        remove.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                DefaultTableModel m = (DefaultTableModel) typeTable.getModel ();

                int[] selection = typeTable.getSelectedRows ();

                for (int i = selection.length - 1; i > -1; i--)
                {

                    Tag tag = (Tag) m.getValueAt (selection[i],
                                                  0);

                    // Remove the row.
                    m.removeRow (selection[i]);
                    
                    try
                    {
                        
                        Environment.deleteTag (tag);
                        
                    } catch (Exception e) {

                        Environment.logError ("Unable to delete tag: " +
                                              tag,
                                              e);
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to delete tag.");
                        
                        return;
                        
                    }
                    
                }

                ((DefaultListSelectionModel) typeTable.getSelectionModel ()).clearSelection ();

            }

        });

        fb.add (Box.createVerticalStrut (5));
        
        fb.add (UIUtils.createButtonBar2 (buts, Component.LEFT_ALIGNMENT));

        this.add (fb);
        
        JButton finish = new JButton ("Finish");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        this.add (Box.createVerticalStrut (10));
        
        this.add (bp);        
        
        //return b;

    }
    
    private boolean showError (String m)
    {
        
        this.error.setText (m);
        
        this.error.setVisible (true);
        
        UIUtils.resizeParent (this.error);

        return false;
        
    }
    
    public void reloadTypes ()
    {

        Set<Tag> tags = null;
        
        try
        {
            
            tags = Environment.getAllTags ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get all tags",
                                  e);
            
            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to get the tags.");
            
            return;
            
        }

        Vector tagsData = new Vector ();

        TreeSet<Tag> nt = new TreeSet (NamedObjectSorter.getInstance ());
        
        nt.addAll (tags);
        
        for (Tag t : nt)
        {
            
            Vector d = new Vector ();
            d.add (t);
                        
            tagsData.add (d);
            
        }

        Vector<String> cols = new Vector ();
        cols.add ("Tag");

        this.typeModel.setDataVector (tagsData,
                                      cols);
        
    }
    
    public String getWindowTitle ()
    {

        return "Manage the Tags";

    }

    public String getHeaderTitle ()
    {

        return "Manage the Tags";

    }

    public String getHeaderIconType ()
    {

        return Constants.TAG_ICON_NAME;

    }

    public String getHelpText ()
    {

        return null;

    }

    public String getNewTypeHelp ()
    {
        
        return "Note: removing a tag will remove it from <b>all</b> {projects}.  You can change the tag name by editing the values below, double click on a type to edit it.";
        
    }

    public String getTypesName ()
    {
        
        return "Tags";
        
    }
    
}
