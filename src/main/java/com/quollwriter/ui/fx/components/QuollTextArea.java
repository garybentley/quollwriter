package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.web.*;

import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import org.w3c.dom.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollTextArea extends VBox
{

    private Label maxlabel = null;
    //private TextArea text = null;
    private TextEditor text = null;
    private Boolean autoGrabFocus = false;
    private boolean formattingEnabled = false;

    private QuollTextArea (Builder b)
    {

        final QuollTextArea _this = this;

        this.getStyleClass ().add (StyleClassNames.QTEXTAREA);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.viewer == null)
        {

            b.viewer = Environment.getFocusedViewer ();

            //throw new IllegalArgumentException ("Viewer must be provided.");

        }

        this.text = new TextEditor (null,
                                    null,
                                    b.dictProv);
        this.text.setFormattingEnabled (b.formattingEnabled);
        this.text.setSpellCheckEnabled (b.spellCheckEnabled);
        this.text.setPlaceholder (b.placeHolder);
        VBox.setVgrow (this.text,
                       Priority.ALWAYS);

        Nodes.addInputMap (this.text,
                           InputMap.process (EventPattern.mouseReleased (),
                                              ev ->
                                              {

                                                  if (ev.isPopupTrigger ())
                                                  {

                                                      ContextMenu cm = new ContextMenu ();
                                                      Set<MenuItem> its = this.text.getSpellingSynonymItemsForContextMenu (b.viewer);

                                                      cm.getItems ().addAll (its);

                                                      MenuItem eit = this.text.getCompressedEditItemsForContextMenu ();
                                                      MenuItem fit = this.text.getCompressedFormatItemsForContextMenu ();

                                                      if ((eit != null)
                                                          ||
                                                          (fit != null)
                                                         )
                                                      {

                                                          if (its.size () > 0)
                                                          {

                                                              cm.getItems ().add (new SeparatorMenuItem ());

                                                          }

                                                      }

                                                      if (eit != null)
                                                      {

                                                          cm.getItems ().add (eit);

                                                      }

                                                      if (fit != null)
                                                      {

                                                          cm.getItems ().add (fit);

                                                      }
                                              /*
                                                      boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

                                                      cm.getItems ().addAll (this.getContextMenuItems (compress));
                                              */
                                                      this.text.setContextMenu (cm);

                                                  }

                                                  return InputHandler.Result.PROCEED;

                                              }));

        //this.text = new TextArea ();
        //this.text.setWrapText (true);
        VBox.setVgrow (this.text,
                       Priority.ALWAYS);

        this.text.setEditable (true);

        if (b.text != null)
        {

            this.text.setText (b.text);

        }

        this.autoGrabFocus = b.autoGrabFocus;

        this.text.addEventHandler (MouseEvent.MOUSE_ENTERED,
                                   ev ->
        {

            if (_this.autoGrabFocus)
            {

                _this.text.requestFocus ();

            }

        });

        this.maxlabel = new Label ();
        this.maxlabel.getStyleClass ().add (StyleClassNames.MAX);
        this.maxlabel.managedProperty ().bind (this.maxlabel.visibleProperty ());

        this.maxlabel.setVisible (b.maxChars > 0);

        this.maxlabel.textProperty ().bind (Bindings.createStringBinding (() ->
        {

            String t = String.format (getUIString (textarea,maxchars),
                                      Environment.formatNumber (b.maxChars));

            int l = _this.text.getText ().length ();

            if (l > 0)
            {

                if (l > b.maxChars)
                {

                    t += String.format (getUIString (textarea,charsover),
                                        Environment.formatNumber (l - b.maxChars));
                    _this.maxlabel.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);

                } else {

                    t += String.format (getUIString (textarea,charsremaining),
                                        Environment.formatNumber ((b.maxChars - l)));
                    _this.maxlabel.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);

                }

            }

            return t;

        },
        Environment.uilangProperty (),
        this.text.textProperty ()));

        VirtualizedScrollPane<?> n = new VirtualizedScrollPane<> (this.text);
        VBox.setVgrow (n,
                       Priority.ALWAYS);

        this.getChildren ().addAll (n, this.maxlabel);

    }

    public void setFormattingEnabled (boolean v)
    {

        this.text.setFormattingEnabled (v);

    }

    public void setSpellCheckEnabled (boolean v)
    {

        this.text.setSpellCheckEnabled (v);

    }

    public TextEditor getTextEditor ()
    {

        return this.text;

    }
/*
    public ObservableValue<StringWithMarkup> textProperty ()
    {

        return this.text.textProperty ();

    }
*/
    public void setOnTextKeyReleased (EventHandler<KeyEvent> h)
    {

        this.text.setOnKeyReleased (h);

    }

    public void setText (StringWithMarkup text)
    {

        this.text.readyForUseProperty ().addListener ((pr, oldv, newv) ->
        {

            if (text == null)
            {

                this.setText ("");

            } else {

                this.text.setText (text);

            }

        });

    }

    public void setText (String text)
    {

        this.text.setText (new StringWithMarkup (text));

    }

    public String getText ()
    {

        return this.text.getText ();

    }

    public StringWithMarkup getTextWithMarkup ()
    {

        return this.text.getTextWithMarkup ();

    }

    /**
     * Get a builder to create a new QuollPopup.
     *
     * Usage: QuollPopup.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollTextArea.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollTextArea>
    {

        private StringProperty placeHolder = null;
        private StringWithMarkup text = null;
        private int maxChars = -1;
        private String styleName = null;
        private Boolean autoGrabFocus = false;
        private AbstractViewer viewer = null;
        private DictionaryProvider2 dictProv = null;
        private boolean formattingEnabled = false;
        private boolean spellCheckEnabled = false;

        private Builder ()
        {

        }

        public Builder spellCheckEnabled (boolean v)
        {

            this.spellCheckEnabled = v;
            return this;

        }

        public Builder formattingEnabled (boolean v)
        {

            this.formattingEnabled = v;
            return this;

        }

        public Builder dictionaryProvider (DictionaryProvider2 prov)
        {

            this.dictProv = prov;
            return this;

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder autoGrabFocus (Boolean v)
        {

            this.autoGrabFocus = v;
            return this;

        }

        public Builder text (StringWithMarkup text)
        {

            this.text = text;
            return this;

        }

        public Builder text (String text)
        {

            this.text = new StringWithMarkup (text);
            return this;

        }

        public Builder placeholder (List<String> prefix,
                                    String...    ids)
        {

            return this.placeholder (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder placeholder (String... ids)
        {

            return this.placeholder (getUILanguageStringProperty (ids));

        }

        public Builder placeholder (StringProperty prop)
        {

            this.placeHolder = prop;
            return this;

        }

        public Builder maxChars (int c)
        {

            this.maxChars = c;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollTextArea build ()
        {

            return new QuollTextArea (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
