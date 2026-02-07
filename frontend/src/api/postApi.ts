import api from './axios';

export interface PostCreateRequest {
    title: string;
    content: string;
    files?: File[];
}

export interface PostUpdateRequest {
    title: string;
    content: string;
    files?: File[];
}

export interface PostResponse { // For Detail
    id: number;
    title: string;
    content: string;
    userName: string;
    createdAt: string;
    imageUrls: string[];
    comments: CommentResponse[];
}

export interface PostListResponse { // For Home Feed
    id: number;
    title: string;
    writer: string;
    thumbnailUrl: string | null;
    createdAt: string;
    commentCount: number;
    // content is NOT in PostListResponseDto! Home.tsx used it.
    // I should remove content from Home display or add it to backend if needed.
    // But usually feed only shows title/thumbnail/meta.
}

export interface CommentResponse {
    id: number;
    content: string;
    writer: string;
    createdAt: string;
}

export interface PostPageResponse {
    postList: PostListResponse[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}


export const postApi = {
    getPosts: async (page: number = 1) => {
        const response = await api.get<PostPageResponse>(`/posts?page=${page}`);
        return response.data;
    },

    getPost: async (id: string) => {
        const response = await api.get<PostResponse>(`/posts/${id}`);
        return response.data;
    },

    createPost: async (data: PostCreateRequest) => {
        const formData = new FormData();

        // JSON part
        const jsonPart = JSON.stringify({
            title: data.title,
            content: data.content
        });
        const blob = new Blob([jsonPart], { type: 'application/json' });
        formData.append('data', blob);

        // Files part
        if (data.files) {
            data.files.forEach(file => {
                formData.append('files', file);
            });
        }

        const response = await api.post('/posts', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    updatePost: async (id: string, data: PostUpdateRequest) => {
        const formData = new FormData();

        const jsonPart = JSON.stringify({
            title: data.title,
            content: data.content
        });
        const blob = new Blob([jsonPart], { type: 'application/json' });
        formData.append('data', blob);

        if (data.files) {
            data.files.forEach(file => {
                formData.append('files', file);
            });
        }

        const response = await api.patch(`/posts/${id}`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    deletePost: async (id: string) => {
        await api.delete(`/posts/${id}`);
    },

    createComment: async (postId: string, content: string) => {
        await api.post(`/posts/${postId}/comments`, { content });
    }
};
