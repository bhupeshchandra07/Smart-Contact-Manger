package com.smart.controller;

import com.smart.Services.EmailService;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
public class forgotController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

//    Random class is used to generate random numbers(here otp)
    Random random = new Random(1000);


    @RequestMapping("/forgot")
    public String openEmailForm(){

        return "forgot_email_form";
    }

//    we make a controller whcih accept the smail from the forgot_email_form and then send the otp to the user email
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session){
        System.out.println("Email: "+email);

//        we are going to generationg 4 digit otp by Random Class of java.util.

        int otp = random.nextInt(10000);
        System.out.println("OTP: "+otp);

        //now we write the main logic of the otp sent to the email..
        //now if we autowire the emailservice then we can use it to send the otp to the user email
        String subject = "OTP from Smart Contact Manager";
        String message = "" +
                "<div style='border: 1px solid #e2e2e2; padding: 20px'>" +
                "<h1>" +
                "OTP is: " +
                "<b>" +otp+
                "</b>" +
                "</h1>" +
                "</div>";
        String to = email;
        boolean flag = this.emailService.sendEmail(subject,message,to);

        if (flag){
//            we have to save the otp for a while.
            session.setAttribute("myotp",otp);
            session.setAttribute("email",email);
            return "verify_otp";
        }else{
            //we hvae to sent a message.
            session.setAttribute("message",new Message("Please check your email id !!","danger"));
            return "forgot_email_form";
        }

    }

//    now make a new handler for the verify otp;
    @PostMapping("/verify-otp")
    public String verifyotp(@RequestParam("otp") int otp,HttpSession session){
        int myotp = (int)session.getAttribute("myotp");
        String email = (String)session.getAttribute("email");

        if (myotp == otp){
            //password change form will be shown
            User user = this.userRepository.getUserByUserName(email);
            if (user == null){
                session.setAttribute("message",new Message("User not found !!","danger"));
                return "forgot_email_form";
            }else{

            }
            return "password_change_form";

        }else {
            session.setAttribute("message",new Message("Please enter correct otp !!","danger"));
            return "verify_otp";
        }
    }

//    change password
    @PostMapping("/change-password")
    public String changepassword(@RequestParam("newPassword") String newPassword,HttpSession session){
//        email fetch
        String email = (String)session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);

        user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
//        now save to the database
        this.userRepository.save(user);
        return "redirect:/signin?change=password change successfully..";
    }
}
