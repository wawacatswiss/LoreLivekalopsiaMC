# Lore & Canon Lives

## Features

- **Toggle lore mode**: press "H" to toggle the lives system on or off 
- **3 Canon Lives System**: Every player starts with 3 Canon Lives.
- **Deaths**: Dying with lore mode makes you lose one of you lives
- **Limbo**: Dying with your last life puts you into limbo (a place where the mods set where you respawn)
- **Tab List**: On the player list you can see players of how many hearts they're on if they have lore mode on
- **Physical hearts**: You can withdraw lore hearts to trade with other people
---

## Commands

### Player Commands
- `/lore toggle` - Toggle Lore Mode on or off.
- `/lore status [player]` - View active mode and remaining canon lives.
- `/withdraw [amount]` - Withdraw Canon Lives into physical heart items.

### Admin Commands (Op 2)
- `/loreadmin lives <targets> set <amount>` - Sets lives of selected player.
- `/loreadmin lives <targets> add <amount>` - Gives lives of the selected player.
- `/loreadmin lives <targets> take <amount>` - Takes lives of the selected player.
- `/loreadmin revive <targets>` - Revives the player from limbo into world spawn and gives them 1 life
- `/loreadmin limbo set` - Sets the destination of limbo.
- `/loreadmin limbo send <targets>` - Teleports players into limbo
- `/loreadmin mode <targets> [true|false]` - Forces the selected player into lore mode.
- `/loreadmin reset <targets>` - Resets the player (3 lives and survival mode).
- `/loreadmin reload` - Reload configuration file.

---

## Requirements
- **Minecraft**: 1.20.1
- **Fabric Loader**: 0.15.0+
- **Fabric API**
