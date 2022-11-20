# Developing

## Table Of Contents
* [Requirements](#requirements)
* [Download](#download)
* [Building](#building)
* [Installing](#installing)
* [Testing](#testing)

## Requirements

* [Apache Maven 3.3.3+](https://maven.apache.org/install.html)
* [Java 8](https://adoptopenjdk.net)
* Network access to https://repository.sonatype.org/content/groups/sonatype-public-grid

Also, there is a good amount of information available at [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development).

You may also find it helpful to configure your IDE to use the [Sonatype Code style](https://github.com/sonatype/codestyle).

## Download

Pre-compiled plugin files can be found on the [releases page](https://github.com/l3ender/nexus-repository-ansiblegalaxy/releases).

## Building

To build the project and generate the bundle use Maven:
```bash
mvn clean package -PbuildKar
```

If everything checks out, the bundle for ansiblegalaxy should be available in the `target` folder.

### Build Docker image

To build a Nexus Docker image with this plugin bundled, run the following:

```bash
docker build -t nexus-repository-ansiblegalaxy .
```

The above will require the Docker build process to download Maven dependencies for each build. To simplify the build process during development, use the following instead:

```bash
mvn clean package -PbuildKar
docker build -f Dockerfile-dev -t nexus-repository-ansiblegalaxy .
```

### Run as a Docker container

After building the Docker image, you can run as follows:

```bash
docker run -d -p 8081:8081 --name nexus-repository-ansiblegalaxy nexus-repository-ansiblegalaxy
```

For further information like how to persist volumes check out [the GitHub repo for our official image](https://github.com/sonatype/docker-nexus3).

After allowing some time to spin up, the application will be available from your browser at http://localhost:8081.

To read the generated admin password for your first login to the web UI, you can use the command below against the running docker container:

```bash
docker exec -it nexus-repository-ansiblegalaxy cat /nexus-data/admin.password && echo
```

For simplicity, you should check `Enable anonymous access` in the prompts following your first login.

## Installing

See [installing the plugin](../README.md#installing-the-plugin).

## Testing

Configure `ansible-galaxy` to use your local Nexus server, such as:

```bash
ansible-galaxy collection install azure.azcollection -s http://localhost:8081/repository/ansible/
```

See [Ansible Galaxy Configuration documentation](docs/ansiblegalaxy_user_documentation.md) for more detail.

It may be helpful to install Ansible into a virtual environment. Full detail on doing so is outside the scope of this documentation, but the following command may be useful, which creates a local virtual environment and install Ansible:

```bash
python3 -m venv venv && . venv/bin/activate && pip3 install --upgrade pip && pip3 install wheel && pip3 install ansible
```
