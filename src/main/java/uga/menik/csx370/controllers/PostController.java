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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.utility.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import uga.menik.csx370.services.CommentService;
import uga.menik.csx370.services.PostService;



/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error,
            jakarta.servlet.http.HttpSession session) {
        System.out.println("The user is attempting to view post with id: " + postId);
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                mv.setViewName("redirect:/login");
                return mv;
            }

            Long currentUserId = Long.parseLong(userIdObj.toString());
            Long postIdLong = Long.parseLong(postId);
            
            ExpandedPost post = postService.getPostWithComments(postIdLong, currentUserId);
            if (post != null) {
                List<ExpandedPost> posts = List.of(post);
                mv.addObject("posts", posts);
            } else {
                mv.addObject("isNoContent", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample data if there's an error
            List<ExpandedPost> posts = Utility.createSampleExpandedPostWithComments();
            mv.addObject("posts", posts);
        }

        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;
    }

    /**
     * Handles comments added on posts.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
                            @RequestParam(name = "comment") String comment,
                            HttpSession session) {
        System.out.println("The user is attempting add a comment:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tcomment: " + comment);

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userId = Long.parseLong(userIdObj.toString());
        Long postIdLong = Long.parseLong(postId);

        try {
            commentService.addComment(userId, postIdLong, comment);
            System.out.println("Comment added successfully by user " + userId);
            return "redirect:/post/" + postId; // reload post with comment visible
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to post the comment. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }


    /**
     * Handles likes added on posts.
     */
    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd,
            jakarta.servlet.http.HttpSession session) {
        System.out.println("The user is attempting add or remove a heart:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                return "redirect:/login";
            }

            Long userId = Long.parseLong(userIdObj.toString());
            Long postIdLong = Long.parseLong(postId);
            
            postService.toggleLike(userId, postIdLong, isAdd);
            System.out.println("Like toggled successfully");
            
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    /**
     * Handles bookmarking posts.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd,
            jakarta.servlet.http.HttpSession session) {
        System.out.println("The user is attempting add or remove a bookmark:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                return "redirect:/login";
            }

            Long userId = Long.parseLong(userIdObj.toString());
            Long postIdLong = Long.parseLong(postId);
            
            postService.toggleBookmark(userId, postIdLong, isAdd);
            System.out.println("Bookmark toggled successfully");
            
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    /**
     * Handles creating a new post by the logged-in user.
 */
@PostMapping("/new")
public String createPost(@RequestParam("content") String content,
                         HttpSession session,
                         Model model) {

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
        System.out.println("DEBUG: About to call postService.createPost with userId=" + userId + ", content=" + content);
        postService.createPost(userId, content);
        System.out.println("New post created successfully by user " + userId);
    } catch (IllegalArgumentException e) {
        System.out.println("DEBUG: IllegalArgumentException caught: " + e.getMessage());
        model.addAttribute("error", e.getMessage());
        String message = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        return "redirect:/?error=" + message;
    } catch (Exception e) {
        System.out.println("DEBUG: Exception caught in createPost: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        e.printStackTrace();
        String message = URLEncoder.encode("Failed to create post.", StandardCharsets.UTF_8);
        return "redirect:/?error=" + message;
    }

    return "redirect:/";
}
}
