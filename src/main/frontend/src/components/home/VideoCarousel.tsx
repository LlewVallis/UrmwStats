import React, { useEffect, useState } from "react";
import { Carousel, Pagination } from "react-bootstrap";
import PageSpinner from "../PageSpinner";

const RecentActivityEndpoint = "https://api.rss2json.com/v1/api.json?rss_url=https%3A%2F%2Fwww.youtube.com%2Ffeeds%2Fvideos.xml%3Fchannel_id%3DUC_3WbejKtKl_s2iRqyTNfMQ";

const VideoWidth = "560px";
const VideoHeight = "315px";

interface RecentActivity {
  status: string;
  items: Video[];
}

interface Video {
  guid: string;
  title: string;
  link: string;
  thumbnail: string;
}

const VideoCarousel = () => {
  const [ recentActivity, setRecentActivity ] = useState<RecentActivity | null>(null);
  const [ selectedVideo, setSelectedVideo ] = useState(0);
  const [ errored, setErrored ] = useState(false);

  useEffect(() => {
    fetch(RecentActivityEndpoint).then(async response => {
      if (!response.ok) {
        throw new Error(`did not expect status ${response.status} when fetching recent videos`);
      }

      const activity = await response.json() as RecentActivity;
      if (activity.status !== "ok") {
        throw new Error(`did not expect encoded status ${activity.status} when fetching recent videos`);
      }

      setRecentActivity(activity);
    }).catch(error => {
      console.error("Failed to fetch recent videos", error);
      setErrored(true);
    });
  }, []);

  const goto = (indexUnbounded: number) => {
    const videoCount = recentActivity!.items.length;
    const index = ((indexUnbounded % videoCount) + videoCount) % videoCount;
    setSelectedVideo(index);
  };

  if (recentActivity) {
    return (
      <>
        <Carousel activeIndex={selectedVideo} controls={false} indicators={false} style={{
          margin: "0 auto",
          width: VideoWidth,
          height: VideoHeight,
          backgroundColor: "black",
        }}>
          {recentActivity.items.map(video => (
            <Carousel.Item>
              <iframe 
                className="d-block w-100"
                title={video.title}
                width={VideoWidth}
                height={VideoHeight}
                src={`https://www.youtube-nocookie.com/embed/${video.guid.replace("yt:video:", "")}`}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                frameBorder="0"
                allowFullScreen 
              />
            </Carousel.Item>
          ))}
        </Carousel>

        <Pagination style={{
          marginTop: "1rem",
          justifyContent: "center",
        }}>
          <Pagination.First onClick={() => goto(0)} />
          <Pagination.Prev onClick={() => goto(selectedVideo - 1)} />

          {recentActivity.items.map((video, index) => (
            <Pagination.Item key={video.guid} active={index === selectedVideo} onClick={() => goto(index)}>
              {index + 1}
            </Pagination.Item>
          ))}

          <Pagination.Next onClick={() => goto(selectedVideo + 1)} />
          <Pagination.Last onClick={() => goto(-1)} />
        </Pagination>
      </>
    );
  } else {
    return <PageSpinner message="Loading recent videos" errored={errored} />;
  }
};

export default VideoCarousel;