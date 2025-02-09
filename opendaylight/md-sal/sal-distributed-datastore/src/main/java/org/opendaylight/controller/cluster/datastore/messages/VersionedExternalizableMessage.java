/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.datastore.messages;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.controller.cluster.datastore.DataStoreVersions;
import org.opendaylight.controller.cluster.datastore.node.utils.stream.NormalizedNodeStreamVersion;

/**
 * Abstract base class for a versioned Externalizable message.
 *
 * @author Thomas Pantelis
 */
public abstract class VersionedExternalizableMessage implements Externalizable, SerializableMessage {
    private static final long serialVersionUID = 1L;

    private short version = DataStoreVersions.CURRENT_VERSION;

    public VersionedExternalizableMessage() {
    }

    public VersionedExternalizableMessage(final short version) {
        this.version = version <= DataStoreVersions.CURRENT_VERSION ? version : DataStoreVersions.CURRENT_VERSION;
    }

    public short getVersion() {
        return version;
    }

    protected final @NonNull NormalizedNodeStreamVersion getStreamVersion() {
        return version < DataStoreVersions.SODIUM_VERSION
                ? NormalizedNodeStreamVersion.LITHIUM : NormalizedNodeStreamVersion.SODIUM;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        version = in.readShort();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeShort(version);
    }

    @Override
    public final Object toSerializable() {
        if (getVersion() < DataStoreVersions.BORON_VERSION) {
            throw new UnsupportedOperationException("Versions prior to " + DataStoreVersions.BORON_VERSION
                    + " are not supported");
        }

        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [version=" + getVersion() + "]";
    }
}
