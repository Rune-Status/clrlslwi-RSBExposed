package org.dreambot.server.net.datatype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Demmonic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginData {

    private String username;
    private String password;

}
