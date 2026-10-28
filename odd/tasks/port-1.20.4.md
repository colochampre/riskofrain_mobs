# Port to 1.20.4

Objective: port the mod from 1.20.1 (Fabric + Forge) to 1.20.4 with Fabric and NeoForge.
Branch: `1.20.4` (from `1.20.1` at `ddd9337`); push, PR and the target branch are the user's decisions. Delivery: ask-on-risk. Test runner: none (no unit-test harness); checks are `./gradlew build`, `:fabric:runServer` / `:neoforge:runServer` and in-game manual tests.

## Specs

User request: "Dale, arrancá el port con Fabric y NeoForge. La primera vez que hice el setup con architectury tuve que hacer una instalación particular. Cualquier cosa que precises que haga, avisame"

- S1. The mod builds for Minecraft 1.20.4 with two platforms: Fabric and NeoForge. The Forge platform is replaced by NeoForge ("arrancá el port con Fabric y NeoForge").
- S2. Dependency versions for 1.20.4, latest stable from their Maven repositories (2026-10-28): Architectury API 11.1.17, NeoForge 20.4.253, Fabric API 0.97.3+1.20.4, Cloth Config 13.0.138, Mod Menu 9.2.0, Parchment 1.20.4 2024.04.14.
- S3. Same gameplay and behavior as the 1.20.1 version: all the mobs, drones, items, sounds, config screen and events work on both platforms.
- S4. Assumption, following the 1.20.1 decision (`fece21f`, `277ef25`): metadata declares only 1.20.4 and the matching loader, Architectury and Cloth Config ranges, with Cloth Config required.

## Tasks

| ID | Specs | Route | Status | Evidence / commit |
|----|-------|-------|--------|-------------------|
| T1 | S1, S2, S4 | inline | done | Gradle setup: `forge/` -> `neoforge/`, versions, NeoForge metadata, per-version run dirs. Build green. |
| T2 | S1, S3 | inline | done | Only API break in `common/` and `fabric/`: `getPerceivedTargetDistanceSquareForMeleeAttack` removed (1.20.2). Build green. |
| T3 | S1, S3 | inline | done | NeoForge entrypoint, renderers and config screen on `net.neoforged` packages (names read from loader-2.0.17, bus-7.2.0, neoforge-20.4.253 jars). Build green. |
| T4 | S3 | inline | in progress | Fabric and NeoForge dedicated servers reach `Done` and stop cleanly (the required mixin applies on both). NeoForge needed `type="required"` (see L5). Verifier blocker fixed: NeoForge natural spawns (see L6). Pending: in-game checks by the user, independent verifier. |

Route: inline, no writer reason (sequential units that share the build). Independent verifier at the end (risk item 5: dependencies and build).

## Log

- L1 (2026-10-28): "Dale, arrancá el port con Fabric y NeoForge. La primera vez que hice el setup con architectury tuve que hacer una instalación particular. Cualquier cosa que precises que haga, avisame"
- L2 (2026-10-28): Earlier decision: "Dale, arrancá el port con Fabric y NeoForge" after the recommendation to use NeoForge from 1.20.4 (Architectury supports NeoForge from 1.20.2; NeoForge 20.4 has the most builds). Versions read from the Maven metadata of Architectury, NeoForged, FabricMC, Shedaniel, TerraformersMC and ParchmentMC. Network: ARBA ethernet disabled by the user, wifi without the Mojang/Forge block.
- L3 (2026-10-28): Build notes. Mod Menu 9.2.0 lists `eu.pb4:placeholder-api` as a transitive dependency that no configured repository has; excluded, since Mod Menu bundles it (`META-INF/jars/placeholder-api-2.4.0-pre.1+1.20.4.jar`). Inside the `loom {}` block `rootProject` resolves to the extension's `isRootProject()`, so the run dir is computed outside it. Run dirs are now `run/<minecraft_version>`, so 1.20.1 test worlds in `fabric/run` and `forge/run` are never upgraded; untracked `forge/run` and `forge/build` were moved back after `git mv`. Architectury 11 on NeoForge has no `EventBuses` (it finds the mod bus itself). `getPerceivedTargetDistanceSquareForMeleeAttack` was `max(dist(target.getMeleeAttackReferencePosition()), dist(target.position()))`, which only differed while the target rode a vehicle; replaced by `distanceToSqr(target)`.
- L4 (2026-10-28): User: "Dale, te autorizo a aceptar el EULA en las dos". `eula=true` written in `fabric/run/1.20.4` and `neoforge/run/1.20.4`.
- L5 (2026-10-28): NeoForge 20.4 rejects `mandatory` in mods.toml dependencies (`InvalidModFileException: Deprecated 'mandatory' field is used in dependency`, then `Failed to find system mod: minecraft`); switched to `type="required"`. Both servers then reach `Done`. "No data fixer registered for riskofrain_mobs:*" also appears in the 1.20.1 Fabric log (dev-only vanilla log).
- L6 (2026-10-28): Independent verifier (baseline `ddd9337`, candidate `afaea57`): S1, S2, S4 PASS; S3 FAIL on NeoForge, one blocker: no natural spawns. Architectury 11.1.17 ships its biome modifier only at `data/architectury/forge/biome_modifier/impl.json` (checked in the jar), but NeoForge 20.4 reads `data/<ns>/neoforge/biome_modifier/` (`CommonHooks.prefixNamespace`), so `BiomeModifications.addProperties` never runs (upstream Architectury issue #480). Fixed by adding `neoforge/src/main/resources/data/architectury/neoforge/biome_modifier/impl.json` with the same content (same `architectury:impl` id, so another mod shipping the workaround replaces it instead of doubling spawns). Passed: no override silently lost (ASM supertype walk against both jars), Architectury events and networking unchanged, resources valid. Advisories: melee distance only differed for camels (`RiderShieldingMount`, gone in 1.20.4); `pack.mcmeta` format 22 vs data 26 is not enforced for mod packs; `items/gunner_turret_item.json` uses the 1.21.4 format and was already at the baseline. Natural spawns on NeoForge need an in-game check.
- L7 (2026-10-28): User, testing in game: "Estoy testeando y todo parece correcto". Spawn follow-up decided (to be done from `1.20.1` and merged forward): "Sacaría los biomas en donde tengan 100% del spawn y dejaría el sistema como está, aceptando los biomas que tengan del 33% para abajo". Branch strategy: "Las features nuevas van incluso a la 1.20.1".
