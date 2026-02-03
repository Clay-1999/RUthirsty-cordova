import { useState } from 'react';
import { Card, Button, Slider, Space, message } from 'antd';
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  ArrowLeftOutlined,
  ArrowRightOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
} from '@ant-design/icons';
import { ptzApi } from '@/api/ptz';
import type { PTZCommand } from '@/types';

interface PTZControlProps {
  deviceId: string;
  channelId: string;
}

const PTZControl: React.FC<PTZControlProps> = ({ deviceId, channelId }) => {
  const [speed, setSpeed] = useState(50);
  const [loading, setLoading] = useState(false);

  const handleControl = async (command: PTZCommand['command']) => {
    setLoading(true);
    try {
      await ptzApi.control({
        deviceId,
        channelId,
        command,
        speed,
      });
    } catch (error) {
      message.error('PTZ control failed');
    } finally {
      setLoading(false);
    }
  };

  const handleStop = () => {
    handleControl('STOP');
  };

  return (
    <Card title="PTZ控制" size="small">
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        {/* Direction Control */}
        <div>
          <div style={{ textAlign: 'center', marginBottom: 8 }}>方向控制</div>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 60px)',
              gap: 8,
              justifyContent: 'center',
            }}
          >
            <div />
            <Button
              icon={<ArrowUpOutlined />}
              onMouseDown={() => handleControl('UP')}
              onMouseUp={handleStop}
              onMouseLeave={handleStop}
              loading={loading}
            />
            <div />
            <Button
              icon={<ArrowLeftOutlined />}
              onMouseDown={() => handleControl('LEFT')}
              onMouseUp={handleStop}
              onMouseLeave={handleStop}
              loading={loading}
            />
            <div />
            <Button
              icon={<ArrowRightOutlined />}
              onMouseDown={() => handleControl('RIGHT')}
              onMouseUp={handleStop}
              onMouseLeave={handleStop}
              loading={loading}
            />
            <div />
            <Button
              icon={<ArrowDownOutlined />}
              onMouseDown={() => handleControl('DOWN')}
              onMouseUp={handleStop}
              onMouseLeave={handleStop}
              loading={loading}
            />
            <div />
          </div>
        </div>

        {/* Zoom Control */}
        <div>
          <div style={{ textAlign: 'center', marginBottom: 8 }}>变倍控制</div>
          <Space style={{ width: '100%', justifyContent: 'center' }}>
            <Button
              icon={<ZoomInOutlined />}
              onMouseDown={() => handleControl('ZOOM_IN')}
              onMouseUp={handleStop}
              onMouseLeave={handleStop}
              loading={loading}
            >
              放大
            </Button>
            <Button
              icon={<ZoomOutOutlined />}
              onMouseDown={() => handleControl('ZOOM_OUT')}
              onMouseUp={handleStop}
              onMouseLeave={handleStop}
              loading={loading}
            >
              缩小
            </Button>
          </Space>
        </div>

        {/* Speed Control */}
        <div>
          <div style={{ marginBottom: 8 }}>速度: {speed}</div>
          <Slider
            min={1}
            max={100}
            value={speed}
            onChange={setSpeed}
            tooltip={{ formatter: (value) => `${value}` }}
          />
        </div>
      </Space>
    </Card>
  );
};

export default PTZControl;
