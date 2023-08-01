package io.kriffer.webterminal.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Request {

    private String username;
    private String password;
    private String host;
    private String sessionUser;
    private int port;
    private String command;
    private String res;

}
