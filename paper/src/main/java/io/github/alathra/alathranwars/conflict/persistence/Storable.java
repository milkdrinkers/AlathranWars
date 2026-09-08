package io.github.alathra.alathranwars.conflict.persistence;

import org.jooq.impl.TableRecordImpl;

public interface Storable<T extends TableRecordImpl<T>> {
    /**
     * Deserialize the object to its a jOOQ record representation.
     *
     * @return the deserialized object
     */
    T deserialize();
}
