package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.event.*;
import java.util.Set;
import java.util.LinkedHashSet;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.components.ScrollableBox;

public class ProjectEditorsAccordionItem extends AccordionItem implements EditorChangedListener
{

    private Box editorsBox = null;
    private ProjectViewer viewer = null;
    private ComponentListener listener = null;
    private boolean showPreviousEditors = false;

    public ProjectEditorsAccordionItem (ProjectViewer pv)
    {

        super ("{Editors}",
               Constants.EDIT_ICON_NAME);
               //Constants.EDITORS_ICON_NAME);    
    
        this.viewer = pv;
        
        final ProjectEditorsAccordionItem _this = this;
    
        this.editorsBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.editorsBox.setBorder (UIUtils.createPadding (0, 10, 0, 0));
    
        JLabel help = UIUtils.createInformationLabel ("People who are editing this {project} for you.");        
        help.setBorder (UIUtils.createPadding (0, 0, 5, 0));
        
        this.editorsBox.add (help);    
        
        this.listener = new ComponentAdapter ()
        {
            
            @Override
            public void componentResized (ComponentEvent ev)
            {
                
                int count = _this.editorsBox.getComponentCount ();
                
                for (int i = 0; i < count; i++)
                {
                    
                    Component c = _this.editorsBox.getComponent (i);
                    
                    if (c instanceof EditorInfoBox)
                    {
                    
                        EditorInfoBox infBox = (EditorInfoBox) _this.editorsBox.getComponent (i);
                        
                        if (infBox == ev.getSource ())
                        {
                        
                            _this.setBorder (infBox,
                                             i == (count - 1));
                            
                        }

                    }
                    
                }                    
                
            }
            
        };
        
        EditorsEnvironment.addEditorChangedListener (this);        
            
    }
      
    public void editorChanged (EditorChangedEvent ev)
    {

        if (this.viewer.getProject () == null)
        {
            
            return;
            
        }
    
        EditorEditor ed = ev.getEditor ();

        // See if the editor is a project editor.
        ProjectEditor pe = this.viewer.getProject ().getProjectEditor (ed);
        
        if (pe == null)
        {
            
            return;
            
        }
        
        EditorInfoBox bb = null;
        
        // Check to see if we already have the editor.
        for (int i = 0; i < this.editorsBox.getComponentCount (); i++)
        {
            
            Component c = this.editorsBox.getComponent (i);
            
            if (c instanceof EditorInfoBox)
            {
                
                EditorInfoBox b = (EditorInfoBox) c;
                
                if (b.getEditor () == ed)
                {
                    
                    if (ev.getType () == EditorChangedEvent.EDITOR_DELETED)
                    {
                        
                        this.editorsBox.remove (b);
                        
                        this.updateBorders ();
                        
                        this.validate ();
                        this.repaint ();
                        
                        return;
                        
                    }
                    
                    if (ev.getType () == EditorChangedEvent.EDITOR_CHANGED)
                    {
                        
                        bb = b;
                        
                    }
                    
                }
                
            }
            
        }
        
        if (bb == null)
        {

            // Editor is new.
            EditorInfoBox infBox = null;
            
            try
            {
                
                infBox = this.getEditorBox (ed);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to get editor info box for editor: " +
                                      pe.getEditor (),
                                      e);
            
                return;
                
            }
            
            infBox.init ();
            
            this.editorsBox.add (infBox,
                                 1);
            
            this.updateBorders ();
            
            this.updateItemCount ();
            
            this.validate ();
            this.repaint ();
            
        } else {
            
            // See if there is a project editor and if they are now previous, if so remove them.
            if (!pe.isCurrent ())
            {
                
                // Should we be showing it?
                bb.setVisible (this.showPreviousEditors);
                
                this.updateBorders ();
                
                this.updateItemCount ();
                
                this.validate ();
                this.repaint ();
                
            }
            
        }
        
    }    
      
    private EditorInfoBox getEditorBox (EditorEditor ed)
                                 throws GeneralException
    {
        
        EditorInfoBox b = new EditorInfoBox (ed,
                                             this.viewer,
                                             true);
                                             
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
                
        b.addFullPopupListener ();
        
        b.init ();
        
        b.addComponentListener (this.listener);
        
        return b;
        
    }

    @Override
    public JComponent getContent ()
    {
        
        return this.editorsBox;
        
    }    
    
    private Set<ProjectEditor> getVisibleEditors ()
    {
        
        Set<ProjectEditor> pes = this.viewer.getProject ().getProjectEditors ();
        
        Set<ProjectEditor> ret = new LinkedHashSet ();
        
        if (pes != null)
        {
            
            for (ProjectEditor pe : pes)
            {
                
                if ((!this.showPreviousEditors)
                    &&
                    (!pe.isCurrent ())
                   )
                {
                    
                    continue;
                    
                }
                
                ret.add (pe);
                
            }

        }

        return ret;
        
    }
    
    public void updateItemCount ()
    {
        
        Set<ProjectEditor> pes = this.getVisibleEditors ();
        
        String title = String.format ("%s (%s)",
                                      this.getTitle (),
                                      Environment.formatNumber (pes.size ()));

        // Set the title on the header directly.
        this.header.setTitle (title);
        
    }    
    
    @Override
    public void setContentVisible (boolean v)
    {

        Set<ProjectEditor> pes = this.getVisibleEditors ();
        
        super.setContentVisible (pes.size () > 0);
                
        this.updateItemCount ();
                
    }
    
    private void fillEditorsBox ()
    {
        
        int count = this.editorsBox.getComponentCount ();
        
        // Check to see if we already have the editor.
        for (int i = count - 1; i > 0; i--)
        {
            
            Component c = this.editorsBox.getComponent (i);
            
            if (c instanceof EditorInfoBox)
            {
             
                this.editorsBox.remove (c);
                
            }
            
        }
                
        Set<ProjectEditor> pes = this.getVisibleEditors ();
        
        for (ProjectEditor pe : pes)
        {
                                         
            EditorInfoBox infBox = null;
            
            try
            {
                
                infBox = this.getEditorBox (pe.getEditor ());
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to get editor info box for editor: " +
                                      pe.getEditor (),
                                      e);
            
                continue;
                
            }
            
            infBox.init ();
                                                        
            this.editorsBox.add (infBox);
            
        }
        
        this.updateBorders ();

    }
    
    private void updateBorders ()
    {
        
        EditorInfoBox last = null;
        
        // Check to see if we already have the editor.
        for (int i = 0; i < this.editorsBox.getComponentCount (); i++)
        {
            
            Component c = this.editorsBox.getComponent (i);
            
            if (c instanceof EditorInfoBox)
            {
                
                EditorInfoBox b = (EditorInfoBox) c;
                
                this.setBorder (b,
                                false);
                
                last = b;                
                
            }
            
        }
        
        if (last != null)
        {
        
            this.setBorder (last,
                            true);
            
        }        
        
    }
    
    
    @Override
    public void init ()
    {

        final ProjectEditorsAccordionItem _this = this;
    
        this.getHeader ().addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                // TODO: Make this nicer
                if (!EditorsEnvironment.hasRegistered ())
                {
                    
                    if (EditorsEnvironment.isEditorsServiceAvailable ())
                    {
                    
                        try
                        {
                    
                            EditorsUIUtils.showRegister (_this.viewer);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to show editors service register",
                                                  e);
                            
                        }
                        
                    }
                    
                }
                                
            }
            
        });
                                     
        this.fillEditorsBox ();
        
        this.setContentVisible (true);        
                                                                           
        super.init ();
                
    }
    
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {
        
        final ProjectEditorsAccordionItem _this = this;
        
        m.add (UIUtils.createMenuItem ("Invite someone to edit this {project}",
                                       Constants.ADD_ICON_NAME,
                                       new ActionListener ()
                                       {
                                        
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                            
                                                EditorsUIUtils.showInviteEditor (_this.viewer);
                                            
                                            }
                                        
                                        }));

        m.add (UIUtils.createMenuItem ("Show all {contacts}",
                                       Constants.EDITORS_ICON_NAME,
                                       new ActionListener ()
                                       {
                                        
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                            
                                                try
                                                {
                                            
                                                    _this.viewer.viewEditors ();
                                                
                                                } catch (Exception e) {
                                                
                                                    Environment.logError ("Unable to view all editors",
                                                                          e);
                                                
                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                              "Unable to view all {contacts}");
                                                
                                                }
                                            
                                            }
                                        
                                        }));        

        // Get all previous editors.
        if (!this.showPreviousEditors)
        {
            
            int prevCount = 0;
            
            for (EditorEditor ed : EditorsEnvironment.getEditors ())
            {
            
                if (ed.isPrevious ())
                {
                    
                    prevCount++;
                    
                }
            
            }
          
            if (prevCount > 0)
            {
    
                m.add (UIUtils.createMenuItem (String.format ("View the previous {editors} (%s)",
                                                                  Environment.formatNumber (prevCount)),
                                                   Constants.STOP_ICON_NAME,
                                                   new ActionListener ()
                                                   {
                                                   
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            //_this.showPreviousEditors ();
                                                                                                                                                
                                                        }
                                                   
                                                   }));
                
            }
            
        }
        
    }
    
    private void setBorder (EditorInfoBox b,
                            boolean       isLast)
    {

        if (b.isShowAttentionBorder ())
        {
            
            b.setBorder (new CompoundBorder (new MatteBorder (0, 2, 0, 0, UIUtils.getColor ("#ff0000")),
                                             new CompoundBorder (new EmptyBorder (0, 5, 0, 0),
                                                                 (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0)))));
        
        } else {
            
            b.setBorder (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0));
            
        }
        
    }    
    
}