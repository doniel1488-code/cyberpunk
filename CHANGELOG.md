# Changelog

## 0.1.0-alpha.4

- Increased Neural Accelerator to Speed III and Haste II and added a stronger electric/cloud afterimage.
- Optical Camo now provides Speed II with denser portal shimmer and end-rod glints.
- Berserk now grants Strength III, Resistance II, Fire Resistance and Speed I, with flame and critical-hit particles.
- Sandevistan now has a longer electric afterimage, stronger usage pulses and periodic activation audio.
- Each active implant sends subtle synchronized client-side visual pulses while it is running.
- Rebalanced neural load: 35% warning, 65% critical stage, 100% cyberpsychosis.
- Cyberpsychosis now interrupts the current implant, locks cyberware for 15 seconds, applies a short blindness flash plus 15 seconds of nausea/weakness and 6 seconds of slowness, then resets neural load to 30%.
- Added red screen interference and recurring heartbeat/particle feedback during overload.
- Fixed activation ordering so an implant cannot start after the same activation has triggered cyberpsychosis.

## 0.1.0-alpha.3

- Added per-implant activation sounds, particle bursts and client-side colored HUD flashes.
- Redesigned neural stress into warning, critical and overload thresholds.
- Added client-side visual payloads and Russian/English text for activation and neural-load cues.

## 0.1.0-alpha.2

- Added the Sandevistan «Apogee» implant with its own item icon, model, recipe and Russian/English localization.
- Added the B keybind and server-side activation payload.
- Added a 9-second Sandevistan field with a 64-block radius and a 15-second cooldown.
- Added a MobEntity Mixin that allows nearby mobs to run one full tick out of every seven while the field is active.
- Added Speed III to the owner and an electric afterimage particle trail.

## 0.1.0-alpha.1

- Initial Fabric 1.21.8 project structure with Java 21 and Fabric API.
- Added Neural Accelerator, Optical Camo and Berserk modules.
- Added server-side ability validation, cooldowns, neural stress, localization, recipes and original item icons.
