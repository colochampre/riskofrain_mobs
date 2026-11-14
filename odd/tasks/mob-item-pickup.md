# Mob item pickup (mobs side)

Objective: riskofrain_mobs mobs pick up Risk of Rain items from the ground, carry them, and get their passive effects from riskofrain_items. This document covers the mobs side (inventory, pickup, persistence, drops, config). The items side (counting and effects for any `InventoryCarrier` in `#riskofrain_items:item_users`) is done in riskofrain_items, branch `1.20.1-mob-item-pickup`, document `odd/tasks/mob-item-pickup.md` there (T1-T3 done, T4 is the shared in-game test).
Branch: `1.20.1-mob-item-pickup` from `1.20.1` (`0c04f54`). Delivery: ask-on-risk. Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build` (online on the home machine), `:fabric:runServer` / `:forge:runServer` stopped through rcon, and the user's in-game test on Fabric and Forge with both mods installed.

## Specs

User request (2026-11-14): "se podría hacer que los mobs de riskofrain_mobs tengan la capacidad de acumular items de riskofrain_items como lo hacen los aldeanos granjeros? La idea es que se beneficien de las pasivas de los items al igual que un jugador."

- S1: No hard dependency. riskofrain_mobs never references riskofrain_items classes; it uses the item tag `#riskofrain_items:passive_items` by id. Without riskofrain_items the tag is empty, so mobs pick up nothing and behave as today.
- S2: "Por ahora solo que los puedan juntar del piso." Beetle, Lemurian, Stone Golem, Wisp, Gunner Drone and Gunner Turret implement vanilla `InventoryCarrier` with a `SimpleContainer`, saved and loaded with the entity. They pick up item entities in `#riskofrain_items:passive_items` through the vanilla loot pickup (`canPickUpLoot`, `wantsToPickUp`, `pickUpItem`), only while the container can take the stack. Stacks of the same item merge. Nothing else is picked up (no armor or weapons).
- S3: Effects come from riskofrain_items. riskofrain_mobs adds its six entities to `#riskofrain_items:item_users` from its own data (`data/riskofrain_items/tags/entity_types/item_users.json`, `"replace": false`). Which items do something on a mob is decided there (Infusion, Golden Gun, Smart Shopper and Soldier's Syringe are picked up but do nothing).
- S4: A mob that carries at least one item never despawns, whatever its `ENABLE_DESPAWN` setting says.
- S5: A mob drops everything it carries when it dies, whatever killed it. A tamed drone that breaks down to its for-sale state and a turret that its owner picks up as an item also drop what they carry.
- S6: "Que sea configurable. Por defecto mobs si, drones no. La cantidad de slots también, con 8 por defecto." New config section `ITEM_PICKUP` (collapsible, with tooltip), shown as "Item pickup", tooltip "Mobs pick up Risk of Rain items from the ground and gain their passive effects. Carried items drop on death." Entries: enemies can pick up items (true), drones can pick up items (false), inventory slots (8). The name "Artifact of Evolution" is reserved for a future spawn-with-items mode.
- S7 (assumption): inventory slots bounded 1 to 27. A mob loaded with more stacks than the current setting keeps the first ones and drops the rest at its position, so lowering the setting never deletes items. Turning pickup off stops new pickups; items already carried stay and keep working.
- S8: Config labels and tooltip in en_us, es_es, es_mx and zh_cn.
- S9 (assumption): vanilla loot pickup only runs while the `mobGriefing` game rule is true, as for zombies and villagers. Kept as is.

## Tasks

- [ ] T1 S6-S8: `ITEM_PICKUP` config, bounds and clamp in `validatePostLoad`, labels and tooltip in the four lang files.
- [ ] T2 S1-S5, S7: shared pickup support (inventory, NBT, tag check, pickup, despawn rule, death drop, overflow on load) for the four enemies; tag file `item_users.json`.
- [ ] T3 S2, S5: the same for Gunner Drone and Gunner Turret, plus drops on break-down and on turret pick-up.
- [ ] T4: build, both servers, and a console check with riskofrain_items loaded in the dev run (local jar, git-ignored): a dropped item is picked up, a damaged Beetle with Bustling Fungus heals, the item drops on kill, a drone does not pick up by default.
- [ ] T5: user in-game test on Fabric and Forge with both mods (closes T4 of the items side too).

Risk: item 6 (no automated test covers pickup or drops).

## Log

- L1 (2026-11-14), user: design agreed in conversation. Ground pickup only; Infusion, Golden Gun, Smart Shopper ("y cualquier item que no aplique a mobs") picked up without effect; Soldier's Syringe without effect on mobs ("Rompería animaciónes timeadas para que coincidan con el sonido"); Goat Hoof and Rose Buckler trigger while moving on mobs. "Tenes razon. Vamos por la 1": descriptive config name instead of Artifact of Evolution.
- L2: findings before writing. The four enemies override `removeWhenFarAway` with their `ENABLE_DESPAWN` setting, so S4 goes there. No entity picks up anything today. Later, out of scope: "Más adelante ajustamos además el momento en que se aplica el daño del hit de la animación".
