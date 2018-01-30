package org.dreambot.server.net.datatype;

import lombok.Data;

/**
 * @author Demmonic
 */
@Data
public class ScriptMainClassData {

    private String repository;
    private String module;
    private String loginHash;
    private int memberID;

}
