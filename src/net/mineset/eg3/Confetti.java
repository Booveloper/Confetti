package net.mineset.eg3;

import net.mineset.eg3.util.HoloAPI;
import net.mineset.eg3.util.ScoreHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Confetti extends JavaPlugin implements Listener {

    private File statsConfigFile;

    private FileConfiguration statsConfig;

    @Override
    public void onEnable() {

        createStatsConfig();

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }, 0, 20);

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        saveStatsConfig();
    }

    private void createScoreboard(Player player) {

        ScoreHelper helper = ScoreHelper.createScore(player);

        helper.setTitle("&a" + Bukkit.getOnlinePlayers().size() + " Playing...");
        helper.setSlot(8, "&r");
        helper.setSlot(7, "&cDeaths: &f" + getStatsConfig().getInt(player.getUniqueId() + ".deaths"));
        helper.setSlot(6, "&r");
        helper.setSlot(5, "&eMined Gold: &f" + getStatsConfig().getInt(player.getUniqueId() + ".gold"));
        helper.setSlot(4, "&bMined Diamonds: &f" + getStatsConfig().getInt(player.getUniqueId() + ".diamonds"));
        helper.setSlot(3, "&r");
        helper.setSlot(2, "&dElixir: &f" + getStatsConfig().getInt(player.getUniqueId() + ".XP"));
        helper.setSlot(1, "&9Current Level: &f" + getStatsConfig().getInt(player.getUniqueId() + ".level"));
    }

    private void updateScoreboard(Player player) {
        if(ScoreHelper.hasScore(player)) {

            ScoreHelper helper = ScoreHelper.getByPlayer(player);

            helper.setTitle("&a" + Bukkit.getOnlinePlayers().size() + " Playing...");
            helper.setSlot(7, "&cDeaths: &f" + getStatsConfig().getInt(player.getUniqueId() + ".deaths"));
            helper.setSlot(5, "&eMined Gold: &f" + getStatsConfig().getInt(player.getUniqueId() + ".gold"));
            helper.setSlot(4, "&bMined Diamonds: &f" + getStatsConfig().getInt(player.getUniqueId() + ".diamonds"));
            helper.setSlot(2, "&dElixir: &f" + getStatsConfig().getInt(player.getUniqueId() + ".XP"));
            helper.setSlot(1, "&9Current Level: &f" + getStatsConfig().getInt(player.getUniqueId() + ".level"));

        }

    }

    private ArrayList<Player> jumpers = new ArrayList<Player>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SLIME_BLOCK) {

            event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(2));
            event.getPlayer().setVelocity(new Vector(event.getPlayer().getVelocity().getX(), 0.5D, event.getPlayer().getVelocity().getZ()));

            jumpers.add(event.getPlayer());

            Player p = event.getPlayer();
            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 50, 0);
            // do particles

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Location spawn = new Location(Bukkit.getWorld("world"),  0.35, 69, -6.5);

        Player player = event.getPlayer();

        event.setJoinMessage(ChatColor.GOLD + " + " + ChatColor.YELLOW + player.getName() + " Joined the game");

        createScoreboard(player);
        createStats(player);

        player.sendMessage(ChatColor.GRAY + "If you didn't know already we have a discord with tons of widgets to play around with " + ChatColor.BLUE + "https://discord.gg/7AbCvB");


        HoloAPI drop = new HoloAPI(spawn, ChatColor.BLUE + "Welcome to Mineset Survival", ChatColor.GOLD + "Season I");
        drop.display(player);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        saveStatsConfig();

        event.setQuitMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + player.getName() + " Left the game");

        if (ScoreHelper.hasScore(player)) {

            ScoreHelper.removeScore(player);

        }

    }

    @EventHandler
    public void PublicChannel(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (e.getPlayer().isOp()) {
            e.setMessage(e.getMessage().replace("&", "§"));
        }

        String Rank = "";
        if (p.isOp()) {
            Rank = "§c§lADMIN §7";
            e.setFormat("§9" + getStatsConfig().getInt(p.getUniqueId().toString() + ".level") + " " + Rank + "" + p.getName() + "§7 ➟ §f" + e.getMessage());
        } else {
            e.setFormat("§9" + getStatsConfig().getInt(p.getUniqueId().toString() + ".level") + "§7 " + p.getName() + "§7 ➟ §f" + e.getMessage());
        }



    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        player.sendMessage(ChatColor.RED + "You died! Teleported to spawn...");
        player.sendMessage(ChatColor.RED + "+ 1 Death");

        addDeath(player);

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = (Player) event.getPlayer();
        Block block = (Block) event.getBlock();

        if (block.getType() == Material.COAL_ORE) {

            block.setType(Material.AIR);

            player.getInventory().addItem(new ItemStack(Material.COAL));
            player.sendMessage(ChatColor.GRAY + "+ 1 Coal");

            mineCoal(player);

            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50, 0);

        }

        if (block.getType() == Material.GOLD_ORE) {

            block.setType(Material.AIR);

            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
            player.sendMessage(ChatColor.YELLOW + "+ 1 Gold");

            mineGold(player);
            addXP(player);

            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50, 0);

        }

        if (block.getType() == Material.IRON_ORE) {

            block.setType(Material.AIR);

            player.getInventory().addItem(new ItemStack(Material.IRON_INGOT));
            player.sendMessage(ChatColor.WHITE + "+ 1 Iron");

            mineIron(player);

            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50, 0);

        }

        if (block.getType() == Material.DIAMOND_ORE) {

            block.setType(Material.AIR);

            player.getInventory().addItem(new ItemStack(Material.DIAMOND));
            player.sendMessage(ChatColor.AQUA + "+ 1 Diamond");

            mineDiamond(player);
            addXP(player);

            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50, 0);

        }
    }

    public FileConfiguration getStatsConfig() {
        return this.statsConfig;
    }

    public void saveStatsConfig() {
        try {
            statsConfig.save(statsConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createStatsConfig() {
        statsConfigFile = new File(getDataFolder(), "stats.yml");

        if (!statsConfigFile.exists()) {
            statsConfigFile.getParentFile().mkdirs();
            saveResource("stats.yml", false);

        }

        statsConfig= new YamlConfiguration();
        try {
            statsConfig.load(statsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void createStats(Player p) {
        if(getStatsConfig().get(p.getUniqueId().toString()) == null) {


            getStatsConfig().set(p.getUniqueId().toString() + ".level", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".deaths", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".coal", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".gold", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".iron", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".diamonds", 0);
            saveStatsConfig();
            updateScoreboard(p);

        }

    }

    public void addDeath(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".deaths", getStatsConfig().getInt(p.getUniqueId().toString() + ".deaths") + 1);
        saveStatsConfig();
        updateScoreboard(p);

    }

    public void mineCoal(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".coal", getStatsConfig().getInt(p.getUniqueId().toString() + ".coal") + 1);
        saveStatsConfig();
        updateScoreboard(p);

    }

    public void mineGold(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".gold", getStatsConfig().getInt(p.getUniqueId().toString() + ".gold") + 1);
        saveStatsConfig();
        updateScoreboard(p);

    }

    public void mineIron(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".iron", getStatsConfig().getInt(p.getUniqueId().toString() + ".iron") + 1);
        saveStatsConfig();
        updateScoreboard(p);

    }

    public void mineDiamond(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".diamonds", getStatsConfig().getInt(p.getUniqueId().toString() + ".diamonds") + 1);
        saveStatsConfig();
        updateScoreboard(p);

    }

    public void addXP(Player p) {
        Random r = new Random();
        int low = 2;
        int high = 12;
        int result = r.nextInt(high-low) + low;
        if (getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") < 45) {
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result);
            saveStatsConfig();
            p.sendMessage("§d+ " + result + " XP");
        } else if ((getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result) < 50) {
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result);
            saveStatsConfig();
            p.sendMessage("§d+ " + result + " XP");
        } else {
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", (getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result) - 50);
            getStatsConfig().set(p.getUniqueId().toString() + ".level", getStatsConfig().getInt(p.getUniqueId().toString() + ".level") + 1);
            Bukkit.getServer().broadcastMessage("§9➥ " + p.getName() + " has leveled up to level " + getStatsConfig().getInt(p.getUniqueId().toString() + ".level"));
            saveStatsConfig();
            p.sendMessage("§d+ " + result + " XP");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUnknown(PlayerCommandPreprocessEvent event) {
        if (!(event.isCancelled())) {

            Player player = event.getPlayer();
            String errorMSG = event.getMessage().split(" ")[0];
            HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(errorMSG);

            if (topic == null) {

                player.sendMessage("Unknown command!");
                event.setCancelled(true);

            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This plugin is for players only!");
            return true;
        }

        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("setspawn")) {
            getConfig().set("spawn.world", p.getLocation().getWorld().getName());
            getConfig().set("spawn.x", p.getLocation().getX());
            getConfig().set("spawn.y", p.getLocation().getY());
            getConfig().set("spawn.z", p.getLocation().getZ());
            saveConfig();
            p.sendMessage(ChatColor.GREEN + "Spawn was set!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("spawn")) {
            if (getConfig().getConfigurationSection("spawn") == null) {
                p.sendMessage(ChatColor.RED + "Error");
                return true;
            }
            World w = Bukkit.getServer().getWorld(getConfig().getString("spawn.world"));
            double x = getConfig().getDouble("spawn.x");
            double y = getConfig().getDouble("spawn.y");
            double z = getConfig().getDouble("spawn.z");
            p.teleport(new Location(w, x, y, z));
            p.sendMessage(ChatColor.GREEN + "Welcome to the Mineset spawn!");
        }
        return true;
    }
}

