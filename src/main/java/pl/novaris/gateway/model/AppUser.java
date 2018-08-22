package pl.novaris.gateway.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.*;

@Table
@Entity(name = "user")
public class AppUser implements UserDetails  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank
    @Column(name = "username")
    private String username;
    @NotBlank
    @Column(name = "password")
    private String password;
    @NotBlank
    @Column(name = "email")
    private String email;
    @Column(name = "enabled")
    private boolean enabled;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Role> roles = new ArrayList<>();
    @Column(name = "reset_token")
    private String resetToken;


    public AppUser() {
        this.enabled=true;
        this.resetToken=null;
    }

    public AppUser(@NotBlank String username, @NotBlank String password, @NotBlank String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled=true;
        this.resetToken=null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> setAuth = new ArrayList<>();
        for (Role role : roles) {
            setAuth.add(new SimpleGrantedAuthority(role.getRole()));
        }
        return setAuth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addNewRoles(Role role){
        roles.add(role);
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}
