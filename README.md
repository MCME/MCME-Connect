# MCME-Connect

Cross-server connectivity plugin for the MCME (Minecraft Middle-Earth) network. One jar serves
two roles and is deployed unchanged to the proxy and to every backend:

- **Velocity proxy plugin** (`com.mcmiddleearth.connect.proxy.velocity.ConnectVelocityPlugin`):
  network-wide teleports (`/tp`, `/tphere`, `/tpa`, `/tpahere`, `/tpaccept`, `/tpdeny`,
  `/tpacancel`), server switching (`/mvtp`, `/warp`, `/theme`, `/survival` and one `/<server>`
  command per registered server), vanish awareness, chat and Discord message relaying, restart
  orchestration (`/reboot`, `/stop`), the server watchdog, MyWarp lookups, `/restorestats`, and
  the tab list reserved panel with `/tabnews`.
- **Paper backend plugin** (`com.mcmiddleearth.connect.ConnectPlugin`): the backend half of the
  plugin-message protocol, statistics sync to MySQL, first-join date sync, scheduled restarts,
  `/restart` and `/stop`.

BungeeCord/Waterfall support was removed in 3.0.0. The network runs on Velocity only.

## Requirements

| Component | Version |
|-----------|---------|
| Paper | 26.2 (compiled against `paper-api 26.2.build.123-stable`) |
| Velocity | 4.1.0 line (compiled against `velocity-api 4.1.0-SNAPSHOT`) |
| Java at runtime | 25, as run on the network; the jar itself is Java 21 bytecode |
| MCME-Base | 2.0.1-SNAPSHOT or newer, on the proxy and on every backend |
| Backend, optional | PremiumVanish, DiscordSRV, Multiverse-Core, EssentialsX (AFK status in the tab list); PluginUtils 2.0.2 or newer for first-join date sync |
| Proxy, optional | a MyWarp database for `/warp` |

## Building

The build needs **JDK 25** because the Paper and MCME-Base APIs are Java 25 bytecode, but it emits
Java 21 bytecode (see the comment in `pom.xml`).

```bash
mvn clean verify
```

This runs the unit tests and produces the shaded `target/MCME-Connect-<version>.jar`.

Dependencies, MCME-Base included, resolve from the MCME Maven repository at
`https://repo.mcmiddleearth.com` (deployment in the private [q220/maven-repo](https://github.com/q220/maven-repo)):
`releases` and `snapshots` hold the MCME libraries, `mirror` caches every upstream repository the
build uses, and the upstreams stay in the pom as fallbacks. Nothing needs a local `mvn install`.

`libs/` is an in-project Maven repository for the vendored `tbnbt` library, whose upstream source
was deleted; see `libs/README.md`.

GitHub Actions builds and tests every push and pull request and uploads the jar as a workflow
artifact.

## Configuration

`config.yml` holds a proxy section and a backend section; each side reads its own keys. Points
worth knowing:

- Database credentials ship as `changeme` placeholders. Set them before enabling `myWarp` on the
  proxy or `syncStatistic` on a backend.
- `restorestatsBasePath` enables `/restorestats`; leave it empty or unset to disable the feature.
- `reserved:` configures the tab list reserved panel (slots 41-60): an **Announcements** section
  managed in game with `/tabnews add|remove|list`, and a **Tips** section that rotates through the
  configured entries. Announcements are stored in `announcements.yml` in the proxy plugin
  directory, `plugins/mcmeconnect/`. `/tabnews` requires `mcmeconnect.tabnews`.
- Scheduled restarts and `/restart` rely on a start script that restarts the server while
  `plugins/MCME-Connect/restart.nfo` exists; an example loop is in the comments of `config.yml`.

## Releasing

1. Set the new version in `pom.xml` **and** in the `@Plugin` annotation of
   `ConnectVelocityPlugin`. The Velocity descriptor (`velocity-plugin.json`) is generated from that
   annotation at compile time, so the two must match. Update `minecraft.version` in the pom if the
   target Minecraft version changed.
2. Add a section to `CHANGELOG.md` headed `## [X.Y.Z] - YYYY-MM-DD`.
3. Push a tag `vX.Y.Z` on the release commit. GitHub Actions checks the tag against the pom
   version, builds and tests, and creates the GitHub Release "version X.Y.Z for mc <minecraft.version>"
   with the shaded jar, its SHA-256 and the CHANGELOG section as notes.
