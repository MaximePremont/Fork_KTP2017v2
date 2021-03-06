package net.samagames.ktp2017;

import com.google.gson.JsonObject;
import net.samagames.api.SamaGamesAPI;
import net.samagames.ktp2017.events.AreaJoinEvent;
import net.samagames.tools.LocationUtils;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.TreeSet;
import java.util.UUID;
import static org.bukkit.Bukkit.getWorld;

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
public class KTPArea {

    private int areaId;
    private Location areaLocation;
    private TreeSet<UUID> inArea;

    public KTPArea(int id){

        this.areaId = id;
        this.inArea = new TreeSet<UUID>();
        JsonObject configuration = SamaGamesAPI.get().getGameManager().getGameProperties().getConfigs();
        World areaWorld = getWorld(configuration.get("world-name").getAsString());
        this.areaLocation = LocationUtils.str2loc(areaWorld.getName() + ", " + configuration.get("area_" + this.areaId).getAsString());

    }

        /**
         * joinArea : Add player on area's players list and call the AreaJoinEvent.
         * @param player The player.
         */

    public void joinArea(UUID player){
        this.inArea.add(player);
        KTPMain.getInstance().getServer().getPluginManager().callEvent(new AreaJoinEvent(player, this));
    }

        /**
         * leaveArea : Remove player from area's players list.
         * @param player The player.
         */

    public void leaveArea(UUID player){
        this.inArea.remove(player);
    }

        /**
         * Get the area's ID.
         * @return The area's ID.
         */

    public int getAreaId(){
        return this.areaId;
    }

        /**
         * Get the area's location.
         * @return The area's location.
         */

    public Location getAreaLocation(){
        return this.areaLocation;
    }

        /**
         * Get players on the area (not include spectators)
         * @return players in area.
         */

    public TreeSet<UUID> getAreaPlayers(){
        return this.inArea;
    }

}
