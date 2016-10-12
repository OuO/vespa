// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.config.server.http.v2;

import com.google.inject.Inject;
import com.yahoo.config.provision.TenantName;
import com.yahoo.container.jdisc.HttpRequest;
import com.yahoo.container.jdisc.HttpResponse;
import com.yahoo.container.logging.AccessLog;
import com.yahoo.log.LogLevel;
import com.yahoo.vespa.config.server.ApplicationRepository;
import com.yahoo.vespa.config.server.tenant.Tenant;
import com.yahoo.vespa.config.server.tenant.Tenants;
import com.yahoo.vespa.config.server.http.ContentHandler;
import com.yahoo.vespa.config.server.http.NotFoundException;
import com.yahoo.vespa.config.server.http.SessionHandler;
import com.yahoo.vespa.config.server.http.Utils;
import com.yahoo.vespa.config.server.session.LocalSession;
import com.yahoo.vespa.config.server.session.LocalSessionRepo;

import java.util.concurrent.Executor;

/**
 * A handler that will return content or content status for files or directories
 * in the session's application package
 *
 * @author lulf
 * @since 5.1
 */
public class SessionContentHandler extends SessionHandler {
    private final Tenants tenants;
    private final ContentHandler contentHandler = new ContentHandler();

    @Inject
    public SessionContentHandler(Executor executor,
                                 AccessLog accessLog,
                                 Tenants tenants,
                                 ApplicationRepository applicationRepository) {
        super(executor, accessLog, applicationRepository);
        this.tenants = tenants;
    }

    @Override
    public HttpResponse handleGET(HttpRequest request) {
        TenantName tenantName = Utils.getTenantFromSessionRequest(request);
        log.log(LogLevel.DEBUG, "Found tenant '" + tenantName + "' in request");
        Tenant tenant = Utils.checkThatTenantExists(tenants, tenantName);
        LocalSession session = applicationRepository.getLocalSession(tenant, getSessionIdV2(request));
        return contentHandler.get(SessionContentRequestV2.create(request, session));
    }

    @Override
    public HttpResponse handlePUT(HttpRequest request) {
        TenantName tenantName = Utils.getTenantFromSessionRequest(request);
        log.log(LogLevel.DEBUG, "Found tenant '" + tenantName + "' in request");
        Tenant tenant = Utils.checkThatTenantExists(tenants, tenantName);
        LocalSession session = applicationRepository.getLocalSession(tenant, getSessionIdV2(request));
        return contentHandler.put(SessionContentRequestV2.create(request, session));
    }

    @Override
    public HttpResponse handleDELETE(HttpRequest request) {
        TenantName tenantName = Utils.getTenantFromSessionRequest(request);
        log.log(LogLevel.DEBUG, "Found tenant '" + tenantName + "' in request");
        Tenant tenant = Utils.checkThatTenantExists(tenants, tenantName);
        LocalSession session = applicationRepository.getLocalSession(tenant, getSessionIdV2(request));
        return contentHandler.delete(SessionContentRequestV2.create(request, session));
    }
}
