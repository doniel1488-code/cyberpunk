# Research and porting notes

Target: Minecraft Java 1.21.8, Fabric, Java 21.

## Projects reviewed

| Project | Publicly listed content | Compatibility | License / decision |
|---|---|---|---|
| [Psych0se - Cyberpunk](https://modrinth.com/mod/psych0se-cyberpunk/) | Implants, weapons, structures | NeoForge 1.21.8 | ARR on Modrinth. Do not copy code or assets. |
| [Project: Edgerunner](https://modrinth.com/project/project-edgerunner) | Cyberware slots, Ripperdoc, cyberpsychosis | Forge 1.20.1 | CC-BY-NC-SA-4.0 on Modrinth. Any direct derivative must follow attribution, non-commercial and share-alike terms. No source or assets copied into this repository. |
| [Minerunner 2077](https://www.curseforge.com/minecraft/mc-mods/minerunner-2077) | Cyberware and combat abilities | Forge/Fabric 1.20.1 | All Rights Reserved. Do not copy code or assets. |
| [Sandevistan](https://github.com/watsbunny/sandevistan) | Time dilation, visual trail, HUD and cyberpsychosis | Fabric 1.20.1 | No separate reusable license was confirmed during review. Only high-level feature ideas are used; no code or assets copied. |
| [Create: Cybernetics](https://github.com/TimurSl/create-cyberimplants) | Multi-slot implants, surgical station and cyberdeck | NeoForge 1.21.1 + Create | Project metadata declares MIT, but this port does not import its source or assets. Its Create/NeoForge dependencies require a separate compatibility design. |
| [Cyberpunk 2077 Guns for Vic's Point Blank](https://modrinth.com/datapack/cyberpunk-2077-guns-for-vics-point-blank) | Weapon content pack for Vic's Point Blank | Fabric and datapack files exist for newer Minecraft versions | ARR. Do not redistribute or absorb its content. An optional compatibility layer may be considered later, if supported by its maintainer. |

## Implementation boundary

This repository is an independent Fabric implementation. It does not bundle third-party source code, models, textures, sounds, maps, dialogue, logos, or extracted game assets from the projects above. The first milestone reimplements generic mechanics: nearby-enemy time dilation, temporary optical camouflage, a berserk combat state, implant installation flags, cooldowns and neural stress.

## Compatibility policy

A mod being available for 1.21.8 on NeoForge does not make it a Fabric mod. A Fabric file for 1.20.1 also cannot be relabelled as 1.21.8. Each future dependency must be checked for:
- exact loader and Minecraft version;
- API compatibility and server/client distribution;
- source availability and license;
- whether required assets may be modified and redistributed.
