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
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LinkOutlined,
  DisconnectOutlined,
  SyncOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { cascadeApi } from '@/api/cascade';
import type { CascadeConfig } from '@/types';
import dayjs from 'dayjs';

const CascadeManage = () => {
  const [configs, setConfigs] = useState<CascadeConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingConfig, setEditingConfig] = useState<CascadeConfig | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadConfigs();
  }, []);

  const loadConfigs = async () => {
    setLoading(true);
    try {
      const data = await cascadeApi.getList();
      setConfigs(data);
    } catch (error) {
      message.error('Failed to load cascade configs');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingConfig(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: CascadeConfig) => {
    setEditingConfig(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await cascadeApi.delete(id);
      message.success('Cascade config deleted successfully');
      loadConfigs();
    } catch (error) {
      message.error('Failed to delete cascade config');
    }
  };

  const handleRegister = async (id: number) => {
    try {
      await cascadeApi.register(id);
      message.success('Registration started');
      setTimeout(loadConfigs, 1000);
    } catch (error) {
      message.error('Failed to register');
    }
  };

  const handleUnregister = async (id: number) => {
    try {
      await cascadeApi.unregister(id);
      message.success('Unregistration started');
      setTimeout(loadConfigs, 1000);
    } catch (error) {
      message.error('Failed to unregister');
    }
  };

  const handleSyncCatalog = async (id: number) => {
    try {
      await cascadeApi.syncCatalog(id);
      message.success('Catalog sync started');
    } catch (error) {
      message.error('Failed to sync catalog');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingConfig) {
        await cascadeApi.update({ ...editingConfig, ...values });
        message.success('Cascade config updated successfully');
      } else {
        await cascadeApi.add(values);
        message.success('Cascade config added successfully');
      }
      setModalVisible(false);
      loadConfigs();
    } catch (error) {
      message.error('Failed to save cascade config');
    }
  };

  const getStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      REGISTERED: { color: 'success', text: '已注册' },
      ENABLED: { color: 'processing', text: '已启用' },
      DISABLED: { color: 'default', text: '已禁用' },
      UNREGISTERED: { color: 'warning', text: '未注册' },
    };
    const config = statusMap[status] || { color: 'default', text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const columns: ColumnsType<CascadeConfig> = [
    {
      title: '平台编码',
      dataIndex: 'platformId',
      key: 'platformId',
      width: 180,
    },
    {
      title: '平台名称',
      dataIndex: 'platformName',
      key: 'platformName',
    },
    {
      title: '服务器地址',
      key: 'server',
      width: 200,
      render: (_, record) => `${record.serverIp}:${record.serverPort}`,
    },
    {
      title: '本地编码',
      dataIndex: 'localId',
      key: 'localId',
      width: 180,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => getStatusTag(status),
    },
    {
      title: '注册时间',
      dataIndex: 'registerTime',
      key: 'registerTime',
      width: 180,
      render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 300,
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
          {record.status === 'REGISTERED' ? (
            <>
              <Button
                type="link"
                size="small"
                icon={<DisconnectOutlined />}
                onClick={() => handleUnregister(record.id!)}
              >
                注销
              </Button>
              <Button
                type="link"
                size="small"
                icon={<SyncOutlined />}
                onClick={() => handleSyncCatalog(record.id!)}
              >
                同步
              </Button>
            </>
          ) : (
            <Button
              type="link"
              size="small"
              icon={<LinkOutlined />}
              onClick={() => handleRegister(record.id!)}
            >
              注册
            </Button>
          )}
          <Popconfirm
            title="确定删除此级联配置?"
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
    <Card>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加级联
        </Button>
        <Button icon={<ReloadOutlined />} onClick={loadConfigs}>
          刷新
        </Button>
      </Space>

      <Table
        columns={columns}
        dataSource={configs}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1400 }}
        pagination={{
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
      />

      <Modal
        title={editingConfig ? '编辑级联配置' : '添加级联配置'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="platformId"
            label="上级平台编码"
            rules={[{ required: true, message: '请输入上级平台编码' }]}
          >
            <Input placeholder="请输入上级平台编码(20位)" disabled={!!editingConfig} />
          </Form.Item>

          <Form.Item
            name="platformName"
            label="上级平台名称"
            rules={[{ required: true, message: '请输入上级平台名称' }]}
          >
            <Input placeholder="请输入上级平台名称" />
          </Form.Item>

          <Form.Item
            name="serverIp"
            label="上级平台IP"
            rules={[{ required: true, message: '请输入上级平台IP' }]}
          >
            <Input placeholder="请输入上级平台IP" />
          </Form.Item>

          <Form.Item
            name="serverPort"
            label="上级平台端口"
            rules={[{ required: true, message: '请输入上级平台端口' }]}
          >
            <Input type="number" placeholder="请输入上级平台端口" />
          </Form.Item>

          <Form.Item name="serverDomain" label="上级平台域">
            <Input placeholder="请输入上级平台域" />
          </Form.Item>

          <Form.Item
            name="localId"
            label="本级平台编码"
            rules={[{ required: true, message: '请输入本级平台编码' }]}
          >
            <Input placeholder="请输入本级平台编码(20位)" />
          </Form.Item>

          <Form.Item
            name="localIp"
            label="本级平台IP"
            rules={[{ required: true, message: '请输入本级平台IP' }]}
          >
            <Input placeholder="请输入本级平台IP" />
          </Form.Item>

          <Form.Item
            name="localPort"
            label="本级平台端口"
            rules={[{ required: true, message: '请输入本级平台端口' }]}
          >
            <Input type="number" placeholder="请输入本级平台端口" />
          </Form.Item>

          <Form.Item name="username" label="认证用户名">
            <Input placeholder="请输入认证用户名" />
          </Form.Item>

          <Form.Item name="password" label="认证密码">
            <Input.Password placeholder="请输入认证密码" />
          </Form.Item>

          <Form.Item name="transport" label="传输协议" initialValue="UDP">
            <Select>
              <Select.Option value="UDP">UDP</Select.Option>
              <Select.Option value="TCP">TCP</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item name="expires" label="注册有效期(秒)" initialValue={3600}>
            <Input type="number" />
          </Form.Item>

          <Form.Item name="keepaliveInterval" label="心跳周期(秒)" initialValue={60}>
            <Input type="number" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default CascadeManage;
