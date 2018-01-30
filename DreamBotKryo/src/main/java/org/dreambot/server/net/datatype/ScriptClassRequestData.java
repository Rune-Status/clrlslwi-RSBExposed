package org.dreambot.server.net.datatype;

import lombok.Data;

/**
 * @author Demmonic
 */
@Data
public class ScriptClassRequestData {

    private String className;
    private String repo;
    private String module;
    private String loginHash;
    private int memberID;

}
