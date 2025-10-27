/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.utility.Utility;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

/**
 * This controller handles the home page and some of it's sub URLs.
 */
@Controller
@RequestMapping
public class HomeController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    /**
     * This is the specific function that handles the root URL itself.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error,
                               HttpSession session) {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("home_page");

        try {
            // Get the logged-in user
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                mv.setViewName("redirect:/login");
                return mv;
            }
            Long userId = Long.parseLong(userIdObj.toString());

            // Get posts from users that the logged-in user follows
            List<Post> posts = postService.getPostsFromFollowing(userId);
            mv.addObject("posts", posts);

            // If no posts found, show no content message
            if (posts.isEmpty()) {
                mv.addObject("isNoContent", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample data if there's an error
            List<Post> posts = Utility.createSamplePostsListWithoutComments();
            mv.addObject("posts", posts);
        }

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;
    }

    /**
     * This function handles the /createpost URL.
     * This handles a post request that is going to be a form submission.
     * The form for this can be found in the home page. The form has a
     * input field with name = posttext. Note that the @RequestParam
     * annotation has the same name. This makes it possible to access the value
     * from the input from the form after it is submitted.
     */
    @PostMapping("/createpost")
    public String createPost(@RequestParam(name = "posttext") String postText,
                           HttpSession session,
                           Model model) {
        System.out.println("User is creating post: " + postText);

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException e) {
            System.err.println("Invalid userId format in session: " + userIdObj);
            return "redirect:/login";
        }
        
        try {
            postService.createPost(userId, postText);
            System.out.println("New post created successfully by user " + userId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            String message = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/?error=" + message;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to create post.", StandardCharsets.UTF_8);
            return "redirect:/?error=" + message;
        }

        return "redirect:/";
    }

}
