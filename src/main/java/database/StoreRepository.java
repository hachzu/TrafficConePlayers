package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static database.AchievementRepository.creditWallet;
import static database.AchievementRepository.number;
import static database.AchievementRepository.recordTransaction;
import static database.AchievementRepository.update;

public final class StoreRepository {
    public static final int GUARANTEE = 12;

    public enum Crate {
        COMMON("Common", 150, 84, 12, 2, 1, 1),
        RARE("Rare", 300, 54, 34, 9, 2, 1),
        EPIC("Epic", 500, 25, 40, 29, 5, 1),
        LEGENDARY("Legendary", 800, 8, 24, 45, 19, 4),
        MYTHIC("Mythic", 1500, 4, 10, 30, 44, 12);

        public final String title;
        public final int price;
        private final int[] odds;

        Crate(String title, int price, int... odds) {
            this.title = title;
            this.price = price;
            this.odds = odds;
        }

        public int chance(int rarity) {
            return odds[rarity];
        }
    }

    public record Preset(String title, String asset, Crate rarity) {
        @Override public String toString() { return title; }
    }

    private static final List<Preset> PRESETS = buildPresets();

    public record Inventory(int balance, int untilGuarantee) { }
    public record Pull(Preset preset, int refund) { }

    private StoreRepository() { }

    private static List<Preset> buildPresets() {
        String[][] names = {
            {"Copper", "Amber", "Clay", "Sand", "Sage", "Moss", "Pine", "Slate",
                    "Mist", "Denim", "Lilac", "Rose", "Coral", "Peach", "Oat"},
            {"Lagoon", "Orchid", "Ember", "Glacier", "Citrus", "Tide", "Plum", "Jade"},
            {"Neon Reef", "Solar Flare", "Ultraviolet", "Cherry Static", "Acid Rain", "Blue Hour"},
            {"Golden Grid", "Royal Voltage", "Crimson Crown", "Aurora Gold"},
            {"Prismatic Dawn", "Event Horizon"}
        };
        List<Preset> presets = new ArrayList<>();
        for (Crate rarity : Crate.values()) {
            String folder = rarity.name().toLowerCase(Locale.ROOT);
            String[] titles = names[rarity.ordinal()];
            for (int index = 0; index < titles.length; index++) {
                presets.add(new Preset(titles[index], "/assets/presets/gacha/" + folder
                        + "/" + (index + 1) + ".properties", rarity));
            }
        }
        presets.add(new Preset("Roadwork Orange", "/assets/presets/roadwork.properties", Crate.COMMON));
        presets.add(new Preset("Midnight Detour", "/assets/presets/midnight.properties", Crate.RARE));
        presets.add(new Preset("Golden Gridlock", "/assets/presets/gridlock.properties", Crate.LEGENDARY));
        return List.copyOf(presets);
    }

    public static Inventory loadInventory(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return new Inventory(number(connection,
                    "SELECT balance FROM currency_wallet WHERE user_id = ?", userId), GUARANTEE - misses(connection, userId));
        }
    }

    public static Pull open(int userId, Crate crate) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Catalog writes use a named lock because skin_asset_path is not unique.
            if (number(connection, "SELECT GET_LOCK('tcp_gacha_catalog', 5)") != 1) {
                throw new SQLException("Gacha catalog is busy.");
            }
            connection.setAutoCommit(false);
            try {
                lockUser(connection, userId);
                if (update(connection, "UPDATE currency_wallet SET balance = balance - ? "
                        + "WHERE user_id = ? AND balance >= ?", crate.price, userId, crate.price) != 1) {
                    connection.rollback();
                    return null;
                }
                Crate rarity = choose(crate, misses(connection, userId),
                        ThreadLocalRandom.current().nextInt(100));
                List<Preset> pool = PRESETS.stream().filter(preset -> preset.rarity == rarity
                        && preset.asset.startsWith("/assets/presets/gacha/")).toList();
                Preset preset = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
                int skinId = registerSkin(connection, preset);
                int refund = 0;
                if (number(connection, "SELECT COUNT(*) FROM user_skin WHERE user_id = ? AND skin_id = ?",
                        userId, skinId) > 0) {
                    refund = crate.price / 4;
                    creditWallet(connection, userId, refund);
                    recordTransaction(connection, userId, refund, "EARN",
                            "Gacha duplicate: " + preset.title);
                } else {
                    update(connection, "INSERT INTO user_skin (user_id, skin_id) VALUES (?, ?)",
                            userId, skinId);
                }
                recordTransaction(connection, userId, crate.price, "SPEND", "Gacha: " + crate.title);
                update(connection, "INSERT INTO gacha_transaction (user_id, skin_id, currency_spent) "
                        + "VALUES (?, ?, ?)", userId, skinId, crate.price);
                connection.commit();
                return new Pull(preset, refund);
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    private static int registerSkin(Connection connection, Preset preset) throws SQLException {
        String lookup = "SELECT skin_id FROM skin WHERE skin_asset_path = ? ORDER BY skin_id LIMIT 1 FOR UPDATE";
        int id = number(connection, lookup, preset.asset);
        if (id != 0) {
            return id;
        }
        // The existing SQL enum stops at Legendary. The asset path identifies Mythics.
        String storedRarity = preset.rarity == Crate.MYTHIC ? "LEGENDARY" : preset.rarity.name();
        update(connection, "INSERT INTO skin (skin_name, rarity, skin_asset_path) VALUES (?, ?, ?)",
                preset.title, storedRarity, preset.asset);
        return number(connection, "SELECT LAST_INSERT_ID()");
    }

    static Crate choose(Crate crate, int misses, int roll) {
        int cumulative = 0;
        for (Crate rarity : Crate.values()) {
            cumulative += crate.chance(rarity.ordinal());
            if (roll < cumulative) {
                return misses >= GUARANTEE - 1 && rarity.ordinal() < Crate.LEGENDARY.ordinal()
                        ? Crate.LEGENDARY : rarity;
            }
        }
        throw new IllegalArgumentException("Invalid gacha roll.");
    }

    private static int misses(Connection connection, int userId) throws SQLException {
        String sql = "SELECT s.rarity, s.skin_asset_path FROM gacha_transaction g "
                + "JOIN skin s ON g.skin_id = s.skin_id WHERE g.user_id = ? "
                + "AND s.skin_asset_path LIKE '/assets/presets/gacha/%' "
                + "ORDER BY g.gacha_id DESC LIMIT 11";
        int count = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    Crate rarity = rows.getString("skin_asset_path").contains("/mythic/")
                            ? Crate.MYTHIC : Crate.valueOf(rows.getString("rarity"));
                    if (rarity.ordinal() >= Crate.LEGENDARY.ordinal()) {
                        break;
                    }
                    count++;
                }
            }
        }
        return count;
    }

    public static List<Preset> owned(int userId) throws SQLException {
        List<Preset> presets = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            for (Preset preset : PRESETS) {
                if (owns(connection, userId, preset) > 0) {
                    presets.add(preset);
                }
            }
        }
        return presets;
    }

    public static Preset equipped(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            for (Preset preset : PRESETS) {
                if (number(connection, "SELECT COUNT(*) FROM user_skin us JOIN skin s "
                        + "ON s.skin_id = us.skin_id WHERE us.user_id = ? AND us.is_equipped = 1 "
                        + "AND s.skin_asset_path = ?", userId, preset.asset) > 0) {
                    return preset;
                }
            }
        }
        return null;
    }

    public static boolean equip(int userId, Preset preset) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockUser(connection, userId);
                int skinId = preset == null ? 0 : owns(connection, userId, preset);
                if (preset != null && skinId == 0) {
                    connection.rollback();
                    return false;
                }

                update(connection, "UPDATE user_skin SET is_equipped = 0 WHERE user_id = ?", userId);
                if (preset != null) {
                    update(connection, "UPDATE user_skin SET is_equipped = 1 "
                            + "WHERE user_id = ? AND skin_id = ?", userId, skinId);
                }
                connection.commit();
                return true;
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    private static int owns(Connection connection, int userId, Preset preset) throws SQLException {
        return number(connection, "SELECT MIN(s.skin_id) FROM user_skin us JOIN skin s "
                + "ON s.skin_id = us.skin_id WHERE us.user_id = ? AND s.skin_asset_path = ?",
                userId, preset.asset);
    }

    private static void lockUser(Connection connection, int userId) throws SQLException {
        if (number(connection, "SELECT user_id FROM `user` WHERE user_id = ? FOR UPDATE", userId) == 0) {
            throw new SQLException("Player account does not exist.");
        }
    }
}
