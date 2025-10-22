package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    //home page
    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("title","Home - Smart Contact Manager");
        return "home";
    }

    //about page
    @RequestMapping("/about")
    public String about(Model model){
        model.addAttribute("title","About - Smart Contact Manager");
        return "about";
    }

    //about signup page
    @RequestMapping("/signup")
    public String signup(Model model){
        model.addAttribute("title","Signup - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    //handler for user registaration page
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute("user") User user ,BindingResult bindingresult, @RequestParam (value = "agreement", defaultValue = "false") boolean agreement , Model model, HttpSession session) {
        try{
            if (!agreement) {
                System.out.println("You must agree the terms and conditions");
                throw new Exception("You must agree the terms and conditions");
            }

            if (bindingresult.hasErrors()) {
                System.out.println("Error: "+bindingresult.toString());
                model.addAttribute("user", user);
                return "signup";
            }

            //setting some role
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            //this will encrypt the password and save
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            System.out.println("Agreement "+ agreement);
            System.out.println("USER "+user);

            //to save user in database
            User result = this.userRepository.save(user);

            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully Registered !!","alert-success"));
            return "signup";
        }
        catch (Exception e){
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong! "+e.getMessage(),"alert-danger"));
            return "signup";
        }
    }
    
    // Add a method to clear the message after page is rendered (simulate flash attribute)
    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String signupClearMessage(Model model, HttpSession session) {
        model.addAttribute("title","Signup - Smart Contact Manager");
        model.addAttribute("user", new User());
        // Remove the message after showing it once
        session.removeAttribute("message");
        return "signup";
    }

    //handler for custom login page
    @GetMapping("/signin")
    public String customLogin(Model model){
        model.addAttribute("title","Login - Smart Contact Manager");
        return "login";
    }
}
