# Original LazyGarden production assets

These raster assets are consumed by the native Android game; there is no HTML scene.
They were generated with the built-in imagegen tool and converted to WebP at quality 95
without changing their alpha channel. Existing portrait assets remain available to the story archive.

## Resident atlas

`app/src/main/res/drawable-nodpi/resident_atlas.webp`: 1536 × 1024, 8 columns × 4 rows.
Rows: Meti, Bejdi, Meli, Lina. Columns: idle, walk A, walk B, inspect, talk,
work A, work B, celebrate. `ResidentAnimation` maps these poses to seven resident states.

Initial production prompt:

> Use case: stylized-concept. Production asset for original LazyGarden Android 2D garden restoration game, NOT a mockup. Create one transparent PNG sprite atlas, exactly 8 columns by 4 rows, equal cells, no labels, no grid lines. Each row is a different ORIGINAL friendly FULL BODY character, head to boots fully visible in every cell, consistent size and baseline with generous transparent spacing. Row 1: adult bearded gardener green vest cream shirt brown work trousers. Row 2: young male apprentice blue cap navy overalls. Row 3: adult garden designer dusty rose headscarf teal tunic trousers. Row 4: young woman straw hat yellow shirt pink overalls. Polished hand painted 2D mobile game art, dimensional soft shading, expressive readable silhouettes, three-quarter facing right. Columns represent animation poses consistently for each character: 1 idle closed mouth; 2 walking left leg forward right arm forward; 3 walking right leg forward left arm forward; 4 inspecting leaning head toward right hand at chin; 5 talking mouth open hand gesture; 6 working bent forward hands using small wrench; 7 working second pose turning wrench; 8 celebration both arms raised smiling jumping. Every character must be full body, do not crop legs. No scenery, no text, no proprietary characters. Crisp individual sprites on actual transparent background.

Final edit prompt, using the initial generated atlas as its input:

> Edit this production sprite atlas, preserve exact 8 columns 4 rows layout and all identities. Remove ALL brown background to genuine alpha transparency between and around the figures, no background shading, no baked shadows. Column 3 MUST be the opposite walking step from column 2: swap left/right leg forward and arm swing. Preserve full body boots and head within each cell. Row 3 woman must wear a dusty rose headscarf covering hair in every pose, preserve teal outfit. Other poses same. Crisp isolated game sprites with transparent background.

## Launcher foreground

`app/src/main/res/drawable-nodpi/launcher_garden_foreground.webp`: adaptive foreground,
with separate native background color and API 24 / API 26 launcher resources.

Production prompt:

> Use case: logo-brand. Original LazyGarden launcher foreground artwork, polished premium 2D mobile garden restoration game. One centered elegant turquoise flowing garden fountain surrounded by lush emerald leaves, golden apple and coral flower, tiny warm cream garden manor roof behind. Dimensional painted casual mobile game asset, glossy readable at small size, warm sunlight, very crisp silhouette, no text no letters no border no mockup. Actual transparent background. Keep all artwork inside central 60 percent of square with wide transparent padding for Android adaptive icon mask.

The atlas is pose animation, not a high-frame-count skeletal animation system. Additional
walk cycles and facial frames can be added without changing the scene/save timeline.

## Fountain repair hardware

`app/src/main/res/drawable-nodpi/fountain_worksite.webp`: a transparent 2 × 2 atlas
containing the clogged pump, repaired pump, toolbox and work barrier. The user supplied
storyboard is used as a style/hardware reference. The running scene samples these props
individually, crossfades the repair state, renders a live progress meter and animates its
camera/water independently. It never displays the storyboard as the game.

Production prompt (built-in imagegen, reference image supplied by the user):

> Use case: stylized-concept. The reference is STYLE AND REPAIR HARDWARE reference only. Generate a PRODUCTION GAME SPRITE ATLAS, NOT a comic, not a screenshot, no characters, no text, no scenery. Exactly 2 columns x 2 rows equal rectangular cells, generous transparent spacing. Entire background must be genuine transparent alpha. Polished detailed painted dimensional 2D game art with the warm garden restoration material quality in reference. Upper left cell: ONE broken exposed fountain pump assembly, worn turquoise cylindrical mechanical pump centered with two horizontal curved metal pipes extending left/right, brass flange bolts, red valve wheel, visible leaf clog in an open pipe, small broken pressure gauge. Upper right: EXACT same pump assembly same placement scale perspective and silhouette, now repaired polished turquoise, unblocked pipes, new brass bolts, green valve and working gauge. Lower left: one red open toolbox with visible wrench, screwdriver, pliers, small replacement pipe couplings, detached as a single ground prop. Lower right: one work-zone foreground barrier with two small wooden posts, draped rope, two orange-white cones and one small yellow triangular digging pictogram sign, no lettering. Front three-quarter slightly elevated view, flat orthographic camera suitable for placing over the garden path. No base platforms, no backgrounds, no panels or dividing lines, no people, no fountains. Four separate isolated high-detail props. Do NOT recreate the reference storyboard. This is artwork for a native Android game, assets only.

## Native restoration expansion, 0.10.0

Built-in Imagegen produced the following original production atlases (PNG → WebP quality 95, alpha retained):

- `drawable-nodpi/restoration_objects.webp`: 3 columns × 4 rows. Rows: bench, planting cluster, gate, manor; columns: damaged, repaired, finished. Final source `exec-3a662b16-0b46-4a57-bfec-273a49de51d1.png`. Prompt: transparent cutout atlas, exactly 3×4 cells, original hand-painted garden props, broken/repaired/restored bench; dead/sapling/lush planting; damaged/repaired/finished gate; dilapidated/scaffold/restored manor, no text, no scenery, no gradient background. Earlier brown-background variants were rejected and are not shipped.
- `drawable-nodpi/garden_work_props.webp`: 4×3; tulips, lavender, daisies, textured soil bed; broken slab, clean slab, watering can, shovel/rake/broom; wheelbarrow, toolbox/planks/paint, scaffold, butterfly. Source `exec-bb0a249b-0dc0-4bcd-be83-503d1bfdbba8.png`. Prompt requested isolated detailed hand-painted original game props with transparent gutters and no text.
- `drawable-nodpi/resident_work_atlas.webp`: 8×4, preserving the four existing resident identities. Two digging poses, two kneeling planting poses, two painting poses, watering, seated. Source `exec-0f6b77d0-f54b-4f18-bcc5-25d5e760b2f3.png`; original resident atlas supplied as identity reference. Prompt required full body, transparent margins, tools held naturally, no scenery. Consumed by native scene renderers rather than displayed as a sheet.
- `drawable-nodpi/match_obstacles.webp`: 3×2; garden crate, layered stone, soil, flower watering objective, seed parcel, songbird in basket. Source `exec-8ae8874b-807d-490a-a5fb-9c3598d80786.png`. Prompt requested original detailed readable small mobile-game pieces, six cells, no UI or proprietary designs.

Sound effects are original short PCM synthesis in `GardenSfx`; dialogue uses installed Android TTS, not recorded professional voices. No third-party commercial game material was used.
