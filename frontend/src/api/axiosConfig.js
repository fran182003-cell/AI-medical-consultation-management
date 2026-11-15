// src/api/axiosConfig.js
import axios from 'axios';

const api = axios.create({
    // Al usar solo '/api', React usará automáticamente 
    // el dominio y puerto en el que esté abierto el navegador.
    baseURL: '/api', 
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);  

export default api;