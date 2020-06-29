package com.quollwriter.ui.fx.popups;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AddEditStructureItemPopup extends PopupContent<ProjectViewer>
{

    private static final String ADD_TO_CHAPTER_TEMP_OPTION = "addToChapter";
    private static final String POPUP_ID = "structureitem";
    private ChapterItem item = null;
    private Chapter chapter = null;
    private Form form = null;
    private QuollTextArea desc = null;
    private CheckBox addToChapter = null;
    private boolean addMode = false;
    private HyperlinkLinkedToPanel linkedToPanel = null;

    public AddEditStructureItemPopup (ProjectViewer viewer,
                                      ChapterItem   item,
                                      Chapter       chapter)
    {

        super (viewer);

        this.item = item;
        this.chapter = chapter;

        if (this.item.getKey () == null)
        {

            throw new IllegalArgumentException ("Expected item to have a key.");

        }

        this.addMode = this.item.getKey () < 0;

        ChapterEditorPanelContent editor = viewer.getEditorForChapter (this.chapter);

        this.form = this.addItems (Form.builder (),
                                   editor.getSelectedText ())
            .confirmButton (getUILanguageStringProperty (buttons,save))
            .cancelButton (getUILanguageStringProperty (buttons,cancel))
            .build ();

        this.form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                                   ev ->
        {

            this.close ();

        });

        this.form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                                   ev ->
        {

            this.form.hideError ();

            StringProperty err = this.getFormError ();

            if (err != null)
            {

                this.form.showError (err);
                return;

            }

            if (this.handleSave ())
            {

                this.close ();

            }

        });

        this.getChildren ().add (this.form);

    }

    public void setOnClose (Runnable r)
    {

        this.getPopup ().setOnClose (r);

    }

    public void setOnCancel (EventHandler<Form.FormEvent> h)
    {

        this.form.setOnCancel (h);

    }

    private StringProperty getFormError ()
    {

        if ((this.desc.getText () == null)
            ||
            (this.desc.getText ().trim ().length () == 0)
           )
        {

            return getUILanguageStringProperty (chapteritems,errors,nodescription);
            //"Please enter a description.");

        }

        return null;

    }

    private Form.Builder addItems (Form.Builder builder,
                                   String       selectedText)
    {

        this.desc = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (chapteritems,labels,description,tooltip))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .withViewer (this.viewer)
            .autoGrabFocus (true)
            .formattingEnabled (true)
            .build ();

        builder.item (getUILanguageStringProperty (chapteritems,labels,description,text),
                      this.desc);

        this.addToChapter = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (chapteritems,labels,adddesctochapter))
            .selected (this.viewer.hasTempOption (ADD_TO_CHAPTER_TEMP_OPTION) ? this.viewer.isTempOption (ADD_TO_CHAPTER_TEMP_OPTION) : true)
            .build ();

        if (this.addMode)
        {

            builder.item (this.addToChapter);

            if ((selectedText != null)
                &&
                (selectedText.trim ().length () > 0)
               )
            {

                this.desc.setText (new StringWithMarkup (selectedText));
                this.addToChapter.setSelected (false);

            }

        } else
        {

            this.desc.setText (this.item.getDescription ());

        }

        this.linkedToPanel = new HyperlinkLinkedToPanel (this.item,
                                                         this.getBinder (),
                                                         this.viewer);

        builder.item (this.linkedToPanel);

        return builder;

    }

    private boolean handleSave ()
    {

        this.item.setDescription (this.desc.getTextWithMarkup ());

        this.viewer.setTempOption ("addToChapter",
                                   this.addToChapter.isSelected ());

        ChapterEditorPanelContent editor = this.viewer.getEditorForChapter (this.chapter);

        if (this.addMode)
        {

            try
            {

                this.item.setKey (null);
                this.item.setChapter (this.chapter);

                String d = this.item.getDescriptionText ();

                if (this.addToChapter.isSelected ())
                {

                    if ((d != null) &&
                        (d.trim ().equals ("")))
                    {

                        d = null;

                        this.item.setDescription (null);

                    } else
                    {

                        if ((d != null)
                            &&
                            (!d.trim ().equals (""))
                           )
                        {

                            String toAdd = d.trim () + "\n";

                            // We need to append the text.
                            editor.insertText (this.item.getPosition (),
                                               toAdd);

                            // Need to update the text position because it will have moved.
                            //this.item.setTextPosition2 (editor.createTextPosition (this.item.getPosition () - toAdd.length () + 1));

                        }

                    }

                }

                // See if we are adding at the end of the chapter.
                if (editor.isPositionAtTextEnd (this.item.getPosition ()))
                {

                    // Add a newline to the end of the chapter.
                    editor.insertText (this.item.getPosition (),
                                       "\n");

                    //this.item.setTextPosition2 (editor.createTextPosition (this.item.getPosition () - 1));

                }

                // Need to save the object first so the key is setup correctly.
                this.viewer.saveObject (this.item,
                                        true);

                if (this.item instanceof Scene)
                {

                    Set<OutlineItem> oitems = this.chapter.getItemsFromPositionToNextScene (this.item.getPosition ());

                    Scene s = (Scene) this.item;

                    // Change the scene for each item.
                    for (OutlineItem i : oitems)
                    {

                        s.addOutlineItem (i);

                    }

                    // Add the item to the chapter.
                    this.chapter.addChapterItem (this.item);

                }

                if (this.item instanceof OutlineItem)
                {

                    Scene s = this.chapter.getLastScene (this.item.getPosition ());

                    if (s != null)
                    {

                        s.addOutlineItem ((OutlineItem) this.item);

                    } else
                    {

                        this.chapter.addChapterItem (this.item);

                    }

                }

                this.item.setLinks (this.linkedToPanel.getLinkedToPanel ().getSelected ());

                this.viewer.saveObject (this.item,
                                        true);

                // Force a save of the chapter.
                /*
                this.viewer.saveObject (this.chapter,
                                        true);
*/
                this.viewer.fireProjectEvent (this.item instanceof OutlineItem ? ProjectEvent.Type.outlineitem : ProjectEvent.Type.scene,
                                              ProjectEvent.Action._new,
                                              this.item);

            } catch (Exception e)
            {

                // TODO Remove?this.chapter.removeChapterItem (this.item);

                Environment.logError ("Unable to add new chapter item",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Arrays.asList (chapteritems,add,actionerror),
                                                                              Environment.getObjectTypeName (this.item.getObjectType ())));
                                                         //)"Unable to add new " + Environment.getObjectTypeName (this.object.getObjectType ()) + ".");

                return false;

            }

        } else
        {

            // Update.
            try
            {

                this.item.setLinks (this.linkedToPanel.getLinkedToPanel ().getSelected ());

                this.viewer.saveObject (this.item,
                                        true);

                this.viewer.fireProjectEvent (this.item instanceof OutlineItem ? ProjectEvent.Type.outlineitem : ProjectEvent.Type.scene,
                                              ProjectEvent.Action.edit,
                                              this.item);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save item: " +
                                      this.item,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Arrays.asList (chapteritems,edit,actionerror),
                                                                              Environment.getObjectTypeName (this.item.getObjectType ())));
                                          //"Unable to save " + Environment.getObjectTypeName (this.object.getObjectType ()).toLowerCase () + ".");

                return false;

            }

        }

        editor.requestFocus ();

        return true;

    }

    @Override
    public QuollPopup createPopup ()
    {

        StringProperty title = null;

        if (!this.addMode)
        {

            title = getUILanguageStringProperty (Arrays.asList (chapteritems,edit,LanguageStrings.title),
                                                 Environment.getObjectTypeName (this.item.getObjectType ()));
                              //"Edit " + Environment.getObjectTypeName (this.object.getObjectType ());

        } else {

            title = getUILanguageStringProperty (Arrays.asList (chapteritems,add,LanguageStrings.title),
                                                 Environment.getObjectTypeName (this.item.getObjectType ()));

        }

        QuollPopup p = QuollPopup.builder ()
            .title (title)
            .styleClassName (this.addMode ? StyleClassNames.ADD : StyleClassNames.EDIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (AddEditStructureItemPopup.getPopupIdForChapterItem (this.item))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.getStyleClass ().add (StyleClassNames.CHAPTERITEM);
        p.getStyleClass ().add (this.item instanceof OutlineItem ? StyleClassNames.OUTLINEITEM : StyleClassNames.SCENE);

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            this.desc.requestFocus ();

        });

        return p;

    }

    public static String getPopupIdForChapterItem (ChapterItem ci)
    {

        return POPUP_ID + ci.getObjectReference ().asString ();

    }

}
