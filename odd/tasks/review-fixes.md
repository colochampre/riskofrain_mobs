# Review fixes

Objective: apply the fixes found in the full code review (2026-10-20), block by block.
Branch: `1.20.1-fixes` (from `1.20.1-golem`). Delivery: ask-on-risk. Test runner: none (no unit-test harness in the mod); checks are `./gradlew build`, `./gradlew :fabric:runServer` / `:forge:runServer` and in-game manual tests.

## Specs

User request: "Me ayudas a revisar los mobs, eventos y demás en busca de posibles correcciones, mejoras de robustez, buenas prácticas, etc?" Then: "Sí, arrancá por el bloque 1".

### Block 1: crashes (authorized)
- S1. A dedicated server (Fabric and Forge) must start without loading client-only classes. Client-only registration (S2C sound receiver, renderers, model layers) runs only from client entrypoints. Check: `runServer` reaches the EULA prompt instead of crashing.
- S2. Paying a drone with the last gold item in hand must not crash, and must reduce the price by that item's value. Repairing with the last iron item must heal the amount for that item.
- S3. Spawn config values loaded from disk must be clamped/validated so a hand-edited config (e.g. min group size > max) cannot crash natural spawning.

### Block 2: drones (not started, awaiting go-ahead)
### Block 3: enemies (not started, awaiting go-ahead)
### Block 4: cleanup and performance (not started, awaiting go-ahead)
Open decision for the user: HARD difficulty damage is scaled twice (vanilla x1.5 plus custom multipliers). Keep or remove.

## Tasks

| ID | Specs | Route | Status | Evidence / commit |
|----|-------|-------|--------|-------------------|
| T1 | S1 | inline | done (Forge server + in-game client check pending) | RED: Fabric runServer crashes with `Cannot load class net.minecraft.client.resources.sounds.SoundInstance in environment type SERVER`. GREEN: Fabric runServer logs `Initialized RoR Mod` and reaches the EULA prompt. Forge checks the EULA before loading mods, so it is unverified. |
| T2 | S2 | inline | done (in-game check pending) | No runnable RED: no unit-test harness; fix is in mobInteract, which needs a live player. Build green. |
| T3 | S3 | inline | done (in-game check pending) | No runnable RED without a running world (needs eula=true). `validatePostLoad` clamps spawn rates to 0-100, group sizes to 1-8 and max >= min. Build green. |

## Log

- L1 (2026-10-20): "Me ayudas a revisar los mobs, eventos y demás en busca de posibles correcciones, mejoras de robustez, buenas prácticas, etc?"
- L2 (2026-10-20): Two read-only reviewers (allies; enemies + infrastructure) reported findings, grouped into blocks 1-4. Most severe spot-checked by the parent.
- L3 (2026-10-20): "Sí, arrancá por el bloque 1". Branch `1.20.1-fixes` created.
- L4 (2026-10-20): T1 RED observed on `:fabric:runServer`.
- L5 (2026-10-20): T1 moved the S2C receiver and renderer/layer registration into `client/RoRModClient`, called from `RoRFabricClient` and from `RoRModForge` via `DistExecutor`. Full world start on a dedicated server needs `eula=true`, which is the user's call; not accepted.
- L6 (2026-10-20): Commits: T1 `17fb0d6`, T2 `09f2aec`, T3 `248171b`. Independent verifier (baseline `cb0d9e0`): S1 PASS (Forge by code reading only), S2 PASS, S3 PASS, no blockers. Verifier found the Forge server also needed T1: Architectury marks `registerS2CReceiver` `@OnlyIn(Dist.CLIENT)` on Forge. Cloth saves clamped values back to disk on load. Advisories: (a) Forge server and in-game client not checked at runtime; (b) the Cloth GUI saves without `validatePostLoad`, so min > max set in the GUI can still crash spawning in the same session until restart (pre-existing; a save listener that clamps would close it); (c) a hand-edited `"MOBS": null` would NPE in `validatePostLoad` (unrealistic, pre-existing).
