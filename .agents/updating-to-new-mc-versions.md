# Updating to a New Minecraft Version

Process followed during the MC 26.1.x migration. Fabric-first; NeoForge follows the same pattern once Fabric is stable.

---

## Phase 1 — Inventory and version lookup

1. Identify the target MC version and find compatible dependency versions:
   - Fabric API: check [maven.fabricmc.net](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/)
   - Fabric Loader: check [fabricmc.net](https://fabricmc.net/use/installer/)
   - Loom plugin, ForgeConfigAPIPort, ModMenu, WTHIT, Terraform Wood API: look up each on their Maven/Modrinth/CurseForge page for the new MC version

2. Update all versions centrally in `gradle.properties`; update library coordinates only in `fabric/build.gradle`.

---

## Phase 2 — Compile-fix loop

```sh
./gradlew :fabric:compileJava
```

Repeat until clean. Common breakage categories:

| Category | What to check |
|---|---|
| Fabric API method signature change | Check the new API javadoc or source for the updated method name/parameters |
| Minecraft internal API change | Read the method signature in the merged jar with `javap -p` |
| Mixin `compatibilityLevel` mismatch | Must match the Java version in `build.gradle` (`JAVA_25` for this project) |
| Missing mapped name | Try `officialMojangMappings()` if a Parchment mapping is unavailable for the new MC version |

---

## Phase 3 — Runtime verification

Install the built jar and verify these in-game, in order:

1. **Block registration** — chopped log block appears and saves correctly.
2. **Particles** — breaking a chopped log shows wood particles (not pink/magenta).
3. **HUD** — chop indicator renders at the correct position and size.
4. **Crack animation** — mining a chopped log shows breaking cracks that conform to the log's current shape.

Particles and crack animation are the most likely to regress because they depend on Fabric API rendering mixins that change between versions.

---

## Phase 4 — Rendering regression triage

When a visual feature breaks, assume a Fabric API mixin has changed the call path. Do not guess — inspect:

```sh
# Find the relevant Fabric API module jar
ls ~/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/

# Extract and inspect the mixin responsible for the broken feature
cd /tmp/inspect
~/.jdk25/bin/jar xf <fabric-renderer-api-jar> "net/fabricmc/fabric/mixin/client/renderer/block/render/BlockFeatureRendererMixin.class"
~/.jdk25/bin/javap -p -c net/fabricmc/fabric/mixin/client/renderer/block/render/BlockFeatureRendererMixin
```

Key things to check in the bytecode:
- Which methods are called on your custom model class
- What arguments are passed (especially static sentinel values like `EMPTY`, `ZERO`, `AIR`)
- Whether the vanilla method body has been replaced (`@Inject` at `HEAD` with `ci.cancel()`) or only wrapped

Once the actual call path is understood, write the fix against the real behavior, not the assumed one.

---

## Checklist

- [ ] All version strings updated in `gradle.properties`
- [ ] `fabric/build.gradle` dependency coordinates updated
- [ ] `treechop.mixins.json` `compatibilityLevel` matches Java version
- [ ] `./gradlew :fabric:compileJava` is clean
- [ ] `./gradlew :fabric:jar` produces the correct artifact
- [ ] In-game: particles, HUD, crack animation all verified
