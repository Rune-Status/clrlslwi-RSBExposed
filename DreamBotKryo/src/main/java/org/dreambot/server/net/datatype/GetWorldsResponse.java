package org.dreambot.server.net.datatype;

import lombok.Data;
import org.dreambot.api.methods.world.World;

/**
 * @author Demmonic
 */
@Data
public class GetWorldsResponse {

    private World[] worlds;

}
