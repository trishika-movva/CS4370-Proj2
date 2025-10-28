package uga.menik.csx370.models;

public class TrendingHashtag {
    private final String tag;
    private final long usageCount;

    public TrendingHashtag(String tag, long usageCount) {
        this.tag = tag;
        this.usageCount = usageCount;
    }

    public String getTag() {
        return tag;
    }

    public long getUsageCount() {
        return usageCount;
    }

    /** Link to your existing hashtag search route */
    public String getHashtagLink() {
        return "/hashtagsearch?hashtags=%23" + tag;
    }
}
