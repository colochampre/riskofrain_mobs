# Carried items display

Objective: show above each mob the Risk of Rain items it carries (from the mob item pickup feature), as item icons with a count, so players can see which mobs are worth hunting.
Branch: `1.20.1-carried-items-display` from `1.20.1-mob-item-pickup` (`a404e64`, PR #16 open); retarget to `1.20.1` once #16 is merged. Delivery: ask-on-risk. Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build`, dedicated servers stopped through rcon (no client classes loaded on the server), and the user's in-game test on Fabric and Forge.

## Specs

User request (2026-11-14): "se puede mostrar de alguna forma los items que tienen actualmente los mobs que los hayan recogido. Por ejemplo encima de su cabeza, así como los drones muestran como nombre el precio cuando no estan domesticados. Se puede mostrar el icono del item que tienen, y en caso de tener multiples de un tipo, con (item)x2 x3 x4... segun lo que tengan"

- S1: The six mobs that can carry items render a row of item icons above their head, one icon per distinct item, in the order the items were first found in the inventory. An item with a total count above 1 shows `x<count>` next to its icon (`x2`, `x3`, ...). The count is the total across stacks of that item.
- S2: Not through the custom name: names are text only in 1.20.1 and a name tag must keep working. Rendering is a client-only render layer or renderer hook, never loaded on a dedicated server.
- S3: The client learns what a mob carries through synced entity data: a compact summary (item id and total count per distinct item), updated on the server only when the inventory changes, not every tick. Nothing is synced for an empty inventory beyond the empty summary.
- S4: "Probamos con siempre dentro de cierta distancia, configurable". The row is visible whenever the mob is within the configured distance of the camera, not only when looked at. Not shown for an invisible mob, or when the HUD is hidden (F1).
- S5: "se puede volver a ponerles un nametag, y en ese caso, de tener items, estarían ambos presentes". When the mob's name would render (vanilla `shouldShowName`, which also covers the untamed drone price), the row is drawn above the name, so they never overlap.
- S6: "Probemos con 4 por fila. Luego del test te digo si prefiero más o menos". More than 4 distinct items wrap to another row, rows centered; rows face the camera like a name tag. Keep the per-row count in one constant so it is easy to change after the test.
- S7: Config entries in the existing "Item pickup" section: show carried items (true) and display distance in blocks (16, bounded 1 to 64), translated in en_us, es_es, es_mx and zh_cn. Both are read by the client only.

## Tasks

- [ ] T1 S3: synced summary in the shared pickup support, refreshed when the inventory changes (pickup, load, drops); readable from the client.
- [ ] T2 S1, S2, S4-S6: client render of the icon rows for the six mobs, registered from the client entrypoints of both platforms.
- [ ] T3 S7: config entries and lang keys.
- [ ] T4: build and both dedicated servers start and stop (proves no client class on the server).
- [ ] T5: user in-game test on Fabric and Forge: icons and counts, distance, name tag on an enemy and on a tamed drone, untamed drone price.

Risk: item 6 (no automated test covers rendering or sync).

## Log

- L1 (2026-11-14), user chose "always within a configurable distance" over "only when aimed at". Drone price uses `setCustomName` (`AbstractDroneEntity.java:374`), text only, so icons need their own render.
