//
// $Id$

package com.threerings.msoy.person.data;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.threerings.msoy.web.data.MemberCard;

/**
 * Contains information on a particular conversation message.
 */
public class ConvMessage
    implements IsSerializable
{
    /** The card for the author of this message. */
    public MemberCard author;

    /** The date on which this message was sent. */
    public Date sent;

    /** The text part of the message body. */
    public String body;

    /** The (optional) object part of the message body. */
    public MailPayload payload;
}
