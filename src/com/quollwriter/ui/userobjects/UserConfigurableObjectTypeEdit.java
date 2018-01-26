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

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

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

        tabs.add (getUIString (userobjects,basic,title),
                    //"General",
                  basicInfoEdit);

        FieldsAddEdit fieldsEdit = new FieldsAddEdit (viewer,
                                                      userObjType,
                                                      true);

        tabs.add (getUIString (userobjects,fields,title),
                //"Fields",
                  fieldsEdit);

        LayoutAddEdit layoutEdit = new LayoutAddEdit (viewer,
                                                      userObjType);

        tabs.add (getUIString (userobjects,layout,title),
                    //"Layout",
                  layoutEdit);

        basicInfoEdit.refresh ();

        fieldsEdit.refresh ();

        fieldsEdit.setHelpText (String.format (Environment.getUIString (LanguageStrings.userobjects,
                                                                        LanguageStrings.fields,
                                                                        LanguageStrings.view,
                                                                        LanguageStrings.text),
                                               userObjType.getObjectTypeName ()));

        layoutEdit.setHelpText (String.format (Environment.getUIString (LanguageStrings.userobjects,
                                                                        LanguageStrings.layout,
                                                                        LanguageStrings.view,
                                                                        LanguageStrings.text),
                                                userObjType.getObjectTypeNamePlural ()));

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
                                              Environment.getUIString (LanguageStrings.userobjects,
                                                                       LanguageStrings.type,
                                                                       LanguageStrings._new,
                                                                       LanguageStrings.actionerror));
                                              //"Unable to add new type.");

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

                java.util.List<String> prefix = new ArrayList ();
                prefix.add (LanguageStrings.userobjects);
                prefix.add (LanguageStrings.type);
                prefix.add (LanguageStrings._new);
                prefix.add (LanguageStrings.wizard);

                if (stage.equals ("basic"))
                {

                    ws.title = Environment.getUIString (prefix,
                                                        LanguageStrings.basic,
                                                        LanguageStrings.title);
                                                        //"The basic details";
                    ws.helpText = Environment.getUIString (prefix,
                                                           LanguageStrings.basic,
                                                           LanguageStrings.text);
                                                        //"Enter the name and plural name (what do you call lots of your object) for your object.  For example, you might call it <b>Widget</b> and <b>Widgets</b>.";

                    basicInfo.showEdit (null,
                                        null,
                                        null,
                                        false);

                    ws.panel = basicInfo;

                }

                if (stage.equals ("fields"))
                {

                    ws.title = Environment.getUIString (prefix,
                                                        LanguageStrings.fields,
                                                        LanguageStrings.title);
                                                        //"Add the fields you want the object to have";
                    ws.helpText = Environment.getUIString (prefix,
                                                           LanguageStrings.fields,
                                                           LanguageStrings.text);
                                                        //String.format ("Use the button below to add a new field for your object.  Drag-n-drop the fields to change the order of them.");

                    fields.init ();

                    ws.panel = fields;

                }

                if (stage.equals ("layout"))
                {

                    ws.title = Environment.getUIString (prefix,
                                                        LanguageStrings.layout,
                                                        LanguageStrings.title);
                                                           //"How should the fields be displayed";
                    ws.helpText = Environment.getUIString (prefix,
                                                           LanguageStrings.layout,
                                                           LanguageStrings.text);
                                                           //"Finally, and this step is optional, select how you want the fields to be displayed.  Remember you can change any of this information later so don't worry about it too much for now.";

                    ws.panel = layout;

                    //layout.refresh ();

                }

                return ws;

            }

        };

        return w;

    }

}
