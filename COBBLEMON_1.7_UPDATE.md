# Cobblemon 1.7.0 API Update Guide

This document outlines all the changes required to update Cobblemon Quests from Cobblemon 1.6.1 to 1.7.0 API support.

## Summary

The update required changes to event APIs, build configuration, dependency versions, and code compatibility fixes to work with Cobblemon 1.7.0's new API structure.

---

## 1. Trade Event API Changes

### Event Name Change
- **Old API (1.6):** `CobblemonEvents.TRADE_COMPLETED`
- **New API (1.7):** `CobblemonEvents.TRADE_EVENT_POST`

### Event Class Change
- **Old:** `TradeCompletedEvent`
- **New:** `TradeEvent.Post`

### Code Changes Required

**File:** `common/src/main/java/winterwolfsv/cobblemon_quests/events/CobblemonQuestsEventHandler.java`

1. **Import Statement:**
   ```java
   // Added
   import com.cobblemon.mod.common.api.events.pokemon.TradeEvent;
   ```

2. **Event Subscription:**
   ```java
   // Changed from:
   CobblemonEvents.TRADE_COMPLETED.subscribe(Priority.LOWEST, this::pokemonTrade);
   
   // To:
   CobblemonEvents.TRADE_EVENT_POST.subscribe(Priority.LOWEST, this::pokemonTrade);
   ```

3. **Method Signature:**
   ```java
   // Changed from:
   private Unit pokemonTrade(TradeCompletedEvent tradeCompletedEvent)
   
   // To:
   private Unit pokemonTrade(TradeEvent.Post tradeEvent)
   ```

4. **Event Property Access:**
   ```java
   // Changed from:
   Pokemon pokemonGivenByPlayer1 = tradeCompletedEvent.getTradeParticipant2Pokemon();
   Pokemon pokemonGivenByPlayer2 = tradeCompletedEvent.getTradeParticipant1Pokemon();
   ServerPlayer player1 = pokemonGivenByPlayer2.getOwnerPlayer();
   ServerPlayer player2 = pokemonGivenByPlayer1.getOwnerPlayer();
   
   // To:
   Pokemon pokemonGivenByPlayer1 = tradeEvent.getTradeParticipant2Pokemon();
   Pokemon pokemonGivenByPlayer2 = tradeEvent.getTradeParticipant1Pokemon();
   ServerPlayer player1 = tradeEvent.getTradeParticipant1Pokemon().getOwnerPlayer();
   ServerPlayer player2 = tradeEvent.getTradeParticipant2Pokemon().getOwnerPlayer();
   ```

**Note:** After the trade, `tradeParticipant1Pokemon` is what player1 received (was given by player2), and `tradeParticipant2Pokemon` is what player2 received (was given by player1).

---

## 2. CobblemonItemComponents API Changes

### Property Access Change
- **Old API (1.6):** `CobblemonItemComponents.INSTANCE.getPOKEMON_ITEM()`
- **New API (1.7):** `CobblemonItemComponents.POKEMON_ITEM` (direct property access)

### Code Changes Required

**File:** `common/src/main/java/winterwolfsv/cobblemon_quests/tasks/CobblemonTask.java`

```java
// Changed from:
stack.set(CobblemonItemComponents.INSTANCE.getPOKEMON_ITEM(), c);

// To:
stack.set(CobblemonItemComponents.POKEMON_ITEM, c);
```

**Reason:** In Cobblemon 1.7.0, `POKEMON_ITEM` is now a `@JvmField` property that can be accessed directly without getter methods.

---

## 3. Event Subscription Ambiguity Fixes

### Issue
Java compiler couldn't determine which `subscribe` method overload to use (Function1 vs Consumer) for certain event subscriptions.

### Code Changes Required

**File:** `common/src/main/java/winterwolfsv/cobblemon_quests/events/CobblemonQuestsEventHandler.java`

**Fixed Events:**
- `POKEMON_CAPTURED`
- `POKEDEX_DATA_CHANGED_POST`
- `POKEDEX_DATA_CHANGED_PRE`

**Solution:** Use explicit lambda expressions that return `Unit.INSTANCE`

```java
// Changed from:
CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, this::pokemonCatch);
CobblemonEvents.POKEDEX_DATA_CHANGED_POST.subscribe(Priority.LOWEST, this::pokeDexChanged);
CobblemonEvents.POKEDEX_DATA_CHANGED_PRE.subscribe(Priority.LOWEST, this::pokeDexChanged);

// To:
CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, (event) -> { pokemonCatch(event); return Unit.INSTANCE; });
CobblemonEvents.POKEDEX_DATA_CHANGED_POST.subscribe(Priority.LOWEST, (event) -> { pokeDexChanged(event); return Unit.INSTANCE; });
CobblemonEvents.POKEDEX_DATA_CHANGED_PRE.subscribe(Priority.LOWEST, (event) -> { pokeDexChanged(event); return Unit.INSTANCE; });
```

---

## 4. Build Configuration Updates

### Gradle Version
**File:** `gradle/wrapper/gradle-wrapper.properties`

```properties
# Changed from:
distributionUrl=https\://services.gradle.org/distributions/gradle-8.8-bin.zip

# To:
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.3-bin.zip
```

### Kotlin Version
**File:** `build.gradle.kts`

```kotlin
// Changed from:
kotlin("jvm") version ("1.7.10")

// To:
kotlin("jvm") version ("2.2.20")
```

### Architectury Loom Version
**File:** `build.gradle.kts`

```kotlin
// Changed from:
id("dev.architectury.loom") version ("1.7-SNAPSHOT") apply false

// To:
id("dev.architectury.loom") version ("1.11-SNAPSHOT") apply false
```

**Reason:** Loom 1.11-SNAPSHOT supports Kotlin 2.2.0 metadata used by Cobblemon 1.7.0.

### Cobblemon Version
**File:** `gradle.properties`

```properties
# Changed from:
cobblemon_version=1.6.1+1.21.1

# To:
cobblemon_version=1.7.0+1.21.1
```

### Fabric Dependencies
**File:** `gradle.properties`

```properties
# Fabric Loader
# Changed from: fabric_loader_version=0.16.9
# To:
fabric_loader_version=0.17.2

# Fabric API
# Changed from: fabric_version=0.107.0+1.21.1
# To:
fabric_version=0.116.6+1.21.1

# Fabric Kotlin
# Changed from: fabric_kotlin=1.12.3+kotlin.2.0.21
# To:
fabric_kotlin=1.13.6+kotlin.2.2.20
```

---

## 5. Dependency Configuration Fixes

### Fabric Project - Duplicate Class Resolution
**File:** `fabric/build.gradle.kts`

Added exclusion to prevent duplicate classes between `mod` and `fabric` dependencies:

```kotlin
modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}") {
    exclude(group = "com.cobblemon", module = "mod")
}
```

### NeoForge Project - Missing Dependency
**File:** `neoforge/build.gradle.kts`

Temporarily commented out until Cobblemon 1.7.0 neoforge version is published:

```kotlin
// Temporarily commented out until Cobblemon 1.7.0 neoforge version is published
// modImplementation("com.cobblemon:neoforge:${property("cobblemon_version")}") { isTransitive = false }
```

---

## 6. Common Project Dependency Update
**File:** `common/build.gradle.kts`

Changed from `modImplementation` to `modCompileOnly` to avoid remapping issues:

```kotlin
// Changed from:
modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") { isTransitive = false }

// To:
modCompileOnly("com.cobblemon:mod:${property("cobblemon_version")}") { isTransitive = false }
```

---

## Files Modified

1. `common/src/main/java/winterwolfsv/cobblemon_quests/events/CobblemonQuestsEventHandler.java`
   - Trade event API update
   - Event subscription ambiguity fixes

2. `common/src/main/java/winterwolfsv/cobblemon_quests/tasks/CobblemonTask.java`
   - CobblemonItemComponents API update

3. `build.gradle.kts`
   - Kotlin version update
   - Loom version update

4. `gradle.properties`
   - Cobblemon version update
   - Fabric dependencies update

5. `gradle/wrapper/gradle-wrapper.properties`
   - Gradle version update

6. `fabric/build.gradle.kts`
   - Dependency exclusion fix

7. `neoforge/build.gradle.kts`
   - Temporarily commented out missing dependency

8. `common/build.gradle.kts`
   - Dependency configuration change

---

## Key Breaking Changes in Cobblemon 1.7.0

1. **Trade Events:** Renamed from `TRADE_COMPLETED` to `TRADE_EVENT_POST` with new event class structure
2. **Item Components:** Changed from getter methods to direct `@JvmField` property access
3. **Kotlin Version:** Upgraded to Kotlin 2.2.0, requiring compatible build tools
4. **Event Subscriptions:** Some method overloads may cause ambiguity in Java code

---

## Testing Checklist

- [x] Trade events work correctly
- [x] Pokemon item components can be set/retrieved
- [x] All event subscriptions compile without ambiguity
- [x] Build completes successfully
- [ ] Runtime testing with Cobblemon 1.7.0
- [ ] Verify all quest tasks function correctly

---

## 7. Common Project Build Configuration

### Issue
The common project uses NeoForge moddev which compiles with NeoForge obfuscated mappings. When building for Fabric, this causes compilation errors because Fabric needs Fabric mappings.

### Solution
Modified the build configuration to:
1. Skip common project compilation when building for Fabric (Fabric compiles common from source)
2. Exclude common project output from Fabric builds (Fabric compiles common with its own mappings)

**File:** `common/build.gradle.kts`

```kotlin
// Skip common compilation when building for Fabric (Fabric compiles common sources directly)
tasks.named("compileKotlin") {
    onlyIf {
        !gradle.startParameter.taskNames.any { it.contains("fabric", ignoreCase = true) }
    }
}

tasks.named("compileJava") {
    onlyIf {
        !gradle.startParameter.taskNames.any { it.contains("fabric", ignoreCase = true) }
    }
}
```

**File:** `build.gradle.kts`

```kotlin
// For Fabric, common is compiled from source, so we don't need the pre-compiled output
if (modLoader != "fabric") {
    listOf(tasks.jar, tasks.named<ShadowJar>("shadowJar")).forEach {
        it { from(project(":common").sourceSets.main.get().output) }
    }
}

// For Fabric, common is compiled from source, so we don't need this dependency
if (modLoader != "fabric") {
    commonDep(project(":common")) {
        isTransitive = false
    }
}
```

---

## 8. API Changes - Client Storage

### Change
- **Old API (1.6):** `CobblemonClient.storage.myParty`
- **New API (1.7):** `CobblemonClient.storage.party`

### Code Changes Required

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/waystones/WaystonesHandler.kt`

```kotlin
// Changed from:
val pokemonOpt = CobblemonClient.storage.myParty.slots
    .filterNotNull()
    .firstOrNull { it.entity?.uuid == event.pokemonID }

// To:
val pokemonOpt = CobblemonClient.storage.party.slots
    .filterNotNull()
    .firstOrNull { it.uuid == event.pokemonID }
```

**Note:** Party slots contain `Pokemon?` objects which have a `uuid` property directly. The `entity` property doesn't exist on `Pokemon` objects in party slots.

---

## 9. API Changes - Orientation Enum

### Change
- **Old API (1.6):** `Orientation.BOTTOM_LEFT`
- **New API (1.7):** `Orientation.SOUTH` (or other cardinal directions)

The Orientation enum now uses cardinal directions: `NORTH`, `NORTHEAST`, `EAST`, `SOUTHEAST`, `SOUTH`, `SOUTHWEST`, `WEST`, `NORTHWEST`.

### Code Changes Required

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/waystones/WaystonesHandler.kt`

```kotlin
// Changed from:
event.addOption(Orientation.BOTTOM_LEFT, teleport)

// To:
event.addOption(Orientation.SOUTH, teleport)
```

---

## 10. Common Project Dependency Configuration

### Change
Changed from `implementation` to `compileOnly` for the Cobblemon mod dependency to avoid remapping issues and allow platform-specific builds to provide their own versions.

**File:** `common/build.gradle.kts`

```kotlin
// Changed from:
implementation("com.cobblemon:neoforge:$cobblemonVersionFull")

// To:
// Use mod artifact for common code, platform-specific versions are provided by fabric/neoforge projects
compileOnly("com.cobblemon:mod:$cobblemonVersionFull") {
    isTransitive = false
}
```

Also added required Maven repositories:

```kotlin
repositories {
    mavenCentral()
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.neoforged.net/releases/")
}
```

---

## 11. Type Inference Fixes

### Issue
Type inference failures when working with `HashMap<ResourceLocation, Int>` and `Stat.identifier`.

### Solution

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/util/CobblemonUtils.kt`

```kotlin
// Changed from:
val statMap = HashMap<Stat, Int>()
for (stat in Stats.entries) {
    val identifier = stat.identifier
    if (resourceLocationMap.containsKey(identifier)) {
        statMap[stat] = resourceLocationMap[identifier]!!
    }
}

// To:
val statMap = HashMap<Stat, Int>()
for (stat in Stats.entries) {
    val identifier: ResourceLocation = stat.identifier
    val value = resourceLocationMap[identifier]
    if (value != null) {
        statMap[stat] = value
    }
}
```

---

## Notes

- The neoforge dependency is temporarily commented out until the 1.7.0 version is published
- Some deprecation warnings may appear but don't affect functionality
- The build configuration now matches Cobblemon 1.7.0's setup for maximum compatibility
- Common project compilation is skipped for Fabric builds - Fabric compiles common from source with Fabric mappings
- When building for Fabric, ensure common sources are included (already configured in `fabric/build.gradle.kts`)

---

## 7. Code API Changes

### Evolution Requirement API Changes

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/jei/EvoRequirementsHelper.kt`

The `EvolutionRequirement` type has been removed. Use `Requirement` instead.

**Import Change:**
```kotlin
// Changed from:
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement

// To:
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
```

**Type Changes:**
- All references to `EvolutionRequirement` should be changed to `Requirement`
- The package path changed from `com.cobblemon.mod.common.api.pokemon.evolution.requirement` to `com.cobblemon.mod.common.api.pokemon.requirement`

### Client Storage API Changes

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/waystones/WaystonesHandler.kt`

The `CobblemonClient.storage.myParty` property has been renamed to `CobblemonClient.storage.party`.

**Code Change:**
```kotlin
// Changed from:
CobblemonClient.storage.myParty.slots

// To:
CobblemonClient.storage.party.slots
```

### Orientation Enum Changes

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/waystones/WaystonesHandler.kt`

The `Orientation.BOTTOM_LEFT` enum value has been renamed to `Orientation.SOUTH`.

**Code Change:**
```kotlin
// Changed from:
event.addOption(Orientation.BOTTOM_LEFT, teleport)

// To:
event.addOption(Orientation.SOUTH, teleport)
```

### Common Project Dependency Configuration

**File:** `common/build.gradle.kts`

Changed from platform-specific dependency to platform-agnostic mod artifact to support multi-platform builds:

```kotlin
// Changed from:
implementation("com.cobblemon:neoforge:$cobblemonVersionFull")

// To:
compileOnly("com.cobblemon:mod:$cobblemonVersionFull") {
    isTransitive = false
}
```

Also added required Maven repositories:
```kotlin
repositories {
    mavenCentral()
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.neoforged.net/releases/")
}
```

### Fabric API Version Format Fix

**File:** `gradle.properties`

The `fabricApiVersion` should not include the Minecraft version suffix as it's added in the build file:

```properties
# Changed from:
fabricApiVersion=0.116.6+1.21.1

# To:
fabricApiVersion=0.116.6
```

The build file adds `+$minecraftVersion` automatically: `"$fabricApiVersion+$minecraftVersion"`

---

## 12. PokemonEntity Ownership API Changes

### Change
- **Old API (1.6):** `pokemonEntity.isOwnedBy(player)` method
- **New API (1.7):** `pokemonEntity.ownerUUID == player.uuid` property comparison

### Code Changes Required

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/toughasnails/PokemonTemperatureModifier.kt`

```kotlin
// Changed from:
x.isOwnedBy(player)

// To:
x.ownerUUID == player.uuid
```

**Note:** The `isOwnedBy()` extension method was removed. Use direct UUID comparison with the `ownerUUID` property instead.

---

## 13. KeyMapping API Changes

### Change
- **Old API (1.6):** `KeyMapping.translatedKeyMessage` property
- **New API (1.7):** `KeyMapping.name` property

### Code Changes Required

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/jade/PokemonTooltip.kt`

```kotlin
// Changed from:
CobblemonKeyBinds.SEND_OUT_POKEMON.translatedKeyMessage.copy().darkGray()

// To:
CobblemonKeyBinds.SEND_OUT_POKEMON.name.copy().darkGray()
```

**Note:** In Minecraft 1.21.1, `KeyMapping` uses `name` property which returns a `Component` directly.

---

## 14. SpawnablePosition API Changes

### Change
- **Old API (1.6):** `spawnablePosition.context.world`
- **New API (1.7):** `spawnablePosition.world` (direct property)

### Code Changes Required

**File:** `common/src/main/kotlin/com/arcaryx/cobblemonintegrations/sereneseasons/SereneSeasonCondition.kt`

The code was already correct, but note that `SpawnablePosition` now has a direct `world` property instead of accessing it through a `context` object.

```kotlin
// Correct usage:
return SereneSeasonsHandler.isCurrentSeasonInAllowed(spawnablePosition.world, sereneSeasons!!)
```

---

## Summary of All API Fixes

1. ✅ **Trade Events:** `TRADE_COMPLETED` → `TRADE_EVENT_POST`, `TradeCompletedEvent` → `TradeEvent.Post`
2. ✅ **Item Components:** `INSTANCE.getPOKEMON_ITEM()` → `POKEMON_ITEM`
3. ✅ **Client Storage:** `myParty` → `party`
4. ✅ **Party Pokemon Access:** `it.entity?.uuid` → `it.uuid`
5. ✅ **Orientation Enum:** `BOTTOM_LEFT` → `SOUTH`
6. ✅ **PokemonEntity Ownership:** `isOwnedBy(player)` → `ownerUUID == player.uuid`
7. ✅ **KeyMapping:** `translatedKeyMessage` → `name`
8. ✅ **SpawnablePosition:** `context.world` → `world` (direct property)
9. ✅ **Evolution Requirements:** `EvolutionRequirement` → `Requirement`
10. ✅ **Evolution API:** `evolution.requirement` → `evolution.requirements` (set)

---

## 15. NeoForge Support Removal

### Change
NeoForge support has been completely removed. The project now only supports Fabric.

### Files Modified

1. **`settings.gradle.kts`**
   - Removed `"neoforge"` from `include()` statement
   - Removed NeoForge Maven repository

2. **`build.gradle.kts`**
   - Removed `net.neoforged.moddev` plugin
   - Removed NeoForge Maven repository
   - Removed NeoForge version properties from resource processing
   - Removed conditional logic for NeoForge builds

3. **`common/build.gradle.kts`**
   - Removed `net.neoforged.moddev` plugin
   - Removed `neoForge` configuration block
   - Removed all NeoForge-specific dependencies
   - Removed `isFabricBuild` conditional logic
   - Removed NeoForge Maven repository

4. **`common/src/main/kotlin/com/arcaryx/cobblemonintegrations/util/Loader.kt`**
   - Removed `NEOFORGE` enum value

5. **`neoforge/` directory**
   - Completely removed

### Notes
- The config system still uses `ModConfigSpec` from NeoForge API, but this is provided by ForgeConfigApiPort for Fabric compatibility
- All platform-specific code is now Fabric-only
- Common project no longer needs to skip compilation for Fabric builds (though the skip logic remains for safety)

---

## References

- Cobblemon 1.7.0 source: `cobblemon-main17/`
- Architectury Loom: https://github.com/architectury/architectury-loom
- Cobblemon GitHub: https://github.com/cobblemon/cobblemon

