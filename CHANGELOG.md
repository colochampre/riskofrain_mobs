# Changelog

## 1.3.0 - Minecraft 1.20.1 (Fabric & Forge)

### New
- **Fabric support.** The mod now runs on both Fabric and Forge (requires Architectury API and Cloth Config; Fabric API on Fabric).
- **In-game config screen** (Cloth Config, via Mod Menu on Fabric). Note: settings from the previous version are not carried over.
- **New drops:** Lemurians drop a fire charge and Wisps drop charcoal (both affected by Looting).
- **Muzzle flashes** on drone and turret shots.

### Changed
- **Natural spawns rebalanced.** Enemies now share each biome's monster spawn weight instead of being added on top of it, so they no longer crowd out vanilla monsters. Biomes with too few monsters (such as mushroom fields and the Deep Dark) get none.
- **Drones** no longer spawn in biomes without animals (rivers, badlands, snowy beaches), where they used to pile up. Both drone types can now spawn in the same biome.
- **Wisps** need free ground to spawn, like other monsters.
- **Stone Golem loot:** the redstone block is now a 50% drop when killed by a player; blackstone drops 3 (both affected by Looting).
- **Stone Golem summoning** accepts replaceable blocks (grass, snow…) in the gaps around the T.
- **Hard difficulty** no longer doubles enemy damage on top of vanilla scaling.
- **Breaking a tamed drone** resets it to its unowned, for-sale state.
- **Risk of Rain sounds** (advancement, chat, difficulty, level up, death) are now off by default. Turn them on in the config if you want them.

### Drones & targeting
- Drone shots no longer hit their owner or the owner's pets.
- Drones don't target players their owner can't hurt (e.g. PvP off).
- Drones and mobs no longer target what they can't see through walls.
- Mobs stop attacking broken-down drones.

### Fixes
- Fixed a crash when paying a drone with your last gold or repairing with your last iron.
- Fixed a crash on dedicated servers.
- Fixed a crash from a config with a minimum group size above the maximum.
- Tamed turrets can be picked up by their owner after reloading the world.
- Stone Golem: the laser deals both hits, no longer fires twice, and stays fully lit from any angle.
- Wisp: charges before every shot and no longer shows hurt particles when immune.
- Lemurian: fixed strafing and fireball aim; fireballs no longer shrink after a chunk reload.
- The difficulty-change sound plays when changed from the pause menu.
- Fixed the missing Stone Golem spawn egg model.

## 1.2.1 - Minecraft 1.20.1 (Forge)

- Last release built from the former `riskofrainmod` project.
