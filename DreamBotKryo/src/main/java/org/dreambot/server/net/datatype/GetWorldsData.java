package org.dreambot.server.net.datatype;

import lombok.Data;

/**
 * @author Demmonic
 */
@Data
public class GetWorldsData {

    private String loginToken;
    private int memberId;

}
