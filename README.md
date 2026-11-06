# Minecraft Plugin: Graviton Mace Spell

Graviton Mace Spell is a Minecraft plugin for Paper servers that adds a spell mace with increased damage, upward mob launching, and a temporary gravitational tether that pulls hit mobs toward the player.

## Install

1. Use Java 21.
2. Use a Paper Minecraft 1.21.11 server.
3. Build the plugin with Maven:

   ```bash
   mvn package
   ```

4. Copy the generated `target/plugin.jar` file into your server's `plugins` folder.
5. Start or restart the Paper server.
6. Confirm that the plugin is enabled.

## Use

1. Join the server as an operator or use a player with the `macespell.use` permission.
2. Run:

   ```text
   /macespell
   ```

3. Hold the received Mace of the Tether in your main hand.
4. Hit a living non-player entity with the mace.
5. The hit deals 1.5 times the normal damage, launches the target upward, and tethers it to you for 60 ticks.
6. While the tether is active, the target is pulled toward you every 2 ticks when you are in the same world, within 32 blocks, and online.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/f1b68797-74e7-4eae-a308-d2ea17a94bfc?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=f1b68797-74e7-4eae-a308-d2ea17a94bfc
