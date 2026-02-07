import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { postApi } from '../api/postApi';
import type { PostResponse } from '../api/postApi';
import { MessageCircle, Clock, User, ChevronLeft, ChevronRight, Send, Pencil, Trash2, AlertTriangle } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';

export const PostDetail = () => {
    const { postId } = useParams<{ postId: string }>();
    const navigate = useNavigate();
    const { user } = useAuthStore();
    const [post, setPost] = useState<PostResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [newComment, setNewComment] = useState('');
    const [submittingComment, setSubmittingComment] = useState(false);

    // Delete Modal State
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    // Carousel State
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    const nextImage = () => {
        if (!post?.imageUrls) return;
        setCurrentImageIndex((prev) =>
            prev === post.imageUrls.length - 1 ? 0 : prev + 1
        );
    };

    const prevImage = () => {
        if (!post?.imageUrls) return;
        setCurrentImageIndex((prev) =>
            prev === 0 ? post.imageUrls.length - 1 : prev - 1
        );
    };

    const loadPost = async () => {
        if (!postId) return;
        try {
            const data = await postApi.getPost(postId);
            setPost(data);
        } catch (err: any) {
            console.error(err);
            setError('게시글을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPost();
    }, [postId]);

    const handleCommentSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!postId || !newComment.trim()) return;

        setSubmittingComment(true);
        try {
            await postApi.createComment(postId, newComment);
            setNewComment('');
            // Reload post to see new comment
            await loadPost();
        } catch (err: any) {
            console.error(err);
        } finally {
            setSubmittingComment(false);
        }
    };

    const handleDeleteClick = () => {
        setIsDeleteModalOpen(true);
    };

    const handleDeleteConfirm = async () => {
        if (!postId) return;

        setIsDeleting(true);
        try {
            await postApi.deletePost(postId);
            setIsDeleteModalOpen(false);
            navigate('/');
        } catch (err: any) {
            console.error(err);
            setIsDeleting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[50vh]">
                <div className="animate-spin rounded-full h-8 w-8 border-4 border-slate-200 border-t-slate-800"></div>
            </div>
        );
    }

    if (error || !post) {
        return (
            <div className="text-center py-12 text-red-500 bg-red-50 rounded-lg">
                {error || '게시글을 찾을 수 없습니다.'}
            </div>
        );
    }

    return (
        <div className="max-w-3xl mx-auto pb-20">
            {/* Header */}
            <button
                onClick={() => navigate(-1)}
                className="mb-6 text-slate-500 hover:text-slate-800 flex items-center gap-1 transition-colors"
            >
                <ChevronLeft size={20} />
                <span>뒤로가기</span>
            </button>

            <article className="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
                {/* Images Carousel */}
                {post.imageUrls && post.imageUrls.length > 0 && (
                    <div className="w-full bg-slate-100 relative group">
                        <div className="aspect-video w-full flex items-center justify-center bg-black/5 overflow-hidden">
                            <img
                                src={post.imageUrls[currentImageIndex]}
                                alt={`Post image ${currentImageIndex + 1}`}
                                className="w-full h-full object-contain"
                            />
                        </div>

                        {/* Navigation Buttons */}
                        {post.imageUrls.length > 1 && (
                            <>
                                <button
                                    onClick={prevImage}
                                    className="absolute left-4 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full opacity-0 group-hover:opacity-100 transition-all backdrop-blur-sm"
                                    title="이전 사진"
                                >
                                    <ChevronLeft size={24} />
                                </button>
                                <button
                                    onClick={nextImage}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full opacity-0 group-hover:opacity-100 transition-all backdrop-blur-sm"
                                    title="다음 사진"
                                >
                                    <ChevronRight size={24} />
                                </button>

                                {/* Indicators */}
                                <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
                                    {post.imageUrls.map((_, idx) => (
                                        <div
                                            key={idx}
                                            className={`w-2 h-2 rounded-full transition-all ${idx === currentImageIndex ? 'bg-white w-4' : 'bg-white/50'
                                                }`}
                                        />
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                )}

                <div className="p-8">
                    {/* Meta */}
                    <div className="flex items-center justify-between mb-6">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-slate-100 rounded-full flex items-center justify-center text-slate-500">
                                <User size={20} />
                            </div>
                            <div>
                                <h3 className="font-semibold text-slate-900">{post.userName}</h3>
                                <div className="flex items-center gap-2 text-xs text-slate-500 bg-slate-50 px-2 py-1 rounded">
                                    <Clock size={12} />
                                    <span>{new Date(post.createdAt).toLocaleString()}</span>
                                </div>
                            </div>
                        </div>

                        {/* Owner Actions */}
                        {user?.username === post.userName && (
                            <div className="flex gap-2">
                                <button
                                    onClick={() => navigate(`/posts/${postId}/edit`)}
                                    className="p-2 text-slate-400 hover:text-blue-500 transition-colors"
                                    title="게시글 수정"
                                >
                                    <Pencil size={18} />
                                </button>
                                <button
                                    onClick={handleDeleteClick}
                                    className="p-2 text-slate-400 hover:text-red-500 transition-colors"
                                    title="게시글 삭제"
                                >
                                    <Trash2 size={18} />
                                </button>
                            </div>
                        )}
                    </div>

                    <h1 className="text-2xl font-bold text-slate-900 mb-6">{post.title}</h1>
                    <div className="prose prose-slate max-w-none text-slate-700 whitespace-pre-wrap leading-relaxed">
                        {post.content}
                    </div>
                </div>
            </article>

            {/* Comments Section */}
            <div className="mt-8 bg-white rounded-2xl shadow-sm border border-slate-100 p-8">
                <h2 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
                    <MessageCircle className="text-blue-500" />
                    댓글 ({post.comments ? post.comments.length : 0})
                </h2>

                <form onSubmit={handleCommentSubmit} className="mb-8 relative">
                    <input
                        type="text"
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder="댓글을 작성하세요..."
                        className="w-full px-4 py-3 pr-12 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-all"
                    />
                    <button
                        type="submit"
                        disabled={!newComment.trim() || submittingComment}
                        className="absolute right-2 top-1/2 -translate-y-1/2 p-2 text-blue-500 hover:bg-blue-50 rounded-lg disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                    >
                        <Send size={18} />
                    </button>
                </form>

                <div className="space-y-6">
                    {post.comments && post.comments.length > 0 ? (
                        post.comments.map((comment) => (
                            <div key={comment.id} className="flex gap-3 animate-fade-in">
                                <div className="w-8 h-8 bg-slate-50 rounded-full flex items-center justify-center text-slate-400 flex-shrink-0">
                                    <User size={16} />
                                </div>
                                <div className="flex-1 bg-slate-50 px-4 py-3 rounded-lg rounded-tl-none">
                                    <div className="flex items-center justify-between mb-1">
                                        <span className="text-sm font-semibold text-slate-800">{comment.writer}</span>
                                        <span className="text-xs text-slate-400">
                                            {new Date(comment.createdAt).toLocaleDateString()}
                                        </span>
                                    </div>
                                    <p className="text-slate-700 text-sm whitespace-pre-wrap">{comment.content}</p>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="text-center py-8 text-slate-400 italic">
                            첫 번째 댓글을 남겨보세요!
                        </div>
                    )}
                </div>
            </div>

            {/* Delete Confirmation Modal */}
            <Modal
                isOpen={isDeleteModalOpen}
                onClose={() => setIsDeleteModalOpen(false)}
                title="게시글 삭제"
            >
                <div className="text-center space-y-4">
                    <div className="w-12 h-12 bg-red-100 text-red-500 rounded-full flex items-center justify-center mx-auto">
                        <AlertTriangle size={24} />
                    </div>
                    <p className="text-slate-600">
                        정말로 이 게시글을 삭제하시겠습니까?<br />
                        삭제된 게시글은 복구할 수 없습니다.
                    </p>
                    <div className="flex gap-3 pt-2">
                        <Button
                            variant="outline"
                            onClick={() => setIsDeleteModalOpen(false)}
                            className="flex-1"
                        >
                            취소
                        </Button>
                        <Button
                            variant="danger"
                            onClick={handleDeleteConfirm}
                            isLoading={isDeleting}
                            className="flex-1"
                        >
                            삭제하기
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};
