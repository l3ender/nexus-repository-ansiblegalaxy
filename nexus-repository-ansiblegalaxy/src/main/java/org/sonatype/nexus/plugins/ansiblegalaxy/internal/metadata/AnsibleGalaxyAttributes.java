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

/**
 * Object for storing AnsibleGalaxy specific attributes.
 */
public final class AnsibleGalaxyAttributes
{
  private final String author;

  private final String module;

  private final String version;

  public AnsibleGalaxyAttributes(String author, String module, String version) {
    this.author = author;
    this.module = module;
    this.version = version;
  }

  public String getAuthor() {
    return author;
  }

  public String getModule() {
    return module;
  }

  public String getVersion() {
    return version;
  }
}
