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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.events.*;

public class TagsEditor extends Box
{

    private DefaultTableModel typeModel = null;
    private AbstractViewer viewer = null;

    public TagsEditor (AbstractViewer pv)
    {

        super (BoxLayout.Y_AXIS);
        
        this.viewer = pv;

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

        tp.setBorder (new EmptyBorder (5,
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
        
        UIUtils.listenToTableForCellChanges (typeTable,
                                             new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                TableCellListener tcl = (TableCellListener) ev.getSource ();
                
                Tag tag = (Tag) ((DefaultTableModel) typeTable.getModel ()).getValueAt (tcl.getRow (),
                                                                                        tcl.getColumn ());
                
                tag.setName (tcl.getNewValue ().toString ());
                
                try
                {
                    
                    Environment.saveTag (tag);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update tag: " +
                                          tag,
                                          e);
                    
                    UIUtils.showErrorMessage (_this.viewer,
                                              "Unable to update tag.");
                    
                }
                
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
        fb.setBorder (new EmptyBorder (5,
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
        fb.setBorder (new EmptyBorder (5,
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

        for (Tag t : tags)
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
        
        return "Note: removing a type will only remove it from the list of types to select when adding/editing a note.  You can also change the type name by editing the values, double click on a type to edit it.";
        
    }

    public String getTypesName ()
    {
        
        return "Tags";
        
    }

}
