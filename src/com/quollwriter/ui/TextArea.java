package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.quollwriter.*;

public class TextArea extends Box
{
    
    private JTextArea text = null;
    private JScrollPane scrollPane = null;
    private String placeholder = null;
    private int maxChars = -1;
    private boolean autoGrabFocus = true;
    
    public TextArea (String placeholder,
                     int    rows,
                     int    maxChars)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.text = UIUtils.createTextArea (rows);
        
        this.text.setText (Environment.replaceObjectNames (placeholder));
        
        this.placeholder = placeholder;
        
        if (placeholder != null)
        {
            
            this.text.setForeground (UIUtils.getHintTextColor ());
            
        }
        
        this.maxChars = maxChars;
        
        final TextArea _this = this;
        
        final JLabel maxText = new JLabel ("Max " + Environment.formatNumber (maxChars) + " characters");
        maxText.setForeground (UIUtils.getHintTextColor ());
        
        this.scrollPane = UIUtils.createScrollPane ((JComponent) this.text);

        this.text.addKeyListener (new KeyAdapter ()
        {
           
            public void keyPressed (KeyEvent ev)
            {
                
                if (_this.text.getForeground () != Color.BLACK)
                {
                    
                    _this.text.setText ("");
                    
                }

                _this.text.setForeground (Color.BLACK);

                if (_this.maxChars <= 0)
                {
                    
                    return;
                    
                }
                
                maxText.setForeground (UIUtils.getHintTextColor ());
                
                int l = _this.text.getText ().trim ().length ();
                
                String t = "Max " + Environment.formatNumber (_this.maxChars) + " characters";
                
                if (l > 0)
                {
                
                    if (l > 5000)
                    {

                        t += ", over " + Environment.formatNumber (_this.maxChars) + " characters";
                        maxText.setForeground (Color.RED);
                        
                    } else {
                
                        t += ", " + Environment.formatNumber ((5000 - l)) + " remaining";
                        
                    }

                }
                
                maxText.setText (t);
                
            }
            
        });
        
        this.text.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                if ((!_this.text.getText ().equals (""))
                    &&
                    (_this.text.getForeground () != Color.BLACK)
                   )
                {
                    
                    _this.text.getCaret ().setDot (0);
                    
                }

                if (_this.isAutoGrabFocus ())
                {
                
                    _this.text.grabFocus ();
                    
                }

            }
            
            public void mousePressed (MouseEvent ev)
            {
                
                if ((!_this.text.getText ().equals (""))
                    &&
                    (_this.text.getForeground () == Color.BLACK)
                   )
                {
                    
                    return;
                    
                }
                
                _this.text.setText ("");
                _this.text.setForeground (Color.BLACK);                
                
            }

            public void mouseExited (MouseEvent ev)
            {
                                
            }
            
        });        
                
        this.add (this.scrollPane);
        
        this.add (maxText);
        
        if (this.maxChars <= 0)
        {
            
            maxText.setVisible (false);
            
        }
        
    }
    
    public boolean isAutoGrabFocus ()
    {
        
        return this.autoGrabFocus;
        
    }
    
    public void setAutoGrabFocus (boolean v)
    {
        
        this.autoGrabFocus = v;
        
    }
    
    public JScrollPane getScrollPane ()
    {
        
        return this.scrollPane;
        
    }
    
    public String getText ()
    {
        
        if (this.placeholder != null)
        {
        
            if (this.text.getText ().equals (this.placeholder))
            {
                
                return "";
                
            }
        
        }
        
        return this.text.getText ();
        
    }
    
    public void setText (String t)
    {
        
        this.text.setText (t);
        
        this.text.setForeground (Color.BLACK);
        
    }
    
    /**
     * Pass through to the underlying JTextArea.
     *
     * @param l The listener.
     */
    @Override
    public void addKeyListener (KeyListener l)
    {
        
        this.text.addKeyListener (l);
        
    }
    
    /**
     * Pass through to the underlying JTextArea.
     *
     * @param l The listener.
     */
    @Override
    public void removeKeyListener (KeyListener l)
    {
        
        this.text.removeKeyListener (l);
        
    }

    public JTextArea getTextArea ()
    {
        
        return this.text;
        
    }
    
}