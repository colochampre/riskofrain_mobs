# Carried items brightness

Objective: the item icons shown above mobs (carried items display) look as bright and clear as the same items in the inventory.
Branch: `1.20.1-carried-items-brightness` from `1.20.1` (`ae830c9`). Delivery: ask-on-risk. Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build`, dedicated servers stopped through rcon (no client classes on the server), and the user's in-game test on Fabric and Forge.

## Specs

User request (2026-11-15): "Aclarar/iluminar ítems renderizados que juntaron los mobs (por ejemplo en el inventario se ven perfectamente claros, y en el render un poco más oscuros)".

- S1: The icons above a mob render with the same brightness and colors as the item in the inventory, for flat items (generated sprites, the usual Risk of Rain items) and for block items, at any time of day and in any world light.
- S2: The fix only affects the carried items icons; mobs, name tags, the `x<count>` labels and other rendering are unchanged.
- S3: Client-only, as the rest of `CarriedItemsRenderer`; nothing loads on a dedicated server.
- S4: The changelog keeps an "Unreleased" section updated with every user-facing change, so nothing is forgotten at release time. User: "También hay que mantener el changelog actualizado, así no olvidamos nada".

## Tasks

- [x] T1 S4 (`3f208f7`): changelog "Unreleased" section with the features merged after 1.3.0 (item pickup #16, carried items display #17, melee and walk animations #18).
- [ ] T2 S1-S3: find the cause in vanilla 1.20.1 (how GUI item lighting differs from the world: `Lighting.setupFor3DItems`/`setupForFlatItems` against `setupLevel`, the entity shader's diffuse term on vertex normals) and fix it in `CarriedItemsRenderer.renderIcon`; changelog entry.
- [ ] T3: build, and both dedicated servers start and stop.
- [ ] T4: user in-game test on Fabric and Forge: icons against the inventory, day and night, flat and block items, name tags and counts unchanged.

Risk: item 6 (rendering is only verifiable in game).

## Log

- L1 (2026-11-15), finding before writing: `renderIcon` draws with `ItemDisplayContext.GUI` and `LightTexture.FULL_BRIGHT`. Full bright only covers block and sky light; the darkening is assumed to come from the diffuse shading of the entity shader, which lights each face by its normal against the level light directions, while the inventory uses a flat front light. To confirm in T2. Candidate fixes: override the vertex normals with one the level lighting treats as fully lit, or flush the icons with GUI flat lighting set and restore the level lighting after.
- L2 (2026-11-15), user: "el texto como los precios de drones o el x2, x3, etc al lado de los items renderizados, siempre estan claros, por lo que a lo mejor sirven de referencia". Supports L1: text render types have no normals and no diffuse term, item render types do.
