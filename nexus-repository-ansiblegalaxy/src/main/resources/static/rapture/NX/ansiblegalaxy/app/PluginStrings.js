/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2020-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/*global Ext, NX*/

/**
 * AnsibleGalaxy plugin strings.
 */
Ext.define('NX.ansiblegalaxy.app.PluginStrings', {
    '@aggregate_priority': 90,

    singleton: true,
    requires: [
        'NX.I18n'
    ],

    keys: {
        Repository_Facet_AnsibleGalaxyFacet_Title: 'AnsibleGalaxy Settings',
        SearchAnsibleGalaxy_Group: 'AnsibleGalaxy Repositories',
        SearchAnsibleGalaxy_License_FieldLabel: 'License',
        SearchAnsibleGalaxy_Text: 'AnsibleGalaxy',
        SearchAnsibleGalaxy_Description: 'Search for components in AnsibleGalaxy repositories',
    }
}, function (self) {
    NX.I18n.register(self);
});
