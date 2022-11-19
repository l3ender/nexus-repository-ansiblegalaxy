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
<!-- generated with nexus-format-archetype version 1.0.48 on Dec 27, 2020 -->
# Nexus Repository Ansible Galaxy Format

## Table Of Contents

* [Developing](#developing)
* [Using Ansible Galaxy with Nexus Repository Manager 3](#using-ansiblegalaxy-with-nexus-repository-manager-3)
* [Compatibility with Nexus Repository Manager 3 Versions](#compatibility-with-nexus-repository-manager-3-versions)
* [Features Implemented In This Plugin](#features-implemented-in-this-plugin)
   * [Supported Ansible Galaxy Commands](#supported-ansible-galaxy-commands)
* [Installing the plugin](#installing-the-plugin)
   * [Easiest Install](#easiest-install)
   * [Temporary Install](#temporary-install)
   * [(more) Permanent Install](#more-permanent-install)
   * [(most) Permament Install](#most-permanent-install)
* [The Fine Print](#the-fine-print)
* [Getting Help](#getting-help)
* [Integration Tests](#integration-tests)

## Developing

[View development documentation to get started](docs/developing.md)

## Using Ansible Galaxy With Nexus Repository Manager 3

[See instructions on how to get started!](docs/ansiblegalaxy_user_documentation.md)

## Compatibility with Nexus Repository Manager 3 Versions

The table below outlines what version of Nexus Repository the plugin was built against.

| Plugin Version | Nexus Repository Version |
|----------------|--------------------------|
| v0.1.0         | 3.29.1-01                |
| v0.2.0         | 3.29.1-01                |
| v0.2.1         | 3.31.0-01                |

If a new version of Nexus Repository is released and the plugin needs changes, a new release will be made, and this
table will be updated to indicate which version of Nexus Repository it will function against. This is done on a time 
available basis, as this is community supported. If you see a new version of Nexus Repository, go ahead and update the
plugin and send us a PR after testing it out!

All released versions can be found on [the releases page](https://github.com/l3ender/nexus-repository-ansiblegalaxy/releases).

## Features Implemented In This Plugin

| Feature | Implemented          |
|---------|----------------------|
| Proxy   | :heavy_check_mark:   |
| Hosted  |                      |
| Group   |                      |

### Supported `ansible-galaxy` Commands

#### Proxy

| Plugin Version               | Nexus Repository Version |
|------------------------------|--------------------------|
| `ansible-galaxy collection install`         | :heavy_check_mark:       |
| `ansible-galaxy role install`         | :heavy_check_mark: <sup>*</sup>     |

Be sure to [configure the `ansible-galaxy` client](docs/ansiblegalaxy_user_documentation.md#configuring-the-ansible-galaxy-client).

<sup>*</sup> See [role installation support](docs/ansiblegalaxy_user_documentation.md#role-installation-support).

## Installing the plugin

There are a range of options for installing the ansiblegalaxy plugin. You'll need to build it first, and then install the plugin with the options shown below.

### Easiest Install

To install the `ansiblegalaxy` plugin, follow these steps:

* Download the plugin from [the releases page](https://github.com/l3ender/nexus-repository-ansiblegalaxy/releases) or build from source as described on the [development documentation](docs/developing.md).
* Copy the `nexus-repository-ansiblegalaxy-*-bundle.kar` file from the downloaded location (or from `target` folder, if building from source) to the `deploy` folder for your Nexus Repository installation.

Once you've done this, go ahead and either restart Nexus Repo, or go ahead and start it if it wasn't running to begin with.

You should now see `ansiblegalaxy (proxy)` in the available Repository Recipes.

### Temporary Install

Installations can be done via the Karaf console and will be wiped out with every restart of Nexus Repository. This is a good installation path if you are just testing or doing development on the plugin.

* Enable Nexus Repo console: edit `<nexus_dir>/bin/nexus.vmoptions` and change `karaf.startLocalConsole`  to `true`.

  More details here: [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development+Overview)

* Run Nexus Repo console:
  ```bash
  # sudo su - nexus
  $ cd <nexus_dir>/bin
  $ ./nexus run
  > bundle:install file:///tmp/nexus-repository-ansiblegalaxy-*.jar
  > bundle:list
  ```
  (look for org.sonatype.nexus.plugins:nexus-repository-ansiblegalaxy ID, should be the last one)
  ```bash
  > bundle:start <org.sonatype.nexus.plugins:nexus-repository-ansiblegalaxy ID>
  ```

### (more) Permanent Install

For more permanent installs of the nexus-repository-ansiblegalaxy plugin, follow these instructions:

* Copy the bundle (nexus-repository-ansiblegalaxy-*.jar) into <nexus_dir>/deploy

This will cause the plugin to be loaded with each restart of Nexus Repository. As well, this folder is monitored by Nexus Repository and the plugin should load within 60 seconds of being copied there if Nexus Repository is running. You will still need to start the bundle using the karaf commands mentioned in the temporary install.

### (most) Permanent Install

If you are trying to use the ansiblegalaxy plugin permanently, it makes more sense to do the following:

* Copy the bundle into `<nexus_dir>/system/org/sonatype/nexus/plugins/nexus-repository-ansiblegalaxy/0.2.1/nexus-repository-ansiblegalaxy-0.2.1.jar`
* Make the following additions marked with + to `<nexus_dir>/system/org/sonatype/nexus/assemblies/nexus-core-feature/3.x.y/nexus-core-feature-3.x.y-features.xml`:

   ```
         <feature prerequisite="false" dependency="false">wrap</feature>
   +     <feature prerequisite="false" dependency="false">nexus-repository-ansiblegalaxy</feature>
   ```
   to the `<feature name="nexus-core-feature" description="org.sonatype.nexus.assemblies:nexus-core-feature" version="3.x.y.xy">` section below the last (above is an example, the exact last one may vary).
   
   And add the following as the last feature:
   ```
   + <feature name="nexus-repository-ansiblegalaxy" description="org.sonatype.nexus.plugins:nexus-repository-ansiblegalaxy" version="0.2.1">
   +     <details>org.sonatype.nexus.plugins:nexus-repository-ansiblegalaxy</details>
   +     <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-ansiblegalaxy/0.2.1</bundle>
   + </feature>
    </features>
   ```

This will cause the plugin to be loaded and started with each startup of Nexus Repository.

## The Fine Print

It is worth noting that this is **NOT SUPPORTED** by Sonatype, and is a contribution of ours to the open source community (read: you!)

Don't worry, using this community item does not "void your warranty". In a worst case scenario, you may be asked by the Sonatype Support team to remove the community item in order to determine the root cause of any issues.

Remember:

* Use this contribution at the risk tolerance that you have.
* Do NOT file Sonatype support tickets related to Ansible Galaxy support in regard to this plugin.
* DO file issues here on GitHub, so that the community can pitch in.

## Getting help

Looking to contribute to our code but need some help? There's a few ways to get information:

* Chat with us on [Gitter](https://gitter.im/sonatype/nexus-developers)
* Check out the [Nexus3](http://stackoverflow.com/questions/tagged/nexus3) tag on Stack Overflow
* Check out the [Nexus Repository User List](https://groups.google.com/a/glists.sonatype.com/forum/?hl=en#!forum/nexus-users)

## Integration Tests

There a still some rough edges around writing integration tests, which are noted below. Please report any problems you find.

The project has a "format" module, and an "IT" module. This allows the "format" module to be bundled up and used by the IT framework classes in the "it" module.

In this project, the sub module [nexus-repository-ansiblegalaxy](nexus-repository-ansiblegalaxy) is the "format" module, while the sub module[nexus-repository-ansiblegalaxy-it](nexus-repository-ansiblegalaxy-it) is the "it" module.

#### Debugging ITs

You can connect a remote debugger to port 5005 to debug Integration Tests. Just add the `-Dit.debug=true` argument when running ITs. For example:

```bash
mvn clean verify -Dit.debug=true
```

After the IT starts you would see the following in a terminal:

```bash
...
[INFO] --- maven-failsafe-plugin:2.18.1:integration-test (default) @ nexus-repository-...-it ---
...
-------------------------------------------------------
  T E S T S
-------------------------------------------------------
Running org.sonatype.nexus.plugins...
```

You can then attach a remote debugger to port 5005. Keep trying to attach the remote debugger until the connection succeeds.

After each IT runs, you have to reconnect the remote debugger.

You can run a single IT by adding the `-Dit.test=MyIntegrationTestToRunIT` property. The example below also skips running the unit tests.

```bash
mvn clean verify -Dit.debug=true -Dtest=skip -Dit.test=MyIntegrationTestToRunIT
```

When running ITs, the Nexus Repository Manager will write log output to the following file:

```bash
nexus-repository-ansiblegalaxy/nexus-repository-ansiblegalaxy-it/target/it-data/1/nexus3/log/nexus.log
```

With multiple ITs, the `1` in the path above will be incremented for each IT.
