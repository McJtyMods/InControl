# InControl for Fabric 26.2

Fabric port of InControl 11.0.0. Controls mob spawning and despawning, loot, experience, player effects, block interactions, phases, areas, and custom world events using the existing InControl JSON rules and commands.

Requires Minecraft **26.2**, **Java 25**, **Fabric Loader 0.19.3 or newer**, and **Fabric API 0.155.2+26.2 or newer for 26.2**. Team Reborn Energy 5.0.0 is included in the mod jar for energy conditions.

Install the built mod jar and Fabric API in `mods/`. Rules remain in `config/incontrol/` with the same filenames and syntax. Server settings remain in `<world>/serverconfig/incontrol-server.toml`; `defaultconfigs/incontrol-server.toml` supplies defaults for new worlds. `/incontrol reload` reloads both rules and server settings. Fabric inventory and energy conditions use Fabric Transfer API and Team Reborn Energy providers. Use Fabric's `c:` convention tags in rules that previously referenced Forge tags.

[InControl documentation](https://mcjty.eu/docs/mods/control-mods)

## Build

Run with a Java 25 JDK:

```sh
./gradlew build
```

The installable jar is `build/libs/incontrol-26.2-11.0.0-fabric.jar`; the `-sources.jar` is for development.

Minecraft 26.2 uses unobfuscated names, so the installable artifact is produced by `jar`; no remapping task is needed. Gradle 9.5.1 and Loom 1.17.17 are configured in this project.

Existing world phases, numbers, and day counters migrate from `data/InControlData.dat` to `data/incontrol/data.dat` on first load.

## Publishing

The build uses a local `gradletools.gradle`, shared by the build and publishing configuration for Fabric 26.2. Release settings (`version`, `curse_type`, `projectId`, `projectSlug`, `modrinthId`, and `github_project`) are in `gradle.properties`. The `modpublisher(...)` helper configures ModPublisher to upload the Fabric jar for Minecraft 26.2, with Fabric API as a required dependency and the existing optional dependency declarations.

Provide `curseforge_key` and `modrinth_token` in your user Gradle properties, or set `CURSEFORGE_TOKEN` (also accepts `CURSE_TOKEN`) and `MODRINTH_TOKEN`. Run `./gradlew publishMod` for both configured platforms, or `./gradlew publishCurseforge` / `./gradlew publishModrinth` individually. Platforms without credentials are skipped.

The changelog uses the first section of `changelog.txt`, including GitHub issue links. Set `CHANGELOG` to override it (`none` uses the file). Add `-PpublishDebug=true` to preview the publishing payload without uploading; the plugin still makes read requests to resolve platform metadata.

## IntelliJ IDEA

Use Java 25 for both the project SDK and Gradle JVM, then reload the Gradle project. Loom generates the **Fabric Client** and **Fabric Server** application configurations, which support IntelliJ debugging. To generate them explicitly, run `./gradlew ideaSyncTask`.

Select **Fabric Client** or **Fabric Server** in the run selector. Remove any old `InControl1.21_fabric: Client` / `Server` configurations left from NeoForge. A configuration referencing `ideBeforeRun` or `cpw.mods.bootstraplauncher.BootstrapLauncher` belongs to the old NeoForge setup and must be replaced with a Fabric configuration.

## Optional integrations

Serene Seasons, Lost Cities, CustomNPCs, and KubeJS have optional API adapters, activated only when their mod IDs are present. They require compatible **Fabric** versions of those mods exposing the corresponding APIs. These integrations have not been tested with external mods. NeoForge mod jars cannot be used on Fabric. The Game Stages, Baubles, and Enigma integrations remain the placeholders present in the original source; they do not provide working integrations.

Spawn finalization hooks cover vanilla mobs, including subclass overrides, and apply actions once through each inheritance chain. Modded mobs calling a vanilla superclass finalizer receive the hook there; mods that bypass all vanilla finalizers require their own integration. Placement rules cover player block-item placement, including rejected placement rollback. Interactions or spawning implemented entirely through another mod's custom code may require additional hooks.
