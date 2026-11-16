# Changelog

## Unreleased - Minecraft 1.20.1 (Fabric & Forge)

### New
- **Mobs pick up Risk of Rain items** (with Risk of Rain Items installed). Beetles, Lemurians, Stone Golems and Wisps pick up item drops from the ground and gain their passive effects; tamed drones and turrets can too if enabled. Carried items drop on death, and a mob carrying items never despawns. Configurable in the new "Item pickup" section (enemies on, drones off, 8 slots).
- **Carried items above the head.** Mobs show the icons of the items they carry, with `x2`, `x3`... counts, when you look at them (or always, configurable), within a configurable distance.
- **Melee wind-ups.** The Stone Golem opens its arms before clapping, the Beetle pulls its neck back before ramming, and the Lemurian pulls its fist back before punching.
- **Melee hits land on contact.** Damage is dealt when the animation connects, not when the attack starts. Backing away can dodge a hit, but walking away is not enough: you need to sprint early.
- **New walk animations.** The Lemurian walks like a deer's hind leg and sidesteps while strafing, the Stone Golem bends its knees one way only with arms in step, and a tamed Gunner Turret trots on its legs while led on a leash.

### Changed
- **Walk animations stay calm at high speed.** Speed boosts no longer make Stone Golems, Beetles and Lemurians flail their limbs.
- **Footsteps follow the feet.** Stone Golem, Beetle and Lemurian step sounds play when a foot lands, at any speed.

### Fixes
- Stone Golem torso and Beetle legs no longer stay stuck in a pose after an attack.
- Two Lemurians attacking at once no longer mix up which hand punches.
- Carried item icons above mobs are as bright as in the inventory, whatever the time of day or where you look.

## 1.3.0 - Minecraft 1.20.1 (Fabric & Forge)

### New
- **Fabric support.** The mod now runs on both Fabric and Forge (requires Architectury API and Cloth Config; Fabric API on Fabric).
- **In-game config screen** (Cloth Config, via Mod Menu on Fabric). Note: settings from the previous version are not carried over.
- **New drops:** Lemurians drop a fire charge and Wisps drop charcoal (both affected by Looting).
- **Muzzle flashes** on drone and turret shots.
- **Simplified Chinese** translation, now complete (mob names follow the Chinese localization of Risk of Rain 2).

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
