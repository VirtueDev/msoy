//
// $Id$

package com.threerings.msoy.avrg.client {

import com.threerings.util.Log;

import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.ConfirmAdapter;
import com.threerings.presents.client.ResultWrapper;

import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.msoy.client.DeploymentConfig;
import com.threerings.msoy.data.MsoyCodes;

import com.threerings.msoy.world.client.WorldContext;

import com.threerings.msoy.game.client.GameLiaison;
import com.threerings.msoy.game.data.MsoyGameConfig;

import com.threerings.msoy.avrg.client.AVRService_AVRGameJoinListener;
import com.threerings.msoy.avrg.data.AVRGameConfig;
import com.threerings.msoy.avrg.data.AVRGameMarshaller;
import com.threerings.msoy.avrg.data.AVRGameObject;
import com.threerings.msoy.avrg.data.AVRMarshaller;

/**
 * Handles the AVRG-specific aspects of the game server connection.
 */
public class AVRGameLiaison extends GameLiaison
    implements AVRService_AVRGameJoinListener
{
    public static const log :Log = Log.getLog(AVRGameLiaison);

    public function AVRGameLiaison (ctx :WorldContext, gameId :int)
    {
        super(ctx, gameId);

        // ensure that the compiler includes these necessary symbols
        var c :Class;
        c = AVRGameMarshaller;
        c = AVRMarshaller;
    }
    
    override public function clientWillLogon (event :ClientEvent) :void
    {
        super.clientWillLogon(event);

        // AVRG's need access to the world services, too.
        _gctx.getClient().addServiceGroup(MsoyCodes.WORLD_GROUP);

    }

    override public function clientDidLogon (event :ClientEvent) :void
    {
        super.clientDidLogon(event);

        var svc :AVRService = (_gctx.getClient().requireService(AVRService) as AVRService);
        svc.activateGame(_gctx.getClient(), _gameId, this)
    }

    // from AVRGameJoinListener
    override public function requestFailed (cause :String) :void
    {
        // GameLiaison conveniently already handles this
        super.requestFailed(cause);

        shutdown();
    }

    // from AVRGameJoinListener
    public function avrgJoined (placeOid :int, config :AVRGameConfig) :void
    {
        // since we hijacked the movement process server-side, let the client catch up
        _gctx.getLocationDirector().didMoveTo(placeOid, config);

        // now that the controller is created, tell it about the world context as well
        getAVRGameController().initializeWorldContext(_wctx);
    }

    override public function shutdown () :void
    {
        _gctx.getLocationDirector().leavePlace();

        super.shutdown();
    }

    public function leaveAVRGame () :void
    {
        var svc :AVRService = (_gctx.getClient().requireService(AVRService) as AVRService);
        svc.deactivateGame(_gctx.getClient(), _gameId, new ConfirmAdapter (
            function (cause :String) :void {
                log.warning("Failed to deactivate AVRG [gameId=" + _gameId +
                            ", cause=" + cause + "].");        
            }, shutdown));
    }

    /**
     * Returns the backend if we're currently in an AVRG, null otherwise.
     */
    public function getAVRGameBackend () :AVRGameBackend
    {
        var ctrl :AVRGameController = getAVRGameController();
        return (ctrl != null) ? ctrl.backend : null;
    }

    /**
     * Returns the game object if we're currently in an AVRG, null otherwise.
     */
    public function getAVRGameController () :AVRGameController
    {
        var ctrl :PlaceController = _gctx.getLocationDirector().getPlaceController();
        return (ctrl != null) ? (ctrl as AVRGameController) : null;
    }
}
}
