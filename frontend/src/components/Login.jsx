import React, { useState } from 'react';
import api from '../api/axiosConfig';
import logoUJA from '../logo-uja.png';

const Login = () => {
    const [creds, setCreds] = useState({ username: '', password: '' });
    const [errorGenerico, setErrorGenerico] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorGenerico('');

        try {
            const response = await api.post('/auth/login', creds);
            if (response.data && response.data.token) {
                localStorage.setItem('jwtToken', response.data.token);
                localStorage.setItem('medicoUser', creds.username);
                window.location.href = '/dashboard';
            }
        } catch (err) {
            if (err.response && err.response.status === 400) {
                setErrorGenerico('Error al iniciar sesión.');
            } else if (err.response && err.response.status === 401) {
                setErrorGenerico('Usuario o contraseña incorrectos.');
            } else {
                setErrorGenerico('Fallo de conexión con el servidor.');
            }
        }
    };

    const styles = {
        container: { display: 'flex', flexDirection: 'column', height: '100vh', background: '#f4f4f4', fontFamily: 'Arial, sans-serif' },
        header: { backgroundColor: '#009640', padding: '15px 40px', color: 'white', display: 'flex', alignItems: 'center', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' },
        logo: { height: '60px', width: 'auto', objectFit: 'contain', marginRight: '20px' },
        headerTitle: { fontSize: '24px', fontWeight: '300', borderLeft: '1px solid rgba(255,255,255,0.3)', paddingLeft: '20px' },
        content: { flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' },
        card: { background: 'white', padding: '40px', width: '100%', maxWidth: '400px', boxShadow: '0 4px 15px rgba(0,0,0,0.05)', borderTop: '4px solid #009640', borderRadius: '4px' },
        inputGroup: { marginBottom: '20px' },
        label: { display: 'block', marginBottom: '8px', color: '#333', fontSize: '14px', fontWeight: 'bold' },
        input: { width: '100%', padding: '12px', border: '1px solid #ccc', borderRadius: '0', fontSize: '16px', boxSizing: 'border-box' },
        button: { width: '100%', padding: '12px', backgroundColor: '#009640', color: 'white', border: 'none', fontSize: '16px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.3s' },
        footer: { textAlign: 'center', padding: '20px', color: '#666', fontSize: '13px', borderTop: '1px solid #e0e0e0', backgroundColor: 'white' }
    };

    return (
        <div style={styles.container}>
            <header style={styles.header}>
                <img src={logoUJA} alt="Logo UJA" style={styles.logo} />
                <div style={styles.headerTitle}>Servicio de Salud de Jaén</div>
            </header>

            <div style={styles.content}>
                <div style={styles.card}>
                    <h2 style={{marginTop: 0, color: '#009640', fontSize: '22px'}}>Acceso Profesional</h2>
                    <p style={{fontSize: '13px', color: '#666', marginBottom: '20px'}}>
                        Introduzca sus credenciales de acceso al sistema
                    </p>
                    
                    <form onSubmit={handleSubmit}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Usuario</label>
                            <input 
                                type="text" 
                                value={creds.username}
                                onChange={(e) => setCreds({...creds, username: e.target.value})}
                                style={styles.input}
                                placeholder="Ej: doctor_uja"
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Contraseña</label>
                            <input 
                                type="password" 
                                value={creds.password}
                                onChange={(e) => setCreds({...creds, password: e.target.value})}
                                style={styles.input}
                                placeholder="••••••••"
                            />
                        </div>

                        {errorGenerico && (
                            <div style={{color: '#d32f2f', marginBottom: '15px', fontSize: '14px', background: '#ffebee', padding: '10px', borderRadius: '4px', border: '1px solid #d32f2f', fontWeight: 'bold'}}>
                                ⚠️ {errorGenerico}
                            </div>
                        )}
                        
                        <button 
                            type="submit" 
                            style={styles.button}
                            onMouseOver={(e) => e.target.style.backgroundColor = '#007A33'}
                            onMouseOut={(e) => e.target.style.backgroundColor = '#009640'}
                        >
                            Entrar
                        </button>
                    </form>
                </div>
            </div>

            <footer style={styles.footer}>
                © Trabajo de Fin de Grado en Telemática - Universidad de Jaén
            </footer>
        </div>
    );
};

export default Login;