# Changelog

All notable changes to MCME-Connect are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [3.0.1] - 2026-09-21

Bug-fix release for the Velocity 4 / Paper 26.2 network. 3.0.0 was prepared but never tagged,
released or published, so this is the first MCME-Connect artifact published to
repo.mcmiddleearth.com.

### Fixed

- `/warp` and `/to` are no longer taken from MCME-Warps. The legacy MyWarp cross-server bridge was
  registered unconditionally, and Velocity replaces an existing alias without complaint, so Connect
  claimed both commands on every start and players were forwarded to their backend where EssentialsX
  answered. Registration is now gated on `myWarp.enabled` **and** the alias not already being owned.
  The `null` permission compounded it: `ConnectCommand.hasPermission` returns true when the
  permission is null, so the aliases were also stripped of their permission check, which is why
  `mcmewarps.cmd.warp` was never evaluated.
- `WarpHandler.handle` and `updateCache` tolerate a missing MyWarp connector instead of throwing, so
  `myWarp.enabled: false` is now actually a safe setting.
- The Multiverse `NoClassDefFoundError` flood on player arrival, roughly 120 warnings a minute for
  as long as anyone was online. Multiverse 5 renamed its packages, and the surrounding
  `catch (NullPointerException)` could never catch an `Error`. What turned one failure into a flood
  was `runAfterArrival` invoking the callback before `cancel()`, leaving the repeating task
  scheduled; it now cancels first and catches `Throwable`.

### Changed

- Multiverse dependency moved to `org.mvplugins.multiverse.core:multiverse-core:5.8.1`, and
  `Multiverse-Core` is declared as a softdepend, which it never was.
- PluginUtils resolves from `com.mcmiddleearth` on repo.mcmiddleearth.com rather than JitPack's
  `com.github.MCME`, so one coordinate describes one library.
- Jars are named `{PluginName}-{A.B.C}-{TYPE}-{commit}`. Two Connect jars have been in production
  that both declare version 2.0.1, one registering `/warp` and one not.

### Added

- `distributionManagement` and a gated CI deploy step, so releases publish to
  repo.mcmiddleearth.com. Previously CI built and cut a GitHub release but never ran `mvn deploy`.

## [3.0.0] - 2026-09-13

Release for the Minecraft 26.2 network update. Runs on Paper 26.2 and a Velocity proxy from the
4.1.0 line on the network's Java 25 servers (the jar is Java 21 bytecode), and requires
MCME-Base 2.0.1 on the proxy and on every backend.

### Added
- Tab list reserved panel on the proxy (slots 41-60): a staff-managed **Announcements** section
  and a rotating **Tips** section, configured under `reserved:` in `config.yml`.
- `/tabnews add|remove|list` manages announcements in game; they are stored in
  `announcements.yml`. Guarded by the new `mcmeconnect.tabnews` permission, deliberately separate
  from `mcmeconnect.tablist`.
- `restorestatsBasePath` config key. The path was previously hard-coded.
- JUnit 5 test suite for the tab list model, run by `mvn verify`.
- `CHANGELOG.md` and a README that describes the Velocity/Paper setup and the build.

### Changed
- **Migrated to Minecraft 26.2.** Compiles against `paper-api 26.2.build.123-stable`,
  `velocity-api 4.1.0-SNAPSHOT` and MCME-Base 2.0.1, with JDK 25, emitting Java 21
  bytecode.
- The Velocity descriptor is generated from the `@Plugin` annotation at compile time (annotation
  processor path in the pom). It now declares the dependency on `mcme-base`, so Velocity always
  loads MCME-Base first instead of relying on directory order.
- Default database credentials are `changeme` placeholders instead of real-looking values.
- PluginUtils dependency moved from 1.9.0 (unpublished, built against Paper 1.21.4 NMS) to
  2.0.2 from JitPack, built for Paper 26.2. `plugin.yml` now soft-depends on PluginUtils so it
  loads first when present.
- A malformed `reserved:` section or `announcements.yml` disables the tab list panel and logs the
  cause instead of aborting the rest of plugin start-up.

### Removed
- **BungeeCord/Waterfall support.** The Bungee proxy plugin, the packet-based tab view, `bungee.yml`
  and the BungeeCord dependencies are gone.
- The obsolete Bungee tab-view resources (`views.yml`, `playerItems.yml`, `headerFooter.yml`,
  `tabList.yml`). `views.yml` was still being packaged although nothing read it.
- Unused dependencies on SuperVanish, LuckPerms and SnakeYAML, and the dead CubeKrowd and
  Multiverse repository entries.

### Fixed
- SQL injection: queries that include player-supplied values in the statistics, restorestats and
  MyWarp code now use prepared statements.
- Plugin messages are validated: messages without a sender are rejected and `RESTART` requires
  `mcmeconnect.restart`.
- Thread safety in the connection, TPA, vanish and watchdog handlers (concurrent collections,
  atomic removal in `ConnectionHandler`), and a `RestartHandler` loop that could spin forever.
- Resource handling: result sets are closed in `StatisticDBConnector`; the vanish and warp files
  are written atomically via a temp file and move.
- Input validation: cap of 100 pending TPA requests, guarded config parsing, array bounds checks
  on plugin messages.
- Tab list: entries are bound to the tab list of the viewer they belong to, the reserved panel is
  re-asserted for every viewer when players join or leave, and a failure for one viewer no longer
  aborts the update for everyone else.

### Build
- `tbnbt 0.1.4`, whose upstream repository was deleted, is served from `repo.mcmiddleearth.com`
  (`releases`); the temporary in-project `libs/` repository is gone.
- Paper API pinned to a concrete 26.2 build. The open-ended `[26.2.build,)` range had started
  resolving to 26.3 pre-release builds.
- Dependencies resolve through the MCME Maven repository (`repo.mcmiddleearth.com`,
  Reposilite) with the upstream repositories as fallbacks. GitHub Actions builds and tests every
  push and turns a `v*` tag into a GitHub Release.

### Upgrade notes
- Deploy MCME-Base 2.0.1 (GitHub Release of MCME-Base, or `repo.mcmiddleearth.com`) before this
  version. The code also compiles against 2.0.0, but 2.0.1 carries a `YamlConfiguration`
  file-handle fix the proxy relies on.
- The proxy data directory stays `plugins/mcmeconnect/`; existing `config.yml` and
  `playerServers.yml` are picked up unchanged. Copy the `reserved:` section from the default
  `config.yml` to customise the panel; without it, built-in defaults apply.
- Grant `mcmeconnect.tabnews` to staff who manage announcements.

## [1.1.5] - 2020-04-27

Last release for Minecraft 1.13, published as tag `v1.1`. Earlier history lives in git.

[Unreleased]: https://github.com/MCME/MCME-Connect/compare/v3.0.1...HEAD
[3.0.1]: https://github.com/MCME/MCME-Connect/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/MCME/MCME-Connect/compare/v1.1...v3.0.0
[1.1.5]: https://github.com/MCME/MCME-Connect/releases/tag/v1.1
