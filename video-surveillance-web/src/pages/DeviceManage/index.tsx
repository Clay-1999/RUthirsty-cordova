import { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Tag,
  Popconfirm,
  Card,
  Statistic,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SyncOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { deviceApi } from '@/api/device';
import type { Device } from '@/types';
import dayjs from 'dayjs';

const DeviceManage = () => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDevice, setEditingDevice] = useState<Device | null>(null);
  const [form] = Form.useForm();
  const [stats, setStats] = useState({ total: 0, online: 0, offline: 0 });

  useEffect(() => {
    loadDevices();
    loadStats();
  }, []);

  const loadDevices = async () => {
    setLoading(true);
    try {
      const data = await deviceApi.getList();
      setDevices(data);
    } catch (error) {
      message.error('Failed to load devices');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const data = await deviceApi.getStatus();
      setStats(data);
    } catch (error) {
      console.error('Failed to load stats');
    }
  };

  const handleAdd = () => {
    setEditingDevice(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Device) => {
    setEditingDevice(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deviceApi.delete(id);
      message.success('Device deleted successfully');
      loadDevices();
      loadStats();
    } catch (error) {
      message.error('Failed to delete device');
    }
  };

  const handleSync = async (deviceId: string) => {
    try {
      await deviceApi.syncChannels(deviceId);
      message.success('Channel sync started');
    } catch (error) {
      message.error('Failed to sync channels');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingDevice) {
        await deviceApi.update({ ...editingDevice, ...values });
        message.success('Device updated successfully');
      } else {
        await deviceApi.add(values);
        message.success('Device added successfully');
      }
      setModalVisible(false);
      loadDevices();
      loadStats();
    } catch (error) {
      message.error('Failed to save device');
    }
  };

  const columns: ColumnsType<Device> = [
    {
      title: '设备编码',
      dataIndex: 'deviceId',
      key: 'deviceId',
      width: 180,
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 100,
      render: (type) => (
        <Tag color={type === 'GB28181' ? 'blue' : 'green'}>{type}</Tag>
      ),
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        <Tag color={status === 'ONLINE' ? 'success' : 'default'}>
          {status === 'ONLINE' ? '在线' : '离线'}
        </Tag>
      ),
    },
    {
      title: '最后心跳',
      dataIndex: 'lastKeepaliveTime',
      key: 'lastKeepaliveTime',
      width: 180,
      render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          {record.deviceType === 'GB28181' && (
            <Button
              type="link"
              size="small"
              icon={<SyncOutlined />}
              onClick={() => handleSync(record.deviceId)}
            >
              同步
            </Button>
          )}
          <Popconfirm
            title="确定删除此设备?"
            onConfirm={() => handleDelete(record.id!)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card>
            <Statistic title="设备总数" value={stats.total} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="在线设备" value={stats.online} valueStyle={{ color: '#3f8600' }} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="离线设备" value={stats.offline} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
      </Row>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加设备
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadDevices}>
            刷新
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={devices}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      <Modal
        title={editingDevice ? '编辑设备' : '添加设备'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="deviceId"
            label="设备编码"
            rules={[{ required: true, message: '请输入设备编码' }]}
          >
            <Input placeholder="请输入设备编码" disabled={!!editingDevice} />
          </Form.Item>

          <Form.Item
            name="deviceName"
            label="设备名称"
            rules={[{ required: true, message: '请输入设备名称' }]}
          >
            <Input placeholder="请输入设备名称" />
          </Form.Item>

          <Form.Item
            name="deviceType"
            label="设备类型"
            rules={[{ required: true, message: '请选择设备类型' }]}
          >
            <Select placeholder="请选择设备类型">
              <Select.Option value="GB28181">GB28181</Select.Option>
              <Select.Option value="ONVIF">ONVIF</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item name="manufacturer" label="厂商">
            <Input placeholder="请输入厂商" />
          </Form.Item>

          <Form.Item name="model" label="型号">
            <Input placeholder="请输入型号" />
          </Form.Item>

          <Form.Item name="ipAddress" label="IP地址">
            <Input placeholder="请输入IP地址" />
          </Form.Item>

          <Form.Item name="port" label="端口">
            <Input type="number" placeholder="请输入端口" />
          </Form.Item>

          <Form.Item name="username" label="用户名">
            <Input placeholder="请输入用户名" />
          </Form.Item>

          <Form.Item name="password" label="密码">
            <Input.Password placeholder="请输入密码" />
          </Form.Item>

          <Form.Item name="transport" label="传输协议">
            <Select placeholder="请选择传输协议">
              <Select.Option value="UDP">UDP</Select.Option>
              <Select.Option value="TCP">TCP</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DeviceManage;
