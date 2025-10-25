package uga.menik.csx370.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createPost(Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty.");
        }
        
        String insertPostSql = "INSERT INTO post (userId, content) VALUES (?, ?)";
        jdbcTemplate.update(insertPostSql, userId, content.trim());

        Long postId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        Set<String> hashtags = new HashSet<>();

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase()); 
        }

        for (String tag : hashtags) {
            jdbcTemplate.update("INSERT IGNORE INTO hashtag (tag) VALUES (?)", tag);

            jdbcTemplate.update(
                    "INSERT INTO post_hashtag (pid, hid) " +
                    "SELECT ?, hid FROM hashtag WHERE tag = ?",
                    postId, tag
            );
        }
    }
}


