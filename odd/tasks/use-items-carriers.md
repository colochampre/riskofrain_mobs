# Use riskofrain_items carriers

Objective: the riskofrain_mobs mobs carry Risk of Rain items through the riskofrain_items carrier system, and riskofrain_mobs drops its own inventory, sync, icon renderer and duplicate settings (backlog item 3b). Companion task: riskofrain_items `odd/tasks/carrier-extension-api.md`, which merges first.
Branch: `1.20.1-use-items-carriers` from `1.20.1` (`a9a5f72`). Delivery: ask-on-risk. Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build`, dedicated servers stopped through rcon, and the user's in-game test on Fabric and Forge.

## Specs

User decisions: "Si tomasemos la decision de unificarlos, convendría hacerlo antes del port y release" (2026-11-16); no migration of saved `Inventory` data, test worlds are recreated (2026-11-22); riskofrain_items is an optional dependency, "Dale, vamos con A" (2026-11-23).

- S1: Optional dependency. riskofrain_mobs compiles against riskofrain_items but runs without it: `fabric.mod.json` and `mods.toml` declare it optional (ordered after it when present), and every reference to riskofrain_items classes lives in one compat class that is only loaded when the mod is present. Without riskofrain_items the mobs work as before and never pick up anything (no Risk of Rain items exist). The compile-time source of the riskofrain_items jar must work on a fresh clone (to decide in T1: a published maven artifact if one exists, otherwise a documented local jar).
- S2: Pickup rules, registered through the riskofrain_items registration point: Beetle, Lemurian, Stone Golem and Wisp follow `ENEMIES_CAN_PICK_UP` (default true); Gunner Drone and Gunner Turret follow `DRONES_CAN_PICK_UP` (default false) and only while tamed. These two settings stay in riskofrain_mobs.
- S3: Leaving the world without dying keeps dropping the items: a drone breaking down (`breakDown`) and a turret picked up by its owner call riskofrain_items `CarriedItems.dropAll` through the compat class.
- S4: Removed from riskofrain_mobs: `ItemPickup`, `CarriedItemsSource`, `CarriedItemsRenderer`, `GuiLitBufferSource`, the six renderer hooks, `DATA_CARRIED_ITEMS` and the inventories, `InventoryCarrier` and the pickup, save, drop and despawn overrides on the mobs, the `item_users` tag file, and the `INVENTORY_SLOTS`, `SHOW_CARRIED_ITEMS`, `DISPLAY_DISTANCE` and `DISPLAY_MODE` settings with their lang keys (riskofrain_items owns slots and display now). Each mob's own `ENABLE_DESPAWN` setting stays; the "carriers never despawn" rule comes from riskofrain_items.
- S5: What the mobs gain for free: owner take-back for drones and the turret (sneak + right-click with an empty hand), conversions, and the same icons as every other carrier.
- S6: Saved items under the old `Inventory` key are not migrated (never released).
- S7: The changelog "Unreleased" section is updated: the riskofrain_mobs entries for item pickup and the carried items display now point to riskofrain_items, and the removed settings are listed under Changed.

## Tasks

- [ ] T1 S1: compile dependency on riskofrain_items (source decided and recorded), optional entries in both loader metadata files, the compat class and its guarded init.
- [ ] T2 S2-S3: rules registered; drops on breakdown and turret pickup.
- [ ] T3 S4-S6: remove the old system and settings.
- [ ] T4 S7: changelog.
- [ ] T5: build, both dedicated servers with and without riskofrain_items in `libs`, rcon checks: a Beetle picks up and drops on death, a tamed drone picks up with the drone setting on and drops on breakdown, an untamed drone does not, nothing crashes without riskofrain_items.
- [ ] T6: fresh review of both branches.
- [ ] T7: user in-game test on Fabric and Forge: pickup, icons (height on Beetle, Turret, Wisp), effects, take-back from a drone, settings screens of both mods.

Risk: item 6 (no automated tests).

## Log
- L1 (2026-11-23), user on the compile source: "Por ahora usamos los jars locales, porque no los publiqué todavía." riskofrain_items is not published; T1 compiles against the local jar in `<platform>/libs`, documented for a fresh clone.
