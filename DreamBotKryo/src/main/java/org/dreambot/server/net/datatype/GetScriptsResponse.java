package org.dreambot.server.net.datatype;

import lombok.Data;

import java.util.Collection;

/**
 * @author Demmonic
 */
@Data
public class GetScriptsResponse {

    private Collection<ScriptData> scripts;

}
