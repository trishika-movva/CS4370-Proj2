# CS4370-Proj2

Readme.txt file that contains the member names and their contributions, instructions on how to run the project and if you refer to any external resources to learn any concepts, a list of them.
Describe the new feature you implemented. Make sure to mention how to access it and where to find the code you wrote for it under each of the following parts: UI, controller, service and SQL/schema-additions.


Group Contributions:

    Trishika Movva - Implemented the non-trivial feature (hashtags and likes trending page) as well as worked on the people page.

    Vibhu Seenappa - Worked on the Functionality for liking, the posts of following on the homepage, and bookmarking

    Jemia Johnson- Worked on functionality for posts, commenting, and hashtags, er table

    Silvia Chen - Worked on “Make Post” feature, also added date formatting to display post timestamps in a user-friendly format (e.g., “Mar 07, 2024, 10:54 PM”).

    Carlos Cruz - Helped with GitHub setup, database integration, and testing.

How to Run the Project:
- Run using: mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'
- Open a browser and go to http://localhost:8081/

New Feature:
- It displays the top hashtags and most liked posts in the last however many days that the user wants to see and users can change the 'days' and 'limit' filters.
- You can access it by clicking the 'Trends' link in the navigation bar
    - Url: /trending
- Code Locatons:
    UI: src/main/resources/templates/trending_page.mustache
        - Displays two sections: top hashtags and most-liked posts.
    Controller: src/main/java/uga/menik/csx370/controllers/TrendingController.java
        - Handles /trending GET requests and gets the analytics data.
    Service: src/main/java/uga/menik/csx370/services/TrendingService.java
        - Runs SQL aggregation queries for hashtags and posts.
    Database (SQL Operation): dml.sql
        - Uses JOIN, GROUP BY, and COUNT() to compute trending results.

External Resources Used
1. Spring Boot Documentation
https://spring.io/projects/spring-boot
2. MySQL Reference Manual
https://dev.mysql.com/doc/refman/8.0/en/

