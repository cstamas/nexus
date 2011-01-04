/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.ui.client.constants;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface RepositoryConstants extends ConstantsWithLookup {

    String headerName();
    String headerRepoType();
    String headerRepoPolicy();
    String headerStatusProxyMode();
    String headerStatusLocalStatus();
    String headerStatusRemoteStatus();

    String id();
    String name();
    String remoteStorageRemoteStorageUrl();
    String allowWrite();
    String browseable();
    String indexable();
    String advancedSettings();
    String defaultLocalStorageUrl();
    String overrideLocalStorageUrl();
    String notFoundCacheTTLBefore();
    String notFoundCacheTTLAfter();
    String artifactMaxAgeBefore();
    String artifactMaxAgeAfter();
    String metadataMaxAgeBefore();
    String metadataMaxAgeAfter();
    String remoteStorageAuthenticationSettings();
    String remoteStorageAuthenticationUsername();
    String remoteStorageAuthenticationPassword();
    String remoteStorageAuthenticationPrivateKey();
    String remoteStorageAuthenticationPassphrase();
    String remoteStorageAuthenticationNtlmHost();
    String remoteStorageAuthenticationNtlmDomain();
    String remoteStorageConnectionSettings();
    String remoteStorageConnectionSettingsUserAgentString();
    String remoteStorageConnectionSettingsQueryString();
    String remoteStorageConnectionSettingsConnectionTimeout();
    String remoteStorageConnectionSettingsRetrievalRetryCount();
    String remoteStorageHttpProxySettings();
    String remoteStorageHttpProxySettingsProxyHostname();
    String remoteStorageHttpProxySettingsProxyPort();
    String shadowOf();
    String format();
    String formatMaven1();
    String formatMaven2();
    String syncAtStartup();

    String repoTypeHosted();
    String repoTypeProxy();
    String repoTypeVirtual();

    String repoPolicyRelease();
    String repoPolicySnapshot();

    String save();
    String cancel();
    
}
