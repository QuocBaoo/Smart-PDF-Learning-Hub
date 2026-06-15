import { create } from "zustand";

interface UserProfile {
  id: string;
  email: string;
  fullName?: string;
  avatarUrl?: string;
}

interface AuthState {
  user: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  setAuth: (user: UserProfile | null, token: string | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  setAuth: (user, token) => set({ 
    user, 
    token, 
    isAuthenticated: !!token 
  }),
  logout: () => set({ 
    user: null, 
    token: null, 
    isAuthenticated: false 
  }),
}));
