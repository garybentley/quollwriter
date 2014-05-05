package com.quollwriter.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Set;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.DnDTabbedPane;
import com.quollwriter.ui.components.TabHeader;

public class EditorsSideBar extends AbstractSideBar 
{
    
    private DnDTabbedPane tabs = null;
    private EditorList editorList = null;
    private EditorFindPanel editorFindPanel = null;
    private ProjectViewer projectViewer = null;
    
    public EditorsSideBar (ProjectViewer v)
    {
        
        super (v);
        
        this.projectViewer = v;
        
    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public void onClose ()
    {
        
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public String getIconType ()
    {
        
        return Constants.EDITORS_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Editors";
        
    }
    
    public void panelShown (MainPanelEvent ev)
    {
                
    }
        
    private void update ()
    {
        
        if (!this.isVisible ())
        {
            
            return;
            
        }
                
    }
        
    public List<JButton> getHeaderControls ()
    {

        final EditorsSideBar _this = this;
        
        List<JButton> buts = new ArrayList ();        
        
        JButton b = UIUtils.createButton ("online",
                                          Constants.ICON_SIDEBAR,
                                          "Click to change your status from online",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                              }
                                            
                                          });

        buts.add (b);

        b = UIUtils.createButton (Constants.FIND_ICON_NAME,
                                          Constants.ICON_SIDEBAR,
                                          "Click to find editors",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  _this.toggleFindEditorsTab ();  
                                                
                                              }
                                            
                                          });

        buts.add (b);
                                          
        b = UIUtils.createButton (Constants.OPTIONS_ICON_NAME,
                                  Constants.ICON_SIDEBAR,
                                  "Click to view the config options",
                                  new ActionAdapter ()
                                  {
                                            
                                      public void actionPerformed (ActionEvent ev)
                                      {
                                                            
                                          JMenuItem mi = null;

                                          JPopupMenu popup = new JPopupMenu ();
                                          
                                          popup.add (UIUtils.createMenuItem ("Edit your {Project} information",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  _this.projectViewer.showAdvertiseProjectPanel ();                                                
                                                
                                              }
                                            
                                          }));

                                          popup.add (UIUtils.createMenuItem ("Edit your information",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             null));

                                          popup.add (UIUtils.createMenuItem ("Edit your password",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             null));

                                          popup.add (UIUtils.createMenuItem ("Register as an Editor",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  _this.projectViewer.showRegisterAsAnEditorPanel ();
                                                
                                              }
                                            
                                          }));

                                          JComponent s = (JComponent) ev.getSource ();
                            
                                          popup.show (s,
                                                      s.getWidth () / 2,
                                                      s.getHeight ());
                                             
                                      }
                                            
                                  });

        buts.add (b);

        return buts;        
        
    }

    private void createEditorList ()
    {
        
        this.editorList = new EditorList (this);
        
        JScrollPane sp = new JScrollPane (this.editorList);

        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setOpaque (false);
        sp.getViewport ().setOpaque (false);
        sp.setBorder (null);

        JLabel add = new JLabel (Environment.getIcon ("plan",
                                                      Constants.ICON_TAB_HEADER));
        add.setToolTipText ("The Editors for this project");
        
        this.tabs.add (sp,
                       0);
        this.tabs.setTabComponentAt (0,
                                     add);        
        
    }
    
    public JComponent getContent ()
    {

        final EditorsSideBar _this = this;    
    
        Box box = new Box (BoxLayout.Y_AXIS);
        box.setAlignmentX (Component.LEFT_ALIGNMENT);
        // Turn off the auto border.
        box.setBorder (new EmptyBorder (0, 0, 0, 0));
        
        this.tabs = new DnDTabbedPane ();
        this.tabs.putClientProperty(com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        //this.tabs.putClientProperty(com.jgoodies.looks.Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        this.tabs.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);
        this.tabs.setBorder (new EmptyBorder (5, 2, 0, 0));
        this.tabs.addChangeListener (new ChangeAdapter ()
        {
           
            public void stateChanged (ChangeEvent ev)
            {
                
            }
            
        });
                           
        box.add (this.tabs);

        this.createEditorList ();
                
        box.setPreferredSize (new Dimension (500,
                                             Short.MAX_VALUE));
                   
        return box;
                    
    }
    
    public void toggleFindEditorsTab ()
    {
        
        if ((this.editorFindPanel != null)
            &&
            (this.editorFindPanel.getParent () != null)
           )
        {
            
            this.hideFindEditorsTab ();
            
            return;
            
        }
        
        this.showFindEditorsTab ();
        
    }
    
    public void hideFindEditorsTab ()
    {
        
        if (this.editorFindPanel == null)
        {
            
            return;
            
        }
        
        this.tabs.remove (this.editorFindPanel);
        
    }
    
    public void showFindEditorsTab ()
    {

        if ((this.editorFindPanel != null)
            &&
            (this.editorFindPanel.getParent () != null)
           )
        {
            
            this.tabs.setSelectedComponent (this.editorFindPanel);
            this.tabs.revalidate ();
            this.tabs.repaint ();
            
            return;
            
        }
        
        if (this.editorFindPanel == null)
        {
        
            // Add the editor find panel.
            this.editorFindPanel = new EditorFindPanel (this);

            this.editorFindPanel.init ();

            this.editorFindPanel.setOpaque (true);
            this.editorFindPanel.setBackground (UIUtils.getComponentColor ());
            this.editorFindPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.editorFindPanel.setBorder (new EmptyBorder (5, 5, 5, 5));            
            
        } 
        
        JLabel add = new JLabel (Environment.getIcon (Constants.FIND_ICON_NAME,
                                                      Constants.ICON_TAB_HEADER));
        
        this.tabs.add (this.editorFindPanel,
                       1);
        this.tabs.setTabComponentAt (1,
                                     add);        
        
        this.showFindEditorsTab ();        
        
    }
    
    public EditorPanel getEditorPanel (EditorEditor ed)
    {
        
        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component comp = this.tabs.getComponentAt (i);

            if (comp instanceof EditorPanel)
            {

                if (ed == ((EditorPanel) comp).getEditor ())
                {

                    return (EditorPanel) comp;
                
                }
                
            }
            
        }
        
        return null;
        
    }
    
    public void showMessageBox (final EditorEditor ed)
    {
        
        this.showEditor (ed);
        
        final EditorsSideBar _this = this;
        
        SwingUtilities.invokeLater (new Runnable ()
        {
        
            public void run ()
            {
                
                EditorPanel edPanel = _this.getEditorPanel (ed);
            
                if (edPanel != null)
                {
                    
                    edPanel.showMessageBox ();
                    
                }

            }
                
        });
        
    }
    
    public void showEditor (EditorEditor ed)
    {
        
        EditorPanel edPanel = this.getEditorPanel (ed);
        
        if (edPanel != null)
        {
        
            this.tabs.setSelectedComponent (edPanel);
            this.tabs.revalidate ();
            this.tabs.repaint ();
       
            return;
                                
        }
                    
        // Add them.
        
        final TabHeader th = new TabHeader (this.tabs,
                                            Environment.getIcon (Constants.CANCEL_ICON_NAME,
                                                                 Constants.ICON_TAB_HEADER),
                                            Environment.getTransparentImage (),
                                            ed.getShortName ());

        th.setIcon (Environment.getIcon (this.getStatusIcon (ed.getStatus ()),
                                         Constants.ICON_TAB_HEADER));
        
        EditorPanel ep = new EditorPanel (this,
                                          ed);
        ep.init ();
                            
        ep.setOpaque (true);
        ep.setBackground (UIUtils.getComponentColor ());
        ep.setAlignmentX (Component.LEFT_ALIGNMENT);
        ep.setBorder (new EmptyBorder (5, 5, 5, 5));
                       
        int ind = 1;
        
        if (this.editorFindPanel != null)
        {
            
            ind++;
            
        }
        
        this.tabs.add (ep,
                       ind);
                       
        this.tabs.setTabComponentAt (ind,
                                     th);        
        
        this.showEditor (ed);
        
    }
    
    public void init ()
    {

        super.init ();

    }    
    
    public String getStatusIcon (EditorEditor.Status status)
    {
        
        if (status == null)
        {
            
            return "unknown";
        
        }

        return status.getType ();
        
    }
 
    public JComponent createEditorsList (List<EditorEditor> editors)
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        for (int i = 0; i < editors.size (); i++)
        {
            
            EditorEditor ed = editors.get (i);
            
            EditorInfoBox infBox = this.getEditorBox (ed);
            
            if (i < editors.size () - 1)
            {
            
                infBox.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                       0,
                                                                       1,
                                                                       0,
                                                                       UIUtils.getBorderColor ()),
                                                      new EmptyBorder (5, 0, 5, 0)));

            } else {
                
                infBox.setBorder (new EmptyBorder (5, 0, 5, 0));
                                             
            }

            infBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                  infBox.getPreferredSize ().height + 10));
                                            
            infBox.setMinimumSize (new Dimension (300,
                                                  infBox.getPreferredSize ().height));
            
            b.add (infBox);

        }
        
        b.setBorder (new EmptyBorder (0, 10, 0, 5));
      
        return b;        
        
    }
        
    public JComponent createEditorsFindList (List<EditorEditor> editors)
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        for (int i = 0; i < editors.size (); i++)
        {
            
            EditorEditor ed = editors.get (i);
            
            EditorFindInfoBox infBox = this.getEditorFindBox (ed);
            
            if (i < editors.size () - 1)
            {
            
                infBox.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                       0,
                                                                       1,
                                                                       0,
                                                                       UIUtils.getBorderColor ()),
                                                      new EmptyBorder (5, 0, 5, 0)));

            } else {
                
                infBox.setBorder (new EmptyBorder (5, 0, 5, 0));
                                             
            }

            infBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                  infBox.getPreferredSize ().height + 10));
                                            
            infBox.setMinimumSize (new Dimension (300,
                                                  infBox.getPreferredSize ().height));
            
            b.add (infBox);

        }
        
        b.setBorder (new EmptyBorder (0, 10, 0, 5));
      
        return b;        
        
    }

    private EditorInfoBox getEditorBox (final EditorEditor ed)
    {
        
        EditorInfoBox b = new EditorInfoBox (ed);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        final EditorsSideBar _this = this;
        
        b.addMouseListener (new MouseEventHandler ()
        {
            
            public void handlePress (MouseEvent ev)
            {
                
                if (ev.isPopupTrigger ())
                {
                    
                    // Show the menu.
                
                    return;
                    
                }

                // Show the editor.
                _this.showMessageBox (ed);
                
            }
            
        });
        
        UIUtils.setAsButton (b);
        
        return b;
        
    }

    private EditorFindInfoBox getEditorFindBox (final EditorEditor ed)
    {
        
        EditorFindInfoBox b = new EditorFindInfoBox (ed);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        final EditorsSideBar _this = this;
        
        b.addMouseListener (new MouseEventHandler ()
        {
            
            public void handlePress (MouseEvent ev)
            {
                
                if (ev.isPopupTrigger ())
                {
                    
                    // Show the menu.
                
                    return;
                    
                }

                // Show the editor.
                _this.showMessageBox (ed);
                
            }
            
        });
        
        UIUtils.setAsButton (b);
        
        return b;
        
    }
    
}