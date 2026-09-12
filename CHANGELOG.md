# Changelog

All notable changes to MCME-Connect are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [3.0.0] - 2026-09-13

Release for the Minecraft 26.2 network update. Runs on Paper 26.2 and a Velocity proxy from the
4.1.0 line on the network's Java 25 servers (the jar is Java 21 bytecode), and requires
MCME-Base 2.0.1-SNAPSHOT on the proxy and on every backend.

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
  `velocity-api 4.1.0-SNAPSHOT` and MCME-Base 2.0.1-SNAPSHOT, with JDK 25, emitting Java 21
  bytecode.
- The Velocity descriptor is generated from the `@Plugin` annotation at compile time (annotation
  processor path in the pom). It now declares the dependency on `mcme-base`, so Velocity always
  loads MCME-Base first instead of relying on directory order.
- Default database credentials are `changeme` placeholders instead of real-looking values.
- A malformed `reserved:` section or `announcements.yml` disables the tab list panel and logs the
  cause instead of aborting the rest of plugin start-up.

### Removed
- **BungeeCord/Waterfall support.** The Bungee proxy plugin, the packet-based tab view, `bungee.yml`
  and the BungeeCord dependencies are gone.
- The obsolete Bungee tab-view resources (`views.yml`, `playerItems.yml`, `headerFooter.yml`,
  `tabList.yml`). `views.yml` was still being packaged although nothing read it.

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
- `tbnbt 0.1.4`, whose upstream repository was deleted, is vendored in `libs/` as an in-project
  Maven repository. The jar is now tracked in git (it was matched by the `*.jar` ignore rule), so
  a fresh clone builds.
- Paper API pinned to a concrete 26.2 build. The open-ended `[26.2.build,)` range had started
  resolving to 26.3 pre-release builds.

### Upgrade notes
- Deploy MCME-Base 2.0.1-SNAPSHOT (built from the MCME-Base repository) before this version. The
  code also compiles against the released 2.0.0, but 2.0.1 carries a `YamlConfiguration`
  file-handle fix the proxy relies on.
- The proxy data directory stays `plugins/mcmeconnect/`; existing `config.yml` and
  `playerServers.yml` are picked up unchanged. Copy the `reserved:` section from the default
  `config.yml` to customise the panel; without it, built-in defaults apply.
- Grant `mcmeconnect.tabnews` to staff who manage announcements.

## [1.1.5] - 2020-04-27

Last release for Minecraft 1.13, published as tag `v1.1`. Earlier history lives in git.

[Unreleased]: https://github.com/MCME/MCME-Connect/compare/v3.0.0...HEAD
[3.0.0]: https://github.com/MCME/MCME-Connect/compare/v1.1...v3.0.0
[1.1.5]: https://github.com/MCME/MCME-Connect/releases/tag/v1.1
