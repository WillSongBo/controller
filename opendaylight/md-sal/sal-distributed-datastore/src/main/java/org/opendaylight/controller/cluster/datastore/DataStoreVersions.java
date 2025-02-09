/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.datastore;

/**
 * Defines version numbers where ask-based protocol is concerned.
 *
 * @author Thomas Pantelis
 */
public final class DataStoreVersions {
    public static final short BASE_HELIUM_VERSION = 0;
    public static final short HELIUM_1_VERSION = 1;
    public static final short HELIUM_2_VERSION = 2;
    public static final short LITHIUM_VERSION = 3;
    public static final short BORON_VERSION = 5;
    public static final short FLUORINE_VERSION = 9;
    public static final short SODIUM_VERSION = 10;
    public static final short CURRENT_VERSION = SODIUM_VERSION;

    private DataStoreVersions() {

    }
}
