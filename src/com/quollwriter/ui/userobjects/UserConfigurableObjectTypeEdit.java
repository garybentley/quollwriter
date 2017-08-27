package com.quollwriter.ui.userobjects;

import java.awt.Component;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.*;

import java.io.*;
import java.math.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import com.toedter.calendar.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.components.DnDTabbedPane;
import com.quollwriter.ui.components.ScrollableBox;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;

public class UserConfigurableObjectTypeEdit 
{

    public static JTabbedPane getAsTabs (AbstractViewer             viewer,
                                         UserConfigurableObjectType userObjType)
    {
        
        DnDTabbedPane tabs = new DnDTabbedPane ();

        tabs.setAlignmentX (Component.LEFT_ALIGNMENT);

        tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);        
        
        BasicInfoAddEdit basicInfoEdit = new BasicInfoAddEdit (viewer,
                                                               userObjType);
        
        tabs.add ("General",
                  basicInfoEdit);
        
        FieldsAddEdit fieldsEdit = new FieldsAddEdit (viewer,
                                                      userObjType,
                                                      true);
        
        tabs.add ("Fields",
                  fieldsEdit);

        LayoutAddEdit layoutEdit = new LayoutAddEdit (viewer,
                                                      userObjType);
        
        tabs.add ("Layout",
                  layoutEdit);
        
        basicInfoEdit.refresh ();
        
        fieldsEdit.refresh ();
        
        fieldsEdit.setHelpText (String.format ("Use the button below to add a new field for your %s.  Drag-n-drop the fields to change the order of them.  Use the layout tab above to change how the fields are laid out and displayed.",
                                                    userObjType.getObjectTypeName ().toLowerCase ()));
        
        layoutEdit.setHelpText (String.format ("The layout tells Quoll Writer how you want to display the fields for your %s.  Certain fields like the %s description and picture are given special locations in the layouts.  For <b>Columns of fields</b> it just means that the fields are laid out in the order given in the <b>Fields</b> tab.",
                                                userObjType.getObjectTypeNamePlural ().toLowerCase (),
                                                userObjType.getObjectTypeName ().toLowerCase ()));

        layoutEdit.refresh ();
        
        return tabs;
        
    }
    
    public static Wizard getAsWizard (final AbstractViewer             viewer,
                                      final UserConfigurableObjectType userObjType)
    {

        final BasicInfoAddEdit basicInfo = new BasicInfoAddEdit (viewer,
                                                                 userObjType);
        
        final FieldsAddEdit fields = new FieldsAddEdit (viewer,
                                                        userObjType,
                                                        false)
        {
          
            @Override
            public void saveField (UserConfigurableObjectTypeField f)
            {
                
                // Do nothing.
                
            }
            
        };
        
        final LayoutAddEdit layout = new LayoutAddEdit (viewer,
                                                        userObjType);
        
        Wizard w = new Wizard<AbstractViewer> (viewer)
        {

            @Override
            public int getContentPreferredHeight ()
            {
        
                return 400;
        
            }
        
            @Override
            public String getNextStage (String currStage)
            {

                if (currStage.equals ("fields"))
                {
                    
                    return "layout";
                    
                }

                if (currStage.equals ("basic"))
                {
                    
                    return "fields";
                    
                }
            
                return null;

            }

            @Override
            public String getPreviousStage (String currStage)
            {

                if (currStage.equals ("fields"))
                {
                    
                    return "basic";
                    
                }

                if (currStage.equals ("layout"))
                {
                    
                    return "fields";
                    
                }

                return null;

            }

            public String getStartStage ()
            {

                return "basic";

            }

            public boolean handleStageChange (String oldStage,
                                              String newStage)
            {

                if (oldStage == null)
                {
                    
                    return true;
                    
                }
            
                if ((oldStage.equals ("basic"))
                    &&
                    (newStage.equals ("fields"))
                   )
                {
                    
                    return basicInfo.checkForm ();
                    
                }
            
                return true;

            }

            public boolean handleFinish ()
            {

                // Add a new accordion type in the project sidebar for the type.
                try
                {
                    
                    Environment.updateUserConfigurableObjectType (userObjType);

                    //this.viewer.showUserConfigurableObjectType (userObjType);
                    
                    return true;
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to add user object type: " +
                                          userObjType,
                                          e);
                    
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to add new type");
                                                
                }
            
                return false;

            }

            public void handleCancel ()
            {



            }

            public String getFirstHelpText ()
            {

                return null;
            
            }

            public WizardStep getStage (String stage)
            {

                WizardStep ws = new WizardStep ();
            
                if (stage.equals ("basic"))
                {

                    ws.title = "The basic details";
                    ws.helpText = "Enter the name and plural name (what do you call lots of your object) for your object.  For example, you might call it <b>Widget</b> and <b>Widgets</b>.";
                
                    basicInfo.showEdit (null,
                                        null,
                                        null,
                                        false);
                
                    ws.panel = basicInfo;

                }
                
                if (stage.equals ("fields"))
                {
                    
                    ws.title = "Add the fields you want the object to have";
                    ws.helpText = String.format ("Use the button below to add a new field for your object.  Drag-n-drop the fields to change the order of them.");
                    
                    fields.init ();
                    
                    ws.panel = fields;
                    
                }
                
                if (stage.equals ("layout"))
                {
                    
                    ws.title = "How should the fields be displayed";
                    ws.helpText = "Finally, and this step is optional, select how you want the fields to be displayed.  Remember you can change any of this information later so don't worry about it too much for now.";
                    
                    ws.panel = layout;
                    
                    layout.refresh ();
                    
                }
                
                return ws;

            }

        };

        return w;

    }
        
}
