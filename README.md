# Galley Maven Artifact Transport API

Galley is a series of APIs and implementations mainly focused on pushing, retrieving, deleting, and listing both Apache Maven-style artifacts and raw files from various locations. It provides an SPI and default implementation of local caching for files downloaded via the transports, and more advanced things for Maven like SNAPSHOT and metadata handling.

Along with all this, Galley also provides an API for parsing and navigating Maven POM and artifact metadata files.

## File Transport and Caching

At its most basic, Galley provides a way to model sets of files using a base location and series of paths (`Location` and `Resource`, respectively), along with file transfers (via the `Transfer` class). Resource implementations accommodate the notion that a path in the set might exist on one of a series of base locations via the class `VirtualResource`. Along with the model, Galley provides an API for managing a set of transports that know how to access files on remote locations (`TransportManager`) and for managing access to those transports to conduct various types of file transfers (`TransferManager`). Additionally, Galley provides an SPI for caching local copies of the files (`CacheProvider`), with a filesystem-based default implementation. Galley's `Transport` is an SPI with existing implementations for both files/archives and HTTP locations. Locations can specify a timeout for files in the local cache, or even an alternative cache storage location (aside from directory calculated from global configuration).

## Maven Support

### Artifacts

Building on top of its file transport and caching features, Galley provides an API that understands how to work with Maven artifacts and metadata, including SNAPSHOT version handling. This API can resolve SNAPSHOT meta-versions to timestamped concrete versions, list the available versions for an artifact, and retrieve artifacts from virtualized resources that contain a path and a set of potential base locations. Of course, it can also retrieve, publish, delete, and cache local copies of artifacts (with cache timeouts as described above). Each location can specify capabilities that determine what actions can be performed (or indeed, what types of artifacts are allowed).

### POM and Metadata Parsing

Finally, Galley provides as a convenience an API for parsing Maven POMs and metadata files. It implements inheritance and mix-in behaviors (such as BOM import) using a document set with expressions translated into search XPaths that are applied - in the proper order - to the stack of documents representing the inheritance and mix-in hierarchy specified in the given POM.

This XPath-based approach supports all the same expressions that Maven itself supports (for model interpolation, not for plugin injection), but it also supports much more advanced searches. For instance, it's possible to retrieve the second exclude from the fourth dependency in the POM (`//dependency[4]/exclusions/exclusion[2]`).

## Galley Dependencies

To use Galley, you will need one ore more of the following:

### Galley Core (and Galley API)

Galley's core artifact depends on the Galley API artifact, which provides the essential interfaces you'll use. API is most useful for things that use Galley but aren't deploying it at runtime (they're meant to be included in something that does), in order to have a lightweight dependency to build against. The core artifact provides the default implementations for transport and transfer management, caching, and the like.

To use Galley core from a Maven build:

    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-core</artifactId>
      <version>${galleyVersion}</version>
    </dependency>

Of course, unless you substitute an actual version for `${galleyVersion}`, you'll need to declare this property in your POM as well.

If you're just writing a library that uses Galley without regard for how its transports, caching, etc. are configured (using it as a service), you can use:

    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-api</artifactId>
      <version>${galleyVersion}</version>
    </dependency>

### Galley Transports

If your project will be responsible for configuring and deploying Galley as part of an application (as opposed to using it as a service), you'll also need to add one or more transports to your project:

    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-transport-httpclient</artifactId>
      <version>${galleyVersion}</version>
    </dependency>
    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-transport-filearc</artifactId>
      <version>${galleyVersion}</version>
    </dependency>

### Galley Maven

If you intend to use the Maven artifact or metadata management APIs, or the POM / metadata parsing API, you'll need the Galley Maven artifact:

    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-maven</artifactId>
      <version>${galleyVersion}</version>
    </dependency>

### Galley Testing Harness

Finally, if you want to simplify bootstrapping unit tests that use Galley, you can include:

    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-test-harness-core</artifactId>
      <version>${galleyVersion}</version>
      <scope>test</scope>
    </dependency>

...which gives you access to `CoreFixture`. This class is a jUnit `ExternalResource` implementation, which means it can be used with the `@Rule` annotation for automatic test lifecycle bindings (setup / teardown). The only thing you have to do is call `CoreFixture.initMissingComponents()` somewhere in your test code before using it. This gives the fixture the opportunity to initialize any Galley components your test class hasn't overridden via the fixture's setter methods (or via class inheritance).

If you want to unit test code that uses the Galley Maven artifact, you can include:

    <dependency>
      <groupId>org.commonjava.maven.galley</groupId>
      <artifactId>galley-test-harness-core</artifactId>
      <version>${galleyVersion}</version>
      <scope>test</scope>
    </dependency>

...which gives you access to `GalleyMavenFixture`. This class works just the same way as `CoreFixture` above, in that it can be used as a jUnit `@Rule`, and that it requires you to call `GalleyMavenFixture.initMissingComponents()` before use, to ensure all components have been initialized.
