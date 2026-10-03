# Auto Vault Key (Fabric mod)

Client-side Fabric mod for Minecraft 1.21.1.

## What it does
- Watches your inventory and the ground near you for a **Heavy Core** (or, if
  you switch it, an **Enchanted Golden Apple**) and a **Trident**.
- The moment one appears, it right-clicks a matching **Vault** for you —
  but only if **both** of these are true:
  1. You are **currently holding the correct key** in your hand (Ominous
     Trial Key for the ominous vault, Trial Key for the normal one). The mod
     never swaps items into your hand for you — if you're not already
     holding it, nothing happens.
  2. You are **directly looking at it** (crosshair on it), within normal
     interact reach — same as if you'd right-clicked it yourself.
- Two keybinds, set by you in **Options > Controls > Auto Vault Key**
  (unbound by default):
  - **Toggle Auto Vault** — turn the whole thing on/off.
  - **Switch Ominous Trigger Item** — flip the ominous-vault trigger between
    Heavy Core and Enchanted Golden Apple.
- A blue/black config screen, opened from the **Mods** button (added by
  ModMenu) on the title screen / pause menu, works the same in singleplayer
  and multiplayer. It has the same two toggles plus a Trident on/off switch.

## Works on any server, and singleplayer
This is a 100% **client-side** mod (`"environment": "client"` in
`fabric.mod.json`). It doesn't need anything installed on the server — it
just sends the same right-click packet your client would normally send when
you right-click a block. That means it works unmodified on:
- Singleplayer
- Vanilla multiplayer servers
- Paper / Spigot servers
- Any other server type/version that has the Vault feature (1.21+)

You only need the mod (and Fabric API) installed on **your own client** —
nothing to install server-side, and nothing server-admins need to approve.

### A note on anti-cheat
Because the mod only ever fires when your crosshair is actually on the
vault, the packet it sends is indistinguishable from you right-clicking it
yourself — there's nothing here for reach-check/fake-look anti-cheat
(Grim, Vulcan, NCP, etc. on Paper/Spigot) to flag.

## Important limitations (please read)
- **The key must already be in your hand.** The mod checks your main-hand
  item every tick; it never selects or swaps items for you. If the trigger
  item appears while you're holding something else, it just won't do
  anything — no message, since this check runs constantly and would spam
  otherwise.
- It only opens a vault you're **directly looking at**, within normal
  interact reach — same as a real right-click, it's not teleporting keys
  into distant or out-of-sight vaults.
- If the vault is on cooldown/already mid-animation, it still tries; the
  server just won't do anything if it isn't valid, same as a normal
  right-click at the wrong moment.
- This is a **client-side convenience mod**. It automates something you
  could do by hand (walk up, hold key, right-click) faster and more
  reliably. Some multiplayer servers restrict "auto-clicker" style mods
  regardless of what they automate — check your server's rules before using
  it there.
- I wrote and reviewed this code carefully against the known 1.21.1 Fabric
  API, but I don't have a Minecraft/Java build environment available where I
  built this, so it hasn't been compiled or run. If a method name has moved
  between Yarn mapping builds, you may need a small fix — see below.

## Building
0. This zip includes `gradle-wrapper.properties` but not the wrapper jar
   itself (no internet access when I generated this). Easiest path: open the
   `autovault/` folder in **IntelliJ IDEA** with the Gradle plugin — it will
   fetch everything automatically. Or, if you have Gradle installed
   locally, run `gradle wrapper` once inside the folder to generate the
   missing `gradlew`/`gradlew.bat`/wrapper jar, then use those from then on.
1. Install a JDK 21.
2. From the `autovault/` folder: `./gradlew build` (Windows: `gradlew.bat build`).
3. The mod jar appears in `build/libs/autovault-1.0.0.jar`.
4. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1,
   drop the jar into `.minecraft/mods/`, along with:
   - [Fabric API](https://modrinth.com/mod/fabric-api) (required)
   - [ModMenu](https://modrinth.com/mod/modmenu) (optional — only needed for
     the Mods-list config screen; the keybinds work without it)

## If the build fails on a method/class name
The most likely spots to need a tweak after a Minecraft/mapping update are
in `VaultOpener.java`:
- `VaultBlock.OMINOUS` — the blockstate property name for "is this an
  ominous vault".
- `client.interactionManager.interactBlock(...)` — the client interaction
  call signature.

Everything else (keybindings, config, GUI) is plain, stable Fabric API and
shouldn't need changes.

## Project layout
```
autovault/
  build.gradle, settings.gradle, gradle.properties
  src/main/java/com/example/autovault/
    AutoVaultClient.java        - registers keybinds, ticks VaultOpener
    VaultOpener.java            - detection + auto key-use logic
    config/AutoVaultConfig.java - JSON-backed settings
    gui/AutoVaultConfigScreen.java - blue/black settings screen
    compat/ModMenuIntegration.java - hooks the screen into ModMenu
  src/main/resources/
    fabric.mod.json
    assets/autovault/lang/en_us.json
```
