import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import DeviceManage from './pages/DeviceManage';
import LiveStream from './pages/LiveStream';
import CascadeManage from './pages/CascadeManage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Navigate to="/device" replace />} />
        <Route path="device" element={<DeviceManage />} />
        <Route path="live" element={<LiveStream />} />
        <Route path="cascade" element={<CascadeManage />} />
      </Route>
    </Routes>
  );
}

export default App;
