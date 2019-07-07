package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChapterField extends NamedObjectContent<ProjectViewer, Chapter>
{

    private TextViewEditBox box = null;
    private Button showEditButton = null;
    private AccordionItem acc = null;

    private ChapterField (Builder b)
    {

        super (b.viewer,
               b.chapter);

       if (b.chapter == null)
       {

           throw new IllegalArgumentException ("Chapter must be provided.");

       }

       if (b.name == null)
       {

           throw new IllegalArgumentException ("Name must be provided.");

       }

       final ChapterField _this = this;

       java.util.List<String> prefix = Arrays.asList (project,sidebar,chapterinfo);

       this.box = TextViewEditBox.builder ()
            .viewPlaceHolder (getUILanguageStringProperty (Utils.newList (prefix,view,novalue),
                                                           b.name))
            .editPlaceHolder (getUILanguageStringProperty (Utils.newList (prefix,edit,text)))
            .saveButtonTooltip (getUILanguageStringProperty (project,sidebar,chapterinfo,edit,buttons,save,tooltip))
            .cancelButtonTooltip (getUILanguageStringProperty (project,sidebar,chapterinfo,edit,buttons,cancel,tooltip))
            .bulleted (b.bulleted)
            .onSave (newText ->
            {

                try
                {

                    b.update.accept (newText);

                    this.viewer.saveObject (this.object,
                                            true);

                    _this.showEditButton.setVisible (true);

                    return true;

                } catch (Exception e) {

                    Environment.logError (String.format ("Unable to save %1$s for chapter: %2$s",
                                                         b.name,
                                                         this.object),
                                          e);

                    ComponentUtils.showErrorMessage (_this.viewer,
                                                     project,sidebar,chapterinfo,edit,actionerror);
                                              //"Unable to save the " + this.getFieldNamePlural ());

                    return false;

                }

            })
            .onCancel (ev ->
            {

                _this.showEditButton.setVisible (true);

            })
            .build ();

        StringWithMarkup v = b.value;

        if ((v != null)
            &&
            (v.hasText ())
           )
        {

            this.box.setText (v);

        }

        Set<Node> headerCons = new LinkedHashSet<> ();

        this.showEditButton = QuollButton.builder ()
            .styleClassName (StyleClassNames.EDIT)
            .tooltip (prefix,headercontrols,items,edit,tooltip)
            .onAction (ev ->
            {

                _this.showEdit ();

            })
            .build ();

        headerCons.add (this.showEditButton);

        Set<MenuItem> contextMenuItems = new LinkedHashSet<> ();
        contextMenuItems.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,headerpopupmenu,items,edit),
                                                 b.name))
            .onAction (ev ->
            {

                _this.showEdit ();

            })
            .build ());

        this.acc = AccordionItem.builder ()
            .title (new SimpleStringProperty (b.name))
            .styleClassName (b.styleName)
            .openContent (box)
            .contextMenu (contextMenuItems)
            .headerControls (headerCons)
            .build ();

        this.getChildren ().add (acc);
/*
        String help = Environment.getUIString (prefix,
                                               LanguageStrings.bulletedtext);
                    //"Separate each " + this.getFieldName () + " with a newline.  To save press Ctrl+Enter or use the buttons below.";

        if (!this.isBulleted ())
        {

            help = Environment.getUIString (prefix,
                                            LanguageStrings.text);
                //"Enter the " + this.getFieldName () + ", to save press Ctrl+Enter or use the buttons below.";

        }
*/
    }

    /**
     * Get a builder to create a new ChapterField.
     *
     * Usage: ChapterField.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Builder builder ()
    {

        return new ChapterField.Builder ();

    }

    public static class Builder implements IBuilder<Builder, ChapterField>
    {

        private ProjectViewer viewer = null;
        private Chapter chapter = null;
        private String name = null;
        private StringWithMarkup value = null;
        private Consumer<StringWithMarkup> update = null;
        private String styleName = null;
        private boolean bulleted = false;

        private Builder ()
        {

        }

        @Override
        public ChapterField build ()
        {

            return new ChapterField (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String s)
        {

            this.styleName = s;
            return this;

        }

        public Builder update (Consumer<StringWithMarkup> u)
        {

            this.update = u;
            return this;

        }

        public Builder value (StringWithMarkup s)
        {

            this.value = s;
            return this;

        }

        // TODO Use a property.
        public Builder name (String n)
        {

            this.name = n;
            return this;

        }

        public Builder bulleted (boolean v)
        {

            this.bulleted = v;
            return this;

        }

        public Builder withChapter (Chapter c)
        {

            this.chapter = c;
            return this;

        }

        public Builder withViewer (ProjectViewer v)
        {

            this.viewer = v;
            return this;

        }

    }

    public void setContentVisible (boolean v)
    {

        this.acc.setContentVisible (v);

    }

    public boolean isContentVisible ()
    {

        return this.acc.isOpen ();

    }

    private void showEdit ()
    {

        this.setContentVisible (true);
        this.box.showEdit ();
        this.showEditButton.setVisible (false);

    }

}
