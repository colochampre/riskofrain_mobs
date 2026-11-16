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
- [x] T2 S1-S3 (`0747b5b`): find the cause in vanilla 1.20.1 (how GUI item lighting differs from the world: `Lighting.setupFor3DItems`/`setupForFlatItems` against `setupLevel`, the entity shader's diffuse term on vertex normals) and fix it in `CarriedItemsRenderer.renderIcon`; changelog entry.
- [x] T3: build, and both dedicated servers start and stop.
- [x] T4: user in-game test on Fabric and Forge: icons against the inventory, day and night, flat and block items, name tags and counts unchanged.

Risk: item 6 (rendering is only verifiable in game).

## Log

- L1 (2026-11-15), finding before writing: `renderIcon` draws with `ItemDisplayContext.GUI` and `LightTexture.FULL_BRIGHT`. Full bright only covers block and sky light; the darkening is assumed to come from the diffuse shading of the entity shader, which lights each face by its normal against the level light directions, while the inventory uses a flat front light. To confirm in T2. Candidate fixes: override the vertex normals with one the level lighting treats as fully lit, or flush the icons with GUI flat lighting set and restore the level lighting after.
- L2 (2026-11-15), user: "el texto como los precios de drones o el x2, x3, etc al lado de los items renderizados, siempre estan claros, por lo que a lo mejor sirven de referencia". Supports L1: text render types have no normals and no diffuse term, item render types do.
- L3 (2026-11-15), T2 done in `0747b5b`, `./gradlew build` passes, not seen in game. Cause confirmed (javap and shaders): item render types use `minecraft_mix_light`, factor `min(1, (max(0, L0·N) + max(0, L1·N)) * 0.6 + 0.4)`; in the world `Lighting.setupLevel` puts the two level light directions in view space, so a camera-facing sprite gets ambient plus a fraction that changes with where you look; the GUI `setupForFlatItems` light gives a front-facing sprite exactly 1.0. Text has no normals, hence always bright. Fix: new `client/renderer/GuiLitBufferSource`, a `MultiBufferSource` wrapper whose consumers rewrite each normal as `V * G^T * Sy * n` (V the view rotation, G the GUI light matrix, Sy the GUI y flip), so a face shades in the world as it does in the GUI; `G` is the flat-items matrix for generated sprites and the 3D-items one for block models (`usesBlockLight`), as the inventory does. It overrides `normal()` and the bulk 14-float `vertex()` that `BufferBuilder` uses. Kept batched; labels still use the original buffer. Known limit: the Nether's second light differs, icons there about 2% darker. Changelog line added under Fixes.
- L4 (2026-11-15), user in-game test of `0747b5b`: "Todo parece correcto". T3 done on Fabric and Forge (dedicated servers stopped through rcon): `Done (`, `Stopping server`, `All dimensions are saved`, no class-loading errors, no riskofrain exceptions; a summoned Beetle keeps its `Inventory` of 2 Bustling Fungus. The build there was up to date.
- L5 (2026-11-15), fresh review blocker, fixed in `b1625a5`: the view rotation was `conj(cameraOrientation)`, off by a 180 degree turn around Y. `Camera` builds its quaternion as `rotationYXZ(-yaw, pitch, 0)` while `GameRenderer.renderLevel` sets the view as pitch then yaw + 180, so the view is `Ry(180) * conj(cameraOrientation)`. At pitch 0 the turn only swaps the two level lights, which shade symmetrically, so a level camera looked right (likely why the in-game test passed); at 30 degrees pitch a front sprite dropped to about 0.6-0.7 and at 60 degrees to 0.4 instead of 1.0 (reviewer's simulation of the shader). `./gradlew build` passes with the fix. T4 repeated for pitched views.
- L6 (2026-11-15), user in-game test after `b1625a5`: "Se ve correctamente desde arriba y abajo. Siempre igual de claro". Feature closed.
