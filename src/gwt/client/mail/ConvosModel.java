//
// $Id$

package client.mail;

import java.util.List;

import client.util.ServiceBackedDataModel;

import com.threerings.msoy.person.data.ConvMessage;
import com.threerings.msoy.person.data.Conversation;

/**
 * A data model that provides a member's conversations.
 */
public class ConvosModel extends ServiceBackedDataModel
{
    /**
     * Notes that we've read the specified conversation.
     */
    public void markConversationRead (int convoId)
    {
        for (int ii = 0, ll = _pageItems.size(); ii < ll; ii++) {
            Conversation convo = (Conversation)_pageItems.get(ii);
            if (convo.conversationId == convoId) {
                convo.hasUnread = false;
            }
        }
    }

    /**
     * Notes that we've added a new message to the specified conversation.
     */
    public void noteMessageAdded (int convoId, ConvMessage message)
    {
        for (int ii = 0, ll = _pageItems.size(); ii < ll; ii++) {
            Conversation convo = (Conversation)_pageItems.get(ii);
            if (convo.conversationId == convoId) {
                convo.lastSent = message.sent;
                convo.lastAuthor = message.author;
                convo.lastSnippet = (message.body.length() > Conversation.SNIPPET_LENGTH) ?
                    message.body.substring(0, Conversation.SNIPPET_LENGTH) : message.body;
            }
        }
    }

    // @Override // from ServiceBackedDataModel
    protected void callFetchService (int start, int count, boolean needCount) {
        // TODO: need count
        CMail.mailsvc.loadConversations(CMail.ident, start, count, this); 
    }

    // @Override // from ServiceBackedDataModel
    protected int getCount (Object result) {
        return 0; // TODO: ((MailService.ConvosResult)result).count;
    }

    // @Override // from ServiceBackedDataModel
    protected List getRows (Object result) {
        return (List)result; // TODO ((MailService.ConvosResult)result).convos;
    }
}
