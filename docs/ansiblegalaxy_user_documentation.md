<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2020-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->

### Overview

[Ansible Galaxy](https://galaxy.ansible.com/) provides a way to install community collections and roles for Ansible.

Full documentation on installing `ansible-galaxy` can be found on [the Ansible Galaxy project website](https://docs.ansible.com/ansible/latest/galaxy/user_guide.html).

You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache packages from a remote Ansible Galaxy repository, like
[https://galaxy.ansible.com/](https://galaxy.ansible.com/). Then, you can make the `ansible-galaxy` client use your Nexus Repository Proxy 
instead of the remote repository.

### Installation

See [installing the plugin](../README.md#installing-the-plugin).

### Repository configuration
 
To proxy an Ansible Galaxy repository, you simply create a new 'ansiblegalaxy (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name' - e.g. `ansiblegalaxy-proxy`
- Define URL for 'Remote storage' - e.g. [https://galaxy.ansible.com/](https://galaxy.ansible.com/)
- Select a `Blob store` for `Storage`

### Configuring the `ansible-galaxy` client

You must set the Galaxy API endpoint/server when using the client, either by:
* provide the `-s API_SERVER` or `--server API_SERVER` argument for each installation command.
* configure `GALAXY_SERVER` configuration value or `ANSIBLE_GALAXY_SERVER` environment variable.

See the following resources for additional detail:
* [Ansible documentation on configuring the `ansible-galaxy` client](https://docs.ansible.com/ansible/latest/galaxy/user_guide.html#configuring-the-ansible-galaxy-client).
* [Ansible configuration documentation](https://docs.ansible.com/ansible/latest/reference_appendices/config.html#galaxy-server)).

### Usage

Using the `ansible-galaxy` client, you can now download packages from your NXRM Ansible Galaxy proxy:

```bash
ansible-galaxy collection install azure.azcollection -s http://localhost:8081/repository/ansible/
```

The command above tells ansible-galaxy to fetch (and install) packages from your NXRM Ansible Galaxy proxy. The NXRM Ansible Galaxy proxy will download any missing packages from the remote Ansible Galaxy repository, and cache the packages on the NXRM Ansible Galaxy proxy. The next time any client requests the same package from your NXRM Ansible Galaxy proxy, the already cached package will be returned to the client.

### Role installation support

A bug exists in the `ansible-galaxy` client where role artifacts are downloaded directly from github.com instead of honoring the Galaxy API's result indicating `download_url`. [A fix](https://github.com/ansible/ansible/pull/73114) has been opened in the Ansible repo and is pending merge/release into a future version of the product.

In the meantime, you can branch the above repo with the fix to resolve downloads through Nexus, or adjust your infrastructure accordingly to account for the issue.
