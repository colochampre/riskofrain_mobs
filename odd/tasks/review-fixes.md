# Review fixes

Objective: apply the fixes found in the full code review (2026-10-20), block by block.
Branches: block 1 `1.20.1-fixes` (PR #2, merged); block 2 `1.20.1-drones` (PR #3, merged); block 3 `1.20.1-enemies` (from `1.20.1`). Merged branches deleted. Delivery: ask-on-risk. Test runner: none (no unit-test harness in the mod); checks are `./gradlew build`, `./gradlew :fabric:runServer` / `:forge:runServer` and in-game manual tests.

## Specs

User request: "Me ayudas a revisar los mobs, eventos y demás en busca de posibles correcciones, mejoras de robustez, buenas prácticas, etc?" Then: "Sí, arrancá por el bloque 1".

### Block 1: crashes (authorized)
- S1. A dedicated server (Fabric and Forge) must start without loading client-only classes. Client-only registration (S2C sound receiver, renderers, model layers) runs only from client entrypoints. Check: `runServer` reaches the EULA prompt instead of crashing.
- S2. Paying a drone with the last gold item in hand must not crash, and must reduce the price by that item's value. Repairing with the last iron item must heal the amount for that item.
- S3. Spawn config values loaded from disk must be clamped/validated so a hand-edited config (e.g. min group size > max) cannot crash natural spawning.

### Block 2: drones (in progress)
User: "Cual sería la solución para el UUID y las balas que pegan al dueño y sus drones?" Then: "Dale, arrancá el bloque 2 con esas soluciones".
- S4. A tamed turret's owner hitting it must take the pick-up path (tilt, drop as item), also after a world reload. Owner check through `isOwnedBy`, not UUID `!=`.
- S5. Bullets from a tamed drone must not hit its owner, the shooter, or other `TamableAnimal`s with the same owner (they pass through), so same-owner drones never infight. Other players' drones and pets can still be hit.
User: "Esos arreglalos directamente. Los anteriores solo me causaron dudas por posibles cambios en los comportamientos, pero estuve de acuerdo".
- S6. Taming adds `SitWhenOrderedToGoal` once; `setTame(false)` removes the tame goals and restores gravity instead of adding goals.
- S7. A tamed drone that would die breaks down to its untamed, for-sale state with nothing left over: no target or revenge memory, leash dropped, fire and effects cleared, no owner death message, price reset including the HARD x1.5, and mobs that were fighting it drop it as a target ("Se puede limpiar el target de cualquier mob que rompa un dron?"). Damage that bypasses invulnerability (/kill, the void) still kills it.
- S8. The turret muzzle flash is visible to clients, as a crit particle burst at the muzzle ("Cambiemosla por alguna partigula que se pueda usar como fogonazo de disparo, como la particula de critico"), not a new registered particle.
- S9. Only the owner can dye a tamed drone or turret.
- S10. Drones and turrets do not target players the owner cannot harm (server PvP off, same team without friendly fire), like tamed wolves.
- S11. `DroneFollowOwnerGoal` stops when the cached owner is no longer the drone's owner, dead, a spectator, or gone.
### Block 3: enemies (in progress)
User: "Hecho el merge. Hagamos limpieza de las ramasa remotas que ya se hayan mergeado y no hagan más falta. Despues de eso, seguimos con lo dempas".
- S12. The Wisp charges (sound and beam) before every shot, not only the first; losing sight of the target cancels the charge.
- S13. The Wisp's retaliation goal (`HurtByTargetGoal`) lives in the target selector, registered once.
- S14. The Stone Golem laser deals both of its hits (magic + half attack), not the larger of the two.
- S15. The Stone Golem summon check always runs after the redstone block is placed, also when the placement does not come from a player packet.
- S16. Lemurians are not delayed by the time they spent without seeing the target before they start strafing again (they still need 20 ticks of continuous sight, as in vanilla), and aim fireballs from where they spawn, at any range.
- S17. A Lemurian fireball does not turn into a vanilla small fireball when the chunk is saved and reloaded.
### Block 4: cleanup and performance (not started, awaiting go-ahead)
Open decision for the user: HARD difficulty damage is scaled twice (vanilla x1.5 plus custom multipliers). Keep or remove.

## Tasks

| ID | Specs | Route | Status | Evidence / commit |
|----|-------|-------|--------|-------------------|
| T1 | S1 | inline | done | RED: Fabric runServer crashes with `Cannot load class net.minecraft.client.resources.sounds.SoundInstance in environment type SERVER`. GREEN: Fabric runServer logs `Initialized RoR Mod` and reaches the EULA prompt. With `eula=true` (authorized by the user): Fabric and Forge dedicated servers load a world (`Done`). Forge needed a follow-up fix (see L7). |
| T2 | S2 | inline | done | No runnable RED: no unit-test harness; fix is in mobInteract, which needs a live player. Build green. |
| T3 | S3 | inline | done | No runnable RED without a running world (needs eula=true). `validatePostLoad` clamps spawn rates to 0-100, group sizes to 1-8 and max >= min. Build green. |
| T4 | S4 | inline | done (in-game check pending) | `f076c86`. No runnable RED: no unit-test harness; needs a reloaded world with a tamed turret. Build green. |
| T5 | S5 | inline | done (in-game check pending) | `9b7ccc3`. No runnable RED: needs live drones. `canHitEntity` filters same-owner targets. Build green. |
| T6 | S6, S7 | inline | done | Breakdown moved from the `LIVING_DEATH` event into `AbstractDroneEntity.die`, because vanilla/Fabric `TamableAnimal.die` sends the owner death message before the event can cancel (Forge already deferred it). Commits `d1fcfd0`, verifier fix (see L11). Build green. |
| T7 | S8 | inline | done | `51b8314`, crit swap (see L13). `ServerLevel.sendParticles`. Build green. |
| T8 | S9, S10 | inline | done (in-game check pending) | `f6b34af`. `isOwnedBy` on dyeing; restored the wolf-style `canHarmPlayer` check. Build green. |
| T9 | S11 | inline | done | `a0c9a37`. Build green. |
| T10 | S12, S13 | inline | done (in-game check pending) | RED by reading: after a shot the target goal restores the cleared target, the goal keeps running and `hitScanAttackTick` stays >= duration, so later shots skip the charge. Build green. |
| T11 | S14 | inline | done (in-game check pending) | Vanilla `LivingEntity.hurt` applies only `amount - lastHurt` while `invulnerableTime > 10`. Build green. |
| T12 | S15 | inline | done (in-game check pending) | Bytecode: `execute()` runs inline on the server thread when not inside a task (`ReentrantBlockableEventLoop.scheduleExecutables`); player placements only worked because packet handling runs as a task. Build green. |
| T13 | S16 | inline | done (in-game check pending) | `lastSeen` reset on visibility change, like vanilla `RangedBowAttackGoal`; aim from the spawn point. Build green. |
| T14 | S17 | inline | done | `shouldBeSaved()` false; bytecode: `PersistentEntitySectionManager` filters saved entities with `EntityAccess.shouldBeSaved`. Build green. |

## Log

- L1 (2026-10-20): "Me ayudas a revisar los mobs, eventos y demás en busca de posibles correcciones, mejoras de robustez, buenas prácticas, etc?"
- L2 (2026-10-20): Two read-only reviewers (allies; enemies + infrastructure) reported findings, grouped into blocks 1-4. Most severe spot-checked by the parent.
- L3 (2026-10-20): "Sí, arrancá por el bloque 1". Branch `1.20.1-fixes` created.
- L4 (2026-10-20): T1 RED observed on `:fabric:runServer`.
- L5 (2026-10-20): T1 moved the S2C receiver and renderer/layer registration into `client/RoRModClient`, called from `RoRFabricClient` and from `RoRModForge` via `DistExecutor`. Full world start on a dedicated server needs `eula=true`, which is the user's call; not accepted.
- L6 (2026-10-20): Commits: T1 `17fb0d6`, T2 `09f2aec`, T3 `248171b`. Independent verifier (baseline `cb0d9e0`): S1 PASS (Forge by code reading only), S2 PASS, S3 PASS, no blockers. Verifier found the Forge server also needed T1: Architectury marks `registerS2CReceiver` `@OnlyIn(Dist.CLIENT)` on Forge. Cloth saves clamped values back to disk on load. Advisories: (a) Forge server and in-game client not checked at runtime; (b) the Cloth GUI saves without `validatePostLoad`, so min > max set in the GUI can still crash spawning in the same session until restart (pre-existing; a save listener that clamps would close it); (c) a hand-edited `"MOBS": null` would NPE in `validatePostLoad` (unrealistic, pre-existing).
- L7 (2026-10-20): User: "Si, te autorizo" (accept the EULA in `fabric/run` and `forge/run`). Forge world run disproved the verifier's code-reading PASS: `Attempted to load class net/minecraft/client/resources/sounds/SoundInstance for invalid dist DEDICATED_SERVER` at `RoRModForge.java:21`. Cause: in dev, `DistExecutor.validateSafeReferent` calls `supplier.get()` on every dist (confirmed with javap on fmlcore 47.4.3). Fixed with a plain `FMLEnvironment.dist == Dist.CLIENT` check. Forge server then reached `Done (16.490s)!`; Fabric server reached `Done` earlier in this session.
- L8 (2026-10-21): User: "Todo correcto. Hecho el merge luego de testear el render de mobs y pagar con la ultima pepita de oro un dron. Sin crash". PR #2 merged; T1-T3 in-game checks passed.
- L9 (2026-10-21): User: "Dale, arrancá el bloque 2 con esas soluciones". Branch `1.20.1-drones` from `1.20.1` (`7081243`). Design choice: bullets pass through any `TamableAnimal` of the same owner (wolves and cats too), not only drones.
- L10 (2026-10-21): User: "Esos arreglalos directamente. Los anteriores solo me causaron dudas por posibles cambios en los comportamientos, pero estuve de acuerdo". Behavior changes to report: /kill and the void now kill tamed drones (before they broke down and kept taking damage); no owner death message on breakdown; non-owners can no longer dye; drones stop attacking players when PvP is off or they share a team without friendly fire.
- L11 (2026-10-21): Independent verifier (baseline `7081243`, candidate `41ef915`): S4-S11 PASS from code and vanilla bytecode, one blocker B1: the client runs `die()` from entity event 3, so a tamed drone killed by /kill or the void ran `breakDown()` on the client and skipped the death animation. Fixed with a server-only guard. Advisory taken: `setLastHurtByPlayer(null)` restarts the XP window, so the fields are cleared directly. Advisory noted: Forge 47.4.3 already deferred the owner death message; the fix is correct on both loaders. Build green.
- L12 (2026-10-21): User, after in-game tests: "Se puede ajustar el tamaño del fogonazo de la torreta? Cuando un mob cualquiera rompe un dron, éste sigue golpeandolo, porque sigue vivo. Se puede limpiar el target de cualquier mob que rompa un dron? En cuanto se rompe queda bien, con precio y sin mensaje de chat, El resto de puntos estan ok". S4, S5, S8 (visible), S9, S10 and breakdown reset confirmed in game. Breakdown now clears the target, revenge memory, brain ATTACK_TARGET and NeutralMob anger of every mob within 64 blocks targeting the drone. Vanilla FLASH size is hard-coded (`FireworkParticles$OverlayParticle.getQuadSize` = 7.1 * sin), so resizing needs a custom particle; asked the user.
- L13 (2026-10-21): User: "No, no registres una nueva particla. Cambiemosla por alguna partigula que se pueda usar como fogonazo de disparo, como la particula de critico, por ejemplo". FLASH replaced by 4 `CRIT` particles 0.6 blocks from the turret's eye towards the target. Build green.
- L14 (2026-10-21): User: "Lo probé con 3 esqueletos que fueron golpeados por el dron, pero al romperse, siguieron golpeandolo hasta eliminarlo. Las nuevas particulas están bien, aunque las alejaría un poco más del ojo a 0.8". Cause (bytecode): `TargetGoal.canContinueToUse` falls back to its cached `targetMob` when `getTarget()` is null and sets it again; it only lets go when `Mob.canAttack` fails, which returns `canBeSeenAsEnemy()`. Fix: `AbstractDroneEntity.canBeSeenAsEnemy` returns false while untamed, so mobs never target untamed drones. Muzzle offset 0.6 -> 0.8. Build green.
- L15 (2026-10-21): User: "Un ultimo ajuste del las particulas de 0.8 a 1.0 Y agregale la misma particula de disparo al gunner drone a 0.6 El tema del target al romperse quedo resuelto". Turret muzzle offset 1.0; Gunner Drone gets the same crit muzzle flash at 0.6. Breakdown targeting confirmed in game. Build green.
- L16 (2026-10-21): User: "Cambiá la particula por la de electric spark que es la misma que tienen cuando estan dañados, aparte de la de smoke". Muzzle flash on turret and Gunner Drone switched from `CRIT` to `ELECTRIC_SPARK`. Build green.
- L17 (2026-10-22): User: "Que otras particulas pueden llegar a quedar bien para los disparos de los drones? De paso ajusta la distancia del gunner turret de 1.0 a 1.2". Turret muzzle offset 1.2. Build green.
- L18 (2026-10-22): User: "Probemos small flame y smoke a ver que tal". Muzzle flash on turret and Gunner Drone: 4 `SMALL_FLAME` (speed 0.02) plus 1 `SMOKE`. Build green; in-game look pending.
- L19 (2026-10-22): User: "Cambié las particulas una vez más de manera manual. Y de paso recordé que la entidad Wisp tiene un problema similar con su disparo y las particulas con su hitScan. Podrías corregirlo?". User's manual change committed as `eba83bc` (1 `LAVA` particle at the muzzle). Wisp (pulled forward from block 3): `doHitScanParticles` runs from `WispAttackGoal` (server only) and `hurt` only damages on the server, but both used the client-only `EntityUtils.doParticlesAtEntity`, so the impact and hurt LAVA particles never showed (hurt ones only for the attacking player's melee hits). Added `EntityUtils.sendParticlesAtEntity` (ServerLevel.sendParticles) and used it in both. The client-side beam in `doHitScanParticleEffects` already works. Build green.
- L20 (2026-10-22): PR #3 merged; remote and local branches `1.20.1-golem`, `1.20.1-fixes`, `1.20.1-drones` deleted (all fully merged). Block 3 started on `1.20.1-enemies` from `fc1f613`. The HARD double-damage decision stays open (laser `f *= 2`, Wisp `getAttackDamage` x2 on top of vanilla difficulty scaling).
- L21 (2026-10-22): Independent verifier (baseline `fc1f613`, candidate `8e9c9fc`): S12-S17 PASS, no blockers. Advisory A1 taken: the laser only clears the target's invulnerability when its first hit landed, so i-frames from another attacker are kept. S16 wording tightened (A5). Noted, not changed: second laser hit now adds a 0.4 knockback and can be shield-blocked as a fresh hit; Wisp beam shows the old target if the target switches mid-charge (pre-existing); Lemurian `lastSeen` not reset on target change (pre-existing). On Forge, Architectury PLACE fires after the block is set, so the deferral is harmless there. Build green.
