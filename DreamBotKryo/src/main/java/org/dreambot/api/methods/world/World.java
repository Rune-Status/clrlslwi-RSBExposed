package org.dreambot.api.methods.world;

import lombok.Data;

/**
 * @author Demmonic
 */
@Data
public class World {

    private int id;
    private boolean membersOnly;
    private boolean pvpOnly;
    private int location;
    private boolean highRisk;
    private int levelRequirement;
    private boolean deadMemeMode;
    private boolean lastManStanding;
    private boolean tournamentWorld;


}
