package org.dreambot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Demmonic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginToken {

    private String hash;
    private int memberId;

}
