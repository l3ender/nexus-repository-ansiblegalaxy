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
package org.sonatype.nexus.plugins.ansiblegalaxy.internal;

import java.net.URL;

import javax.annotation.Nonnull;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.fixtures.RepositoryRuleAnsibleGalaxy;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;

import org.junit.Rule;

import static com.google.common.base.Preconditions.checkNotNull;

public class AnsibleGalaxyITSupport
    extends RepositoryITSupport
{
  @Rule
  public RepositoryRuleAnsibleGalaxy repos = new RepositoryRuleAnsibleGalaxy(() -> repositoryManager);

  @Override
  protected RepositoryRuleAnsibleGalaxy createRepositoryRule() {
    return new RepositoryRuleAnsibleGalaxy(() -> repositoryManager);
  }

  public AnsibleGalaxyITSupport() {
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/it-resources/ansiblegalaxy"));
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/test-classes/ansiblegalaxy"));
  }

  @Nonnull
  protected AnsibleGalaxyClient ansiblegalaxyClient(final Repository repository) throws Exception {
    checkNotNull(repository);
    return ansiblegalaxyClient(repositoryBaseUrl(repository));
  }

  protected AnsibleGalaxyClient ansiblegalaxyClient(final URL repositoryUrl) throws Exception {
    return new AnsibleGalaxyClient(clientBuilder(repositoryUrl).build(), clientContext(), repositoryUrl.toURI());
  }
}
