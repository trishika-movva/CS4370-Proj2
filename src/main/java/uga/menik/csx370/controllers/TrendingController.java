package uga.menik.csx370.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import uga.menik.csx370.services.TrendingService;

@Controller
public class TrendingController {

    @Autowired
    private TrendingService trendingService;

    @GetMapping("/trending")
    public ModelAndView page(
            @RequestParam(name = "days",  defaultValue = "7")  int days,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        ModelAndView mv = new ModelAndView("trending_page");
        mv.addObject("days", days);
        mv.addObject("limit", limit);
        mv.addObject("trendingHashtags", trendingService.getTopHashtags(days, limit));
        mv.addObject("popularPosts",     trendingService.getMostLikedPosts(days, limit));
        return mv;
    }
}
