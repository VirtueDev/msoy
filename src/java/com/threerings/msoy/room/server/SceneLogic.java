//
// $Id$

package com.threerings.msoy.room.server;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.UpdateList;

import com.threerings.msoy.server.persist.MemberRepository;

import com.threerings.msoy.group.server.persist.GroupRecord;
import com.threerings.msoy.group.server.persist.GroupRepository;
import com.threerings.msoy.item.data.all.Audio;
import com.threerings.msoy.item.data.all.Decor;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.ItemIdent;
import com.threerings.msoy.item.server.persist.AudioRepository;
import com.threerings.msoy.item.server.persist.DecorRecord;
import com.threerings.msoy.item.server.persist.DecorRepository;
import com.threerings.msoy.item.server.persist.ItemRecord;

import com.threerings.msoy.room.data.FurniData;
import com.threerings.msoy.room.data.MsoySceneModel;
import com.threerings.msoy.room.server.persist.MemoryRepository;
import com.threerings.msoy.room.server.persist.MsoySceneRepository;
import com.threerings.msoy.room.server.persist.SceneFurniRecord;
import com.threerings.msoy.room.server.persist.SceneRecord;

import static com.threerings.msoy.Log.log;

/**
 * Handles scene related logic.
 */
@Singleton
public class SceneLogic
    implements SceneRepository
{
    // from interface SceneRepository
    public SceneModel loadSceneModel (int sceneId)
        throws PersistenceException, NoSuchSceneException
    {
        SceneRecord scene = _sceneRepo.loadScene(sceneId);
        if (scene == null) {
            throw new NoSuchSceneException(sceneId);
        }
        MsoySceneModel model = scene.toSceneModel();

        // populate the name of the owner
        switch (model.ownerType) {
        case MsoySceneModel.OWNER_TYPE_MEMBER:
            model.ownerName = _memberRepo.loadMemberName(model.ownerId);
            break;

        case MsoySceneModel.OWNER_TYPE_GROUP:
            GroupRecord grec = _groupRepo.loadGroup(model.ownerId);
            if (grec != null) {
                model.ownerName = grec.toGroupName();
                model.gameId = grec.gameId;
            }
            break;

        default:
            log.warning("Unable to populate owner name, unknown ownership type", new Exception());
            break;
        }

        List<FurniData> flist = Lists.newArrayList();
        for (SceneFurniRecord furni : _sceneRepo.loadFurni(sceneId)) {
            flist.add(furni.toFurniData());
        }
        model.furnis = flist.toArray(new FurniData[flist.size()]);

        // load up our room decor
        if (model.decor.itemId != 0) {
            DecorRecord record = _decorRepo.loadItem(model.decor.itemId);
            if (record != null) {
                model.decor = (Decor) record.toItem();
            }
        }
        if (model.decor.itemId == 0) { // still the default?
            // the scene specified no or an invalid decor, just load up the default
            model.decor = MsoySceneModel.defaultMsoySceneModelDecor();
        }

        return model;
    }

    // from interface SceneRepository
    public UpdateList loadUpdates (int sceneId)
        throws PersistenceException
    {
        return new UpdateList(); // we don't do scene updates
    }

    // from interface SceneRepository
    public Object loadExtras (int sceneId, SceneModel model)
        throws PersistenceException
    {
        MsoySceneModel mmodel = (MsoySceneModel) model;
        Set<ItemIdent> memoryIds = Sets.newHashSet();
        for (FurniData furni : mmodel.furnis) {
            if (furni.itemType != Item.NOT_A_TYPE) {
                memoryIds.add(furni.getItemIdent());
            }
        }
        if (mmodel.decor != null) {
            memoryIds.add(mmodel.decor.getIdent());
        }
        RoomExtras extras = new RoomExtras();
        if (memoryIds.size() > 0) {
            extras.memories = _memoryRepo.loadMemories(memoryIds);
        }
        extras.playlist = Lists.transform(_audioRepo.loadItemsByLocation(sceneId),
            new ItemRecord.ToItem<Audio>());
        return extras;
    }

    // from interface SceneRepository
    public void applyAndRecordUpdate (SceneModel model, SceneUpdate update)
        throws PersistenceException
    {
        // ensure that the update has been applied
        int targetVers = update.getSceneVersion() + update.getVersionIncrement();
        if (model.version != targetVers) {
            log.warning("Refusing to apply update, wrong version",
               "want", model.version, "have", targetVers, "update", update);
            return;
        }
        // now pass it to the accumulator who will take it from here
        _accumulator.add(update);
    }

    /**
     * Creates a new blank room for the specified member.
     *
     * @param ownerType may be an individual member or a group.
     * @param portalAction to where to link the new room's door.
     * @param firstTime whether this the first room this owner has created.
     */
    public SceneRecord createBlankRoom (byte ownerType, int ownerId, int stockSceneId,
        int themeId, String roomName, String portalAction)
    {
        // load up the stock scene
        SceneRecord record = _sceneRepo.loadScene(stockSceneId);

        // if we fail to load a stock scene, just create a totally blank scene
        if (record == null) {
            log.info("Unable to find stock scene to clone", "type", ownerType);
            MsoySceneModel model = MsoySceneModel.blankMsoySceneModel();
            model.ownerType = ownerType;
            model.ownerId = ownerId;
            model.version = 1;
            model.name = roomName;
            model.themeId = themeId;
            return _sceneRepo.insertScene(model);
        }

        // fill in our new bits and write out our new scene
        record.accessControl = MsoySceneModel.ACCESS_EVERYONE;
        record.ownerType = ownerType;
        record.ownerId = ownerId;
        record.themeGroupId = themeId;
        record.name = roomName;
        record.version = 1;
        record.sceneId = 0;
        _sceneRepo.insertScene(record);

        // now load up furni from the stock scene
        for (SceneFurniRecord furni : _sceneRepo.loadFurni(stockSceneId)) {
            furni.sceneId = record.sceneId;
            // if the scene has a portal pointing to the default public space; rewrite it to point
            // to our specified new portal destination (if we have one)
            if (portalAction != null && furni.actionType == FurniData.ACTION_PORTAL &&
                furni.actionData != null && furni.actionData.startsWith(
                    SceneRecord.Stock.PUBLIC_ROOM.getSceneId() + ":")) {
                furni.actionData = portalAction;
            }
            _sceneRepo.insertFurni(furni);
        }

        return record;
    }

    // dependencies
    @Inject protected AudioRepository _audioRepo;
    @Inject protected DecorRepository _decorRepo;
    @Inject protected GroupRepository _groupRepo;
    @Inject protected MemberRepository _memberRepo;
    @Inject protected MemoryRepository _memoryRepo;
    @Inject protected MsoySceneRepository _sceneRepo;
    @Inject protected UpdateAccumulator _accumulator;
}
