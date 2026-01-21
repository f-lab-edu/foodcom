import { useEffect, useState } from 'react';
import { userApi } from '../api/userApi';
import type { MyPageResponse, MemberUpdateDto } from '../api/userApi';
import { Link } from 'react-router-dom';
import { MessageCircle, Clock, User, Settings, X, Save } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const MyPage = () => {
    const [profile, setProfile] = useState<MyPageResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Edit Modal State
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editForm, setEditForm] = useState<MemberUpdateDto>({
        username: '',
        age: 0,
        gender: 'MALE',
        password: '' // Optional for change
    });
    const [saving, setSaving] = useState(false);

    const loadMyPage = async () => {
        try {
            const data = await userApi.getMyInfo(); // Default page 1
            setProfile(data);
            setEditForm({
                username: data.username,
                age: data.age,
                gender: data.gender,
                password: ''
            });
        } catch (err: any) {
            console.error(err);
            setError('정보를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadMyPage();
    }, []);

    const handleEditSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        try {
            // Only send password if it's not empty
            const updateData: MemberUpdateDto = {
                username: editForm.username,
                age: Number(editForm.age),
                gender: editForm.gender,
            };

            if (editForm.password && editForm.password.trim() !== '') {
                updateData.password = editForm.password;
            }

            await userApi.updateMyInfo(updateData);
            setIsEditModalOpen(false);
            loadMyPage(); // Reload profile
        } catch (err: any) {
            console.error(err);
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[50vh]">
                <div className="animate-spin rounded-full h-8 w-8 border-4 border-slate-200 border-t-slate-800"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-center py-12 text-red-500 bg-red-50 rounded-lg">
                {error}
            </div>
        );
    }

    if (!profile) return null;

    return (
        <div className="max-w-4xl mx-auto space-y-8 relative">
            {/* Profile Header */}
            <div className="bg-white rounded-2xl p-8 shadow-sm border border-slate-100 flex flex-col md:flex-row items-center gap-8">
                <div className="w-24 h-24 bg-gradient-to-br from-blue-100 to-indigo-100 rounded-full flex items-center justify-center text-blue-600">
                    <User size={40} />
                </div>

                <div className="flex-1 text-center md:text-left">
                    <h1 className="text-2xl font-bold text-slate-900 mb-2">{profile.username}</h1>
                    <div className="text-slate-500 space-y-1">
                        <p>ID: {profile.loginId}</p>
                        <p>{profile.age}세 · {profile.gender === 'MALE' ? '남성' : '여성'}</p>
                    </div>
                </div>

                <button
                    onClick={() => setIsEditModalOpen(true)}
                    className="px-4 py-2 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 transition-colors flex items-center gap-2"
                >
                    <Settings size={18} />
                    <span>정보 수정</span>
                </button>
            </div>

            {/* My Posts */}
            <div>
                <h2 className="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <MessageCircle className="text-blue-500" />
                    내 작성 글 ({profile.totalElements})
                </h2>

                <div className="grid gap-6">
                    {profile.posts.length === 0 ? (
                        <div className="text-center py-12 bg-slate-50 rounded-xl text-slate-500">
                            작성한 게시글이 없습니다.
                        </div>
                    ) : (
                        profile.posts.map((post) => (
                            <Link
                                to={`/posts/${post.uuid}`}
                                key={post.uuid}
                                className="block bg-white p-6 rounded-xl shadow-sm border border-slate-100 hover:border-blue-200 transition-colors"
                            >
                                <div className="flex justify-between items-start mb-2">
                                    <h3 className="text-lg font-bold text-slate-900 line-clamp-1">{post.title}</h3>
                                    <div className="flex items-center gap-1 text-xs text-slate-400 bg-slate-50 px-2 py-1 rounded">
                                        <Clock size={12} />
                                        {new Date(post.createdAt).toLocaleDateString()}
                                    </div>
                                </div>
                                <p className="text-slate-600 line-clamp-2 text-sm">{post.content}</p>

                                <div className="mt-4 flex gap-4 text-xs text-slate-500">
                                    <span className="flex items-center gap-1">
                                        <MessageCircle size={12} />
                                        댓글 {post.commentCount}
                                    </span>
                                </div>
                            </Link>
                        ))
                    )}
                </div>
            </div>

            {/* Edit Modal */}
            {isEditModalOpen && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4 backdrop-blur-sm animate-fade-in">
                    <div className="bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden animate-scale-in">
                        <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
                            <h3 className="text-lg font-bold text-slate-900">내 정보 수정</h3>
                            <button
                                onClick={() => setIsEditModalOpen(false)}
                                className="text-slate-400 hover:text-slate-600 transition-colors"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleEditSubmit} className="p-6 space-y-4">
                            <Input
                                label="이름 (닉네임)"
                                value={editForm.username}
                                onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
                            />

                            <Input
                                label="나이"
                                type="number"
                                value={editForm.age}
                                onChange={(e) => setEditForm({ ...editForm, age: Number(e.target.value) })}
                            />

                            <div className="space-y-2">
                                <label className="block text-sm font-medium text-slate-700">성별</label>
                                <div className="flex gap-4">
                                    <label className="flex items-center gap-2 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="gender"
                                            value="MALE"
                                            checked={editForm.gender === 'MALE'}
                                            onChange={() => setEditForm({ ...editForm, gender: 'MALE' })}
                                            className="w-4 h-4 text-blue-600 focus:ring-blue-500"
                                        />
                                        <span className="text-slate-700">남성</span>
                                    </label>
                                    <label className="flex items-center gap-2 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="gender"
                                            value="FEMALE"
                                            checked={editForm.gender === 'FEMALE'}
                                            onChange={() => setEditForm({ ...editForm, gender: 'FEMALE' })}
                                            className="w-4 h-4 text-blue-600 focus:ring-blue-500"
                                        />
                                        <span className="text-slate-700">여성</span>
                                    </label>
                                </div>
                            </div>

                            <hr className="my-4 border-slate-100" />

                            <Input
                                label="새 비밀번호 (변경시에만 입력)"
                                type="password"
                                placeholder="변경하지 않으려면 비워두세요"
                                value={editForm.password}
                                onChange={(e) => setEditForm({ ...editForm, password: e.target.value })}
                            />

                            <div className="pt-4 flex gap-3">
                                <Button
                                    type="button"
                                    variant="outline"
                                    onClick={() => setIsEditModalOpen(false)}
                                    className="flex-1"
                                >
                                    취소
                                </Button>
                                <Button
                                    type="submit"
                                    isLoading={saving}
                                    className="flex-1 flex items-center justify-center gap-2"
                                >
                                    <Save size={18} />
                                    저장
                                </Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};
