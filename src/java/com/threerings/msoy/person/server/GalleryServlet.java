//
// $Id$

package com.threerings.msoy.person.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.PrimitiveArrays;
import com.google.inject.Inject;

import com.samskivert.util.IntIntMap;
import com.threerings.msoy.server.persist.MemberRecord;

import com.threerings.msoy.data.all.MediaDesc;
import com.threerings.msoy.item.data.all.Photo;
import com.threerings.msoy.item.server.persist.ItemRecord;
import com.threerings.msoy.item.server.persist.PhotoRecord;
import com.threerings.msoy.item.server.persist.PhotoRepository;

import com.threerings.msoy.web.data.ServiceCodes;
import com.threerings.msoy.web.data.ServiceException;
import com.threerings.msoy.web.server.MsoyServiceServlet;

import com.threerings.msoy.person.gwt.Gallery;
import com.threerings.msoy.person.gwt.GalleryService;
import com.threerings.msoy.person.server.persist.GalleryRecord;
import com.threerings.msoy.person.server.persist.GalleryRepository;

import static com.threerings.msoy.Log.log;

/**
 * Implements the {@link GalleryService}.
 *
 * @author mjensen
 */
public class GalleryServlet extends MsoyServiceServlet
    implements GalleryService
{
    // from GalleryService
    public Gallery createGallery (String name, String description, List<Integer> photoItemIds)
        throws ServiceException
    {
        MemberRecord memrec = requireAuthedUser();
        // only add photos that the member owns
        photoItemIds.removeAll(validateOwnership(memrec.memberId, photoItemIds));

        // fetch the thumbnail media from the first image
        MediaDesc thumbMedia = null;
        if (photoItemIds.size() > 0) {
            PhotoRecord firstPhoto = _photoRepo.loadItem(photoItemIds.get(0));
            thumbMedia = new MediaDesc(firstPhoto.thumbMediaHash, firstPhoto.thumbMimeType,
                firstPhoto.thumbConstraint);
        }

        return _galleryRepo.insertGallery(
            memrec.memberId, name, description,
            PrimitiveArrays.toIntArray(photoItemIds), thumbMedia).toGallery();
    }

    // from GalleryService
    public void updateGallery (int galleryId, String name, String description,
                               List<Integer> photoItemIds)
        throws ServiceException
    {
        // load the existing gallery record
        GalleryRecord gallery = _galleryRepo.loadGallery(galleryId);

        // check whether gallery exists
        if (gallery == null) {
            log.warning("Gallery does not exist.", "galleryId", galleryId);
            // TODO add i18n "not exist" message or go ahead and create a new gallery?
            throw new ServiceException();
        }

        MemberRecord member = requireAuthedUser();
        if (gallery.ownerId != member.memberId) {
            throw new ServiceException(ServiceCodes.E_ACCESS_DENIED);
        }

        // photos already added to the gallery are known to be valid
        List<Integer> newPhotoIds = Lists.newArrayList(photoItemIds);
        newPhotoIds.removeAll(PrimitiveArrays.asList(gallery.photoItemIds));
        // remove any rejects
        photoItemIds.removeAll(validateOwnership(member.memberId, newPhotoIds));

        // fetch the thumbnail media from the first image
        MediaDesc thumbMedia = null;
        if (photoItemIds.size() > 0) {
            PhotoRecord firstPhoto = _photoRepo.loadItem(photoItemIds.get(0));
            thumbMedia = new MediaDesc(firstPhoto.thumbMediaHash, firstPhoto.thumbMimeType,
                firstPhoto.thumbConstraint);
        }

        _galleryRepo.updateGallery(galleryId, description, name,
            PrimitiveArrays.toIntArray(photoItemIds), thumbMedia);
    }

    // from GalleryService
    public void deleteGallery (int galleryId)
        throws ServiceException
    {
        _galleryRepo.deleteGallery(galleryId);
    }

    // from GalleryService
    public List<Gallery> loadGalleries (int memberId)
        throws ServiceException
    {
        return _galleryLogic.loadGalleries(memberId);
    }

    // from GalleryService
    public List<Photo> loadGallery (int galleryId)
        throws ServiceException
    {
        return loadPhotos(_galleryRepo.loadGallery(galleryId));
    }

    // from GalleryService
    public List<Photo> loadMeGallery (int memberId)
        throws ServiceException
    {
        return loadPhotos(_galleryRepo.loadMeGallery(memberId));
    }

    protected List<Photo> loadPhotos (GalleryRecord gallery)
        throws ServiceException
    {
        if (gallery == null) {
            return null;
        }
        return Lists.transform(_photoRepo.loadItems(PrimitiveArrays.asList(gallery.photoItemIds)),
                               new ItemRecord.ToItem<Photo>());
    }

    /**
     * Finds Photo item IDs that the given member does not own.
     * @return the list of rejected photo IDs that the member does not own.
     */
    protected List<Integer> validateOwnership (int memberId, List<Integer> photoItemIds)
    {
        List<Integer> rejects = Lists.newArrayList();
        if (!photoItemIds.isEmpty()) {
            IntIntMap ownerMap = _photoRepo.loadOwnerIds(photoItemIds);
            for (int photoId : ownerMap.getKeys()) {
                if (memberId != ownerMap.get(photoId)) {
                    rejects.add(photoId);
                    log.warning("Member tried to add a photo that they do not own to a gallery.",
                                "memberId", memberId, "photoItemId", photoId);
                }
            }
        }
        return rejects;
    }

    @Inject protected GalleryRepository _galleryRepo;
    @Inject protected PhotoRepository _photoRepo;
    @Inject protected GalleryLogic _galleryLogic;
}
