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


[Ansible Galaxy](https://galaxy.ansible.com/) provides a way to install community collections and roles for Ansible.

Full documentation on installing `ansible-galaxy` can be found on [the Ansible Galaxy project website](https://docs.ansible.com/ansible/latest/galaxy/user_guide.html).


You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache packages from a remote Ansible Galaxy repository, like
[https://galaxy.ansible.com/](https://galaxy.ansible.com/). Then, you can make the `ansible-galaxy` client use your Nexus Repository Proxy 
instead of the remote repository.
 
To proxy an Ansible Galaxy repository, you simply create a new 'ansiblegalaxy (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name' - e.g. `ansiblegalaxy-proxy`
- Define URL for 'Remote storage' - e.g. [https://galaxy.ansible.com/](https://galaxy.ansible.com/)
- Select a `Blob store` for `Storage`

Using the `ansible-galaxy` client, you can now download packages from your NXRM AnsibleGalaxy proxy like so:

```bash
ansible-galaxy collection install azure.azcollection -s http://localhost:8081/repository/ansible/
```
    
The command above tells ansible-galaxy to fetch (and install) packages from your NXRM Ansible Galaxy proxy. The NXRM Ansible Galaxy proxy will download any missing packages from the remote Ansible Galaxy repository, and cache the packages on the NXRM Ansible Galaxy proxy. The next time any client requests the same package from your NXRM Ansible Galaxy proxy, the already cached package will be returned to the client.
