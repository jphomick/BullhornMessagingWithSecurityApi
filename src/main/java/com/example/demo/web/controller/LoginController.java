package com.example.demo.web.controller;

import com.example.demo.business.entities.User;
import com.example.demo.business.entities.repositories.MessageRepository;
import com.example.demo.business.entities.repositories.UserRepository;
import com.example.demo.business.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

@Controller
public class LoginController {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/register")
    public @ResponseBody String processRegistrationPage(User user) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            boolean isUser = userRepository.findById(user.getId()).isPresent();
            if (isUser) {
                //updating with existed username
                if (userRepository.findByUsername(user.getUsername()) != null &&
                        //current user
                        !userRepository.findByUsername(user.getUsername()).equals(user)) {
                    return "User already exists";
                }

                User userInDB = userRepository.findById(user.getId()).get();
                userInDB.setFirstName(user.getFirstName());
                userInDB.setLastName(user.getLastName());
                userInDB.setEmail(user.getEmail());
                userInDB.setUsername(user.getUsername());
                userInDB.setPassword(userService.encode(user.getPassword()));
                userInDB.setEnabled(user.isEnabled());
                userRepository.save(userInDB);
                return "User successfully updated!";
            }
            //New User
            else {
                //Registering with existed username
                if (userRepository.findByUsername(user.getUsername()) != null) {
                    return "User already exists";
                } else {
                    user.setPassword(userService.encode(user.getPassword()));
                    userService.saveUser(user);
                    return "User successfully added!";
                }
            }
        } catch (Exception e) {
            return "Error adding user";
        }
    }
}
