package org.dreambot.server.net.datatype;

import lombok.Data;

/**
 * @author Demmonic
 */
@Data
public class GetScriptsFreeData {

    private String loginHash;
    private int memberID;

}
