![URMW Stats banner](https://user-images.githubusercontent.com/20552946/110439284-0133f780-8103-11eb-92f6-4fe08318fc23.png)

# URMW Stats

URMW Stats is a comprehensive system for tracking player, match, achievement and tournament statistics and for providing administration tools for the URMW Discord server.

## Feature list

* **Real time statistics scraping**

  URMW Stats scapes unstructured data from the URMW rating system in order to create a detailed profile of every player, match, tournament and achievement.
  It generally takes at most 10 seconds between a rating changing and the URMW Stats recalculating the necessary data and propogating that throughout the entire system.
  This process is also entirely atomic; partially updated data cannot be observed.
  
* **Real time frontend**

  URMW Stats uses a ReactJS and cheap polling on the frontend so any changes in player ratings are reflected on clients immediately without a page refresh.
  This applies to every figures and all the graphs which nicely animate when changes are propagated.
  
* **Public API**

  URMW Stats exposes a public API which is fit for 3rd party consumption.
  The website has a [section detailing this](https://urmw.live/about).
  
* **YouTube video feed**
  
  We regularly upload ranking match recordings to our YouTube channel, so URMW Stats collates the most recent match recordings into a carousel at the bottom of our home page.
  
* **Ratings calculation**

  For players who are interested in how their ratings will change when winning or losing against another team, both the website and Discord bot provide a system for calculating the theoretical rating outcome of arbitrary matches.
  The [web rating calculator](https://urmw.live/calculator) also summarizes the history of selected matchups in a pie graph.
  
* **Web leaderboard**

  The URMW ratings system provides a leaderboard in a Discord channel, but this is quite unweildy.
  To address this, URMW Stats provides a more appealing [web leaderboard](https://urmw.live/players) which additionally links to player profiles.
  
* **Player profiles**

  Each player has their own page with a graph of their rating over time, top opponents, achievements and much more.

* **Achievement and match listings**

  The website also lists [every achievement with their completors](https://urmw.live/achievements) and a full list of [every match in the season](https://urmw.live/history).
  The match history can get quite large, so data is lazy loaded as you scroll.

* **Discord OAuth and staff panel**

  URMW Stats does a lot more than just collating statistics, it also provides many administrative tools for managing the server.
  One of these is a panel where every staff member can log in with their Discord account to participate in regular instant-runoff votes.
  Announcements about the status of polls are automatically sent to our Discord server to keep everyone in the loop.
  
* **Channel exports**

  We regularly backup channels in our Discord server to minimize the amount of data loss in the event of serious incident.
  URMW Stats is able to save every message in a potentially very large channel and present it in a web UI on demand.
  For example, here is an [export of a channel](https://urmw.live/export/768320373942910997/818777662017372180/export.json.gz) our rating system generates.

* **Command based queries**

  It can sometimes be tedius to open the website every time you want to check some statistics.
  To solve this, URMW Stats provides the `%calc`, `%stats`, `%achievements` and `%achievement` commands for looking up data on the go.
  All of these commands perform fuzzy matching on their arguments to provide a nice user experience.
  
* **Voice recording**

  We occasionally run interviews for people volunteering to help out with managing our server.
  We like to record these interviews for future reference, and URMW Stats lets us do that by automatically recording Discord audio.
  If the recording is too large for one file, URMW Stats is able to automatically split the audio into multiple files to make it more managable.
  
* **Sophisticated live message templating system**

  Sometimes its useful to be able to send rich, templated messages in public channels.
  For this, URMW Stats has a message templating system where arbitrary rich message templates can be created using XML and then posted on demand.
  There are even options to have messages which automatically update, e.g. countdowns.

  ![Templating system demo](https://user-images.githubusercontent.com/20552946/110453129-98a04700-8111-11eb-8ac6-d3d88516473f.png)
  
## Contact

For any questions about the project, feel free to contact me on [my website](https://llew.netlify.app).
