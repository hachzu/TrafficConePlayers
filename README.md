# TrafficConePlayers
A Personalized Customizable Utility Player

See the [main UI reference](docs/ui-reference.md) for panel dimensions, colors, and control behavior.

See the [database ERD](docs/database/traffic-cone-player-erd.png) for tables, keys, and relationships.

See the [database defense walkthrough](docs/database-defense.md) for the JDBC and SQL flow.

## MySQL connection

Set `DB_USER` and `DB_PASSWORD` in the environment used to launch the player.
For PowerShell 7, set both in the same terminal where you run Maven.

```powershell
$env:DB_USER = "tcp_java_app"
$env:DB_PASSWORD = Read-Host "MySQL password" -MaskInput
mvn javafx:run
```

Use your MySQL connection credentials here. The player sign-in uses its own
account from the `user` table.

## Saved local media

Start MySQL with the `trafficplayers_db` schema and run the player from the project folder. Click **Sign In** on the landing page. Enter your username and password, then choose **Sign in** for an existing account or **Create account** to register. The player uses that account’s `user_id` for saved media until it closes. New classroom accounts store plain password strings in the existing `password_hash` column. Use demo passwords only. Accounts whose passwords were stored as hashes need a password reset. Use Import to add a local track, then open Playlist to play or remove it. Imports are copied to `src/main/resources/Tracks/Imported` and indexed in MySQL. Removing a saved track deletes its database entry and keeps the file on disk.

## Named playlists

Open **Playlist**, choose **Create playlist**, and open the playlist you named.
Use **Add track** to add imported tracks in playback order. **Play from
selection** starts the saved queue at the highlighted track. **Remove from
playlist** removes only that entry. Choose **All tracks** to return to the
full library.

## Achievements and Cones

Click the medal button on the player toolbar to open **Achievements**. Each row
shows the requirement and its reward in **Cones**. Select **Claim** after meeting
the requirement. Runtime achievements count completed sessions. **Settings**
offers a one-time purchase for 5,000 Cones to unlock customization.

## Roadside Gacha

Open **Store** to spend Cones and open a crate immediately. The five crates cost
150, 300, 500, 800, and 1,500 Cones. All crates share one 12-pull pity counter. After 11 pulls without a Legendary
or Mythic, the next pull guarantees at least Legendary. Either rarity resets it. Duplicate themes refund a quarter of the price, rounded down.
Equip an owned preset from **Settings → Themes**.

The app registers themes on their first pull using the existing database tables.
Mythic presets use the existing Legendary database value and are identified by
their preset paths.
