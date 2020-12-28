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


[AnsibleGalaxy](https://add_URL_To_Format_Info_Here/) provides Add_Format_Description_Here.


Full documentation on installing `ansiblegalaxy` can be found on [the AnsibleGalaxy project website](https://add_Format_Install_Info_URL/).


You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache packages from a remote AnsibleGalaxy repository, like
[Add_Format_Repo_Name](https://add_format_repo_url/). Then, you can make the `ansiblegalaxy` client use your Nexus Repository Proxy 
instead of the remote repository.
 
To proxy a AnsibleGalaxy repository, you simply create a new 'ansiblegalaxy (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name' - e.g. `ansiblegalaxy-proxy`
- Define URL for 'Remote storage' - e.g. [https://add_format_repo_url/](https://add_format_repo_url/)
- Select a `Blob store` for `Storage`

Using the `ansiblegalaxy` client, you can now download packages from your NXRM AnsibleGalaxy proxy like so:

    $ add client command line example here
    
The command above tells ansiblegalaxy to fetch (and install) packages from your NXRM AnsibleGalaxy proxy. The NXRM AnsibleGalaxy proxy will 
download any missing packages from the remote AnsibleGalaxy repository, and cache the packages on the NXRM AnsibleGalaxy proxy.
The next time any client requests the same package from your NXRM AnsibleGalaxy proxy, the already cached package will
be returned to the client.
