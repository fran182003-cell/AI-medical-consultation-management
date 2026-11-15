import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { useAuth } from '../api/Comprobar_rol';

const AdminPanel = () => {
    const [medicos, setMedicos] = useState([]);
    const [nuevoMedico, setNuevoMedico] = useState({ username: '', password: '' });
    
    // --- NUEVO: Solo un mensaje de error genérico ---
    const [errorGenerico, setErrorGenerico] = useState(''); 
    
    const { isAdmin } = useAuth();

    useEffect(() => {
        cargarMedicos();
    }, []);

    const cargarMedicos = async () => {
        try {
            const res = await api.get('/admin/medicos');
            setMedicos(res.data);
        } catch (err) {
            console.error("Error al cargar médicos", err);
        }
    };

    const handleCrearMedico = async (e) => {
        e.preventDefault();
        setErrorGenerico(''); // Limpiamos el error genérico al intentar de nuevo

        try {
            await api.post('/admin/medicos/guardar', nuevoMedico);
            
            alert("Médico añadido correctamente");
            setNuevoMedico({ username: '', password: '' }); 
            setErrorGenerico(''); 
            
            cargarMedicos();
        } catch (err) {
            // --- CAPTURA DE ERROR GENÉRICO (400) ---
            if (err.response && err.response.status === 400) {
                setErrorGenerico("Los datos introducidos no son válidos o el usuario ya existe. Revise el formato.");
            } else {
                setErrorGenerico("Error inesperado al añadir el médico.");
            }
        }
    };

    const handleEliminar = async (id) => {
        if (window.confirm("¿Está seguro de que desea eliminar este facultativo?")) {
            try {
                await api.delete(`/admin/medicos/${id}`);
                cargarMedicos(); 
            } catch (err) {
                alert("Error: No se pudo eliminar el registro.");
            }
        }
    };

    const styles = {
        container: { padding: '20px', fontFamily: 'Arial, sans-serif' },
        header: { color: '#005c27', borderBottom: '2px solid #005c27', paddingBottom: '10px' },
        formCard: { background: '#f9f9f9', padding: '20px', borderRadius: '8px', marginBottom: '30px', border: '1px solid #ddd' },
        // Input estático, ya no cambia de color
        input: { display: 'block', width: '100%', padding: '10px', marginBottom: '15px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box' },
        btnCreate: { backgroundColor: '#005c27', color: 'white', border: 'none', padding: '10px 20px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' },
        table: { width: '100%', borderCollapse: 'collapse', marginTop: '20px', backgroundColor: 'white' },
        th: { backgroundColor: '#005c27', color: 'white', padding: '12px', textAlign: 'left' },
        td: { padding: '12px', borderBottom: '1px solid #ddd', color: '#333' },
        btnDelete: { backgroundColor: '#d32f2f', color: 'white', border: 'none', padding: '6px 12px', borderRadius: '3px', cursor: 'pointer', fontSize: '12px' },
        // Estilo para el error global
        errorBox: { color: '#d32f2f', background: '#ffebee', padding: '10px', marginBottom: '15px', borderRadius: '4px', border: '1px solid #d32f2f', fontSize: '14px', fontWeight: 'bold' }
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.header}>Gestión de Personal Facultativo</h2>

            {isAdmin && (
                <div style={styles.formCard}>
                    <h3>Añadir Nuevo Facultativo</h3>
                    <form onSubmit={handleCrearMedico}>
                        
                        <label style={{fontSize: '14px', fontWeight: 'bold'}}>Nombre de usuario:</label>
                        <input 
                            style={styles.input}
                            type="text" 
                            placeholder="Ej: juan_perez" 
                            value={nuevoMedico.username}
                            onChange={(e) => setNuevoMedico({...nuevoMedico, username: e.target.value})}
                        />

                        <label style={{fontSize: '14px', fontWeight: 'bold'}}>Contraseña:</label>
                        <input 
                            style={styles.input}
                            type="password" 
                            placeholder="Mínimo 4 caracteres" 
                            value={nuevoMedico.password}
                            onChange={(e) => setNuevoMedico({...nuevoMedico, password: e.target.value})}
                        />

                        {/* MENSAJE GENÉRICO DE ERROR AQUÍ */}
                        {errorGenerico && <div style={styles.errorBox}>⚠️ {errorGenerico}</div>}

                        <button type="submit" style={styles.btnCreate}>
                            ➕ Registrar Médico
                        </button>
                    </form>
                </div>
            )}

            <table style={styles.table}>
                <thead>
                    <tr>
                        <th style={styles.th}>ID (UUID)</th>
                        <th style={styles.th}>Nombre de Usuario</th>
                        <th style={styles.th}>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {medicos.map((medico) => (
                        <tr key={medico.id}>
                            <td style={styles.td}>{medico.id}</td>
                            <td style={styles.td}>{medico.username}</td>
                            <td style={styles.td}>
                                {isAdmin ? (
                                    <button style={styles.btnDelete} onClick={() => handleEliminar(medico.id)}>
                                        🗑️ Eliminar
                                    </button>
                                ) : (
                                    <span style={{color: '#999', fontSize: '12px'}}>Sin permisos</span>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminPanel;