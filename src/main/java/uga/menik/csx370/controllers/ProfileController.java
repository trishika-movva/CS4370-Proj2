/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.utility.Utility;
import uga.menik.csx370.models.User;

/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PostService postService;

    /**
     * Constructor-based dependency injection for UserService and PostService.
     */
    @Autowired
    public ProfileController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView profileOfLoggedInUser() {
        System.out.println("User is attempting to view profile of the logged in user.");
        return profileOfSpecificUser(userService.getLoggedInUser().getUserId());
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     */
    @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId) {
        System.out.println("User is attempting to view profile: " + userId);
        
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            // Get the current logged-in user
            User currentUser = userService.getLoggedInUser();
            if (currentUser == null) {
                mv.setViewName("redirect:/login");
                return mv;
            }

            // Get posts from the specific user
            Long targetUserId = Long.parseLong(userId);
            Long currentUserId = Long.parseLong(currentUser.getUserId());
            List<Post> posts = postService.getPostsByUser(targetUserId, currentUserId);
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
        
        return mv;
    }
    
}
