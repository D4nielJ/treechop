# Golden Rules

All newly generated or modified code must follow these rules.

---

## 1. Make the smallest change that solves the problem

Do not refactor, rename, add comments, or improve unrelated code. Touch only what is necessary.

## 2. Read before writing

Read the relevant source files and trace the call chain before editing anything. If the runtime behavior of a dependency is unclear, inspect it with bytecode tools (see [debugging-tools.md](debugging-tools.md)) rather than guessing from the source.

## 3. Preserve the shared / platform split

- Logic that is not platform-specific belongs in `shared/`.
- Fabric-specific code belongs in `fabric/`. The same applies to NeoForge.
- Never import Fabric or NeoForge APIs from `shared/`.

## 4. Register every mixin

Every new mixin class must be listed in the correct JSON before building:
- `shared/src/main/resources/treechop.mixins.json` — for shared mixins
- `fabric/src/main/resources/treechop.fabric.mixins.json` — for Fabric mixins

## 5. Verify with a build after every change

```sh
./gradlew :fabric:jar
```

Do not mark a task done until the build is clean.
