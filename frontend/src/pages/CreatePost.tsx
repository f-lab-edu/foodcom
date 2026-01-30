import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { postApi } from '../api/postApi';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Image as ImageIcon, X } from 'lucide-react';

export const CreatePost = () => {
    const navigate = useNavigate();
    const { postId } = useParams<{ postId: string }>(); // Optional param for edit mode
    const isEditMode = !!postId;

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [images, setImages] = useState<File[]>([]); // Store actual File objects
    const [previewUrls, setPreviewUrls] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    // Load existing post data if in edit mode
    useEffect(() => {
        if (isEditMode) {
            const loadPost = async () => {
                try {
                    const data = await postApi.getPost(postId);
                    setTitle(data.title);
                    setContent(data.content);
                    // Note: Handling existing images for update is complex if backend doesn't support keeping old ones easily with FormData.
                    // For now, we might assume user re-uploads or we just show existing text.
                    // If your backend `updatePost` replaces all images, we should probably warn the user or just focus on text update for now unless we implement sophisticated image merge logic.
                    // Given the simple `updatePost` in api, let's assume it replaces.
                } catch (err: any) {
                    console.error(err);
                    setError('게시글 정보를 불러오는데 실패했습니다.');
                }
            };
            loadPost();
        }
    }, [isEditMode, postId]);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const newFiles = Array.from(e.target.files);
            setImages(prev => [...prev, ...newFiles]);

            const newPreviews = newFiles.map(file => URL.createObjectURL(file));
            setPreviewUrls(prev => [...prev, ...newPreviews]);
        }
    };

    const removeImage = (index: number) => {
        setImages(prev => prev.filter((_, i) => i !== index));
        URL.revokeObjectURL(previewUrls[index]); // Cleanup
        setPreviewUrls(prev => prev.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        if (!title.trim() || !content.trim()) {
            setError('제목과 내용을 모두 입력해주세요.');
            setIsLoading(false);
            return;
        }

        try {
            if (isEditMode) {
                await postApi.updatePost(postId, { title, content, files: images });
                navigate(`/posts/${postId}`);
            } else {
                await postApi.createPost({ title, content, files: images });
                navigate('/');
            }
        } catch (err: any) {
            console.error(err);
            setError('게시글 저장 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto py-8 px-4">
            <h1 className="text-3xl font-bold mb-8 text-transparent bg-clip-text bg-gradient-to-r from-slate-900 to-slate-700">
                {isEditMode ? '게시글 수정' : '새 게시글 작성'}
            </h1>

            {error && (
                <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-6 text-sm">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                        제목
                    </label>
                    <Input
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="제목을 입력하세요"
                        required
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                        내용
                    </label>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder="내용을 입력하세요"
                        className="w-full px-4 py-3 rounded-lg border border-slate-200 focus:border-slate-500 focus:ring-1 focus:ring-slate-500 outline-none transition-all duration-200 min-h-[300px] resize-y bg-slate-50/50"
                        required
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                        이미지 첨부 {isEditMode && <span className="text-xs text-slate-400 font-normal ml-2">(수정 시 기존 이미지는 삭제되고 새로 업로드됩니다)</span>}
                    </label>
                    <div className="mt-2 flex flex-wrap gap-4">
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleImageChange}
                            multiple
                            accept="image/*"
                            className="hidden"
                        />
                        <button
                            type="button"
                            onClick={() => fileInputRef.current?.click()}
                            className="w-24 h-24 flex flex-col items-center justify-center border-2 border-dashed border-slate-300 rounded-lg text-slate-500 hover:border-slate-500 hover:text-slate-700 transition-colors"
                        >
                            <ImageIcon className="w-6 h-6 mb-1" />
                            <span className="text-xs">추가</span>
                        </button>

                        {previewUrls.map((url, index) => (
                            <div key={index} className="relative w-24 h-24 group">
                                <img
                                    src={url}
                                    alt={`Preview ${index}`}
                                    className="w-full h-full object-cover rounded-lg"
                                />
                                <button
                                    type="button"
                                    onClick={() => removeImage(index)}
                                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                                >
                                    <X className="w-3 h-3" />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="flex justify-end gap-3 pt-4">
                    <Button
                        type="button"
                        variant="ghost"
                        onClick={() => {
                            if (isEditMode) {
                                navigate(-1);
                            } else {
                                navigate('/');
                            }
                        }}
                    >
                        취소
                    </Button>
                    <Button
                        type="submit"
                        isLoading={isLoading}
                    >
                        {isEditMode ? '수정하기' : '작성하기'}
                    </Button>
                </div>
            </form>
        </div>
    );
};
