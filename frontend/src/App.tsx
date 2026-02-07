import { Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Signup } from './pages/Signup';
import { CreatePost } from './pages/CreatePost';
import { MyPage } from './pages/MyPage';
import { PostDetail } from './pages/PostDetail';
// Import other pages as created

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/write" element={<CreatePost />} />
        <Route path="/posts/:postId/edit" element={<CreatePost />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/posts/:postId" element={<PostDetail />} />
        {/* Add more routes later */}
      </Route>
    </Routes>
  );
}

export default App;
