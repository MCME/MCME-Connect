# Vendored dependencies (in-project Maven repository)

This directory is a small **file-based Maven repository** for dependencies that are not
reliably available from any public/remote repository. It is wired into the build via a
`<repository>` entry in the project `pom.xml`:

```xml
<repository>
    <id>in-project-libs</id>
    <url>file://${project.basedir}/libs</url>
</repository>
```

Maven resolves artifacts from here just like any other repo (standard
`groupId/artifactId/version/` layout), so a plain `mvn package` works on any clone or CI
runner with no manual `install:install-file` step.

## Contents

### `com.github.mryurihi:tbnbt:0.1.4`

Tiny NBT-parsing library, used **only** by `statistics/FirstJoinDateUpdater` to read the
`firstPlayed` timestamp out of a player's `.dat` file.

**Why it's vendored:** its upstream source (`github.com/mryurihi/tbnbt`) has been **deleted**,
so JitPack can no longer build it (requests now return HTTP 401) and it exists on no other
public repository. The jar here was recovered from the **shaded production `MCME-Connect.jar`**
— tbnbt is shaded unrelocated, so its classes sit at `com/github/mryurihi/tbnbt/**` and were
extracted verbatim (70 classes, Java 8 bytecode).

**Do not delete it** — without it the plugin cannot be built.

**Long-term:** consider replacing tbnbt with Paper's own NBT API (or a maintained NBT library)
so this vendored jar is no longer needed. It's a self-contained ~5-line change in
`FirstJoinDateUpdater`.
