//
// $Id$

package com.threerings.msoy.bureau.client;

import com.threerings.presents.client.InvocationReceiver;

/** Defines client exposure for bureau launching. */
public interface BureauLauncherReceiver extends InvocationReceiver
{
    /** Requests to launch a thane bureau with the given connect-back information. */
    public void launchThane (String bureauId, String token, String server, int port);

    /** Adds a server to the launcher's connections. */
    public void addGameServer (String host, int port);
}
