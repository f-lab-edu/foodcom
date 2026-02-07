import { useEffect, useState, useRef } from 'react';
import { postApi } from '../api/postApi';
import type { PostPageResponse, PostListResponse } from '../api/postApi';
import { Link } from 'react-router-dom';
import { MessageCircle, Clock } from 'lucide-react';

export const Home = () => {
    const [posts, setPosts] = useState<PostListResponse[]>([]);
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const hasFetched = useRef(false); // Strict Mode 중복 방지

    const loadPosts = async (pageNum: number) => {
        if (loading) return;
        setLoading(true);
        try {
            const data: PostPageResponse = await postApi.getPosts(pageNum);

            if (data.postList.length === 0) {
                setHasMore(false);
            } else {
                // 중복 제거: id 기준으로 필터링
                setPosts(prev => {
                    const existingIds = new Set(prev.map(p => p.id));
                    const newPosts = data.postList.filter(p => !existingIds.has(p.id));
                    return [...prev, ...newPosts];
                });
                setPage(pageNum + 1);
                if (pageNum >= data.totalPages) {
                    setHasMore(false);
                }
            }
        } catch (error) {
            console.error('Failed to load posts', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!hasFetched.current) {
            hasFetched.current = true;
            loadPosts(1);
        }
    }, []);

    return (
        <div className="space-y-6 max-w-2xl mx-auto">
            <h1 className="text-3xl font-bold text-slate-900 mb-8">Latest Posts</h1>

            {posts.map((post) => (
                <Link
                    to={`/posts/${post.id}`}
                    key={post.id}
                    className="block bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden hover:shadow-md transition-shadow duration-300"
                >
                    {post.thumbnailUrl && (
                        <div className="aspect-video w-full relative overflow-hidden bg-slate-100">
                            <img
                                src={post.thumbnailUrl}
                                alt={post.title}
                                className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-500"
                            />
                        </div>
                    )}

                    <div className="p-5">
                        <h2 className="text-xl font-bold text-slate-800 mb-2 line-clamp-1">{post.title}</h2>

                        <div className="flex items-center justify-between text-sm text-slate-500 pt-4 border-t border-slate-100">
                            <div className="flex items-center gap-2">
                                <span className="font-medium text-slate-700">{post.writer}</span>
                            </div>
                            <div className="flex items-center gap-4">
                                <div className="flex items-center gap-1">
                                    <Clock size={14} />
                                    <span>{new Date(post.createdAt).toLocaleDateString()}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <MessageCircle size={14} />
                                    <span>{post.commentCount}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </Link>
            ))}

            {loading && (
                <div className="text-center py-8">
                    <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-blue-500 border-t-transparent"></div>
                </div>
            )}

            {!hasMore && posts.length > 0 && (
                <div className="text-center py-8 text-slate-500">
                    You've reached the end!
                </div>
            )}

            {hasMore && !loading && (
                <div className="text-center py-4">
                    <button onClick={() => loadPosts(page)} className="text-blue-600 hover:text-blue-700 font-medium">Load More</button>
                </div>
            )}
        </div>
    );
};
