package pl.novaris.gateway.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.novaris.gateway.emailconfig.EmailSender;
import pl.novaris.gateway.model.AppUser;
import pl.novaris.gateway.model.Role;
import pl.novaris.gateway.model.UserForm;
import pl.novaris.gateway.model.UserFormEmail;
import pl.novaris.gateway.repository.UserRepository;
import org.springframework.core.env.Environment;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    private final Environment environment;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, EmailSender emailSender, TemplateEngine templateEngine, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.environment = environment;
    }

    @Transactional
    public String addUser(UserFormEmail userFormEmail){
        AppUser newAppUser = new AppUser();
        String loginTemp = RandomStringUtils.random(6,false,true);
        String passwordTemp = RandomStringUtils.random(6,true,true);
        newAppUser.setResetToken(UUID.randomUUID().toString());
        newAppUser.setUsername(loginTemp);
        newAppUser.setEmail(userFormEmail.getEmail());
        newAppUser.setPassword(passwordEncoder.encode(passwordTemp));
        String linkToResetPassword = environment.getProperty("email.link") + newAppUser.getResetToken();
        Role role = new Role("ROLE_USER");
        role.setAppUser(newAppUser);
        newAppUser.addNewRoles(role);
        userRepository.save(newAppUser);
        send(userFormEmail.getEmail(),loginTemp,linkToResetPassword);
        return newAppUser.getUsername();
    }

    @Transactional
    public void addAdmin(UserForm userForm){
        AppUser newAppUser = new AppUser();
        newAppUser.setUsername(userForm.getUsername());
        newAppUser.setEmail(userForm.getEmail());
        newAppUser.setPassword(passwordEncoder.encode(userForm.getPassword()));
        Role role = new Role("ROLE_ADMIN");
        role.setAppUser(newAppUser);
        newAppUser.addNewRoles(role);
        userRepository.save(newAppUser);
    }

    @Transactional
    public void update(AppUser appUser){
        userRepository.save(appUser);
    }

    public Optional<AppUser> findUserByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }

    private boolean isDuplicated(UserForm userForm) {
        int count = userRepository.countByEmailOrUsername(userForm.getEmail(), userForm.getUsername());
        return count != 0;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new org.springframework.security.core.userdetails.User(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.getAuthorities()
        );
    }

    private void send(String to, String login, String linkToResetPassword) {
        Context context = new Context();
        context.setVariable("login", login);
        context.setVariable("link", linkToResetPassword );
        String body = templateEngine.process("template", context);
        emailSender.sendEmail(environment.getProperty("spring.mail.username"),to, environment.getProperty("email.subject"), body);
    }
}
