package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.util.*;
import javafx.util.converter.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class TextItemManager extends VBox
{

    private ListView<String> current = null;
    private TextField addText = null;
    private ErrorBox newError = null;
    private ErrorBox existingError = null;

    private TextItemManager (Builder b)
    {

        final TextItemManager _this = this;

        UIUtils.addStyleSheet (this,
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               StyleClassNames.ITEMMANAGER);

        this.getStyleClass ().add (StyleClassNames.ITEMMANAGER);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        VBox ab = new VBox ();
        this.getChildren ().add (ab);
        ab.getStyleClass ().add (StyleClassNames.ADD);

        ab.getChildren ().add (Header.builder ()
            .title ((b.addTitle != null ? b.addTitle : getUILanguageStringProperty (manageitems,newitems,title)))
            .build ());

        ab.getChildren ().add (BasicHtmlTextFlow.builder ()
            .text ((b.addDesc != null ? b.addDesc : getUILanguageStringProperty (manageitems,newitems,text)))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ());

        this.newError = ErrorBox.builder ()
            .build ();

        ab.getChildren ().add (this.newError);

        this.addText = new TextField ();
        this.current = new ListView<> ();

        String seps = b.sep != null ? b.sep : getUILanguageStringProperty (manageitems,newitems,separators).get ();

        Runnable addItems = () ->
        {

            this.newError.setVisible (false);

            StringTokenizer t = new StringTokenizer (addText.getText (),
                                                     seps);

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                if (b.items.contains (tok))
                {

                    continue;

                }

                b.items.add (tok);

                _this.fireEvent (new ItemEvent (null,
                                                tok,
                                                ItemEvent.ADDED_EVENT));

            }

            addText.setText (null);

        };

        UIUtils.addDoOnReturnPressed (this.addText,
                                      addItems);

        ab.getChildren ().add (this.addText);

        ab.getChildren ().add (QuollButtonBar.builder ()
                .button (QuollButton.builder ()
                            .label ((b.addButtonLabel != null ? b.addButtonLabel : getUILanguageStringProperty (manageitems,newitems,add)))
                            .buttonType (ButtonBar.ButtonData.APPLY)
                            .onAction (ev ->
                            {

                                UIUtils.runLater (addItems);

                            })
                            .build ())
                .build ());

        VBox cb = new VBox ();
        this.getChildren ().add (cb);
        cb.getStyleClass ().add (StyleClassNames.CURRENT);

        cb.getChildren ().add (Header.builder ()
            .title ((b.currentItemsTitle != null ? b.currentItemsTitle : getUILanguageStringProperty (manageitems,table,title)))
            .build ());

        if (b.currentItemsDesc != null)
        {

            cb.getChildren ().add (BasicHtmlTextFlow.builder ()
                .text (b.currentItemsDesc)
                .styleClassName (StyleClassNames.DESCRIPTION)
                .build ());

        }

        this.existingError = ErrorBox.builder ()
            .build ();

        cb.getChildren ().add (this.existingError);

        this.current.getSelectionModel ().setSelectionMode (SelectionMode.MULTIPLE);
        this.current.setEditable (true);
        this.current.setCellFactory (list ->
        {

            TextFieldListCell<String> c = new TextFieldListCell<> ()
            {

                @Override
                public void cancelEdit ()
                {

                    _this.existingError.setVisible (false);
                    this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
                    super.cancelEdit ();

                }

                @Override
                public void commitEdit (String newVal)
                {

                    this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);

                    int ind = -1;

                    newVal = newVal.trim ();

                    for (int i = 0; i < b.items.size (); i++)
                    {

                        if (newVal.equalsIgnoreCase (b.items.get (i)))
                        {

                            ind = i;
                            break;

                        }

                    }

                    if ((ind > -1)
                        &&
                        (ind != this.getIndex ())
                       )
                    {

                        this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);
                        UIUtils.setTooltip (this,
                                            getUILanguageStringProperty (manageitems,table,edit,errors,valueexists));

                        return;

                    }

                    String oldVal = this.getItem ();

                    // TODO Check value here and either allow or prevent.
                    super.commitEdit (newVal);

                    _this.existingError.setVisible (false);

                    _this.fireEvent (new ItemEvent (oldVal,
                                                    newVal,
                                                    ItemEvent.CHANGED_EVENT));

                }

            };

            c.setConverter (new DefaultStringConverter ());

            ContextMenu cm = new ContextMenu ();
            cm.getStyleClass ().add (StyleClassNames.EDIT);

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (manageitems,table,popupmenu,LanguageStrings.items,edit)
                .iconName (StyleClassNames.EDIT)
                .onAction (eev ->
                {

                    this.existingError.setVisible (false);

                    c.startEdit ();

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (manageitems,table,popupmenu,LanguageStrings.items,remove)
                .iconName (StyleClassNames.DELETE)
                .onAction (eev ->
                {

                    _this.fireEvent (new ItemEvent (c.getItem (),
                                                    null,
                                                    ItemEvent.REMOVED_EVENT));

                    _this.current.getItems ().remove (c.getIndex ());

                })
                .build ());

            cm.getItems ().addAll (items);

            c.setContextMenu (cm);

            return c;

        });

        UIUtils.setTooltip (this.current,
                            getUILanguageStringProperty (manageitems,table,tooltip));
        this.setItems (b.items);
        this.current.focusedProperty ().addListener ((p, oldv, newv) ->
        {

            if (!newv)
            {

                //_this.current.getSelectionModel ().clearSelection ();

            }

        });
        cb.getChildren ().add (this.current);

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .label ((b.removeButtonLabel != null ? b.removeButtonLabel : getUILanguageStringProperty (manageitems,table,remove)))
                        .buttonType (ButtonBar.ButtonData.APPLY)
                        .onAction (ev ->
                        {

                            this.existingError.setVisible (false);

                            List<String> sel = new ArrayList<> (_this.current.getSelectionModel ().getSelectedItems ());

                            for (String v : sel)
                            {

                                _this.fireEvent (new ItemEvent (v,
                                                                null,
                                                                ItemEvent.REMOVED_EVENT));

                            }

                            b.items.removeAll (sel);

                        })
                        .build ())
            .build ();

        cb.getChildren ().add (bb);

    }

    public void reset ()
    {


        this.current.getSelectionModel ().clearSelection ();

    }

    public void setItems (ObservableList<String> items)
    {

        this.current.setItems (items);

    }

    public void setOnItemChanged (EventHandler<ItemEvent> handler)
    {

        this.addEventHandler (ItemEvent.CHANGED_EVENT,
                              handler);

    }

    public void setOnItemRemoved (EventHandler<ItemEvent> handler)
    {

        this.addEventHandler (ItemEvent.REMOVED_EVENT,
                              handler);

    }

    public void setOnItemAdded (EventHandler<ItemEvent> handler)
    {

        this.addEventHandler (ItemEvent.ADDED_EVENT,
                              handler);

    }

    public void showNewError (StringProperty e)
    {

        this.newError.setErrors (e);
        this.newError.setVisible (true);

    }

    public void showExistingError (StringProperty e)
    {

        this.existingError.setErrors (e);
        this.existingError.setVisible (true);

    }

    /**
     * Get a builder to create a new TextItemManager.
     *
     * Usage: TextItemManager.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static TextItemManager.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, TextItemManager>
    {

        private String styleName = null;
        private ObservableList<String> items = null;
        private StringProperty addDesc = null;
        private StringProperty addTitle = null;
        private StringProperty addButtonLabel = null;
        private StringProperty currentItemsDesc = null;
        private StringProperty currentItemsTitle = null;
        private StringProperty removeButtonLabel = null;
        private String sep = null;
        private StringProperty currentItemsTooltip = null;

        private Builder ()
        {

        }

        @Override
        public TextItemManager build ()
        {

            return new TextItemManager (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder addDescription (String... ids)
        {

            return this.addDescription (getUILanguageStringProperty (ids));

        }

        public Builder addDescription (List<String> prefix,
                                       String... ids)
        {

            return this.addDescription (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder addDescription (StringProperty prop)
        {

            this.addDesc = prop;
            return this;

        }

        public Builder addTitle (String... ids)
        {

            return this.addTitle (getUILanguageStringProperty (ids));

        }

        public Builder addTitle (List<String> prefix,
                                 String... ids)
        {

            return this.addTitle (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder addTitle (StringProperty prop)
        {

            this.addTitle = prop;
            return this;

        }

        public Builder addButtonLabel (String... ids)
        {

            return this.addButtonLabel (getUILanguageStringProperty (ids));

        }

        public Builder addButtonLabel (List<String> prefix,
                                       String... ids)
        {

            return this.addButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder addButtonLabel (StringProperty prop)
        {

            this.addButtonLabel = prop;
            return this;

        }

        public Builder currentItemsDescription (String... ids)
        {

            return this.currentItemsDescription (getUILanguageStringProperty (ids));

        }

        public Builder currentItemsDescription (List<String> prefix,
                                                String... ids)
        {

            return this.currentItemsDescription (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder currentItemsDescription (StringProperty prop)
        {

            this.currentItemsDesc = prop;
            return this;

        }

        public Builder currentItemsTitle (String... ids)
        {

            return this.currentItemsTitle (getUILanguageStringProperty (ids));

        }

        public Builder currentItemsTitle (List<String> prefix,
                                          String... ids)
        {

            return this.currentItemsTitle (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder currentItemsTitle (StringProperty prop)
        {

            this.currentItemsTitle = prop;
            return this;

        }

        public Builder removeButtonLabel (String... ids)
        {

            return this.removeButtonLabel (getUILanguageStringProperty (ids));

        }

        public Builder removeButtonLabel (List<String> prefix,
                                          String... ids)
        {

            return this.removeButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder removeButtonLabel (StringProperty prop)
        {

            this.removeButtonLabel = prop;
            return this;

        }

        public Builder itemSeparator (String sep)
        {

            this.sep = sep;
            return this;

        }

        public Builder currentItemsTooltip (String... ids)
        {

            return this.currentItemsTooltip (getUILanguageStringProperty (ids));

        }

        public Builder currentItemsTooltip (List<String> prefix,
                                            String... ids)
        {

            return this.currentItemsTooltip (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder currentItemsTooltip (StringProperty prop)
        {

            this.currentItemsTooltip = prop;
            return this;

        }

        public Builder items (ObservableList<String> items)
        {

            this.items = items;
            return this;

        }

    }

    public static class ItemEvent extends Event
    {

        public static final EventType<ItemEvent> ADDED_EVENT = new EventType<> ("textitemmanager.itemadded");
        public static final EventType<ItemEvent> REMOVED_EVENT = new EventType<> ("textitemanager.itemremoved");
        public static final EventType<ItemEvent> CHANGED_EVENT = new EventType<> ("textitemanager.itemchanged");

        private String oldValue = null;
        private String newValue = null;

        public ItemEvent (String               oldValue,
                          String               newValue,
                          EventType<ItemEvent> type)
        {

            super (type);

            this.oldValue = oldValue;
            this.newValue = newValue;

        }

        public String getNewValue ()
        {

            return this.newValue;

        }

        public String getOldValue ()
        {

            return this.oldValue;

        }

    }

}
