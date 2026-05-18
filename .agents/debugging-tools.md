# Debugging Tools

Tools and commands used during the MC 26.1.x migration to diagnose rendering and compilation issues.

---

## Fast compile / build loop

```sh
./gradlew :fabric:compileJava   # quickest signal on API breakage
./gradlew :fabric:jar           # full jar; required before in-game testing
```

Always build first; never assume the game reflects unbuilt code.

---

## Bytecode inspection

This was the single most important tool in this session. Fabric's own API mixins can silently alter the MC rendering pipeline in ways that don't match their source-level documentation. The only reliable way to verify what actually runs at runtime is bytecode inspection.

**JDK location**: `~/.jdk25/bin/`  
**Merged MC jar**: `~/.gradle/caches/fabric-loom/<mc_version>/minecraft-merged.jar`  
**Fabric API jars**: `~/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/<module>/`

### Workflow

```sh
# 1. Extract a specific class from a jar into a working directory
mkdir -p /tmp/inspect
cd /tmp/inspect
~/.jdk25/bin/jar xf <path/to/target.jar> "net/example/pkg/TargetClass.class"

# 2. Disassemble to bytecode
~/.jdk25/bin/javap -p -c net/example/pkg/TargetClass

# 3. (Optional) List all classes in a jar to find the right one
~/.jdk25/bin/jar tf <jar> | grep -i "keyword"
```

**What to look for in the bytecode:**
- `invokeinterface` / `invokevirtual` calls — which method is actually called and with what types
- `getstatic` — which static constants are passed as arguments (e.g. `BlockAndTintGetter.EMPTY`, `BlockPos.ZERO`)
- Lambda handles (`invokedynamic`) — what closures are passed as predicates/consumers

Example discovery from this session: `BlockFeatureRendererMixin.renderBreakingBlockModelSubmits` calls `model.emitQuads(emitter, BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, direction -> false)` — confirmed via bytecode, not visible in any published source.

---

## Source search

```sh
# Find all uses of a symbol across the project
grep -r "SymbolName" shared/ fabric/ --include="*.java" -l

# Find a method signature or annotation
grep -rn "@Mixin\|@Inject\|emitQuads" fabric/src --include="*.java"
```

---

## Jar content verification

After a build, confirm the expected classes are present:

```sh
~/.jdk25/bin/jar tf fabric/build/libs/TreeChop-*.jar | grep "MixinOrClass"
```

---

## Class field / method visibility

To check access modifiers (public/private/final) before writing a `@Shadow` or accessing a field:

```sh
~/.jdk25/bin/javap -p net/minecraft/client/renderer/LevelRenderer.class
```
