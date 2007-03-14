//
// $Id$

package com.threerings.msoy.web.data;


import com.google.gwt.user.client.rpc.IsSerializable;
import com.threerings.io.Streamable;

/**
 * Represents the general object portion of a mail message body. This object is serialized
 * to and unserialized from persistent storage through its 'type' and 'state' members, and
 * whenever it is to be displayed, the relevant functions {@link #widgetForRecipient()} or
 * {@link #widgetForOthers()) will be called to retrieve the relevant UI.
 */
public abstract class MailPayload
    implements IsSerializable, Streamable
{
    /**
     * The identifying integer of a {@link GroupInviteObject} payload. 
     */
    public static final int TYPE_GROUP_INVITE = 1;

    /**
     * The identifying integer of a {@link FriendInviteObject} payload.
     */
    public static final int TYPE_FRIEND_INVITE = 2;

    public static Class getPayloadClass (int type) {
        switch(type) {
        case TYPE_GROUP_INVITE:
            return GroupInviteObject.class;
        case TYPE_FRIEND_INVITE:
            return FriendInviteObject.class;
        }
        throw new IllegalArgumentException("Unknown payload [type= " + type + "]");
    }
    
    public abstract int getType ();
}
