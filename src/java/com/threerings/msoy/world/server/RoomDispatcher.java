//
// $Id$

package com.threerings.msoy.world.server;

import com.threerings.msoy.item.data.all.ItemIdent;
import com.threerings.msoy.world.client.RoomService;
import com.threerings.msoy.world.data.EntityMemoryEntry;
import com.threerings.msoy.world.data.RoomMarshaller;
import com.threerings.msoy.world.data.RoomPropertyEntry;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.data.Location;

/**
 * Dispatches requests to the {@link RoomProvider}.
 */
public class RoomDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public RoomDispatcher (RoomProvider provider)
    {
        this.provider = provider;
    }

    @Override // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new RoomMarshaller();
    }

    @SuppressWarnings("unchecked")
    @Override // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case RoomMarshaller.CHANGE_LOCATION:
            ((RoomProvider)provider).changeLocation(
                source,
                (ItemIdent)args[0], (Location)args[1]
            );
            return;

        case RoomMarshaller.DESPAWN_MOB:
            ((RoomProvider)provider).despawnMob(
                source,
                ((Integer)args[0]).intValue(), (String)args[1], (InvocationService.InvocationListener)args[2]
            );
            return;

        case RoomMarshaller.EDIT_ROOM:
            ((RoomProvider)provider).editRoom(
                source,
                (InvocationService.ResultListener)args[0]
            );
            return;

        case RoomMarshaller.PURCHASE_ROOM:
            ((RoomProvider)provider).purchaseRoom(
                source,
                (InvocationService.ResultListener)args[0]
            );
            return;

        case RoomMarshaller.REQUEST_CONTROL:
            ((RoomProvider)provider).requestControl(
                source,
                (ItemIdent)args[0]
            );
            return;

        case RoomMarshaller.SEND_SPRITE_MESSAGE:
            ((RoomProvider)provider).sendSpriteMessage(
                source,
                (ItemIdent)args[0], (String)args[1], (byte[])args[2], ((Boolean)args[3]).booleanValue()
            );
            return;

        case RoomMarshaller.SEND_SPRITE_SIGNAL:
            ((RoomProvider)provider).sendSpriteSignal(
                source,
                (String)args[0], (byte[])args[1]
            );
            return;

        case RoomMarshaller.SET_ACTOR_STATE:
            ((RoomProvider)provider).setActorState(
                source,
                (ItemIdent)args[0], ((Integer)args[1]).intValue(), (String)args[2]
            );
            return;

        case RoomMarshaller.SET_ROOM_PROPERTY:
            ((RoomProvider)provider).setRoomProperty(
                source,
                (RoomPropertyEntry)args[0]
            );
            return;

        case RoomMarshaller.SPAWN_MOB:
            ((RoomProvider)provider).spawnMob(
                source,
                ((Integer)args[0]).intValue(), (String)args[1], (String)args[2], (InvocationService.InvocationListener)args[3]
            );
            return;

        case RoomMarshaller.UPDATE_MEMORY:
            ((RoomProvider)provider).updateMemory(
                source,
                (EntityMemoryEntry)args[0]
            );
            return;

        case RoomMarshaller.UPDATE_ROOM:
            ((RoomProvider)provider).updateRoom(
                source,
                (SceneUpdate[])args[0], (InvocationService.InvocationListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
