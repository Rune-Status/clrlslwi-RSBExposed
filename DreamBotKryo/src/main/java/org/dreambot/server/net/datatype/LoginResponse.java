package org.dreambot.server.net.datatype;

import lombok.Data;

import java.util.HashSet;

/**
 * @author Demmonic
 */
@Data
public class LoginResponse {

    private String username;
    private String hash;
    private HashSet<Integer> groups;
    private int memberID;


}
