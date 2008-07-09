//
// $Id$

package com.threerings.msoy.bureau.client;

import com.threerings.msoy.bureau.client.BureauLauncherReceiver;
import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link BureauLauncherReceiver} instance.
 */
public class BureauLauncherDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "e594037aadf57c8011c9ecbb0c28050a";

    /** The method id used to dispatch {@link BureauLauncherReceiver#addGameServer}
     * notifications. */
    public static final int ADD_GAME_SERVER = 1;

    /** The method id used to dispatch {@link BureauLauncherReceiver#launchThane}
     * notifications. */
    public static final int LAUNCH_THANE = 2;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public BureauLauncherDecoder (BureauLauncherReceiver receiver)
    {
        this.receiver = receiver;
    }

    @Override // documentation inherited
    public String getReceiverCode ()
    {
        return RECEIVER_CODE;
    }

    @Override // documentation inherited
    public void dispatchNotification (int methodId, Object[] args)
    {
        switch (methodId) {
        case ADD_GAME_SERVER:
            ((BureauLauncherReceiver)receiver).addGameServer(
                (String)args[0], ((Integer)args[1]).intValue()
            );
            return;

        case LAUNCH_THANE:
            ((BureauLauncherReceiver)receiver).launchThane(
                (String)args[0], (String)args[1], (String)args[2], ((Integer)args[3]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
            return;
        }
    }
}
