/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.remote.rpc.messages;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.controller.cluster.datastore.node.utils.stream.SerializationUtils;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -4211279498688989245L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "This field is not Serializable but this class "
            + "implements writeReplace to delegate serialization to a Proxy class and thus instances of this class "
            + "aren't serialized. FindBugs does not recognize this.")
    private final NormalizedNode<?, ?> resultNormalizedNode;

    public RpcResponse(final @Nullable NormalizedNode<?, ?> inputNormalizedNode) {
        resultNormalizedNode = inputNormalizedNode;
    }

    public @Nullable NormalizedNode<?, ?> getResultNormalizedNode() {
        return resultNormalizedNode;
    }

    private Object writeReplace() {
        return new Proxy(this);
    }

    private static class Proxy implements Externalizable {
        private static final long serialVersionUID = 1L;

        private RpcResponse rpcResponse;

        // checkstyle flags the public modifier as redundant which really doesn't make sense since it clearly isn't
        // redundant. It is explicitly needed for Java serialization to be able to create instances via reflection.
        @SuppressWarnings("checkstyle:RedundantModifier")
        public Proxy() {
        }

        Proxy(final RpcResponse rpcResponse) {
            this.rpcResponse = rpcResponse;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            SerializationUtils.writeNormalizedNode(out, rpcResponse.getResultNormalizedNode());
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException {
            rpcResponse = new RpcResponse(SerializationUtils.readNormalizedNode(in).orElse(null));
        }

        private Object readResolve() {
            return rpcResponse;
        }
    }
}
