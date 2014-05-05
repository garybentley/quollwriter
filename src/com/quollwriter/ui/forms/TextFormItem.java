package com.quollwriter.ui.forms;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.text.*;

public class TextFormItem extends FormItem<String>
{
    
    private boolean multiline = false;
    private JTextComponent text = null;
    private int maxCount = -1;
    private String maxType = null;
    private String helpText = null;
        
    public TextFormItem (String label,
                         boolean multiline,
                         int     lineCount,
                         String defaultValue,
                         int    maxCount,
                         String maxType,
                         boolean notNull,
                         String helpText)
    {
        
        super (label,
               notNull,
               null);

        this.maxCount = maxCount;
        this.maxType = maxType;
        this.helpText = helpText;
        
        final TextFormItem _this = this;
        
        KeyAdapter ka = new KeyAdapter ()
        {
            
            private boolean showHelpText = false;
            
            public void keyPressed (KeyEvent ev)
            {
                                 
                if (_this.helpText != null)
                {                                     
                
                    if (_this.text.getText ().trim ().equals (_this.helpText))
                    {
                        
                        _this.text.setText ("");
                        _this.text.setForeground (Color.black);
                        _this.text.setFont (_this.text.getFont ().deriveFont (Font.PLAIN));                            
                                        
                    }
                
                }
                
                _this.updateRequireLabel ();
                    
            }
            
        };
        
        this.multiline = multiline;
        
        if (!multiline)
        {
        
            this.text = new JTextField ();
    
            this.text.setPreferredSize (new Dimension (500,
                                                       this.text.getPreferredSize ().height));
            this.text.setMaximumSize (new Dimension (500,
                                                     this.text.getPreferredSize ().height));
                        
        } else {
                                
            this.text = UIUtils.createTextArea (lineCount);
    
        }
        
        this.text.addKeyListener (ka);
        
        this.text.addFocusListener (new FocusListener ()
        {
           
            public void focusGained (FocusEvent ev)
            {
                
                if (_this.helpText != null)
                {
                                        
                    if (_this.text.getText ().trim ().equals (_this.helpText))
                    {
                                                                    
                        _this.text.setText ("");
                        _this.text.setForeground (Color.black);
                        _this.text.setFont (_this.text.getFont ().deriveFont (Font.PLAIN));
                            
                    }

                }

                _this.updateRequireLabel ();
                
            }
            
            public void focusLost (FocusEvent ev)
            {
                
                if (_this.text.getText ().trim ().equals (""))
                {
                    
                    _this.showHelpText ();
                    
                }
                
            }
            
        });
        
        if (defaultValue != null)
        {
            
            this.text.setText (defaultValue.toString ());
            
        } else {
            
            this.showHelpText ();
            
        }
        
        if (this.helpText != null)
        {
            
            this.text.setToolTipText (this.helpText);
            
        }
        
    }
    
    private void showHelpText ()
    {
        
        if (this.helpText != null)
        {
                                
            this.text.setText (this.helpText);
            this.text.setForeground (UIUtils.getColor ("#999999"));
            this.text.setFont (this.text.getFont ().deriveFont (Font.ITALIC));
            
        }                            
        
        this.updateRequireLabel ();
        
    }
    
    public JComponent getComponent ()
    {
        
        if (this.multiline)
        {
            
            return this.createScrollableTextArea (this.text);
        
        } else {
            
            return this.text;
            
        }
        
    }

    private JScrollPane createScrollableTextArea (JTextComponent text)
    {

        JScrollPane c = this.createScrollPane (text);
        
        c.setMaximumSize (new Dimension (500,
                                         c.getPreferredSize ().height));
        
        return c;
        
    }
    
    public String getValue ()
    {
        
        String v = this.text.getText ().trim ();
        
        if (v.length () == 0)
        {
            
            return null;
            
        }
        
        return v;
    
    }
    
    public boolean hasError ()
    {
        
        boolean err = false;
        
        String v = this.getValue ();
        
        if ((this.helpText != null)
            &&
            (this.helpText.equals (v))
           )
        {
            
            v = null;
            
        }
        
        if ((v == null)
            &&
            (this.notNull)
           )
        {
            
            err = true;
            
        }
        
        if (this.maxType == null)
        {
            
            return err;
            
        }
        
        if ((v != null)
            &&
            (this.maxType != null)
           )
        {
            
            if (this.maxType.equals ("chars"))
            {
                
                if (v.length () > this.maxCount)
                {
                    
                    err = true;
                    
                }
                
            }
            
            if (this.maxType.equals ("words"))
            {
                
                if (TextUtilities.getWordCount (v) > this.maxCount)
                {
                    
                    err = true;
                    
                }
                
            }
            
        }
        
        return err;
                    
    }
    
    public void updateRequireLabel (JLabel requireLabel)
    {

        this.setError (false);
    
        if (this.maxType == null)
        {
            
            return;
            
        }
    
        String text = this.text.getText ().trim ();
        
        int c = -1;
        
        if (this.maxType.equals ("chars"))
        {
            
            c = text.length ();
            
        }
        
        if (this.maxType.equals ("words"))
        {
            
            c = TextUtilities.getWordCount (text);
            
        }
            
        if (this.helpText != null)
        {
            
            if (text.equals (this.helpText))
            {
                
                c = 0;
                
            }
            
        }
                            
        if (c > 0)
        {
            
            if (c > this.maxCount)
            {
                
                c = this.maxCount;
                
                this.setError (true);
                
            }
            
            requireLabel.setText (String.format ("(max %s %s, %s remaining)",
                                                       this.maxCount,
                                                       (this.maxType.equals ("words") ? "words" : "characters"),
                                                       (this.maxCount - c)));
            
        } else {
            
            requireLabel.setText (String.format ("(max %s %s)",
                                                       this.maxCount,
                                                       (this.maxType.equals ("words") ? "words" : "characters")));                            
            
        }            
                        
    }
    
}
