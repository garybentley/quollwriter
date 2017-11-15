package com.quollwriter.ui.actionHandlers;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class ChapterItemActionHandler<E extends ChapterItem> extends AbstractFormPopup<ProjectViewer, E>
{

    private MultiLineTextFormItem descField = null;
    private CheckboxFormItem      addToChapter = null;
    protected Chapter        chapter = null;
    private int            showAt = -1;
    protected ChapterItemViewer<ProjectViewer> itemViewer = null;
    protected ProjectViewer viewer = null;

    public ChapterItemActionHandler (E                                item,
                                     ChapterItemViewer<ProjectViewer> itemViewer,
                                     int                              mode,
                                     int                              showAt)
    {

        super (item,
               itemViewer.getViewer (),
               mode,
               true);

        this.itemViewer = itemViewer;
        this.viewer = this.itemViewer.getViewer ();
        this.chapter = item.getChapter ();

        this.setCallCancelOnClose (true);

        final QTextEditor editor = this.itemViewer.getEditor ();

        final ChapterItemActionHandler _this = this;

        if (mode == ChapterItemActionHandler.EDIT)
        {

            this.showAt = item.getPosition ();

        } else
        {

            this.showAt = showAt;

        }

        this.setPopupOver (this.viewer.getEditorForChapter (this.chapter));

        if (mode == ChapterItemActionHandler.ADD)
        {

            this.setOnShowAction (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        if (_this.mode == ADD)
                        {

                            _this.itemViewer.addItem (_this.getChapterItem ());

                        }

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to add item: " +
                                              _this.object +
                                              " to editor panel",
                                              e);

                    }

                }

            });

        }

    }

    public E getChapterItem ()
    {

        // Damn compiler...
        return (E) this.object;

    }

    @Override
    public Point getShowAtPosition ()
    {

        int y = 0;

        Point lastMousePosition = this.itemViewer.getLastMousePosition ();

        int at = this.showAt;

        QTextEditor editor = this.itemViewer.getEditor ();

        if (at == -1)
        {

            // Calculate where it should be displayed.
            String sel = editor.getSelectedText ();

            if ((sel != null) &&
                (!sel.trim ().equals ("")))
            {

                // We have some text so use "at"...
                at = editor.getSelectionStart ();

            } else
            {

                int c = editor.getCaret ().getDot ();

                if (c >= 0)
                {

                    at = c;

                } else
                {

                    if (lastMousePosition != null)
                    {

                        at = editor.viewToModel (new Point (lastMousePosition.x,
                                                            lastMousePosition.y));

                    } else
                    {

                        at = editor.getText ().length () - 1;

                    }

                }

            }

        }

        this.object.setPosition (at);

        Rectangle r = null;

        try
        {

            r = editor.modelToView (at);

        } catch (Exception e)
        {

            Environment.logError ("Position: " +
                                  at +
                                  " is not valid.",
                                  e);

            return super.getShowAtPosition ();

        }

        JScrollPane scrollPane = this.itemViewer.getScrollPane ();

        y = r.y + 22 - scrollPane.getVerticalScrollBar ().getValue ();

        if ((y < 0) ||
            (y > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ())))
        {

            // Recalculate y since we have moved the scroll position.
            y = r.y + 22; // - scrollPane.getVerticalScrollBar ().getValue ();

        }
/*
        // Adjust the bounds so that the form is fully visible.
        if ((y + this.f.getPreferredSize ().height) > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ()))
        {

            y = y - 22 - this.f.getPreferredSize ().height;

        }

        y -= this.f.getPreferredSize ().height;
  */
        int xOffset = this.itemViewer.getIconColumnXOffset (this.object);

        Point p = new Point (this.itemViewer.getIconColumn ().getWidth () - xOffset,
                             y);

        return p;

    }

    @Override
    public JComponent getFocussedField ()
    {

        return this.descField;

    }

    @Override
    public Icon getIcon (int iconSizeType)
    {

        return Environment.getIcon (this.object.getObjectType (),
                                    iconSizeType);

    }

    @Override
    public String getTitle ()
    {

        if (this.mode == ChapterItemActionHandler.EDIT)
        {

            return String.format (getUIString (chapteritems,edit,title),
                                  Environment.getObjectTypeName (this.object.getObjectType ()));
                                  //"Edit " + Environment.getObjectTypeName (this.object.getObjectType ());

        }

        return String.format (getUIString (chapteritems,add,title),
                              Environment.getObjectTypeName (this.object.getObjectType ()));
        //"Add New " + Environment.getObjectTypeName (this.object.getObjectType ());

    }

    @Override
    public Set<FormItem> getFormItems (String      selectedText)
    {

        this.descField = new MultiLineTextFormItem (getUIString (chapteritems,labels,description,text),
                                                    //"Description",
                                                    this.viewer,
                                                    getUIString (chapteritems,labels,description,tooltip),
                                                    //"Enter the description here...",
                                                    5,
                                                    -1,
                                                    false,
                                                    null);

        this.descField.setCanFormat (true);
        this.descField.setAutoGrabFocus (true);

        UIUtils.addDoActionOnReturnPressed (this.descField.getTextArea (),
                                            this.getSaveAction ());

        this.addToChapter = new CheckboxFormItem (null,
                                                  getUIString (chapteritems,labels,adddesctochapter));
                                                  //"Add the description to the {Chapter}"));

        boolean sel = true;

        if (this.viewer.hasTempOption ("addToChapter"))
        {

            sel = this.viewer.isTempOption ("addToChapter");

        }

        this.addToChapter.setSelected (sel);

        Set<FormItem> items = new LinkedHashSet ();

        items.add (this.descField);

        if (mode == ChapterItemActionHandler.ADD)
        {

            items.add (this.addToChapter);

            if ((selectedText != null)
                &&
                (selectedText.trim ().length () > 0)
               )
            {

                this.descField.setText (new StringWithMarkup (selectedText));
                this.addToChapter.setSelected (false);

            }

        } else
        {

            this.descField.setText (this.object.getDescription ());

        }

        return items;

    }

    @Override
    public Set<String> getFormErrors ()
    {

        Set<String> errs = new LinkedHashSet ();

        if (this.descField.getText () == null)
        {

            errs.add (getUIString (chapteritems,errors,nodescription));
            //"Please enter a description.");

        }

        return errs;

    }

    @Override
    public void handleCancel ()
    {

        if (this.mode == ADD)
        {

            this.itemViewer.removeItem (this.object);

        }

    }

    @Override
    public boolean handleSave ()
    {

        this.object.setDescription (this.descField.getValue ());

        this.viewer.setTempOption ("addToChapter",
                                   this.addToChapter.isSelected ());

        QTextEditor editor = this.itemViewer.getEditor ();

        if (this.mode == ChapterItemActionHandler.ADD)
        {

            try
            {

                String d = this.object.getDescriptionText ();

                if (this.addToChapter.isSelected ())
                {

                    if ((d != null) &&
                        (d.trim ().equals ("")))
                    {

                        d = null;

                        this.object.setDescription (null);

                    } else
                    {

                        if ((d != null)
                            &&
                            (!d.trim ().equals (""))
                           )
                        {

                            String toAdd = d.trim () + "\n";

                            // We need to append the text.
                            editor.insertText (this.object.getPosition (),
                                               toAdd);

                            // Need to update the text position because it will have moved.
                            this.object.setTextPosition (editor.getDocument ().createPosition (this.object.getPosition () - toAdd.length () + 1));

                        }

                    }

                }

                // See if we are adding at the end of the chapter.
                if (editor.isPositionAtTextEnd (this.object.getPosition ()))
                {

                    // Add a newline to the end of the chapter.
                    editor.insertText (this.object.getPosition (),
                                       "\n");

                    this.object.setTextPosition (editor.getDocument ().createPosition (this.object.getPosition () - 1));

                }

                // Need to save the object first so the key is setup correctly.
                this.viewer.saveObject (this.object,
                                        true);

                if (this.object instanceof Scene)
                {

                    Set<OutlineItem> oitems = this.chapter.getItemsFromPositionToNextScene (this.object.getPosition ());

                    Scene s = (Scene) this.object;

                    // Change the scene for each item.
                    for (OutlineItem i : oitems)
                    {

                        s.addOutlineItem (i);

                    }

                    // Add the item to the chapter.
                    this.chapter.addChapterItem (this.object);

                }

                if (this.object instanceof OutlineItem)
                {

                    Scene s = this.chapter.getLastScene (this.object.getPosition ());

                    if (s != null)
                    {

                        s.addOutlineItem ((OutlineItem) this.object);

                    } else
                    {

                        this.chapter.addChapterItem (this.object);

                    }

                }

                // Force a save of the chapter.
                this.viewer.saveObject (this.chapter,
                                        true);

                this.viewer.fireProjectEvent (this.object.getObjectType (),
                                              ProjectEvent.NEW,
                                              this.object);

            } catch (Exception e)
            {

                this.chapter.removeChapterItem (this.object);

                Environment.logError ("Unable to add new chapter item",
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          String.format (getUIString (chapteritems,add,actionerror),
                                                         Environment.getObjectTypeName (this.object.getObjectType ())));
                                                         //)"Unable to add new " + Environment.getObjectTypeName (this.object.getObjectType ()) + ".");

                return false;

            }

        } else
        {

            // Update.
            try
            {

                this.viewer.saveObject (this.object,
                                               true);

                this.viewer.fireProjectEvent (this.object.getObjectType (),
                                              ProjectEvent.EDIT,
                                              this.object);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save item: " +
                                      this.object,
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          String.format (getUIString (chapteritems,edit,actionerror),
                                                         Environment.getObjectTypeName (this.object.getObjectType ())));
                                          //"Unable to save " + Environment.getObjectTypeName (this.object.getObjectType ()).toLowerCase () + ".");

                return false;

            }

        }

        // Reload the entire tree.
        this.viewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

        editor.grabFocus ();

        return true;

    }

}
