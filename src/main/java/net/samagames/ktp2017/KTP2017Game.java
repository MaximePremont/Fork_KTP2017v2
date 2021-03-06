package net.samagames.ktp2017;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Game;
import net.samagames.ktp2017.events.GameEndEvent;
import net.samagames.tools.Titles;
import org.bukkit.*;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getOnlinePlayers;
import static org.bukkit.Bukkit.getWorlds;

/*
 * This file is part of KTP2017².
 *
 * KTP2017² is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KTP2017² is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KTP2017².  If not, see <http://www.gnu.org/licenses/>.
 */
public class KTP2017Game extends Game<KTPPlayer> {

    private GamePhase current;
    private WorldBorder worldBorder;
    private List<KTPArea> avaibleAreas;
    private KTPArea currentlyPlayedArea;
    BukkitTask remotenessTask;
    Random random;

    // Creating KTP arenas
    public static KTPArea area1 = new KTPArea(1);
    public static KTPArea area2 = new KTPArea(2);
    public static KTPArea area3 = new KTPArea(3);
    public static KTPArea area4 = new KTPArea(4);

    public KTP2017Game() {

        super("ktp2017", "KTP2017²", "Awersome !", KTPPlayer.class);

        // Initializing all the things
        this.avaibleAreas = new ArrayList<>();
        this.random = new Random();
        this.worldBorder = getWorlds().get(0).getWorldBorder();
        this.worldBorder.setSize(32);
        this.worldBorder.setWarningDistance(3);
        this.worldBorder.setDamageAmount(0);

        // Registering areas
        this.avaibleAreas.add(KTP2017Game.area1);
        this.avaibleAreas.add(KTP2017Game.area2);
        this.avaibleAreas.add(KTP2017Game.area3);
        this.avaibleAreas.add(KTP2017Game.area4);

        // Setting current phase to WAIT
        this.updateGamePhase(GamePhase.WAIT);

        // Preparing area
        this.setupArea(getRandomlyArea());

        /* Remoteness detection temporary disabled. Fixed in the next commits.
        this.remotenessTask = KTPMain.getInstance().getServer().getScheduler().runTaskTimer(KTPMain.getInstance(), new Runnable() {

            @Override
            public void run() {

                for(Player p : getOnlinePlayers()){
                    if(p.getLocation().distance(getCurrentlyPlayedArea().getCheckableEntity().getLocation()) >= 20){
                        p.teleport(getCurrentlyPlayedArea().getAreaLocation().clone().add(0.5, 10.00D, 0.5));
                    }
                }

            }

        }, 0L, 100L);*/

        // Debugging game variables during developement phase
        logDebug();

    }

        /**
         * handleLogin : Called by SamaGamesAPI on player connexion.
         * @param player The player.
         */

    @Override
    public void handleLogin(Player player){

        if(this.getCurrentGamePhase() == GamePhase.WAIT || this.getCurrentGamePhase() == GamePhase.AREA_STARTED){
            preparePlayer(player);
        } else {
            this.setSpectator(player);
            player.setGameMode(GameMode.SPECTATOR);
        }

        logDebug();

    }

        /**
         * handleLogin : Called by SamaGamesAPI on player quit.
         * @param player The player.
         */

    @Override
    public void handleLogout(Player player){

        if(this.getCurrentlyPlayedArea().getAreaPlayers().contains(player.getUniqueId())){
            KTPMain.getInstance().getGame().eliminatePlayer(player);
        }

    }

    public enum GamePhase {

        WAIT, AREA_STARTED, GAME_COMBAT, GAME_DONE

    }

        /**
         * setupArea : Prepare area to be played.
         * @param area The arena.
         */

    public void setupArea(KTPArea area){

        // Setting-up WorldBorder
        this.worldBorder.setCenter(area.getAreaLocation());

        // Update the current played Area
        this.currentlyPlayedArea = area;

        // Update the Game Phase
        this.updateGamePhase(GamePhase.AREA_STARTED);

        KTPMain.getInstance().getLogger().log(Level.INFO, "Area " + area.getAreaId() + " sucessfully installed.");

    }

        /**
         * preparePlayer : Prepare player to play.
         * @param area The player.
         */

    public void preparePlayer(Player player){

        KTPMain.getInstance().getLogger().log(Level.INFO, "PREPARING PLAYER " + player);

        if(this.getSpectatorPlayers().containsKey(this.getPlayer(player.getUniqueId()))){
            this.getSpectatorPlayers().remove(this.getPlayer(player.getUniqueId()));
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(0.1);
        player.setHealthScale(0.1);
        this.getCurrentlyPlayedArea().joinArea(player.getUniqueId());
        player.teleport(this.getCurrentlyPlayedArea().getAreaLocation());

    }

        /**
         * eliminatePlayer : Called when a player died.
         * @param area The player.
         */

    public void eliminatePlayer(Player player){
        Titles.sendTitle(player, 20, 60, 20, ChatColor.RED + "WASTED", "");
        player.setVelocity(player.getLocation().getDirection().multiply(-0.5));
        this.getCurrentlyPlayedArea().leaveArea(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        this.setSpectator(player);
        this.checkWinDetection();
    }

    public void checkWinDetection(){
        if(this.getCurrentlyPlayedArea().getAreaPlayers().size() == 1){

            Player winner = Bukkit.getPlayer(this.getCurrentlyPlayedArea().getAreaPlayers().first());

            FireworkEffect fwWinner_one = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.RED).withFade(Color.SILVER).withFlicker().build();
            FireworkEffect fwWinner_two = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.YELLOW).withFade(Color.AQUA).withFlicker().build();
            FireworkEffect fwWinner_three = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.BLUE).withFade(Color.ORANGE).withFlicker().build();

            SamaGamesAPI.get().getGameManager().getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.AQUA + "Victoire de " + ChatColor.RED + ChatColor.BOLD + winner.getDisplayName() + " !", true);

            getOnlinePlayers().forEach(player -> Titles.sendTitle(player, 20, 100, 20, ChatColor.RED + winner.getDisplayName(), "" + ChatColor.YELLOW + ChatColor.BOLD + "a gagné la partie !"));
            getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 20, 20));

            new BukkitRunnable(){

                int seconds = 0;

                @Override
                public void run(){

                    seconds++;
                    if(seconds == 6){
                        KTPMain.getInstance().getServer().getPluginManager().callEvent(new GameEndEvent(winner.getUniqueId()));
                        this.cancel();
                        return;
                    }

                    Utils.launchfw(winner.getLocation(), fwWinner_one);
                    Utils.launchfw(winner.getLocation(), fwWinner_two);
                    Utils.launchfw(winner.getLocation(), fwWinner_three);

                }

            }.runTaskTimer(KTPMain.getInstance(), 0L, 20L);

        }
    }

    public void logDebug(){
        KTPMain.getInstance().getLogger().log(Level.INFO,"------- DEBUG -------");
        KTPMain.getInstance().getLogger().log(Level.INFO,"Current game phase : " + this.getCurrentGamePhase());
        KTPMain.getInstance().getLogger().log(Level.INFO,this.avaibleAreas.size() + " areas registered. " + this.avaibleAreas.toString());
        KTPMain.getInstance().getLogger().log(Level.INFO,"Current area : " + this.getCurrentlyPlayedArea());
        KTPMain.getInstance().getLogger().log(Level.INFO,"-------- END --------");
    }

    public void updateGamePhase(GamePhase phase){
        this.current = phase;
    }

    public GamePhase getCurrentGamePhase(){
        return this.current;
    }

        /**
         * Get randomly the next area.
         * @return The next area (randomly selected and cannot be same than the previous)
         */

    public KTPArea getRandomlyArea(){

        KTPArea suggest = this.avaibleAreas.get(this.random.nextInt(this.avaibleAreas.size()));
        if(this.getCurrentlyPlayedArea() == null){

            return this.avaibleAreas.get(0);

        } else if (suggest.getAreaId() == this.getCurrentlyPlayedArea().getAreaId()){

            // It's so bad - ugly, i know. I'll try to get back to it later.
            if(this.getCurrentlyPlayedArea().getAreaId()+1 >= this.avaibleAreas.size()){
                return this.avaibleAreas.get(this.getCurrentlyPlayedArea().getAreaId()-2);
            } else if (this.getCurrentlyPlayedArea().getAreaId()+1 < this.avaibleAreas.size()){
                return this.avaibleAreas.get(this.getCurrentlyPlayedArea().getAreaId()+1);
            }

        }

        return suggest;

    }

        /**
         * Get the current played area.
         * @return The area.
         */

    public KTPArea getCurrentlyPlayedArea(){
        return this.currentlyPlayedArea;
    }

}
