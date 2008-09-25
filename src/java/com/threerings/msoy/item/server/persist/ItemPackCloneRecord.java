//
// $Id$

package com.threerings.msoy.item.server.persist;

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.annotation.TableGenerator;
import com.samskivert.jdbc.depot.expression.ColumnExp;

import com.threerings.msoy.money.data.all.Currency;

/** Clone records for ItemPacks. */
@TableGenerator(name="cloneId", pkColumnValue="ITEMPACK_CLONE")
public class ItemPackCloneRecord extends CloneRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The qualified column identifier for the {@link #itemId} field. */
    public static final ColumnExp ITEM_ID_C =
        new ColumnExp(ItemPackCloneRecord.class, ITEM_ID);

    /** The qualified column identifier for the {@link #originalItemId} field. */
    public static final ColumnExp ORIGINAL_ITEM_ID_C =
        new ColumnExp(ItemPackCloneRecord.class, ORIGINAL_ITEM_ID);

    /** The qualified column identifier for the {@link #ownerId} field. */
    public static final ColumnExp OWNER_ID_C =
        new ColumnExp(ItemPackCloneRecord.class, OWNER_ID);

    /** The qualified column identifier for the {@link #purchaseTime} field. */
    public static final ColumnExp PURCHASE_TIME_C =
        new ColumnExp(ItemPackCloneRecord.class, PURCHASE_TIME);

    /** The qualified column identifier for the {@link #currency} field. */
    public static final ColumnExp CURRENCY_C =
        new ColumnExp(ItemPackCloneRecord.class, CURRENCY);

    /** The qualified column identifier for the {@link #amountPaid} field. */
    public static final ColumnExp AMOUNT_PAID_C =
        new ColumnExp(ItemPackCloneRecord.class, AMOUNT_PAID);

    /** The qualified column identifier for the {@link #used} field. */
    public static final ColumnExp USED_C =
        new ColumnExp(ItemPackCloneRecord.class, USED);

    /** The qualified column identifier for the {@link #location} field. */
    public static final ColumnExp LOCATION_C =
        new ColumnExp(ItemPackCloneRecord.class, LOCATION);

    /** The qualified column identifier for the {@link #lastTouched} field. */
    public static final ColumnExp LAST_TOUCHED_C =
        new ColumnExp(ItemPackCloneRecord.class, LAST_TOUCHED);

    /** The qualified column identifier for the {@link #name} field. */
    public static final ColumnExp NAME_C =
        new ColumnExp(ItemPackCloneRecord.class, NAME);

    /** The qualified column identifier for the {@link #mediaHash} field. */
    public static final ColumnExp MEDIA_HASH_C =
        new ColumnExp(ItemPackCloneRecord.class, MEDIA_HASH);

    /** The qualified column identifier for the {@link #mediaStamp} field. */
    public static final ColumnExp MEDIA_STAMP_C =
        new ColumnExp(ItemPackCloneRecord.class, MEDIA_STAMP);
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 1 + BASE_SCHEMA_VERSION * BASE_MULTIPLIER;

    @Override
    public void initialize (ItemRecord parent, int newOwnerId, Currency currency, int amountPaid)
    {
        super.initialize(parent, newOwnerId, currency, amountPaid);

        // TODO: copy anything needed from the original
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link ItemPackCloneRecord}
     * with the supplied key values.
     */
    public static Key<ItemPackCloneRecord> getKey (int itemId)
    {
        return new Key<ItemPackCloneRecord>(
                ItemPackCloneRecord.class,
                new String[] { ITEM_ID },
                new Comparable[] { itemId });
    }
    // AUTO-GENERATED: METHODS END
}
