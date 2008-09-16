//
// $Id$

package com.threerings.msoy.person.gwt;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Photo gallery details.
 *
 * @author mdb
 * @author mjensen
 */
public class Gallery implements IsSerializable
{
    public static final int MAX_NAME_LENGTH = 80;
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    public int galleryId;
    public String name;
    public String description;
    public Date lastModified;
}

