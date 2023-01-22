package com.quollwriter.editors.ui;

import java.util.*;
import javafx.scene.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.fx.viewers.*;

public class ChatMessageAccordionItem extends MessageAccordionItem<EditorChatMessage>
{

    public ChatMessageAccordionItem (AbstractViewer         pv,
                                     Date                   d,
                                     Set<EditorChatMessage> messages)
    {

        super (pv,
               d,
               messages);

    }

    @Override
    public Node getMessageBox (EditorChatMessage m)
    {

        ChatMessageBox cmb = new ChatMessageBox (m,
                                                 this.viewer);

        return cmb;

    }

}
