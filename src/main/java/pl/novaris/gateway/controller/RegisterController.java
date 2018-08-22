package pl.novaris.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.novaris.gateway.model.AppUser;
import pl.novaris.gateway.model.PasswordResetForm;
import pl.novaris.gateway.model.UserFormEmail;
import pl.novaris.gateway.service.UserService;

import javax.validation.Valid;
import java.util.Optional;

@Controller
public class RegisterController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public RegisterController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping("/registerPage")
    public String registerPage(Model model) {
        model.addAttribute("userform", new UserFormEmail());
        return "register-form";
    }

    @PostMapping("/register")
    public String showMyLoginPage(@Valid @ModelAttribute("userform") UserFormEmail userFormEmail, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userform", new UserFormEmail());
            return "register-form";
        } else {
            userService.addUser(userFormEmail);
            return "register-confirmation";
        }
    }

    @GetMapping("/reset")
    public String displayResetPasswordPage(Model model, @RequestParam("token") String token) {
        Optional<AppUser> user = userService.findUserByResetToken(token);
        model.addAttribute("passwordResetForm", new PasswordResetForm());
        if (user.isPresent()) {
            model.addAttribute("token", token);
        } else {
            model.addAttribute("error", "Link niewazny");
        }
        return "reset-password";
    }

    @PostMapping("/resetPassword")
    public String setNewPassword(@ModelAttribute("passwordResetForm") @Valid PasswordResetForm form, BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorData", "Bledne dane");
            redirectAttributes.addFlashAttribute("passwordResetForm", form);
            return "redirect:/reset?token=" + form.getToken();
        } else {
            AppUser user = userService.findUserByResetToken(form.getToken()).get();
            user.setPassword(bCryptPasswordEncoder.encode(form.getPassword()));
            user.setResetToken(null);
            userService.update(user);
            return "redirect:/loginPage";
        }
    }
}