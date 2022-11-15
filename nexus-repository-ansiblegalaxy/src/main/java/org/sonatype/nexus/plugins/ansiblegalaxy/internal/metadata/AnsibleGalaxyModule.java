/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata;


public class AnsibleGalaxyModule {
    private String version;
    private String href;
    private String download_url;
    private AnsibleGalaxyModuleCollection collection;
    private AnsibleGalaxyModuleArtifact artifact;
    private AnsibleGalaxyModuleNamespace namespace;
    private AnsibleGalaxyModuleMetadata metadata;

    public String getVersion() {
        return version;
    }

    public AnsibleGalaxyModule setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getHref() {
        return href;
    }

    public AnsibleGalaxyModule setHref(String href) {
        this.href = href;
        return this;
    }

    public String getDownload_url() {
        return download_url;
    }

    public AnsibleGalaxyModule setDownload_url(String download_url) {
        this.download_url = download_url;
        return this;
    }

    public AnsibleGalaxyModuleCollection getCollection() {
        return collection;
    }

    public AnsibleGalaxyModule setCollection(AnsibleGalaxyModuleCollection collection) {
        this.collection = collection;
        return this;
    }

    public AnsibleGalaxyModuleArtifact getArtifact() {
        return artifact;
    }

    public AnsibleGalaxyModule setArtifact(AnsibleGalaxyModuleArtifact artifact) {
        this.artifact = artifact;
        return this;
    }

    public AnsibleGalaxyModuleNamespace getNamespace() {
        return namespace;
    }

    public AnsibleGalaxyModule setNamespace(AnsibleGalaxyModuleNamespace namespace) {
        this.namespace = namespace;
        return this;
    }

    public AnsibleGalaxyModuleMetadata getMetadata() {
        return metadata;
    }

    public AnsibleGalaxyModule setMetadata(AnsibleGalaxyModuleMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public static class AnsibleGalaxyModuleNamespace {

        private String name;

        public String getName() {
            return name;
        }

        public AnsibleGalaxyModuleNamespace setName(String name) {
            this.name = name;
            return this;
        }
    }

    public static class AnsibleGalaxyModuleCollection {

        private String name;

        public String getName() {
            return name;
        }

        public AnsibleGalaxyModuleCollection setName(String name) {
            this.name = name;
            return this;
        }
    }

    public static class AnsibleGalaxyModuleArtifact {

        private String sha256;

        public String getSha256() {
            return sha256;
        }

        public AnsibleGalaxyModuleArtifact setSha256(String sha256) {
            this.sha256 = sha256;
            return this;
        }
    }

    public static class AnsibleGalaxyModuleMetadata {

        private Object dependencies;

        public Object getDependencies() {
            return dependencies;
        }

        public AnsibleGalaxyModuleMetadata setDependencies(Object dependencies) {
            this.dependencies = dependencies;
            return this;
        }
    }
}