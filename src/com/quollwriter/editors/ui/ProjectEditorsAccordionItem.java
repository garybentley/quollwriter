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

public class ProjectEditorsAccordionItem extends AccordionItem implements ProjectEditorChangedListener, EditorMessageListener
{

    private Box wrapper = null;
    private ProjectViewer viewer = null;
    private ComponentListener listener = null;
    private Box currentEditors = null;
    private Box previousEditors = null;
    private boolean showPreviousEditors = false;

    public ProjectEditorsAccordionItem (ProjectViewer pv)
    {

        super ("{Editors}",
               Constants.EDIT_ICON_NAME);
               //Constants.EDITORS_ICON_NAME);    
    
        this.viewer = pv;
        
        final ProjectEditorsAccordionItem _this = this;
    
        this.wrapper = new ScrollableBox (BoxLayout.Y_AXIS);
        this.wrapper.setBorder (UIUtils.createPadding (0, 10, 0, 0));
    
        this.currentEditors = new Box (BoxLayout.Y_AXIS);
        this.currentEditors.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.wrapper.add (this.currentEditors);
        
        this.previousEditors = new Box (BoxLayout.Y_AXIS);
        this.previousEditors.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.wrapper.add (this.previousEditors);
        
        JLabel help = UIUtils.createInformationLabel ("People who are editing this {project} for you.");        
        help.setBorder (UIUtils.createPadding (0, 0, 5, 0));
        
        this.currentEditors.add (help);    
        
        this.previousEditors.add (UIUtils.createBoldSubHeader ("<i>Previous {Editors}</i>",
                                                               null));    

        this.previousEditors.setVisible (false);
        
        this.listener = new ComponentAdapter ()
        {
            
            @Override
            public void componentResized (ComponentEvent ev)
            {
                
                _this.updateBorders ();
                                
            }
            
        };
        
        EditorsEnvironment.addProjectEditorChangedListener (this);        
        EditorsEnvironment.addEditorMessageListener (this);        
            
    }
      
    @Override
    public void handleMessage (EditorMessageEvent ev)
    {
        
        if (this.viewer.getProject () == null)
        {
            
            // The viewer is no longer valid.
            return;
            
        }
        
        // See if the editor is a project editor.
        ProjectEditor pe = this.viewer.getProject ().getProjectEditor (ev.getMessage ().getEditor ());
        
        if (pe == null)
        {
            
            return;
            
        }
      
        this.setContentVisible (true);
      
    }
    
    @Override
    public void projectEditorChanged (ProjectEditorChangedEvent ev)
    {

        if (this.viewer.getProject () == null)
        {
            
            return;
            
        }
            
        ProjectEditor pe = ev.getProjectEditor ();
            
        if (ev.getType () == ProjectEditorChangedEvent.PROJECT_EDITOR_ADDED)
        {
            
            // Editor is new.
            EditorInfoBox infBox = null;
            
            try
            {
                
                infBox = this.getEditorBox (pe);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to get editor info box for project editor: " +
                                      pe,
                                      e);
            
                return;
                
            }
            
            infBox.init ();
            
            this.currentEditors.add (infBox,
                                     1);
            
        } else {                
        
            for (int i = 0; i < this.currentEditors.getComponentCount (); i++)
            {
                
                Component c = this.currentEditors.getComponent (i);
                
                if (c instanceof EditorInfoBox)
                {
                    
                    EditorInfoBox b = (EditorInfoBox) c;
                    
                    if (b.getProjectEditor () == pe)
                    {
    
                        if (ev.getType () == EditorChangedEvent.EDITOR_DELETED)
                        {
                    
                            this.currentEditors.remove (b);
                            
                            break;
                                                        
                        }
                        
                        if (ev.getType () == EditorChangedEvent.EDITOR_CHANGED)
                        {
    
                            // See if there is a project editor and if they are now previous, if so remove them.
                            if (!pe.isCurrent ())
                            {
                                
                                this.previousEditors.add (b);
                                
                                break;
                                                                                        
                            } 
                            
                        }
                                            
                    }
                    
                }
                
            }
                    
            for (int i = 0; i < this.previousEditors.getComponentCount (); i++)
            {
                
                Component c = this.previousEditors.getComponent (i);
                
                if (c instanceof EditorInfoBox)
                {
                    
                    EditorInfoBox b = (EditorInfoBox) c;
                    
                    if (b.getProjectEditor () == pe)
                    {
    
                        if (ev.getType () == EditorChangedEvent.EDITOR_DELETED)
                        {
                    
                            this.previousEditors.remove (b);
                            
                            break;
                                                                                                        
                        }
                        
                        if (ev.getType () == EditorChangedEvent.EDITOR_CHANGED)
                        {
    
                            if (pe.isCurrent ())
                            {
                                
                                this.currentEditors.add (b);
                                  
                                break;
                                                            
                            } 
                            
                        }
                                            
                    }
                    
                }
                
            }
            
        }
        
        this.setContentVisible (true);

    }    
      
    private EditorInfoBox getEditorBox (ProjectEditor pe)
                                 throws GeneralException
    {
        
        return this.getEditorBox (pe.getEditor ());
        
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
        
        return this.wrapper;
        
    }    
    
    private Set<ProjectEditor> getCurrentEditors ()
    {
        
        Set<ProjectEditor> pes = this.viewer.getProject ().getProjectEditors ();
        
        Set<ProjectEditor> ret = new LinkedHashSet ();
        
        if (pes != null)
        {
            
            for (ProjectEditor pe : pes)
            {
                
                if (pe.isPrevious ())
                {
                    
                    continue;
                    
                }
                
                ret.add (pe);
                
            }

        }

        return ret;
        
    }
    
    private Set<ProjectEditor> getPreviousEditors ()
    {
        
        Set<ProjectEditor> pes = this.viewer.getProject ().getProjectEditors ();
        
        Set<ProjectEditor> ret = new LinkedHashSet ();
        
        if (pes != null)
        {
            
            for (ProjectEditor pe : pes)
            {
                
                if (pe.isCurrent ())
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
        
        Set<ProjectEditor> pes = this.getCurrentEditors ();
        
        String title = String.format ("%s (%s)",
                                      this.getTitle (),
                                      Environment.formatNumber (pes.size ()));

        // Set the title on the header directly.
        this.header.setTitle (title);
        
    }    
    
    @Override
    public void setContentVisible (boolean v)
    {

        Set<ProjectEditor> cpes = this.getCurrentEditors ();
        
        Set<ProjectEditor> ppes = this.getPreviousEditors ();

        this.currentEditors.setVisible (cpes.size () > 0);
        this.previousEditors.setVisible ((this.showPreviousEditors && ppes.size () > 0));
        
        super.setContentVisible (this.currentEditors.isVisible () || this.previousEditors.isVisible ());
                
        this.updateBorders ();
                
        this.updateItemCount ();
                
    }
        
    private void updateBorders ()
    {
        
        EditorInfoBox last = null;
        
        for (int i = 0; i < this.currentEditors.getComponentCount (); i++)
        {
            
            Component c = this.currentEditors.getComponent (i);
            
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
        
        last = null;
        
        for (int i = 0; i < this.previousEditors.getComponentCount (); i++)
        {
            
            Component c = this.previousEditors.getComponent (i);
            
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
    
    private void showPreviousEditors ()
    {
        
        this.previousEditors.setVisible (this.showPreviousEditors);

        this.setContentVisible (true);
                
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
                   
        Set<ProjectEditor> pes = this.viewer.getProject ().getProjectEditors ();
        
        if (pes != null)
        {
        
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
                
                if (!pe.isPrevious ())
                {                                                        

                    this.currentEditors.add (infBox);
                    
                } else {

                    this.previousEditors.add (infBox);
                    
                }
                
            }
            
        }
        
        this.setContentVisible (true);        
                                                                           
        super.init ();
                
    }
    
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {
        
        final ProjectEditorsAccordionItem _this = this;
        
        if (EditorsEnvironment.getUserAccount () != null)
        {
        
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

        }
                                            
        // Get all previous editors.
        if (!this.showPreviousEditors)
        {
            
            int prevCount = 0;
            
            Set<ProjectEditor> pes = this.viewer.getProject ().getProjectEditors ();
            
            if (pes != null)
            {
            
                for (ProjectEditor pe : pes)
                {
                
                    if (pe.isPrevious ())
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
                                                                
                                                                _this.showPreviousEditors = true;                                                                
                                                                _this.showPreviousEditors ();
                                                                                                                                                    
                                                            }
                                                       
                                                       }));
                    
                }
                
            }
            
        } else {
            
            int prevCount = 0;
            
            Set<ProjectEditor> pes = this.viewer.getProject ().getProjectEditors ();
            
            if (pes != null)
            {
            
                for (ProjectEditor pe : pes)
                {
                
                    if (pe.isPrevious ())
                    {
                        
                        prevCount++;
                        
                    }
                
                }
              
                if (prevCount > 0)
                {
        
                    m.add (UIUtils.createMenuItem ("Hide the previous {editors}",
                                                   Constants.CANCEL_ICON_NAME,
                                                   new ActionListener ()
                                                   {
                                                       
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            _this.showPreviousEditors = false;    
                                                            _this.showPreviousEditors ();
                                                                                                                                                    
                                                        }
                                                       
                                                    }));
                    
                }            
            
            }
            
        }
        
    }
    
    private void setBorder (EditorInfoBox b,
                            boolean       isLast)
    {

        b.setBorder (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0));

        /*
        if (b.isShowAttentionBorder ())
        {
            
            b.setBorder (new CompoundBorder (new MatteBorder (0, 2, 0, 0, UIUtils.getColor ("#ff0000")),
                                             new CompoundBorder (new EmptyBorder (0, 5, 0, 0),
                                                                 (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0)))));
        
        } else {
            
            b.setBorder (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0));
            
        }
        */
    }    
    
}