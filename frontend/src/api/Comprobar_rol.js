import { jwtDecode } from 'jwt-decode';

export const useAuth = () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) return { isLogged: false, isAdmin: false };

    try {
        const decoded = jwtDecode(token);
        // "role" es el nombre que pusimos en el claim del backend
        return { 
            isLogged: true, 
            isAdmin: decoded.role === 'ROLE_ADMIN',
            username: decoded.sub 
        };
    } catch (e) {
        return { isLogged: false, isAdmin: false };
    }
};
