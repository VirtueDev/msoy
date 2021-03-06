//
// $Id$

package com.threerings.msoy.room.server.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

import com.threerings.util.StreamableHashMap;

import com.threerings.msoy.item.data.all.ItemIdent;
import com.threerings.msoy.item.data.all.MsoyItemType;
import com.threerings.msoy.room.data.EntityMemories;

import static com.threerings.msoy.Log.log;

/**
 * Maintains memory information for "smart" digital items (furniture, pets, etc).
 */
@Entity
public class MemoriesRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<MemoriesRecord> _R = MemoriesRecord.class;
    public static final ColumnExp<MsoyItemType> ITEM_TYPE = colexp(_R, "itemType");
    public static final ColumnExp<Integer> ITEM_ID = colexp(_R, "itemId");
    public static final ColumnExp<byte[]> DATA = colexp(_R, "data");
    // AUTO-GENERATED: FIELDS END

    /** Increment this value if you modify the definition of this persistent
     * object in a way that will result in a change to its SQL counterpart. */
    public static final int SCHEMA_VERSION = 1;

    /** The type of the item for which we're storing memory data. */
    @Id public MsoyItemType itemType;

    /** The id of the item for which we're storing memory data. */
    @Id public int itemId;

    /** A serialized representation of all the memory data. */
    @Column(length=8192) // 4k + extra. See Ray for explanation.
    public byte[] data;

    /** Used when loading instances from the repository. */
    public MemoriesRecord ()
    {
    }

    /**
     * Creates a memory record from the supplied memory information.
     */
    public MemoriesRecord (EntityMemories mems)
    {
        this.itemType = mems.ident.type;
        this.itemId = mems.ident.itemId;

        int size = mems.memories.size();
        if (size == 0) {
            // this.data stays at null. We represent a loss of memory: delete the row.
            return;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            out.writeShort(size);
            for (Map.Entry<String, byte[]> entry : mems.memories.entrySet()) {
                out.writeUTF(entry.getKey());
                byte[] value = entry.getValue();
                out.writeShort(value.length);
                out.write(value);
            }
            out.flush();
            this.data = bos.toByteArray();
            // TEMP: debugging
            if (this.data.length > 4096) {
                log.warning("Encoded fat memories",
                    "itemType", itemType, "itemId", itemId, "length", this.data.length);
            }

        } catch (IOException ioe) {
            throw new RuntimeException("Can't happen", ioe);
        }
    }

    /**
     * Create a memory record initialized with the pre-encoded memories from the viewer.
     */
    public MemoriesRecord (String base64Data)
    {
        if (base64Data == null) {
            return;
        }
        this.data = Base64.decodeBase64(base64Data.getBytes());
        if (this.data.length > 4096) {
            log.warning("Encoded fat memories for purchase", "length", this.data.length);
            if (this.data.length >= 8192) {
                throw new RuntimeException("Memories too fat!");
            }
        }
    }

    /**
     * Encode the memory data.
     */
    public String toBase64 ()
    {
        if (data == null) {
            return null;
        }
        return new String(Base64.encodeBase64(data));
    }

    /**
     * Converts this persistent record to a runtime record.
     */
    public EntityMemories toEntry ()
    {
        EntityMemories mems = new EntityMemories();
        mems.ident = new ItemIdent(this.itemType, this.itemId);
        mems.memories = new StreamableHashMap<String, byte[]>();
        if (this.data != null) {
            try {
                DataInputStream ins = new DataInputStream(new ByteArrayInputStream(this.data));
                int count = ins.readShort();
                for (int ii = 0; ii < count; ii++) {
                    String key = ins.readUTF();
                    byte[] value = new byte[ins.readShort()];
                    ins.read(value, 0, value.length);
                    mems.memories.put(key, value);
                }
                // TEMP: debugging
                if (ins.available() > 0) {
                    log.warning("Haven't fully read memories",
                        "ident", mems.ident, "available", ins.available());
                }

            } catch (IOException ioe) {
                throw new RuntimeException("Can't happen", ioe);
            }
        }
        return mems;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link MemoriesRecord}
     * with the supplied key values.
     */
    public static Key<MemoriesRecord> getKey (MsoyItemType itemType, int itemId)
    {
        return newKey(_R, itemType, itemId);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(ITEM_TYPE, ITEM_ID); }
    // AUTO-GENERATED: METHODS END
}
