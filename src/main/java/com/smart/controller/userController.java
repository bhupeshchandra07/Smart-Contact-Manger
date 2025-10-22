package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.entities.Contact;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;


@Controller
@RequestMapping("/user")
public class userController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    //creating a method in which all the basic detail willl be fetced.
    @ModelAttribute
    public void commonData(Model model, Principal principal){
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        model.addAttribute("user", user);
    }

    //for a dashboard.
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }


    //hander for add contact page.
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact",new Contact());
        return "normal/add_contact_form";
    }

    //processing add contact form.
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session){
        try{
            String name = principal.getName();

            //processing and uploading file
            if(file.isEmpty()){
                System.out.println("File is empty");
                contact.setImage("contact.jpg");
            }else{
                //upload the file to folder  and update the name to contact
                contact.setImage(file.getOriginalFilename());
                //now we need the file pathe where we store the file.
                File savefile = new ClassPathResource("static/img").getFile();

                //to ftech the path where we want to store the file.
                Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
            }

            User user = this.userRepository.getUserByUserName(name);

            contact.setUser(user);

            user.getContacts().add(contact);

            this.userRepository.save(user);//this line save the contact in database

            //message sucess.
            session.setAttribute("message",new Message("Your Contact is Added Successfully!!","success"));
        }catch (Exception e){
            System.out.println("Error" + e.getMessage());
            //message error
            session.setAttribute("message",new Message("Something went wrong !! Try again","danger"));

        }

        return "normal/add_contact_form";//retrun same page with the successfull massage.
    }

    //now make a handler for the view contact page.
    //per page 3 contact will be shown[n]
    //current page will be 0[page]
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal){
        model.addAttribute("title", "Show User Contacts");
        //contact ki list ko bejna hai.
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        //for pagination
        //pageable has two information page and size
        Pageable pageable = PageRequest.of(page,3);

        Page<Contact> contacts = this.contactRepository.findCotactsByUser(user.getId(),pageable);
        model.addAttribute("contacts",contacts);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",contacts.getTotalPages());

        return "normal/show_contacts";
    }

    //showing perticular contact
    //use @getmapping to make it url
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal){
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        //now apply security checks
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if(user.getId() == contact.getUser().getId()){
            model.addAttribute("contact",contact);
            model.addAttribute("title",contact.getcName());
        }

        return "normal/contact_detail";
    }

    //delete contact handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession session,Principal principal){
        Optional<Contact> contactoptional = this.contactRepository.findById(cId);
        Contact contact = contactoptional.get();

        //we can apply check here.
//        this.contactRepository.delete(contact);

        //to unlink the contact from user
//        contact.setUser(null);

//        change to below line
        User user = this.userRepository.getUserByUserName(principal.getName());

        user.getContacts().remove(contact);

        this.userRepository.save(user);

        //we have to remove the photo from folder also.


        //to set message by httpsSession.
        session.setAttribute("message"  , new Message("Your contact is deleted !!","success"));

        return "redirect:/user/show-contacts/0";
    }

    //update handler.
    @PostMapping("/update-contact/{cId}")
    public String updateContact(@PathVariable("cId") Integer cId,Model m){

        m.addAttribute("title","Update Contact");

        Contact contact = this.contactRepository.findById(cId).get();
        m.addAttribute("contact",contact);

        return "normal/update_form";
    }

    //now make a handler for the process-update
    @RequestMapping(value = "/process-update", method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal){
        try{System.out.println("File is empty");
//            old contact details
            Contact oldContactdetails = this.contactRepository.findById(contact.getcId()).get();
//            image
            if (!file.isEmpty()){
                //file work
                //rewrite file.
//                delete the old file
                File deletefile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deletefile,oldContactdetails.getcName());
                file1.delete();
//                and upload the new one.
                //now we need the file pathe where we store the file.
                File savefile = new ClassPathResource("static/img").getFile();

                //to ftech the path where we want to store the file.
                Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            }else{
                contact.setImage(oldContactdetails.getImage());
            }
            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);

            //showing message
            session.setAttribute("message", new Message("Your contact is updated !!","success"));
        }catch (Exception e){
            e.printStackTrace();
        }

        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //handler for the your profile.
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","Profile Page");
        return "normal/profile";
    }
    
    //open setting handler.
    @GetMapping("/settings")
    public String operSettings(){
        return "normal/settings";
    }

    //change password handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session){
        String userName = principal.getName();
        User currentUser = this.userRepository.getUserByUserName(userName);
        System.out.println(currentUser.getPassword());

        if (this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())){
            //change password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            //message print on success change
            session.setAttribute("message",new Message("Your password is changed !!","success"));
        }
        else{
            //print error
            session.setAttribute("message",new Message("Please Enter a correct old password !!","danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }

}
