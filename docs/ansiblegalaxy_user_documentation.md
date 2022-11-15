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

# Table Of Contents

* [Overview](#overview)
* [Installation](#installation)
* [Repository configuration](#repository-configuration)
* [Configuring the client](#configuring-the-ansible-galaxy-client)
* [Usage](#usage)
* [Browsing Repository Packages](#browsing-repository-packages)
* [Publishing Packages](#publishing-ackages)
* [Role installation support](#role-installation-support)

### Overview

[Ansible Galaxy](https://galaxy.ansible.com/) provides a way to install community collections and roles for Ansible.

Full documentation on installing `ansible-galaxy` can be found on [the Ansible Galaxy project website](https://docs.ansible.com/ansible/latest/galaxy/user_guide.html).

You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache packages from a remote Ansible Galaxy repository, like
[https://galaxy.ansible.com/](https://galaxy.ansible.com/).     
You can also create a hosted repository to handle your own collections.    
Then, you can make the `ansible-galaxy` client use your Nexus Repository 
instead of the remote repository.

### Installation

See [installing the plugin](../README.md#installing-the-plugin).

### Repository configuration
 
To create an Ansible Galaxy repository, you simply create a new 'ansiblegalaxy (proxy)' or  'ansiblegalaxy (hosted)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name' - e.g. `ansiblegalaxy-proxy`
- Define URL for 'Remote storage' - e.g. [https://galaxy.ansible.com/](https://galaxy.ansible.com/) => only for proxy
- Select a `Blob store` for `Storage`

### Configuring the `ansible-galaxy` client

You must set the Galaxy API endpoint/server when using the client, either by:
* provide the `-s API_SERVER` or `--server API_SERVER` argument for each installation command.
* configure `GALAXY_SERVER` configuration value or `ANSIBLE_GALAXY_SERVER` environment variable.

If you have multiple hosted repositories or one hosted and one proxy, you can set multiples repositories in your config file `ansible.cfg`
Then the first one owning the desired package will be used

example:    
```cfg
[galaxy]
server_list = my_org_hub,proxy

[galaxy_server.my_org_hub]
url=http://127.0.0.1:8081/repository/ansible-hosted/

[galaxy_server.proxy]
url=http://127.0.0.1:8081/repository/ansible-proxy/

```

See the following resources for additional detail:
* [Ansible documentation on configuring the `ansible-galaxy` client](https://docs.ansible.com/ansible/latest/galaxy/user_guide.html#configuring-the-ansible-galaxy-client).
* [Ansible configuration documentation](https://docs.ansible.com/ansible/latest/reference_appendices/config.html#galaxy-server)).

### Usage

Using the `ansible-galaxy` client, you can now download packages from your NXRM Ansible Galaxy:

```bash
ansible-galaxy collection install azure.azcollection -s http://localhost:8081/repository/ansible/
```

The command above tells ansible-galaxy to fetch (and install) packages from your NXRM Ansible Galaxy proxy. The NXRM Ansible Galaxy proxy will download any missing packages from the remote Ansible Galaxy repository, and cache the packages and the metadata on the NXRM Ansible Galaxy proxy.    
The next time any client requests the same package from your NXRM Ansible Galaxy proxy, the already cached package will be returned to the client. No other call are made to the galaxy proxied instance


### Browsing Repository Packages

You can browse Composer repositories in the user interface inspecting the components and assets and their details, as
described in [Browsing Repositories and Repository Groups](https://help.sonatype.com/display/NXRM3/Browsing+Repositories+and+Repository+Groups).

### Publishing Packages

If you are authoring your own packages and want to distribute them to other users in your organization, you have
to upload them to a hosted repository on the repository manager. The consumers can then download it via the
repository.

An AnsibleGalaxy package should consist of a tar.gz archive of the sources containing a `MANIFEST.json` file.
The file is automatically generated when you [build a collection.](https://docs.ansible.com/ansible/latest/dev_guide/developing_collections_distributing.html#building-your-collection-tarball)
This file is generated from the file `galaxy.yml` which is in your source code in your collection skeleton.

With this information known, the package can be uploaded to your hosted repository (replacing the credentials,
source filename, and <namespace>, <collectionname>, and <version> path segments to match your particular distributable):

`curl -v --user 'user:pass' --upload-file example.tar.gz http://localhost:8081/repository/ansible-hosted/<namespace>-<collectionname>-<version>.tar.gz`

*Note that the path segments should be the same as your collection manifest.

### Role installation support

Due to [a bug](https://github.com/ansible/ansible/issues/73103) in Ansible prior to version 2.12, `ansible-galaxy` would download roles directly from Github.com, even if the `download_url` for the role specified a different location. This resulted in artifacts not being downloaded through Nexus when using this repository format.

[The fix](https://github.com/ansible/ansible/pull/73114) was merged and released in [Ansible 2.12.0](https://github.com/ansible/ansible/blob/e312665990d353ed2ab8610237de3da52da58560/changelogs/CHANGELOG-v2.12.rst#v2120). Please be sure to use version 2.12.0 (or later) to avoid the issue, or adjust your infrastructure accordingly.
