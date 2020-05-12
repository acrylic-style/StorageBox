# StorageBox
Storage Box on Bukkit!

You can contain any blocks into Storage Box with almost unlimited amounts, and with auto collect feature!

## Installation
- Get 1.15.2 spigot server (or Paper). It uses NMS, so this plugin won't work with other versions.
- Build this repository using Maven (`mvn install`)
- Install into your server
- There is no configuration!
- Run your server!

## How to use
- Get 8 diamonds and a chest, then run `/storage new` (for survival players)
- (if you don't want to get items, just do `/storage bypass` with op privileges)
- Change item of storage box via `/storage changetype`, while you're holding a block in offhand.
- (if you're op, you can use `/storage setamount <amount>` for set amounts in storage box, but there is no 'unlimited' mode at this moment)
- Extract the items into inventory via `/storage extract <amount>`.
- Delete the storage box via `/storage delete` (please note, it also deletes items in the storage box)
- Switch the auto collect mode (which you can collect items automatically into storage box when you pickup the item) via `/storage autocollect`
