
    // ROZPADACZ

    public static ItemStack rozpadacz(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Rozpadacz",
                NamedTextColor.DARK_RED
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .append(tekst(" zabiera każdemu w obrębie ", NamedTextColor.GRAY))
                .append(tekst("4 kratek", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("33% życia", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.empty());

        lore.add(tekst(
                "Cooldown: 1 minuta 30 sekund",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "rozpadacz"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }
