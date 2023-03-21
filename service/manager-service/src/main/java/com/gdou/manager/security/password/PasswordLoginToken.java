package com.gdou.manager.security.password;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


public class PasswordLoginToken extends AbstractAuthenticationToken {

    private String id;

    private String password;

    public PasswordLoginToken(String id,String password) {
        super(null);
        this.id = id;
        this.password = password;
    }

    public PasswordLoginToken(String id,String password,Collection<? extends GrantedAuthority> grantedAuthorities) {
        super(grantedAuthorities);
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }


    public String getPassword() {
        return password;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.id;
    }

}
