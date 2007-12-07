//
// $Id$

package com.threerings.msoy.world.client {

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.display.StageQuality;
import flash.external.ExternalInterface;
import flash.geom.Point;

import mx.core.Application;
import mx.resources.ResourceBundle;

import com.adobe.crypto.MD5;

import com.threerings.util.CommandEvent;
import com.threerings.util.Log;
import com.threerings.util.Name;
import com.threerings.util.ResultAdapter;
import com.threerings.util.StringUtil;
import com.threerings.util.ValueEvent;

import com.threerings.flash.MenuUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.Credentials;

import com.threerings.whirled.data.SceneMarshaller;
import com.threerings.whirled.spot.data.SpotMarshaller;
import com.threerings.whirled.spot.data.SpotSceneObject;

import com.threerings.parlor.data.ParlorMarshaller;
import com.threerings.toybox.data.ToyBoxMarshaller;

import com.threerings.msoy.data.MemberLocation;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.MsoyAuthResponseData;
import com.threerings.msoy.data.MsoyCodes;
import com.threerings.msoy.data.MsoyCredentials;
import com.threerings.msoy.data.all.ChannelName;
import com.threerings.msoy.data.all.GroupName;
import com.threerings.msoy.data.all.MemberName;

import com.threerings.msoy.item.data.ItemMarshaller;
import com.threerings.msoy.item.data.all.Avatar;
import com.threerings.msoy.item.data.all.Document;
import com.threerings.msoy.item.data.all.Furniture;
import com.threerings.msoy.item.data.all.Game;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.ItemList;
import com.threerings.msoy.item.data.all.ItemPack;
import com.threerings.msoy.item.data.all.LevelPack;
import com.threerings.msoy.item.data.all.Pet;
import com.threerings.msoy.item.data.all.Photo;
import com.threerings.msoy.item.data.all.Prop;

import com.threerings.msoy.avrg.data.AVRGameMarshaller;
import com.threerings.msoy.avrg.data.AVRMarshaller;

import com.threerings.msoy.chat.client.ReportingListener;
import com.threerings.msoy.chat.data.ChatChannel;

import com.threerings.msoy.notify.data.GuestInviteNotification;
import com.threerings.msoy.notify.data.LevelUpNotification;
import com.threerings.msoy.notify.data.ReleaseNotesNotification;

import com.threerings.msoy.client.BaseClient;
import com.threerings.msoy.client.BaseContext;
import com.threerings.msoy.client.ContextMenuProvider;
import com.threerings.msoy.client.DeploymentConfig;
import com.threerings.msoy.client.Msgs;
import com.threerings.msoy.client.MsoyController;
import com.threerings.msoy.client.Prefs;

import com.threerings.msoy.world.data.MsoySceneMarshaller;
import com.threerings.msoy.world.data.PetMarshaller;
import com.threerings.msoy.world.data.RoomConfig;

/**
 * An event dispatched for tutorial-specific purposes.
 * 
 * @eventType com.threerings.msoy.client.WorldClient.TUTORIAL_EVENT
 */
[Event(name="tutorial", type="com.threerings.util.ValueEvent")]

/**
 * Handles the main services for the world and game clients.
 */
public class WorldClient extends BaseClient
{
    /**
     * An event dispatched for tutorial-specific purposes.
     *
     * @eventType tutorial
     */
    public static const TUTORIAL_EVENT :String = "tutorial";

    public function WorldClient (stage :Stage)
    {
        super(stage);

        // TODO: allow users to choose? I think it's a decision that we should make for them.
        // Jon speculates that maybe we can monitor the frame rate and automatically shift it,
        // but noticable jiggles occur when it's switched and I wouldn't want the entire
        // world to jiggle when someone starts walking, then jiggle again when they stop.
        // So: for now we just peg it to MEDIUM.
        stage.quality = StageQuality.MEDIUM;

        // make sure we're running a sufficiently new version of Flash
        if (_wctx.getTopPanel().verifyFlashVersion()) {
            logon(); // now logon
        }
    }

    // from BaseClient
    override public function fuckingCompiler () :void
    {
        super.fuckingCompiler();
        var c :Class;
        c = AVRMarshaller;
        c = AVRGameMarshaller;
        c = Document;
        c = Furniture;
        c = Game;
        c = GuestInviteNotification;
        c = ItemList;
        c = ItemMarshaller;
        c = ItemPack;
        c = LevelPack;
        c = Prop;
        c = LevelUpNotification;
        c = MemberObject;
        c = MemberLocation;
        c = MsoySceneMarshaller;
        c = ParlorMarshaller;
        c = PetMarshaller;
        c = Photo;
        c = ReleaseNotesNotification;
        c = RoomConfig;
        c = SceneMarshaller;
        c = SpotMarshaller;
        c = SpotSceneObject;
        c = ToyBoxMarshaller;

        // these cause bundles to be compiled in.
        [ResourceBundle("general")]
        [ResourceBundle("chat")]
        [ResourceBundle("game")]
        [ResourceBundle("ezgame")]
        [ResourceBundle("editing")]
        [ResourceBundle("item")]
        [ResourceBundle("notify")]
        [ResourceBundle("prefs")]
        var rb :ResourceBundle;
    }

    // from Client
    override public function gotBootstrap (data :BootstrapData, omgr :DObjectManager) :void
    {
        super.gotBootstrap(data, omgr);

        // save any machineIdent or sessionToken from the server.
        var rdata :MsoyAuthResponseData = (getAuthResponseData() as MsoyAuthResponseData);
        if (rdata.ident != null) {
            Prefs.setMachineIdent(rdata.ident);
        }
        if (rdata.sessionToken != null) {
            Prefs.setSessionToken(rdata.sessionToken);
            // fill our session token into our credentials so that we can log in more efficiently
            // on a reconnect and so that we can log into game servers
            (getCredentials() as MsoyCredentials).sessionToken = rdata.sessionToken;
        }

        if (rdata.sessionToken != null) {
            try {
                if (ExternalInterface.available && !_embedded) {
                    ExternalInterface.call("flashDidLogon", "Foo", 1, rdata.sessionToken);
                }
            } catch (err :Error) {
                log.warning("Unable to inform javascript about login: " + err);
            }
        }

        log.info("Client logged on [built=" + DeploymentConfig.buildTime +
                 ", mediaURL=" + DeploymentConfig.mediaURL +
                 ", staticMediaURL=" + DeploymentConfig.staticMediaURL + "].");
    }

    // from Client
    override public function gotClientObject (clobj :ClientObject) :void
    {
        super.gotClientObject(clobj);

        if (clobj is MemberObject && !_embedded && !_featuredPlaceView) {
            var member :MemberObject = clobj as MemberObject;
            member.addListener(new AvatarUpdateNotifier());
        }

        if (!_featuredPlaceView) {
            // listen for flow and gold updates
            _user = (clobj as MemberObject);
            var updater :StatusUpdater = new StatusUpdater(this);
            _user.addListener(updater);

            // configure our levels to start
            updater.newLevel(_user.level);
            // updater.newGold(_user.gold);
            updater.newFlow(_user.flow);
            updater.newMail(_user.newMailCount);
        }
    }

    /**
     * Exposed to javascript so that it may notify us to logon.
     */
    protected function externalClientLogon (memberId :int, token :String) :void
    {
        if (token == null) {
            return;
        }

        log.info("Logging on via external request [id=" + memberId + ", token=" + token + "].");
        var co :MemberObject = _wctx.getMemberObject();
        if (co == null || co.getMemberId() != memberId) {
            _wctx.getMsoyController().handleLogon(createStartupCreds(token));
        }
    }

    /**
     * Exposed to javascript so that it may notify us to move to a new location.
     */
    protected function externalClientGo (where :String) :void
    {
        log.info("Changing scenes per external request [where=" + where + "].");
        var params :Object = new Object();
        for each (var param :String in where.split("&")) {
            var eidx: int = param.indexOf("=");
            if (eidx == -1) {
                log.warning("Malformed clientGo() parameter [param=" + param + "].");
            } else {
                params[param.substring(0, eidx)] = param.substring(eidx+1);
            }
        }
        _wctx.getMsoyController().goToPlace(params);
    }

    /**
     * Exposed to javascript so that it may notify us to logoff.
     */
    protected function externalClientLogoff (backAsGuest :Boolean = true) :void
    {
        log.info("Logging off via external request [backAsGuest=" + backAsGuest + "].");

        if (backAsGuest) {
            var creds :MsoyCredentials = new MsoyCredentials(null, null);
            creds.ident = "";
            _wctx.getMsoyController().handleLogon(creds);
        } else {
            logoff(false);
        }
    }

    /**
     * Exposed to javascript so that the it may determine if the current scene is a room.
     */
    protected function externalInRoom () :Boolean
    {
        return _wctx.getTopPanel().getPlaceView() is RoomView;
    }

    /**
     * Exposed to javascript so that it may tell us to use this avatar.  If the avatarId of 0 is
     * passed in, the current avatar is simply cleared away, leaving them with the default.
     */
    protected function externalUseAvatar (avatarId :int, scale :Number) :void
    {
        _wctx.getWorldDirector().setAvatar(avatarId, scale);
    }

    /**
     * Exposed to javascript so that it can check the avatar that its showing in the inventory 
     * browser agains the avatar that the user is currently wearing.
     */
    protected function externalGetAvatarId () :int
    {
        var avatar :Avatar = _wctx.getMemberObject().avatar;
        return avatar == null ? 0 : avatar.itemId;
    }

    /**
     * Exposed to javascript so that the avatarviewer may update the scale of an avatar
     * in real-time.
     */
    protected function externalUpdateAvatarScale (avatarId :int, newScale :Number) :void
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            view.updateAvatarScale(avatarId, newScale);
        }
    }

    /**
     * Exposed to javascript so that it may tell us to use items in the current room, either as
     * background items, or as furni as apporpriate.
     */ 
    protected function externalUseItem (itemId :int, itemType :int) :void
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            view.getRoomController().useItem(itemId, itemType);
        }
    }

    /**
     * Exposed to javascript so that it may tell us to remove furni from the current room.
     */
    protected function externalRemoveFurni (itemId :int, itemType :int) :void
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            view.getRoomController().removeFurni(itemId, itemType);
        }
    }

    /**
     * Exposed to javascript so that it may find out the id of some specific item types for the
     * current room.
     */
    protected function externalGetSceneItemId (itemType :int) :int
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            return view.getRoomController().getItemId(itemType);
        } else {
            return 0;
        }
    }

    protected function externalGetFurniList () :Array 
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            return view.getRoomController().getFurniList();
        } else {
            return [];
        }
    }

    protected function externalUsePet (petId :int) :void
    {
        var svc :PetService = _ctx.getClient().requireService(PetService) as PetService;
        svc.callPet(_wctx.getClient(), petId, 
            new ReportingListener(_wctx, MsoyCodes.GENERAL_MSGS, null, "m.pet_called"));
    }

    protected function externalRemovePet (petId :int) :void
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            // ensure this pet really is in this room
            for each (var pet :PetSprite in view.getPets()) {
                if (pet.getItemIdent().itemId == petId) {
                    CommandEvent.dispatch(view, RoomController.ORDER_PET, [petId, Pet.ORDER_SLEEP]);
                    break;
                }
            }
        }
    }

    protected function externalGetPets () :Array
    {
        var view :RoomView = _wctx.getTopPanel().getPlaceView() as RoomView;
        if (view != null) {
            var petIds :Array = [];
            for each (var pet :PetSprite in view.getPets()) {
                petIds.push(pet.getItemIdent().itemId);
            }
            return petIds;
        } else {
            return [];
        }
    }

    protected function externalTutorialEvent (eventName :String) :void
    {
        _wctx.getGameDirector().tutorialEvent(eventName);
    }

    /**
     * Exposed to JavaScript so that it may order us to open chat channels.
     */
    protected function externalOpenChannel (type :int, name :String, id :int) :void
    {
        var nameObj :Name;
        if (type == ChatChannel.MEMBER_CHANNEL) {
            nameObj = new MemberName(name, id);
        } else if (type == ChatChannel.GROUP_CHANNEL) {
            nameObj = new GroupName(name, id);
        } else if (type == ChatChannel.PRIVATE_CHANNEL) {
            nameObj = new ChannelName(name, id);
        } else {
            throw new Error("Unknown channel type: " + type);
        }
        _wctx.getMsoyChatDirector().openChannel(nameObj);
    }

    // from BaseClient
    override protected function createContext () :BaseContext
    {
        return (_wctx = new WorldContext(this));
    }

    // from BaseClient
    override protected function configureExternalFunctions () :void
    {
        super.configureExternalFunctions();

        ExternalInterface.addCallback("clientLogon", externalClientLogon);
        ExternalInterface.addCallback("clientGo", externalClientGo);
        ExternalInterface.addCallback("clientLogoff", externalClientLogoff);
        ExternalInterface.addCallback("inRoom", externalInRoom);
        ExternalInterface.addCallback("useAvatar", externalUseAvatar);
        ExternalInterface.addCallback("getAvatarId", externalGetAvatarId);
        ExternalInterface.addCallback("updateAvatarScale", externalUpdateAvatarScale);
        ExternalInterface.addCallback("useItem", externalUseItem);
        ExternalInterface.addCallback("removeFurni", externalRemoveFurni);
        ExternalInterface.addCallback("getSceneItemId", externalGetSceneItemId);
        ExternalInterface.addCallback("getFurniList", externalGetFurniList);
        ExternalInterface.addCallback("usePet", externalUsePet);
        ExternalInterface.addCallback("removePet", externalRemovePet);
        ExternalInterface.addCallback("getPets", externalGetPets);
        ExternalInterface.addCallback("tutorialEvent", externalTutorialEvent);
        ExternalInterface.addCallback("openChannel", externalOpenChannel);
    }

    // from BaseClient
    override protected function populateContextMenu (custom :Array) :void
    {
        try {
            var allObjects :Array = _stage.getObjectsUnderPoint(
                new Point(_stage.mouseX, _stage.mouseY));
            var seenObjects :Array = [];
            for each (var disp :DisplayObject in allObjects) {
                do {
                    seenObjects.push(disp);
                    if (disp is ContextMenuProvider) {
                        (disp as ContextMenuProvider).populateContextMenu(_wctx, custom);
                    }
                    disp = disp.parent;
                } while (disp != null && (seenObjects.indexOf(disp) == -1));
            }
        } catch (e :Error) {
            log.logStackTrace(e);
        }
    }

    // from BaseClient
    override protected function createStartupCreds (token :String) :Credentials
    {
        var params :Object = _stage.loaderInfo.parameters;
        var creds :MsoyCredentials;
        if ((params["pass"] != null) && (params["user"] != null)) {
            creds = new MsoyCredentials(new Name(String(params["user"])),
                                        MD5.hash(String(params["pass"])));
        } else {
            creds = new MsoyCredentials(null, null);
        }
        creds.ident = Prefs.getMachineIdent();
        if (null == params["guest"]) {
            creds.sessionToken = (token == null) ? params["token"] : token;
        }
        creds.featuredPlaceView = null != params["featuredPlace"];
        return creds;
    }

    protected var _wctx :WorldContext;
    protected var _user :MemberObject;

    private static const log :Log = Log.getLog(WorldClient);
}
}

import flash.external.ExternalInterface;

import com.threerings.util.Log;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.msoy.item.data.all.Avatar;

import com.threerings.msoy.client.BaseClient;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.all.FriendEntry;
import com.threerings.msoy.data.all.SceneBookmarkEntry;

class AvatarUpdateNotifier implements AttributeChangeListener
{
    public function attributeChanged (event :AttributeChangedEvent) :void
    {
        if (MemberObject.AVATAR == event.getName()) {
            try {
                if (ExternalInterface.available) {
                    var newId :int = 0;
                    var oldId :int = 0;
                    var value :Object = event.getValue();
                    if (value is Avatar) {
                        newId = (value as Avatar).itemId;
                    }
                    value = event.getOldValue();
                    if (value is Avatar) {
                        oldId = (value as Avatar).itemId;
                    }
                    ExternalInterface.call("triggerFlashEvent", "avatarChanged", 
                        [ newId, oldId ]);
                }
            } catch (err :Error) {
                Log.getLog(this).warning("triggerFlashEvent failed: " + err);
            }
        }
    }
}

class StatusUpdater implements AttributeChangeListener, SetListener
{
    public function StatusUpdater (client :BaseClient) {
        _client = client;
    }

    public function attributeChanged (event :AttributeChangedEvent) :void {
        if (event.getName() == MemberObject.LEVEL) {
            newLevel(event.getValue() as int, event.getOldValue() as int);
        /*} else if (event.getName() == MemberObject.GOLD) {
            newGold(event.getValue() as int, event.getOldValue() as int); */
        } else if (event.getName() == MemberObject.FLOW) {
            newFlow(event.getValue() as int, event.getOldValue() as int);
        } else if (event.getName() == MemberObject.NEW_MAIL_COUNT) {
            newMail(event.getValue() as int, event.getOldValue() as int);
        }
    }

    public function entryAdded (event :EntryAddedEvent) :void {
        if (event.getName() == MemberObject.FRIENDS) {
            var entry :FriendEntry = (event.getEntry() as FriendEntry);
            _client.dispatchEventToGWT(
                FRIEND_EVENT, [FRIEND_ADDED, entry.name.toString(), entry.name.getMemberId()]);
        } else if (event.getName() == MemberObject.OWNED_SCENES) {
            var scene :SceneBookmarkEntry = (event.getEntry() as SceneBookmarkEntry);
            _client.dispatchEventToGWT(
                SCENEBOOKMARK_EVENT, [SCENEBOOKMARK_ADDED, scene.sceneName, scene.sceneId]);
        }
    }

    public function entryUpdated (event :EntryUpdatedEvent) :void {
        // nada
    }

    public function entryRemoved (event :EntryRemovedEvent) :void {
        if (event.getName() == MemberObject.FRIENDS) {
            var memberId :int = int(event.getKey());
            _client.dispatchEventToGWT(FRIEND_EVENT, [FRIEND_REMOVED, "", memberId]);
        } else if (event.getName() == MemberObject.OWNED_SCENES) {
            var sceneId :int = int(event.getKey());
            _client.dispatchEventToGWT(
                SCENEBOOKMARK_EVENT, [SCENEBOOKMARK_REMOVED, "", sceneId]);
        }
    }

    public function newLevel (level :int, oldLevel :int = 0) :void {
        sendNotification([STATUS_CHANGE_LEVEL, level, oldLevel]);
    }

    public function newFlow (flow :int, oldFlow :int = 0) :void {
        sendNotification([STATUS_CHANGE_FLOW, flow, oldFlow]);
    }

    public function newGold (gold :int, oldGold :int = 0) :void {
        sendNotification([STATUS_CHANGE_GOLD, gold, oldGold]);
    }

    public function newMail (mail :int, oldMail :int = -1) :void {
        sendNotification([STATUS_CHANGE_MAIL, mail, oldMail]);
    }

    protected function sendNotification (args :Array) :void {
        _client.dispatchEventToGWT(STATUS_CHANGE_EVENT, args);
    }

    /** Event dispatched to GWT when we've leveled up */
    protected static const STATUS_CHANGE_EVENT :String = "statusChange";
    protected static const STATUS_CHANGE_LEVEL :int = 1;
    protected static const STATUS_CHANGE_FLOW :int = 2;
    protected static const STATUS_CHANGE_GOLD :int = 3;
    protected static const STATUS_CHANGE_MAIL :int = 4;

    protected static const FRIEND_EVENT :String = "friend";
    protected static const FRIEND_ADDED :int = 1;
    protected static const FRIEND_REMOVED :int = 2;

    protected static const SCENEBOOKMARK_EVENT :String = "sceneBookmark";
    protected static const SCENEBOOKMARK_ADDED :int = 1;
    protected static const SCENEBOOKMARK_REMOVED :int = 2;

    protected var _client :BaseClient;
}
