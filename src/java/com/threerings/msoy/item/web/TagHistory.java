//
// $Id$

package com.threerings.msoy.item.web;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.threerings.io.Streamable;
import com.threerings.msoy.web.data.MemberGName;

/**
 * Keeps a history of tagging events for a given item.
 */
public class TagHistory
    implements Streamable, IsSerializable
{
    public static final byte ACTION_ADDED = 1;
    public static final byte ACTION_REMOVED = 2;
    public static final byte ACTION_COPIED = 3;
    
    /** The item being operated on. */
    public ItemGIdent item;

    /** The tag that was added or deleted, or null for COPIED. */
    public String tag;
    
    /** The member who added or deleted the tag. */
    public MemberGName member;
    
    /** The action taken (ADDED or REMOVED or COPIED). */
    public byte action;

    /** The time of the tagging event. */
    public Date time;
}
