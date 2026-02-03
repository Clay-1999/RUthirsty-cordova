import { useState, useEffect } from 'react';
import { Row, Col, Card, Tree, Button, Space, message, Empty } from 'antd';
import { PlayCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import { deviceApi } from '@/api/device';
import { streamApi } from '@/api/stream';
import FlvPlayer from '@/components/VideoPlayer/FlvPlayer';
import PTZControl from '@/components/PTZControl';
import type { Device, DeviceChannel } from '@/types';

interface VideoWindow {
  id: string;
  deviceId: string;
  channelId: string;
  channelName: string;
  sessionId?: string;
  flvUrl?: string;
}

const LiveStream = () => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [videoWindows, setVideoWindows] = useState<VideoWindow[]>([
    { id: '1', deviceId: '', channelId: '', channelName: '' },
    { id: '2', deviceId: '', channelId: '', channelName: '' },
    { id: '3', deviceId: '', channelId: '', channelName: '' },
    { id: '4', deviceId: '', channelId: '', channelName: '' },
  ]);
  const [selectedWindow, setSelectedWindow] = useState<string>('1');
  const [selectedChannel, setSelectedChannel] = useState<{
    deviceId: string;
    channelId: string;
  } | null>(null);

  useEffect(() => {
    loadDevices();
  }, []);

  const loadDevices = async () => {
    setLoading(true);
    try {
      const deviceList = await deviceApi.getList();
      setDevices(deviceList.filter((d) => d.status === 'ONLINE'));
      await buildTreeData(deviceList.filter((d) => d.status === 'ONLINE'));
    } catch (error) {
      message.error('Failed to load devices');
    } finally {
      setLoading(false);
    }
  };

  const buildTreeData = async (deviceList: Device[]) => {
    const tree: DataNode[] = [];

    for (const device of deviceList) {
      try {
        const channels = await deviceApi.getChannels(device.deviceId);
        const deviceNode: DataNode = {
          title: device.deviceName,
          key: device.deviceId,
          children: channels
            .filter((ch) => ch.status === 'ON')
            .map((channel) => ({
              title: channel.channelName || channel.channelId,
              key: `${device.deviceId}-${channel.channelId}`,
              isLeaf: true,
              data: { deviceId: device.deviceId, channelId: channel.channelId },
            })),
        };
        tree.push(deviceNode);
      } catch (error) {
        console.error(`Failed to load channels for device ${device.deviceId}`);
      }
    }

    setTreeData(tree);
  };

  const handleTreeSelect = (selectedKeys: React.Key[], info: any) => {
    if (info.node.isLeaf && info.node.data) {
      setSelectedChannel(info.node.data);
    }
  };

  const handlePlay = async () => {
    if (!selectedChannel) {
      message.warning('Please select a channel');
      return;
    }

    const window = videoWindows.find((w) => w.id === selectedWindow);
    if (!window) return;

    // Stop existing stream if any
    if (window.sessionId) {
      await handleStop(selectedWindow);
    }

    try {
      const response = await streamApi.play({
        deviceId: selectedChannel.deviceId,
        channelId: selectedChannel.channelId,
        streamType: 'LIVE',
      });

      setVideoWindows((prev) =>
        prev.map((w) =>
          w.id === selectedWindow
            ? {
                ...w,
                deviceId: selectedChannel.deviceId,
                channelId: selectedChannel.channelId,
                channelName: `${selectedChannel.deviceId}-${selectedChannel.channelId}`,
                sessionId: response.sessionId,
                flvUrl: response.flvUrl,
              }
            : w
        )
      );

      message.success('Stream started');
    } catch (error) {
      message.error('Failed to start stream');
    }
  };

  const handleStop = async (windowId: string) => {
    const window = videoWindows.find((w) => w.id === windowId);
    if (!window || !window.sessionId) return;

    try {
      await streamApi.stop(window.sessionId);
      setVideoWindows((prev) =>
        prev.map((w) =>
          w.id === windowId
            ? { id: w.id, deviceId: '', channelId: '', channelName: '' }
            : w
        )
      );
      message.success('Stream stopped');
    } catch (error) {
      message.error('Failed to stop stream');
    }
  };

  return (
    <Row gutter={16} style={{ height: 'calc(100vh - 160px)' }}>
      <Col span={5}>
        <Card title="设备通道" style={{ height: '100%', overflow: 'auto' }}>
          <Tree
            treeData={treeData}
            onSelect={handleTreeSelect}
            showLine
            defaultExpandAll
          />
        </Card>
      </Col>

      <Col span={14}>
        <Card
          title="视频监控"
          extra={
            <Space>
              <Button
                type="primary"
                icon={<PlayCircleOutlined />}
                onClick={handlePlay}
                disabled={!selectedChannel}
              >
                播放
              </Button>
            </Space>
          }
          style={{ height: '100%' }}
        >
          <Row gutter={[8, 8]} style={{ height: 'calc(100% - 60px)' }}>
            {videoWindows.map((window) => (
              <Col span={12} key={window.id}>
                <Card
                  size="small"
                  title={window.channelName || `窗口 ${window.id}`}
                  extra={
                    window.sessionId && (
                      <Button
                        type="text"
                        size="small"
                        danger
                        icon={<CloseCircleOutlined />}
                        onClick={() => handleStop(window.id)}
                      >
                        关闭
                      </Button>
                    )
                  }
                  style={{
                    height: '100%',
                    border: selectedWindow === window.id ? '2px solid #1890ff' : undefined,
                    cursor: 'pointer',
                  }}
                  onClick={() => setSelectedWindow(window.id)}
                  bodyStyle={{ padding: 0, height: 'calc(100% - 40px)' }}
                >
                  {window.flvUrl ? (
                    <FlvPlayer url={window.flvUrl} />
                  ) : (
                    <div
                      style={{
                        height: '100%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        backgroundColor: '#000',
                      }}
                    >
                      <Empty description="无视频" />
                    </div>
                  )}
                </Card>
              </Col>
            ))}
          </Row>
        </Card>
      </Col>

      <Col span={5}>
        {selectedWindow && videoWindows.find((w) => w.id === selectedWindow)?.sessionId && (
          <PTZControl
            deviceId={videoWindows.find((w) => w.id === selectedWindow)!.deviceId}
            channelId={videoWindows.find((w) => w.id === selectedWindow)!.channelId}
          />
        )}
      </Col>
    </Row>
  );
};

export default LiveStream;
