/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.utility.Utility;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;

/**
 * Handles /hashtagsearch URL and possibly others.
 * At this point no other URLs.
 */
@Controller
@RequestMapping("/hashtagsearch")
public class HashtagSearchController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    /**
     * This function handles the /hashtagsearch URL itself.
     * This URL can process a request parameter with name hashtags.
     * In the browser the URL will look something like below:
     * http://localhost:8081/hashtagsearch?hashtags=%23amazing+%23fireworks
     * Note: the value of the hashtags is URL encoded.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "hashtags") String hashtags) {
        System.out.println("User is searching: " + hashtags);

        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            // Get the logged-in user
            User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser == null) {
                mv.setViewName("redirect:/login");
                return mv;
            }

            // Parse hashtags from the input string
            List<String> hashtagList = parseHashtags(hashtags);
            
            if (hashtagList.isEmpty()) {
                mv.addObject("isNoContent", true);
                mv.addObject("posts", new ArrayList<Post>());
            } else {
                // Get posts by hashtags
                Long userId = Long.parseLong(loggedInUser.getUserId());
                List<Post> posts = postService.getPostsByHashtags(hashtagList, userId);
                mv.addObject("posts", posts);

                // If no posts found, show no content message
                if (posts.isEmpty()) {
                    mv.addObject("isNoContent", true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample data if there's an error
            List<Post> posts = Utility.createSamplePostsListWithoutComments();
            mv.addObject("posts", posts);
        }

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);
        
        return mv;
    }

    /**
     * Parses hashtags from the input string.
     * Handles both #hashtag and hashtag formats.
     */
    private List<String> parseHashtags(String hashtags) {
        List<String> hashtagList = new ArrayList<>();
        
        if (hashtags == null || hashtags.trim().isEmpty()) {
            return hashtagList;
        }

        // Split by spaces and process each hashtag
        String[] parts = hashtags.trim().split("\\s+");
        
        for (String part : parts) {
            String hashtag = part.trim();
            if (!hashtag.isEmpty()) {
                // Remove # if present and convert to lowercase
                if (hashtag.startsWith("#")) {
                    hashtag = hashtag.substring(1);
                }
                if (!hashtag.isEmpty()) {
                    hashtagList.add(hashtag.toLowerCase());
                }
            }
        }
        
        return hashtagList;
    }
    
}
