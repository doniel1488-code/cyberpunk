# Changelog

## 0.1.0-alpha.3

- Added per-implant activation sounds, particle bursts and client-side colored HUD flashes.
- Redesigned neural stress into warning, critical and overload thresholds.
- Cyberpsychosis now disables active cyberware for 12 seconds and applies temporary blindness, nausea, weakness and slowness.
- Stress now decays gradually while cyberware is idle; each implant adds a different amount of neural load.
- Improved active effects for Neural Accelerator, Optical Camo, Berserk, Sandevistan and Mantis Arms.
- Added Russian and English text for neural-load warnings and activation cues.
- Added CI checks for the visual payload, client visuals and locale JSON parity.

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
