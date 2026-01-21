import api from './axios';

// Based on MyPostListResponseDto.java
export interface MyPostListResponse {
    uuid: string;
    title: string;
    content: string;
    createdAt: string;
    commentCount: number;
}

export interface MyPageResponse {
    loginId: string;
    username: string; // Corrected
    age: number;
    gender: 'MALE' | 'FEMALE';
    posts: MyPostListResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface MemberUpdateDto {
    password?: string;
    username?: string;
    age?: number;
    gender?: 'MALE' | 'FEMALE';
}

export const userApi = {
    getMyInfo: async (page: number = 1) => {
        const response = await api.get<MyPageResponse>(`/mypage?page=${page}`);
        return response.data;
    },

    updateMyInfo: async (data: MemberUpdateDto) => {
        await api.patch('/mypage', data);
    }
};
