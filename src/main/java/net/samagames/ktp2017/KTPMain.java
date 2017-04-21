package net.samagames.ktp2017;

import net.samagames.api.SamaGamesAPI;
import org.bukkit.plugin.java.JavaPlugin;
import static org.bukkit.Bukkit.getWorlds;

public class KTPMain extends JavaPlugin {

    /**
     *  This is the entry point of the KTP2017Game Game.
     *  @author Vialonyx
     */

    private static KTPMain instance;
    private KTP2017Game game;

    @Override
    public void onEnable(){

        instance = this;

        // Registering game on SamaGamesAPI
        this.game = new KTP2017Game(this, "code", "KTP2017Game", "description", KTPPlayer.class);
        SamaGamesAPI.get().getGameManager().setFreeMode(true);
        SamaGamesAPI.get().getGameManager().registerGame(this.game);

        // Updating gamerules values.
        getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
        getWorlds().get(0).setTime(6000);

    }

    public static KTPMain getInstance(){
        return instance;
    }

    public KTP2017Game getGame(){
        return this.game;
    }

}