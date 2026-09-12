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
| Backend, optional | PremiumVanish or SuperVanish, DiscordSRV, LuckPerms, EssentialsX, Multiverse-Core; PluginUtils for first-join date sync |
| Proxy, optional | a MyWarp database for `/warp` |

## Building

The build needs **JDK 25** because the Paper and MCME-Base APIs are Java 25 bytecode, but it emits
Java 21 bytecode (see the comment in `pom.xml`).

1. MCME-Base is not on a public Maven repository. Build and install it first:

   ```bash
   git clone https://github.com/MCME/MCME-Base.git && cd MCME-Base && mvn install
   ```

2. Build Connect:

   ```bash
   mvn clean verify
   ```

   This runs the unit tests and produces the shaded `target/MCME-Connect-<version>.jar`.

   Maven prints an error line about `dependencies.dependency.systemPath` for
   `org.bukkit:craftbukkit:jar` while reading the published PluginUtils 1.9.0 POM. It is
   harmless: Maven marks that POM invalid, skips its transitive dependencies, and the build
   still succeeds.

`libs/` is an in-project Maven repository for the vendored `tbnbt` library, whose upstream source
was deleted; see `libs/README.md`.

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
   annotation at compile time, so the two must match.
2. Add a section to `CHANGELOG.md`.
3. Run `mvn clean verify`, tag the commit `vX.Y.Z`, and attach the shaded jar to a GitHub release.
