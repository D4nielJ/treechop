# TreeChop — Agent Reference

TreeChop is a Minecraft mod that makes felling trees more satisfying: chopping a log progressively damages the whole tree until it topples. Published on CurseForge and Modrinth.

---

## Module layout

| Module | Purpose |
|---|---|
| `tuber/` | Standalone utility library (graph traversal, math). No MC dependency. |
| `shared/` | Platform-agnostic game logic, mixins, blocks, and entities. |
| `fabric/` | Fabric-specific wiring: events, model loading, client rendering, networking. |
| `neoforge/` | NeoForge equivalent of `fabric/`. |
| `tests/` | Integration test subprojects (fabric-tests, forge-tests). |

## Key dependencies (current)

See `gradle.properties` for canonical version values.

- **Minecraft** 26.1.2 — Java 25, mapped with Parchment
- **Fabric Loader** 0.19.2 / **Fabric API** 0.149.0+26.1.2
- **ForgeConfigAPIPort** 26.1.4 — cross-platform config
- **fabric-model-loading-api-v1** / **fabric-renderer-api-v1** — custom block model pipeline
- **tuber** — internal; published to `publishing/` local Maven repo

## Build

```sh
./gradlew :fabric:jar          # fast; produces fabric/build/libs/TreeChop-*.jar
./gradlew :fabric:compileJava  # compile only, fastest feedback loop
```

## Contributing / adding features

- Platform-agnostic code (new blocks, chop logic, config) goes in `shared/`.
- Fabric-specific glue (events, model plugins, mixins) goes in `fabric/`.
- New Fabric mixins must be registered in `fabric/src/main/resources/treechop.fabric.mixins.json`. Shared mixins go in `treechop.mixins.json`.
- All newly generated or modified code must follow **[.agents/golden-rules.md](.agents/golden-rules.md)**.

## Further reading

- [.agents/golden-rules.md](.agents/golden-rules.md) — mandatory coding rules for this project
- [.agents/debugging-tools.md](.agents/debugging-tools.md) — tools and commands for debugging
- [.agents/updating-to-new-mc-versions.md](.agents/updating-to-new-mc-versions.md) — MC version update playbook
