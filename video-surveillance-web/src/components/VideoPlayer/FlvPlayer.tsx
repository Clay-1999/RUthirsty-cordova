import { useEffect, useRef, useState } from 'react';
import flvjs from 'flv.js';
import { Spin, message } from 'antd';

interface FlvPlayerProps {
  url: string;
  autoplay?: boolean;
  controls?: boolean;
  width?: string | number;
  height?: string | number;
  onError?: (error: any) => void;
}

const FlvPlayer: React.FC<FlvPlayerProps> = ({
  url,
  autoplay = true,
  controls = true,
  width = '100%',
  height = '100%',
  onError,
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const flvPlayerRef = useRef<flvjs.Player | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!url || !videoRef.current) {
      return;
    }

    if (!flvjs.isSupported()) {
      message.error('Your browser does not support FLV playback');
      return;
    }

    const videoElement = videoRef.current;

    // Create FLV player
    const flvPlayer = flvjs.createPlayer({
      type: 'flv',
      url: url,
      isLive: true,
      hasAudio: true,
      hasVideo: true,
    }, {
      enableWorker: false,
      enableStashBuffer: false,
      stashInitialSize: 128,
      autoCleanupSourceBuffer: true,
    });

    flvPlayer.attachMediaElement(videoElement);
    flvPlayer.load();

    if (autoplay) {
      flvPlayer.play().catch((error) => {
        console.error('Failed to autoplay:', error);
      });
    }

    // Event listeners
    flvPlayer.on(flvjs.Events.LOADING_COMPLETE, () => {
      setLoading(false);
    });

    flvPlayer.on(flvjs.Events.ERROR, (errorType, errorDetail, errorInfo) => {
      console.error('FLV player error:', errorType, errorDetail, errorInfo);
      message.error('Video playback error');
      setLoading(false);
      onError?.(errorInfo);
    });

    videoElement.addEventListener('loadedmetadata', () => {
      setLoading(false);
    });

    flvPlayerRef.current = flvPlayer;

    // Cleanup
    return () => {
      if (flvPlayerRef.current) {
        flvPlayerRef.current.pause();
        flvPlayerRef.current.unload();
        flvPlayerRef.current.detachMediaElement();
        flvPlayerRef.current.destroy();
        flvPlayerRef.current = null;
      }
    };
  }, [url, autoplay, onError]);

  return (
    <div style={{ position: 'relative', width, height, backgroundColor: '#000' }}>
      {loading && (
        <div
          style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            zIndex: 10,
          }}
        >
          <Spin size="large" />
        </div>
      )}
      <video
        ref={videoRef}
        controls={controls}
        style={{
          width: '100%',
          height: '100%',
          objectFit: 'contain',
        }}
      />
    </div>
  );
};

export default FlvPlayer;
