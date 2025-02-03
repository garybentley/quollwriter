package com.quollwriter.ui.fx;

import java.util.concurrent.*;

import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;

public abstract class LanguageStringsIdBox<E extends Value, T extends Object> extends VBox implements org.fxmisc.flowless.Cell<Value, LanguageStringsIdBox<E, T>>
{

    protected LanguageStringsIdsPanel panel = null;
    protected E baseValue = null;
    protected E stringsValue = null;
    private Id id = null;
    private Form form = null;

    public LanguageStringsIdBox (final E                       baseValue,
                                 final E                       stringsValue,
                                 final LanguageStringsIdsPanel panel)
    {

        final LanguageStringsIdBox _this = this;

        this.panel = panel;
        this.baseValue = baseValue;
        this.stringsValue = stringsValue;

        this.id = new Id (0,
                          BaseStrings.toId (this.baseValue.getId ()),
                          false);

    }

    public E getBaseValue ()
    {

        return this.baseValue;

    }

    @Override
    public boolean isReusable ()
    {

        return false;

    }

    @Override
    public void updateItem (Value v)
    {



    }

    @Override
    public LanguageStringsIdBox getNode ()
    {

        //this.init ();

        return this;

    }

    public void init ()
    {

        final String fid = BaseStrings.toId (this.baseValue.getId ());

        final LanguageStringsIdBox _this = this;

        Set<MenuItem> items = new LinkedHashSet<> ();
        items.add (QuollMenuItem.builder ()
            .label (new SimpleStringProperty ("Find all references"))
            .iconName (StyleClassNames.FIND)
            .onAction (ev ->
            {

                _this.panel.getEditor ().showFind (BaseStrings.toId (_this.baseValue.getId ()));

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (new SimpleStringProperty ("Report error about this Id"))
            .iconName (StyleClassNames.BUG)
            .onAction (ev ->
            {

                this.getEditor ().showReportProblemForId (BaseStrings.toId (this.baseValue.getId ()));

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (new SimpleStringProperty ("Copy Id"))
            .iconName (StyleClassNames.COPY)
            .onAction (ev ->
            {

                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString (BaseStrings.ID_REF_START + BaseStrings.toId (this.baseValue.getId ()) + BaseStrings.ID_REF_END);
                clipboard.setContent (content);

            })
            .build ());

        Header h = Header.builder ()
            .title (new SimpleStringProperty (BaseStrings.toId (this.baseValue.getId ())))
            .contextMenu (items)
            .build ();

        this.getStyleClass ().add (this.getStyleClassName ());

        this.getChildren ().add (h);

        String comment = this.baseValue.getComment ();

        String scount = "";

        int sc = this.getSCount ();

        if (sc > 0)
        {

            StringBuilder c = new StringBuilder ();

            for (int i = 0; i < sc; i++)
            {

                if (c.length () > 0)
                {

                    c.append (", ");

                }

                c.append ("%" + (i + 1) + "$s");

            }

        }

        if (comment == null)
        {

            comment = "";

        }

        if (scount.length () > 0)
        {

            scount = String.format ("Requires values: %1$s to be present in your value.",
                                    scount);

            if (comment.length () > 0)
            {

                comment += "\n";

            }

            comment += scount;

        }

        Form.Builder fb = Form.builder ()
            .description (new SimpleStringProperty (comment));

        for (Form.Item i : this.getFormItems ())
        {

            fb.item (i);

        }

        this.form = fb.build ();

        this.getChildren ().add (this.form);

        this.showErrors (false);

    }

    private int getSCount ()
    {

        if (this.baseValue instanceof TextValue)
        {

            return ((TextValue) this.baseValue).getSCount ();

        }

        return -1;

    }

    public void updatePreviews ()
    {

        this.panel.getEditor ().updatePreviews ();

    }

    public void updateSideBar (Node    n)
    {

        this.panel.getEditor ().updateSideBar (this.panel.getParentNode ());

    }

    public AbstractLanguageStringsEditor getEditor ()
    {

        return this.panel.getEditor ();

    }

    public abstract String getStyleClassName ();

    public abstract T getUserValue ();

    public abstract Set<Form.Item> getFormItems ();

    public abstract void saveValue ()
                             throws GeneralException;

/*
    public void saveValue ()
                    throws GeneralException
    {

        String uv = this.getUserValue ();

        if (uv != null)
        {

            if (this.stringsValue != null)
            {

                this.stringsValue.setRawText (uv);

            } else {

                this.stringsValue = this.editor.userStrings.insertTextValue (this.baseValue.getId ());

                //this.stringsValue.setSCount (this.baseValue.getSCount ());
                this.stringsValue.setRawText (uv);

            }

        } else {

            this.editor.userStrings.removeNode (this.baseValue.getId ());

        }

    }
*/
/*
    public Id getIdAtOffset (int offset)
    {

        return BaseStrings.getId (this.userValue.getEditor ().getText (),
                                  offset);

    }
*/
/*
    public Id getIdAtCaret ()
    {

        return this.getIdAtOffset (this.userValue.getEditor ().getCaretPosition ());

    }
*/
    public Id getForId ()
    {

        return this.id;

    }

    public boolean hasUserValue ()
    {

        return this.getUserValue () != null;

    }

/*
    public String getUserValue ()
    {

        StringWithMarkup sm = this.userValue.getTextWithMarkup ();

        if (sm != null)
        {

            if (!sm.hasText ())
            {

                return null;

            }

            return sm.getMarkedUpText ();

        }

        return null;

    }
*/
/*
    public void useEnglishValue ()
    {

        this.userValue.updateText (this.baseValue.getRawText ());
        this.showPreview ();
        this.validate ();
        this.repaint ();

    }
*/

    public abstract boolean hasErrors ();

    public abstract boolean showErrors (boolean requireUserValue);

    public boolean showErrors (Set<String> errs)
    {

        if ((errs == null)
            ||
            (errs.size () == 0)
           )
        {

            this.form.hideError ();

            return false;

        }

        this.form.showErrors (new LinkedHashSet<> (errs.stream ()
            .map (e -> new SimpleStringProperty (e))
            .collect (Collectors.toList ())));

        this.updateSideBar (this.baseValue);

        return true;

    }

    public abstract void showPreview ();

}
