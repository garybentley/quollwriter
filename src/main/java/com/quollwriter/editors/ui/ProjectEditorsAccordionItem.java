package com.quollwriter.editors.ui;

import java.util.*;
import java.util.function.*;

import javafx.beans.value.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.collections.*;

import com.quollwriter.data.DataObject;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

import static com.quollwriter.LanguageStrings.*;

public class ProjectEditorsAccordionItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    private VBox content = null;
    private VBox currentEditors = null;
    private VBox previousEditors = null;
    private boolean showPreviousEditors = false;
    private boolean inited = false;
    private IntegerProperty countProp = new SimpleIntegerProperty (0);
    private Map<ProjectEditor, ChangeListener<Boolean>> listeners = new HashMap<> ();

    public ProjectEditorsAccordionItem (ProjectViewer   pv,
                                        IPropertyBinder binder)
    {

        super (pv,
                //"{Editors}",
                binder);

        this.content = new VBox ();

        this.currentEditors = new VBox ();

        this.previousEditors = new VBox ();
        this.previousEditors.managedProperty ().bind (this.previousEditors.visibleProperty ());
        this.previousEditors.setVisible (false);

        long currSize = pv.getProject ().getProjectEditors ().stream ()
            .filter (pe -> !pe.isPrevious ())
            .count ();
        this.countProp.setValue (currSize);

        this.content.getChildren ().addAll (this.currentEditors, this.previousEditors);

        QuollTextView help = QuollTextView.builder ()
            .inViewer (pv)
            .styleClassName (StyleClassNames.INFORMATION)
            .text (getUILanguageStringProperty (project,sidebar,editors,text))
            .build ();

        help.prefWidthProperty ().bind (this.content.widthProperty ());
        help.setVisible (currSize > 0);
        help.managedProperty ().bind (help.visibleProperty ());

        this.currentEditors.getChildren ().add (help);

        this.previousEditors.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.SUBTITLE)
            .label (project,sidebar,editors,previouseditors,title)
            .build ());

        for (ProjectEditor pe : pv.getProject ().getProjectEditors ())
        {

            this.addProjectEditor (pe);

        }

        binder.addSetChangeListener (pv.getProject ().getProjectEditors (),
                                     ev ->
        {

            UIUtils.runLater (() ->
            {

                long _currSize = pv.getProject ().getProjectEditors ().stream ()
                    .filter (pe -> !pe.isPrevious ())
                    .count ();
                this.countProp.setValue (_currSize);
                help.setVisible (_currSize > 0);

                if (ev.wasRemoved ())
                {

                    ProjectEditor pe = ev.getElementRemoved ();

                    pe.currentProperty ().removeListener (this.listeners.get (pe));

                    for (Node n : this.previousEditors.getChildren ())
                    {

                        if (!(n instanceof EditorInfoBox))
                        {

                            continue;

                        }

                        EditorInfoBox ib = (EditorInfoBox) n;

                        if (ib.getEditor ().equals (pe))
                        {

                            this.previousEditors.getChildren ().remove (ib);

                            if (this.previousEditors.getChildren ().size () == 1)
                            {

                                this.previousEditors.setVisible (false);

                            }

                            return;

                        }

                    }

                    for (Node n : this.currentEditors.getChildren ())
                    {

                        if (!(n instanceof EditorInfoBox))
                        {

                            continue;

                        }

                        EditorInfoBox ib = (EditorInfoBox) n;

                        if (ib.getEditor ().equals (pe))
                        {

                            this.currentEditors.getChildren ().remove (ib);
                            return;

                        }

                    }

                }

                if (ev.wasAdded ())
                {

                    ProjectEditor pe = ev.getElementAdded ();

                    this.addProjectEditor (pe);

                }

            });

        });

    }

    private void addProjectEditor (ProjectEditor pe)
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

        ChangeListener<Boolean> currentList = (pr, oldv, newv) ->
        {

            if (!oldv && newv)
            {

                for (Node n : this.previousEditors.getChildren ())
                {

                    if (!(n instanceof EditorInfoBox))
                    {

                        continue;

                    }

                    EditorInfoBox ib = (EditorInfoBox) n;

                    if (ib.getEditor ().equals (pe))
                    {

                        this.previousEditors.getChildren ().remove (ib);
                        this.currentEditors.getChildren ().add (1,
                                                                ib);

                    }

                }

            }

            if (oldv && !newv)
            {
System.out.println ("CHANGED REMOVING");
                for (Node n : this.currentEditors.getChildren ())
                {

                    if (!(n instanceof EditorInfoBox))
                    {

                        continue;

                    }

                    EditorInfoBox ib = (EditorInfoBox) n;

                    if (ib.getEditor ().equals (pe))
                    {

                        this.currentEditors.getChildren ().remove (ib);
                        this.previousEditors.getChildren ().add (1,
                                                                 ib);

                    }

                }

            }

        };

        pe.currentProperty ().addListener (currentList);

        this.listeners.put (pe,
                            currentList);

        if (pe.isPrevious ())
        {

            this.previousEditors.getChildren ().add (1,
                                                     infBox);

        } else {

            this.currentEditors.getChildren ().add (1,
                                                    infBox);

        }

    }

    @Override
    public Node getContent ()
    {

        return this.content;

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.countProp;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        ProjectViewer viewer = this.viewer;
        ProjectEditorsAccordionItem _this = this;

        return () ->
        {

            List<String> prefix = Arrays.asList (project,sidebar,editors,headerpopupmenu,items);

            Set<MenuItem> items = new LinkedHashSet<> ();

            if (EditorsEnvironment.getUserAccount () != null)
            {

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,invite)))
                                                //"Invite someone to edit this {project}",
                    .iconName (StyleClassNames.ADD)
                    .onAction (ev ->
                    {

                        List<EditorEditor> eds = new ArrayList<> (EditorsEnvironment.getEditors ());

                        List<ProjectEditor> projEds = null;

                        try
                        {

                            projEds = EditorsEnvironment.getProjectEditors (viewer.getProject ().getId ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to get project editors for project: " +
                                                  viewer.getProject ().getId (),
                                                  e);

                            ComponentUtils.showErrorMessage (viewer,
                                                             getUILanguageStringProperty (project,sidebar,editors,sendinvite,actionerror));
                                                      //"Unable to show contacts.");

                            return;

                        }

                        for (ProjectEditor pe : projEds)
                        {

                            eds.remove (pe.getEditor ());

                        }

                        Iterator<EditorEditor> iter = eds.iterator ();

                        while (iter.hasNext ())
                        {

                            EditorEditor ed = iter.next ();

                            if (ed.isPending ())
                            {

                                iter.remove ();

                            }

                        }

                        QuollHyperlink l = QuollHyperlink.builder ()
                            .label (project,sidebar,editors,sendinvite,popup,labels,notinlist)
                            .styleClassName (StyleClassNames.EMAIL)
                            .onAction (eev ->
                            {

                                EditorsUIUtils.showInviteEditor (viewer);

                            })
                            .build ();

                        EditorsUIUtils.showContacts (FXCollections.observableList (eds),
                                                     getUILanguageStringProperty (project,sidebar,editors,sendinvite,popup,title),
                                                     viewer,
                                                     ed ->
                        {

                            EditorsUIUtils.showSendProject (viewer,
                                                            ed,
                                                            null);

                        },
                        l);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,vieweditors)))
                    .iconName (StyleClassNames.EDITORS)
                    .onAction (eev ->
                    {

                        try
                        {

                            viewer.viewEditors ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to view all editors",
                                                  e);

                            ComponentUtils.showErrorMessage (viewer,
                                                             getUILanguageStringProperty (editors,vieweditorserror));
                                                      //"Unable to view all {contacts}");

                        }

                    })
                    .build ());

            }

            long prevCount = 0;

            if (viewer.getProject ().getProjectEditors () != null)
            {

                prevCount = viewer.getProject ().getProjectEditors ().stream ()
                .filter (pe -> pe.isPrevious ())
                .count ();

            }

            // Get all previous editors.
            if (!this.showPreviousEditors)
            {

                if (prevCount > 0)
                {

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,previouseditors),
                                                             Environment.formatNumber (prevCount)))
                        .iconName (StyleClassNames.PREVIOUSEDITORS)
                        .onAction (eev ->
                        {

                            this.showPreviousEditors = true;
                            this.showPreviousEditors ();

                        })
                        .build ());

                }

            } else {

                if (prevCount > 0)
                {

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,hidepreviouseditors)))
                        .iconName (StyleClassNames.HIDE)
                        .onAction (eev ->
                        {

                            this.showPreviousEditors = false;
                            this.showPreviousEditors ();

                        })
                        .build ());

                }

            }

            return items;

        };

    }

    @Override
    public List<String> getStyleClassNames ()
    {

        return Arrays.asList (StyleClassNames.CONTACTS);

    }

    @Override
    public BooleanProperty showItemCountOnHeader ()
    {

        return new SimpleBooleanProperty (true);

    }

    @Override
    public boolean canImport (NamedObject o)
    {

        return false;

    }

    @Override
    public void importObject (NamedObject o)
    {

        // Do nothing.

    }

    @Override
    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (project,sidebar,editors,title);

    }

    @Override
    public String getId ()
    {

        return ProjectEditor.OBJECT_TYPE;

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
                                             true,
                                             this.getBinder ());

        b.addFullPopupListener ();

        return b;

    }
/*
TODO?
    public void XupdateItemCount ()
    {

        Set<ProjectEditor> pes = this.viewer.getCurrentEditors ();

        String title = String.format ("%s (%s)",
                                      this.getTitle (),
                                      Environment.formatNumber (pes.size ()));

        // Set the title on the header directly.
        this.header.setTitle (title);

    }
    */
/*
TODO?
    @Override
    public void setContentVisible (boolean v)
    {

        Set<ProjectEditor> cpes = this.getCurrentEditors ();

        Set<ProjectEditor> ppes = this.getPreviousEditors ();

        this.currentEditors.setVisible (cpes.size () > 0);
        this.previousEditors.setVisible ((this.showPreviousEditors && ppes.size () > 0));

        super.setContentVisible (this.currentEditors.isVisible () || this.previousEditors.isVisible ());

        this.updateItemCount ();

    }
*/
    private void showPreviousEditors ()
    {

        this.previousEditors.setVisible (this.showPreviousEditors);

    }
/*
TODO Remove?  put check on header for register or handle better?
    @Override
    public void init ()
    {

        if (this.inited)
        {

            return;

        }

        this.inited = true;

        super.init ();

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

    }
*/
}
