/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.utility.Utility;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.models.User;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles /bookmarks and its sub URLs.
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    /**
     * /bookmarks URL itself is handled by this.
     */
    @GetMapping
    public ModelAndView webpage() {
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            // Get the logged-in user
            User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser == null) {
                mv.setViewName("redirect:/login");
                return mv;
            }

            // Get bookmarked posts for the logged-in user
            Long userId = Long.parseLong(loggedInUser.getUserId());
            List<Post> posts = postService.getBookmarkedPosts(userId);
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
