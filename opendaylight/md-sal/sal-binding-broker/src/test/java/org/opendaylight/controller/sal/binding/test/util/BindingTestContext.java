/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.sal.binding.test.util;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.compat.HeliumNotificationProviderServiceAdapter;
import org.opendaylight.controller.md.sal.binding.compat.HeliumRpcProviderRegistry;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMDataBrokerAdapter;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMMountPointServiceAdapter;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMNotificationServiceAdapter;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMRpcProviderServiceAdapter;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMRpcServiceAdapter;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationService;
import org.opendaylight.controller.md.sal.dom.api.DOMRpcProviderService;
import org.opendaylight.controller.md.sal.dom.api.DOMRpcService;
import org.opendaylight.controller.md.sal.dom.broker.impl.DOMNotificationRouter;
import org.opendaylight.controller.md.sal.dom.broker.impl.DOMRpcRouter;
import org.opendaylight.controller.md.sal.dom.broker.impl.SerializedDOMDataBroker;
import org.opendaylight.controller.md.sal.dom.broker.impl.mount.DOMMountPointServiceImpl;
import org.opendaylight.controller.md.sal.dom.store.impl.InMemoryDOMDataStore;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.impl.RootBindingAwareBroker;
import org.opendaylight.controller.sal.core.spi.data.DOMStore;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public class BindingTestContext implements AutoCloseable {
    private BindingToNormalizedNodeCodec codec;

    private RootBindingAwareBroker baBrokerImpl;

    private HeliumNotificationProviderServiceAdapter baNotifyImpl;


    private final ListeningExecutorService executor;

    private final boolean startWithSchema;

    private DOMMountPointService biMountImpl;

    private ImmutableMap<LogicalDatastoreType, DOMStore> newDatastores;

    private DOMDataBroker newDOMDataBroker;

    private final MockSchemaService mockSchemaService = new MockSchemaService();

    private DataBroker dataBroker;

    private RpcConsumerRegistry baConsumerRpc;

    private BindingDOMRpcProviderServiceAdapter baProviderRpc;
    private DOMRpcRouter domRouter;
    private org.opendaylight.mdsal.dom.broker.DOMRpcRouter delegateDomRouter;

    private NotificationPublishService publishService;

    private NotificationService listenService;

    private DOMNotificationPublishService domPublishService;

    private DOMNotificationService domListenService;

    private Set<YangModuleInfo> schemaModuleInfos;

    public DOMDataBroker getDomAsyncDataBroker() {
        return this.newDOMDataBroker;
    }

    public BindingToNormalizedNodeCodec getCodec() {
        return this.codec;
    }


    protected BindingTestContext(final ListeningExecutorService executor, final boolean startWithSchema) {
        this.executor = executor;
        this.startWithSchema = startWithSchema;
    }

    public void startDomDataBroker() {
    }

    public void startNewDataBroker() {
        checkState(this.executor != null, "Executor needs to be set");
        checkState(this.newDOMDataBroker != null, "DOM Data Broker must be set");
        this.dataBroker = new BindingDOMDataBrokerAdapter(this.newDOMDataBroker, this.codec);
    }

    public void startNewDomDataBroker() {
        checkState(this.executor != null, "Executor needs to be set");
        final InMemoryDOMDataStore operStore = new InMemoryDOMDataStore("OPER",
            MoreExecutors.newDirectExecutorService());
        final InMemoryDOMDataStore configStore = new InMemoryDOMDataStore("CFG",
            MoreExecutors.newDirectExecutorService());
        this.newDatastores = ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
                .put(LogicalDatastoreType.OPERATIONAL, operStore)
                .put(LogicalDatastoreType.CONFIGURATION, configStore)
                .build();

        this.newDOMDataBroker = new SerializedDOMDataBroker(this.newDatastores, this.executor);

        this.mockSchemaService.registerSchemaContextListener(configStore);
        this.mockSchemaService.registerSchemaContextListener(operStore);
    }

    public void startBindingDataBroker() {

    }

    public void startBindingBroker() {
        checkState(this.executor != null, "Executor needs to be set");
        checkState(this.baNotifyImpl != null, "Notification Service must be started");

        this.baConsumerRpc = new BindingDOMRpcServiceAdapter(getDomRpcInvoker(), this.codec);
        this.baProviderRpc = new BindingDOMRpcProviderServiceAdapter(getDomRpcRegistry(), this.codec);

        this.baBrokerImpl = new RootBindingAwareBroker("test");

        final MountPointService mountService = new BindingDOMMountPointServiceAdapter(this.biMountImpl, this.codec);
        this.baBrokerImpl.setMountService(mountService);
        this.baBrokerImpl.setRpcBroker(new HeliumRpcProviderRegistry(this.baConsumerRpc, this.baProviderRpc));
        this.baBrokerImpl.setNotificationBroker(this.baNotifyImpl);
        this.baBrokerImpl.start();
    }

    public void startForwarding() {

    }

    public void startBindingToDomMappingService() {
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry();
        final GeneratedClassLoadingStrategy loading = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
        this.codec = new BindingToNormalizedNodeCodec(loading,  codecRegistry);
        this.mockSchemaService.registerSchemaContextListener(this.codec);
    }

    private void updateYangSchema(final Set<YangModuleInfo> moduleInfos) {
        this.mockSchemaService.changeSchema(getContext(moduleInfos));
    }

    private static SchemaContext getContext(final Set<YangModuleInfo> moduleInfos) {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(moduleInfos);
        return ctx.tryToCreateSchemaContext().get();
    }

    public void start() {
        startNewDomDataBroker();

        startDomBroker();
        startDomMountPoint();
        startBindingToDomMappingService();
        startNewDataBroker();
        startBindingNotificationBroker();
        startBindingBroker();

        startForwarding();

        if (schemaModuleInfos != null) {
            updateYangSchema(schemaModuleInfos);
        } else if (this.startWithSchema) {
            loadYangSchemaFromClasspath();
        }
    }

    private void startDomMountPoint() {
        this.biMountImpl = new DOMMountPointServiceImpl();
    }

    private void startDomBroker() {
        checkState(this.executor != null);

        delegateDomRouter = org.opendaylight.mdsal.dom.broker.DOMRpcRouter.newInstance(mockSchemaService);
        this.domRouter = new DOMRpcRouter(delegateDomRouter.getRpcService(), delegateDomRouter.getRpcProviderService());
    }

    public void startBindingNotificationBroker() {
        checkState(this.executor != null);
        final DOMNotificationRouter router = DOMNotificationRouter.create(16);
        this.domPublishService = router;
        this.domListenService = router;
        this.publishService = new BindingDOMNotificationPublishServiceAdapter(this.codec, this.domPublishService);
        this.listenService = new BindingDOMNotificationServiceAdapter(this.codec, this.domListenService);
        this.baNotifyImpl = new HeliumNotificationProviderServiceAdapter(this.publishService,this.listenService);

    }

    public void loadYangSchemaFromClasspath() {
        updateYangSchema(BindingReflections.loadModuleInfos());
    }

    public RpcProviderRegistry getBindingRpcRegistry() {
        return this.baBrokerImpl.getRoot();
    }

    public DOMRpcProviderService getDomRpcRegistry() {
        return this.domRouter;
    }

    public DOMRpcService getDomRpcInvoker() {
        return this.domRouter;
    }

    public org.opendaylight.mdsal.dom.broker.DOMRpcRouter getDelegateDomRouter() {
        return delegateDomRouter;
    }

    @Override
    public void close() {

    }

    public MountPointService getBindingMountPointService() {
        return this.baBrokerImpl.getMountService();
    }

    public DOMMountPointService getDomMountProviderService() {
        return this.biMountImpl;
    }

    public DataBroker getDataBroker() {
        return this.dataBroker;
    }

    public void setSchemaModuleInfos(Set<YangModuleInfo> moduleInfos) {
        this.schemaModuleInfos = moduleInfos;
    }
}
