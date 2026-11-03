# Main UI Reference

This page records the current window layout, colors, and button behavior. [`TCPUI.java`](../src/main/java/ui/TCPUI.java) contains the screen layout; [`TCPController.java`](../src/main/java/ui/TCPController.java) connects it to the player.

## Startup flow

`Main.main()` opens [`LandingPage.java`](../src/main/java/ui/LandingPage.java) first. The landing page uses `assets/landingpage.png`; Continue opens `TCPUI` and closes the landing page.

## Layout

The canvas is **920 × 360 px**. Coordinates start at the canvas's top-left corner. Bounds are written as `(x, y, width, height)`.

| Area | Bounds | Contents |
| --- | --- | --- |
| Video | `(10, 10, 370, 230)` | Local video or looping vinyl animation |
| Name display | `(390, 10, 520, 60)` | Current track title, playlist number, elapsed time, and duration |
| Utilities | `(390, 80, 520, 160)` | Volume slider, user placeholder, video toggle, settings cog |
| Timeline | `(0, 260, 920, 40)` | Seek slider |
| Controls | `(0, 310, 920, 50)` | Playback and utility buttons |

The canvas uses absolute positioning, so each area receives its own bounds. The title bar is **920 × 36 px** above the canvas. The canvas paints [`mainui_bg.png`](../src/main/resources/assets/mainui_bg.png), scaled to cover the canvas; overflow is cropped evenly from the center.

### Smaller controls

| Control | Size / range |
| --- | --- |
| Volume slider | `0–100`, starts at `70` |
| User button | `122 × 38 px` |
| Video toggle | `100 × 30 px` |
| Settings cog | `32 × 32 px`, no button container |
| Playback / utility icon buttons | `44 × 40 px` |
| Currency display | `112 × 40 px` |

## Color palette

| Use | Color | RGB |
| --- | --- | --- |
| Canvas fallback | `#FFA500` | `(255, 165, 0)` |
| Name and utility panels | `#231710` | `(35, 23, 16)` |
| Borders, title bar, button text | `#692600` | `(105, 38, 0)` |
| Buttons and empty slider track | `#FFE0B2` | `(255, 224, 178)` |
| Name display text | `#FFB428` | `(255, 180, 40)` |
| Slider fill and thumb | `#FF6600` | `(255, 102, 0)` |
| Control bar | `#FF8000` | `(255, 128, 0)` |
| Video surface behind the MP4 | `#000000` | `(0, 0, 0)` |

The timeline panel is transparent, so the canvas background shows through it. Its green `setBackground` value is not currently painted.

## What the controls do

| Control | Current behavior |
| --- | --- |
| Play / pause | Starts or pauses the selected local media. |
| VIDEO toggle | Shows the local video when on and the vinyl loop when off. Audio-only tracks disable the toggle and show the vinyl. |
| Playlist button | Opens the current media playlist. |
| Volume slider | Sets the local media and vinyl player volume from `0.0–1.0`. |
| Timeline slider | Seeks through the current local media and updates its position while playing. |
| User button | Empty dropdown placeholder; no menu items or action yet. |
| Settings cog | Clickable button with no action assigned yet. |
| Previous, stop, next | Control local media playback. |
| Shuffle | Toggles its icon; shuffle ordering is not connected yet. |
| Download | Opens the import dialog; selected files and URLs are added to the playlist. URLs are references, not downloaded files. |
| Store, games | Placeholder buttons. |
| Minimize / close | Minimize the window or close the app. The title bar can be dragged to move the window. |

## Code map

- **Screen layout:** [`TCPUI.java`](../src/main/java/ui/TCPUI.java). Contains component placement and styling without event handlers.
- **Button and slider behavior:** [`TCPController.java`](../src/main/java/ui/TCPController.java).
- **Media and playlist:** [`LocalPlayer.java`](../src/main/java/ui/player/LocalPlayer.java).
- **Player interface:** [`PlayerBackend.java`](../src/main/java/ui/player/PlayerBackend.java).
- **Vinyl animation:** [`VinylVideoPanel.java`](../src/main/java/ui/player/VinylVideoPanel.java), hosted inside `LocalPlayer`.
- **Visual helpers:** [`CanvasBackgroundPanel.java`](../src/main/java/ui/CanvasBackgroundPanel.java) and [`ThemedSliderUI.java`](../src/main/java/ui/ThemedSliderUI.java).

## Main UI assets

- Background: `src/main/resources/assets/mainui_bg.png`
- Vinyl loop: `src/main/resources/assets/media/vinyl-loop.mp4`
- Font: `src/main/resources/assets/fonts/LowresPixel-Regular.otf`
- Icons: `src/main/resources/assets/icons/`
