# SimpleXrayDetector
SimpleXrayDetector is a lightweight plugin for Minecraft servers (Spigot/Paper) designed to detect and prevent suspicious activities related to X-Ray cheats. It tracks players' mining behavior, particularly focusing on diamond ore, and calculates a "suspicious score" based on their actions. Administrators can monitor, reset, and analyze suspicious activities using intuitive commands.
## Suspicious Activity Detection:
- Tracks players who mine diamond ore (DIAMOND_ORE and DEEPSLATE_DIAMOND_ORE).
- Detects suspicious patterns, such as mining diamonds far apart in a short time.
## Commands:
### /suspiciousscore [nickname]
Check the suspicious score of yourself or another player.
### /resetsuspiciousscore [nickname]
Reset the suspicious score of yourself or another player.
### /lastdiamond [nickname]
Check the time since the last mined diamond for a player.
## Other
### Top Suspicious Players
Admins receive a top 5 list of the most suspicious players upon joining the server.
### Data Persistence:
All data (suspicious scores, last mined diamond times, player names) is stored in 
## **PERFECT TO USE WITH CORE PROTECT**
check players with this command

/coreprotect lookups include: diamond_ore || deepslate_diamond_ore action:-block user:[nickname from suspicious top]


<details>
<summary>Spoiler</summary>
todo:
multi-language support;  
resoursepack reload check;
coordinats of dug diamonds clusters
</details>

