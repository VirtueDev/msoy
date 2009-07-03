//
// $Id$

package com.threerings.msoy.server.persist;

import java.sql.Timestamp;

import com.google.common.base.Function;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.*; // for Depot annotations
import com.samskivert.depot.expression.ColumnExp;

/**
 * Tracks people who have elected to use bars to temporarily purchase 1 month of subscription.
 * Their actual subscription is tracked the normal way, and this record is deleted when the
 * subscription runs out.
 */
public class BarscriptionRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<BarscriptionRecord> _R = BarscriptionRecord.class;
    public static final ColumnExp MEMBER_ID = colexp(_R, "memberId");
    public static final ColumnExp EXPIRES = colexp(_R, "expires");
    // AUTO-GENERATED: FIELDS END

    /** Schema version. */
    public static final int SCHEMA_VERSION = 1;

    /** The memberId. */
    @Id
    public int memberId;

    /** The time at which this user's barscription ends. */
    public Timestamp expires;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link BarscriptionRecord}
     * with the supplied key values.
     */
    public static Key<BarscriptionRecord> getKey (int memberId)
    {
        return new Key<BarscriptionRecord>(
                BarscriptionRecord.class,
                new ColumnExp[] { MEMBER_ID },
                new Comparable[] { memberId });
    }
    // AUTO-GENERATED: METHODS END

    /** Turns a Key back into a memberId. */
    public static Function<Key<BarscriptionRecord>,Integer> KEY_TO_MEMBER_ID =
        new Function<Key<BarscriptionRecord>,Integer>() {
            public Integer apply (Key<BarscriptionRecord> key) {
                return (Integer) key.getValues()[0];
            }
        };
}