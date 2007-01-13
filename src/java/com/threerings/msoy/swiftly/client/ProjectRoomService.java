//
// $Id$

package com.threerings.msoy.swiftly.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.msoy.swiftly.data.PathElement;

/**
 * Provides invocation services pertaining to a particular project room.
 */
public interface ProjectRoomService extends InvocationService
{
    /** Requests to add a path element to the project. */
    public void addPathElement (Client client, PathElement element);

    /** Requests that the specified path element be updated (wholesale). */
    public void updatePathElement (Client client, PathElement element);

    /** Requests that the specified path element be removed from the project. */
    public void deletePathElement (Client client, int elementId);
}
