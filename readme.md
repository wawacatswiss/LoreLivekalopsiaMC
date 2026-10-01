A roleplay and canon life management mod for the [Kalopsia SMP](https://discord.gg/FNU2FxFaYv).

## Features

- **Toggle Lore Mode**: Press **H** to toggle Lore Mode on or off at any time.
- **3 Canon Lives System**: Every player begins with 3 Canon Lives.
- **Canon Deaths**: Dying while Lore Mode is active deducts one of your Canon Lives.
- **Limbo Penalty**: Losing your final Canon Life teleports you to Limbo in Adventure Mode until revived by an administrator.
- **Tab List Display**: Active Lore Mode players display their remaining heart counts directly in the player list.
- **Withdrawable Hearts**: Convert your Canon Lives into physical Canon Heart items to trade, gift, or store.

## Commands

### Player Commands
- `/lore toggle` - Toggle Lore Mode on or off.
- `/lore status [player]` - View active mode and remaining canon lives.
- `/withdraw [amount]` - Withdraw Canon Lives into physical heart items (max 10 at a time).
- `/lore on` / `/lore off` - Set Lore Mode directly.

### Admin Commands (Op 2)
- `/loreadmin lives <targets> set <amount>` - Set the Canon Lives of selected player(s).
- `/loreadmin lives <targets> add <amount>` - Add Canon Lives to selected player(s).
- `/loreadmin lives <targets> take <amount>` - Remove Canon Lives from selected player(s).
- `/loreadmin revive <targets>` - Revive a player from Limbo to world spawn with 1 life in Survival Mode.
- `/loreadmin limbo set` - Set the destination coordinates for Limbo.
- `/loreadmin limbo send <targets>` - Teleport selected player(s) directly into Limbo.
- `/loreadmin mode <targets> [true|false]` - Force selected player(s) into or out of Lore Mode.
- `/loreadmin reset <targets>` - Reset player data back to default (3 lives and Survival Mode).
- `/loreadmin reload` - Reload configuration from disk.

### Death Logging
There's a logging system for deaths and revives. 
You can disable these in the config file or enable a discord webhook to log them to a discord channel

## Requirements

- **Minecraft**: 1.20.1
- **Fabric Loader**: 0.15.0+
- **Fabric API**
