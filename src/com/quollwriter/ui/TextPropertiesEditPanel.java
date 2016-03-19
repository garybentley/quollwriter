package com.quollwriter.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;

import java.awt.event.*;

import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.gentlyweb.properties.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.TextProperties;

public class TextPropertiesEditPanel extends Box
{
    
    private AbstractViewer viewer = null;
    private TextProperties textProps = null;
    private String eventType = null;
    private boolean showColorSelectors = false;
    private PopupsSupported popupParent = null;

    private JComboBox            fonts = null;
    private JComboBox            sizes = null;
    private JComboBox            align = null;
    private JComboBox            line = null;
    private JCheckBox            indent = null;
    private JCheckBox            highlightWritingLine = null;
    private JCheckBox            typewriterScrolling = null;
    private JPanel               textcolorSwatch = null;
    private JPanel               bgcolorSwatch = null;
    private JPanel               writingLineHighlightColorSwatch = null;
    private JSlider    textBorder = null;

    private Map<String, QPopup>  popups = new HashMap ();
    
    public TextPropertiesEditPanel (AbstractViewer  pv,
                                    TextProperties  props,
                                    String          eventType,
                                    boolean         showColorSelectors,
                                    PopupsSupported popupParent)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.textProps = props;
        this.eventType = eventType;
        this.viewer = pv;
        this.showColorSelectors = showColorSelectors;
        this.popupParent = popupParent;
        
    }
        
    public Dimension getMinimumSize ()
    {
        
        return this.getPreferredSize ();        
        
    }
    
    public void setTextProperties (TextProperties props)
    {
        
        this.fonts.setSelectedItem (props.getFontFamily ());
        this.sizes.setSelectedItem (props.getFontSize ());
        this.align.setSelectedItem (props.getAlignment ());
        this.line.setSelectedItem (props.getLineSpacing ());
        this.textcolorSwatch.setBackground (props.getTextColor ());
        this.bgcolorSwatch.setBackground (props.getBackgroundColor ());        
            
        this.textProps = props;

        // Need to update the text background/text color.
        this.textProps.setTextColor (props.getTextColor ());
        this.textProps.setBackgroundColor (props.getBackgroundColor ());
        this.textProps.setWritingLineColor (props.getWritingLineColor ());
        this.textProps.setHighlightWritingLine (props.isHighlightWritingLine ());
        
    }
    
    public void init_old ()
    {

        final TextPropertiesEditPanel _this = this;
    
        PanelBuilder    builder = null;
        CellConstraints cc = null;
        FormLayout fl = null;
        JPanel p = null;
        
        fl = new FormLayout ("right:p, 6px, p:grow",
                             "p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p" + (this.showColorSelectors ? ", 10px, p, 10px, p, 10px, p" : ""));

        int r = 1;
    
        builder = new PanelBuilder (fl);
        cc = new CellConstraints ();

        builder.addLabel ("Font",
                          cc.xy (1,
                                 r));

        this.fonts = UIUtils.getFontsComboBox (this.textProps.getFontFamily (),
                                               this.textProps);

        this.fonts.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {            

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_FONT);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.fonts),
                     cc.xy (3,
                            r));
        
        r += 2;

        builder.addLabel ("Size",
                          cc.xy (1,
                                 r));

        this.sizes = UIUtils.getFontSizesComboBox (this.textProps.getFontSize (),
                                                   this.textProps);

        this.sizes.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_FONT_SIZE);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.sizes),
                     cc.xy (3,
                            r));

        r += 2;
    
        builder.addLabel ("Alignment",
                          cc.xy (1,
                                 r));

        this.align = UIUtils.getAlignmentComboBox (this.textProps.getAlignment (),
                                                   this.textProps);

        this.align.addActionListener (new ActionAdapter ()
        {
    
            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_ALIGNMENT);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.align),
                     cc.xy (3,
                            r));

        r += 2;

        builder.addLabel ("Line Spacing",
                          cc.xy (1,
                                 r));

        this.line = UIUtils.getLineSpacingComboBox (this.textProps.getLineSpacing (),
                                                    this.textProps);

        this.line.addActionListener (new ActionAdapter ()
        {
    
            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_LINE_SPACING);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.line),
                     cc.xy (3,
                            r));

        r += 2;

        this.indent = new JCheckBox ("<html>Indent the first line of each <br />paragraph</html>");
        this.indent.setVerticalTextPosition (SwingConstants.TOP);
        this.indent.setOpaque (false);
        this.indent.setSelected (this.textProps.getFirstLineIndent ());

        this.indent.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.textProps.setFirstLineIndent (_this.indent.isSelected ());
            
                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_LINE_INDENT);                
                
            }

        });

        builder.add (this.indent,
                     cc.xy (3,
                            r));
                
        r += 2;

        this.highlightWritingLine = new JCheckBox ("Highlight the writing line");
        this.highlightWritingLine.setOpaque (false);
        this.highlightWritingLine.setSelected (this.textProps.isHighlightWritingLine ());

        this.highlightWritingLine.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.textProps.setHighlightWritingLine (_this.highlightWritingLine.isSelected ());

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_HIGHLIGHT_WRITING_LINE);                
                
            }

        });

        builder.add (this.highlightWritingLine,
                     cc.xy (3,
                            r));

        r += 2;        
    
        builder.addLabel ("Highlight Line",
                          cc.xy (1,
                                 r));
    
        this.writingLineHighlightColorSwatch = QColorChooser.getSwatch (_this.textProps.getWritingLineColor ());
        
        UIUtils.setAsButton (this.writingLineHighlightColorSwatch);
                            
        this.writingLineHighlightColorSwatch.addMouseListener (new MouseAdapter ()
        {

            public void mouseReleased (MouseEvent ev)
            {

                String colors = Environment.getUserProperties ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
            
                Color writingLineColor = _this.textProps.getWritingLineColor ();
  
                QPopup popup = _this.popups.get ("writingline");
                
                if (popup == null)
                {
                
                    popup = QColorChooser.getColorChooserPopup (colors,
                                                                writingLineColor,
                                                                new ChangeAdapter ()
                                                                {
                         
                                                                    public void stateChanged (ChangeEvent ev)
                                                                    {
                        
                                                                        Color c = (Color) ev.getSource ();
                                                                        
                                                                        _this.writingLineHighlightColorSwatch.setBackground (c);
                                                                       
                                                                        _this.textProps.setWritingLineColor (c);

                                                                    }

                                                                },
                                                                new ActionAdapter ()
                                                                {
                                                                    
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {
                                                                        
                                                                        QPopup p = _this.popups.remove ("writingline");
                                                                        
                                                                        p.removeFromParent ();
                                                                        
                                                                    }
                                                                    
                                                                });                
            
                    _this.popups.put ("writingline",
                                      popup);

                }
                
                int x = ev.getXOnScreen ();
                int y = ev.getYOnScreen ();

                Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

                if ((y + popup.getPreferredSize ().height) > d.height)
                {

                    y -= popup.getPreferredSize ().height;

                }

                _this.popupParent.showPopupAt (popup,
                                               _this.writingLineHighlightColorSwatch,
                                               true);

            }
            
        });

        builder.add (UIUtils.getLimitWrapper (this.writingLineHighlightColorSwatch),
                     cc.xy (3,
                            r));
        
        if (this.showColorSelectors)
        {

            r += 2;        
        
            builder.addLabel ("Text",
                              cc.xy (1,
                                     r));
        
            this.textcolorSwatch = QColorChooser.getSwatch (this.textProps.getTextColor ());
            UIUtils.setAsButton (this.textcolorSwatch);
            this.bgcolorSwatch = QColorChooser.getSwatch (this.textProps.getBackgroundColor ());
            UIUtils.setAsButton (this.bgcolorSwatch);
    
            this.textcolorSwatch.addMouseListener (new MouseAdapter ()
            {
    
                public void mouseReleased (MouseEvent ev)
                {
    
                    String colors = Environment.getUserProperties ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
                
                    Color textcolor = _this.textProps.getTextColor ();
                
                    QPopup popup = _this.popups.get ("textcolor");
                    
                    if (popup == null)
                    {
                    
                        popup = QColorChooser.getColorChooserPopup (colors,
                                                                    textcolor,
                                                                    new ChangeAdapter ()
                                                                    {
                             
                                                                         public void stateChanged (ChangeEvent ev)
                                                                         {
                             
                                                                             Color c = (Color) ev.getSource ();
                             
                                                                             _this.textProps.setTextColor (c);
                             
                                                                             textcolorSwatch.setBackground (c);
                                                                             
                                                                             bgcolorSwatch.setBackground (_this.textProps.getBackgroundColor ());
    
                                                                             _this.viewer.fireProjectEvent (_this.eventType,
                                                                                                            ProjectEvent.CHANGE_FONT_COLOR);
                             
                                                                         }
    
                                                                     },
                                                                    new ActionAdapter ()
                                                                    {
                                                                        
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                            
                                                                            QPopup p = _this.popups.remove ("textcolor");
                                                                            
                                                                            p.removeFromParent ();
                                                                            
                                                                        }
                                                                        
                                                                    });                
                
                        _this.popups.put ("textcolor",
                                          popup);
    
                    }
                
                    int x = ev.getXOnScreen ();
                    int y = ev.getYOnScreen ();
    
                    Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
    
                    if ((y + popup.getPreferredSize ().height) > d.height)
                    {
    
                        y -= popup.getPreferredSize ().height;
    
                    }
    
                    _this.popupParent.showPopupAt (popup,
                                                   _this.textcolorSwatch,
                                                   true);
                                                   //new Point (x, y));
                
                }
    
            });
    
            builder.add (UIUtils.getLimitWrapper (textcolorSwatch),
                         cc.xy (3,
                                r));
    
            r += 2;
    
            builder.addLabel ("Background",
                              cc.xy (1,
                                     r));
                
            bgcolorSwatch.addMouseListener (new MouseAdapter ()
            {
    
                public void mouseReleased (MouseEvent ev)
                {
    
                    String colors = Environment.getUserProperties ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
    
                    Color bgcolor = _this.textProps.getBackgroundColor ();   
    
                    QPopup popup = _this.popups.get ("bgcolor");
                    
                    if (popup == null)
                    {
                        
                        popup = QColorChooser.getColorChooserPopup (colors,
                                                                    bgcolor,
                                                                    new ChangeAdapter ()
                                                                    {
     
                                                                        public void stateChanged (ChangeEvent ev)
                                                                        {
     
                                                                            Color c = (Color) ev.getSource ();
     
                                                                            _this.textProps.setBackgroundColor (c);
     
                                                                            //_this.fsf.setBackgroundColor (c);
                                                                            
                                                                            //_this.sizeBoxWrapper.setBackground (c);
                                                                            
                                                                            bgcolorSwatch.setBackground (c);
                                                                            textcolorSwatch.setBackground (_this.textProps.getTextColor ());
                                                                              
                                                                            _this.viewer.fireProjectEvent (_this.eventType,
                                                                                                           ProjectEvent.CHANGE_BG_COLOR);
     
                                                                        }
     
                                                                    },
                                                                     new ActionAdapter ()
                                                                     {
                                                                      
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                          
                                                                          QPopup p = _this.popups.remove ("bgcolor");
                                                                          
                                                                          p.removeFromParent ();
                                                                          
                                                                        }
                                                                      
                                                                    });    
    
                        _this.popups.put ("bgcolor",
                                          popup);
    
                    }
                                          
                    int x = ev.getXOnScreen ();
                    int y = ev.getYOnScreen ();
    
                    Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
    
                    if ((y + popup.getPreferredSize ().height) > d.height)
                    {
    
                        y -= popup.getPreferredSize ().height;
    
                    }
    
                    _this.popupParent.showPopupAt (popup,
                                                   new Point (x, y),
                                                   true);
                    
                }
    
            });
        
            //this.fsf.setFontColor (this.fullScreenTextProperties.getTextColor ());
    
            //this.fsf.setBackgroundColor (this.fullScreenTextProperties.getBackgroundColor ());
    
            builder.add (UIUtils.getLimitWrapper (bgcolorSwatch),
                         cc.xy (3,
                                r));

            r += 2;
    
            final JLabel reset = UIUtils.createClickableLabel ("Reset to defaults",
                                                               null);
        
            reset.setToolTipText ("Click to reset the text properties to their default values");
            
            reset.addMouseListener (new MouseAdapter ()
            {
        
                public void mouseReleased (MouseEvent ev)
                {
                    
                    _this.textProps.resetToDefaults ();
                                
                }
    
            });
        
            builder.add (reset,
                         cc.xywh (3,
                                  r,
                                  1,
                                  1));

        }
    
        p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        p.setBorder (new EmptyBorder (10,
                                      10,
                                      10,
                                      10));

        this.add (p);
        
    }
    
    private void addItem (String     label,
                          JComponent comp,
                          JComponent addTo)
    {
        
        if (label != null)
        {
            
            addTo.add (UIUtils.createLabel (label));
            
        }
        
        if (comp != null)
        {
            
            Box b = new Box (BoxLayout.X_AXIS);

            b.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            
            b.setBorder (UIUtils.createPadding ((label != null ? 5 : 0), 10, 10, 0));
            
            comp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            
            b.add (comp);
           
            addTo.add (b);
            
        }
        
    }
    
    public void init ()
    {

        final TextPropertiesEditPanel _this = this;
    
        Box layout = new Box (BoxLayout.Y_AXIS);
    
        layout.setBorder (UIUtils.createPadding (5, 5, 5, 5));
    
        this.fonts = UIUtils.getFontsComboBox (this.textProps.getFontFamily (),
                                               this.textProps);

        this.fonts.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {            

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_FONT);

            }

        });

        this.addItem ("Font",
                      this.fonts,
                      layout);

        this.sizes = UIUtils.getFontSizesComboBox (this.textProps.getFontSize (),
                                                   this.textProps);

        this.sizes.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_FONT_SIZE);

            }

        });

        this.addItem ("Size",
                      this.sizes,
                      layout);
    
        this.align = UIUtils.getAlignmentComboBox (this.textProps.getAlignment (),
                                                   this.textProps);

        this.align.addActionListener (new ActionAdapter ()
        {
    
            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_ALIGNMENT);

            }

        });

        this.addItem ("Alignment",
                      this.align,
                      layout);

        this.line = UIUtils.getLineSpacingComboBox (this.textProps.getLineSpacing (),
                                                    this.textProps);

        this.line.addActionListener (new ActionAdapter ()
        {
    
            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_LINE_SPACING);

            }

        });

        this.addItem ("Line Spacing",
                      this.line,
                      layout);

        this.textBorder = new JSlider (SwingConstants.HORIZONTAL,
                                       0,
                                       15,
                                       0);
        this.textBorder.setToolTipText ("Drag to change the size of the border between the edge of the writing area and the text");
        this.textBorder.addChangeListener (new ChangeAdapter ()
        {

            public void stateChanged (ChangeEvent ev)
            {

                _this.textProps.setTextBorder (_this.textBorder.getValue () * 5);
            
                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_TEXT_BORDER);                
                

            }

        });

        textBorder.setOpaque (false);
        textBorder.setMaximumSize (new Dimension (150, 20));
                      
        this.addItem ("Text Border Width",
                      textBorder,
                      layout);
                      
        this.indent = new JCheckBox ("<html>Indent the first line of each paragraph</html>");
        this.indent.setVerticalTextPosition (SwingConstants.TOP);
        this.indent.setOpaque (false);
        this.indent.setSelected (this.textProps.getFirstLineIndent ());

        this.indent.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.textProps.setFirstLineIndent (_this.indent.isSelected ());
            
                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_LINE_INDENT);                
                
            }

        });

        this.addItem (null,
                      this.indent,
                      layout);

        this.highlightWritingLine = new JCheckBox ("Highlight the writing line");
        this.highlightWritingLine.setOpaque (false);
        this.highlightWritingLine.setSelected (this.textProps.isHighlightWritingLine ());

        this.highlightWritingLine.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.textProps.setHighlightWritingLine (_this.highlightWritingLine.isSelected ());

                _this.viewer.fireProjectEvent (_this.eventType,
                                               ProjectEvent.CHANGE_HIGHLIGHT_WRITING_LINE);                
                
            }

        });

        this.addItem (null,
                      this.highlightWritingLine,
                      layout);
    
        this.writingLineHighlightColorSwatch = QColorChooser.getSwatch (_this.textProps.getWritingLineColor ());
        
        UIUtils.setAsButton (this.writingLineHighlightColorSwatch);
                            
        this.writingLineHighlightColorSwatch.addMouseListener (new MouseAdapter ()
        {

            public void mouseReleased (MouseEvent ev)
            {

                String colors = Environment.getUserProperties ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
            
                Color writingLineColor = _this.textProps.getWritingLineColor ();
  
                QPopup popup = _this.popups.get ("writingline");
                
                if (popup == null)
                {
                
                    popup = QColorChooser.getColorChooserPopup (colors,
                                                                writingLineColor,
                                                                new ChangeAdapter ()
                                                                {
                         
                                                                    public void stateChanged (ChangeEvent ev)
                                                                    {
                        
                                                                        Color c = (Color) ev.getSource ();
                                                                        
                                                                        _this.writingLineHighlightColorSwatch.setBackground (c);
                                                                       
                                                                        _this.textProps.setWritingLineColor (c);

                                                                    }

                                                                },
                                                                new ActionAdapter ()
                                                                {
                                                                    
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {
                                                                        
                                                                        QPopup p = _this.popups.remove ("writingline");
                                                                        
                                                                        p.removeFromParent ();
                                                                        
                                                                    }
                                                                    
                                                                });                
            
                    _this.popups.put ("writingline",
                                      popup);

                }
                
                int x = ev.getXOnScreen ();
                int y = ev.getYOnScreen ();

                Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

                if ((y + popup.getPreferredSize ().height) > d.height)
                {

                    y -= popup.getPreferredSize ().height;

                }

                _this.popupParent.showPopupAt (popup,
                                               _this.writingLineHighlightColorSwatch,
                                               true);

            }
            
        });
        
        this.addItem ("Highlight line",
                      this.writingLineHighlightColorSwatch,
                      layout);
        
        if (this.showColorSelectors)
        {

            this.textcolorSwatch = QColorChooser.getSwatch (this.textProps.getTextColor ());
            UIUtils.setAsButton (this.textcolorSwatch);
            this.bgcolorSwatch = QColorChooser.getSwatch (this.textProps.getBackgroundColor ());
            UIUtils.setAsButton (this.bgcolorSwatch);
    
            this.textcolorSwatch.addMouseListener (new MouseAdapter ()
            {
    
                public void mouseReleased (MouseEvent ev)
                {
    
                    String colors = Environment.getUserProperties ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
                
                    Color textcolor = _this.textProps.getTextColor ();
                
                    QPopup popup = _this.popups.get ("textcolor");
                    
                    if (popup == null)
                    {
                    
                        popup = QColorChooser.getColorChooserPopup (colors,
                                                                    textcolor,
                                                                    new ChangeAdapter ()
                                                                    {
                             
                                                                         public void stateChanged (ChangeEvent ev)
                                                                         {
                             
                                                                             Color c = (Color) ev.getSource ();
                             
                                                                             _this.textProps.setTextColor (c);
                             
                                                                             textcolorSwatch.setBackground (c);
                                                                             
                                                                             bgcolorSwatch.setBackground (_this.textProps.getBackgroundColor ());
    
                                                                             _this.viewer.fireProjectEvent (_this.eventType,
                                                                                                            ProjectEvent.CHANGE_FONT_COLOR);
                             
                                                                         }
    
                                                                     },
                                                                    new ActionAdapter ()
                                                                    {
                                                                        
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                            
                                                                            QPopup p = _this.popups.remove ("textcolor");
                                                                            
                                                                            p.removeFromParent ();
                                                                            
                                                                        }
                                                                        
                                                                    });                
                
                        _this.popups.put ("textcolor",
                                          popup);
    
                    }
                
                    int x = ev.getXOnScreen ();
                    int y = ev.getYOnScreen ();
    
                    Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
    
                    if ((y + popup.getPreferredSize ().height) > d.height)
                    {
    
                        y -= popup.getPreferredSize ().height;
    
                    }
    
                    _this.popupParent.showPopupAt (popup,
                                                   _this.textcolorSwatch,
                                                   true);
                                                   //new Point (x, y));
                
                }
    
            });
    
            this.addItem ("Text",
                          this.textcolorSwatch,
                          layout);
    
            bgcolorSwatch.addMouseListener (new MouseAdapter ()
            {
    
                public void mouseReleased (MouseEvent ev)
                {
    
                    String colors = Environment.getUserProperties ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
    
                    Color bgcolor = _this.textProps.getBackgroundColor ();   
    
                    QPopup popup = _this.popups.get ("bgcolor");
                    
                    if (popup == null)
                    {
                        
                        popup = QColorChooser.getColorChooserPopup (colors,
                                                                    bgcolor,
                                                                    new ChangeAdapter ()
                                                                    {
     
                                                                        public void stateChanged (ChangeEvent ev)
                                                                        {
     
                                                                            Color c = (Color) ev.getSource ();
     
                                                                            _this.textProps.setBackgroundColor (c);
     
                                                                            //_this.fsf.setBackgroundColor (c);
                                                                            
                                                                            //_this.sizeBoxWrapper.setBackground (c);
                                                                            
                                                                            bgcolorSwatch.setBackground (c);
                                                                            textcolorSwatch.setBackground (_this.textProps.getTextColor ());
                                                                              
                                                                            _this.viewer.fireProjectEvent (_this.eventType,
                                                                                                           ProjectEvent.CHANGE_BG_COLOR);
     
                                                                        }
     
                                                                    },
                                                                     new ActionAdapter ()
                                                                     {
                                                                      
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                          
                                                                          QPopup p = _this.popups.remove ("bgcolor");
                                                                          
                                                                          p.removeFromParent ();
                                                                          
                                                                        }
                                                                      
                                                                    });    
    
                        _this.popups.put ("bgcolor",
                                          popup);
    
                    }
                                          
                    int x = ev.getXOnScreen ();
                    int y = ev.getYOnScreen ();
    
                    Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
    
                    if ((y + popup.getPreferredSize ().height) > d.height)
                    {
    
                        y -= popup.getPreferredSize ().height;
    
                    }
    
                    _this.popupParent.showPopupAt (popup,
                                                   new Point (x, y),
                                                   true);
                    
                }
    
            });
        
            this.addItem ("Background",
                          this.bgcolorSwatch,
                          layout);
        
            //this.fsf.setFontColor (this.fullScreenTextProperties.getTextColor ());
    
            //this.fsf.setBackgroundColor (this.fullScreenTextProperties.getBackgroundColor ());
    
            final JLabel reset = UIUtils.createClickableLabel ("Reset to defaults",
                                                               null);
        
            reset.setToolTipText ("Click to reset the text properties to their default values");
            
            reset.addMouseListener (new MouseAdapter ()
            {
        
                public void mouseReleased (MouseEvent ev)
                {
                    
                    _this.textProps.resetToDefaults ();
                                
                }
    
            });
        
            this.addItem (null,
                          reset,
                          layout);

        }
    
        this.add (layout);
        
    }

}
