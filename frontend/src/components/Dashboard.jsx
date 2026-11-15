import { useNavigate } from 'react-router-dom';
import { useAuth } from '../api/Comprobar_rol';
import logoUJA from '../logo-uja.png';
import React, { useState, useEffect } from 'react';

// === NUEVO: Importamos tu API y las librerías del Calendario ===
import api from '../api/axiosConfig';
import FullCalendar from '@fullcalendar/react';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction'; 
import esLocale from '@fullcalendar/core/locales/es'; 
// ===============================================================

const Dashboard = () => {
    const [dniSearch, setDniSearch] = useState('');
    
    // === NUEVO: Estado para guardar las reservas del calendario ===
    const [eventosAgenda, setEventosAgenda] = useState([]);
    // ===============================================================

    const navigate = useNavigate();
    const medicoUser = localStorage.getItem('medicoUser');
    const { isAdmin } = useAuth(); 

    const basePath = isAdmin ? '/admin' : '';
    
    // === MODIFICADO: Función para cargar la agenda con colores dinámicos ===
    const cargarAgenda = async () => {
        if (!medicoUser || isAdmin) return; // El admin no tiene agenda médica

        try {
            const res = await api.get(`/reservas/agenda?username=${medicoUser}`);
            // Transformamos los datos del backend al formato que entiende FullCalendar
            const eventosMapeados = res.data.map(reserva => ({
                id: reserva.id,
                title: `🩺 Paciente: ${reserva.paciente.nombre}`,
                start: reserva.fechaHora, 
                // Si ya fue atendido, lo pintamos gris. Si no, verde corporativo.
                color: reserva.estado === 'ATENDIDO' ? '#808080' : '#00853B', 
                extendedProps: {
                    dni: reserva.paciente.dni 
                }
            }));
            setEventosAgenda(eventosMapeados);
        } catch (error) {
            console.error("Error al cargar la agenda:", error);
        }
    };
    // ===============================================================

    useEffect(() => {
        // Llamamos a la API al cargar la página
        cargarAgenda();

        const ws = new WebSocket('ws://127.0.0.1:8765');

        ws.onopen = () => {
            console.log('🔌 Conectado al lector de DNIe. Esperando tarjeta...');
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                if (data.dni) {
                    console.log('💳 DNI detectado en el lector:', data.dni);
                    setDniSearch(data.dni);
                    // Magia: Redirige automáticamente
                    navigate(`${basePath}/nueva-cita`, { state: { dni: data.dni } });
                }
            } catch (error) {
                console.error("Error al procesar el DNI:", error);
            }
        };

        ws.onerror = (error) => {
            console.log('Lector DNIe no detectado o puente apagado.');
        };

        return () => ws.close();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [navigate, basePath]);

    // Función que se ejecuta al arrastrar y soltar una cita (Drag & Drop)
    const handleMoverCita = async (info) => {
        const reservaId = info.event.id;
        // Obtenemos la nueva fecha/hora quitando la zona horaria para que cuadre con Spring
        const nuevaFechaHora = info.event.start.toISOString().slice(0, 19); 

        try {
            await api.patch(`/reservas/${reservaId}`, { nuevaFechaHora });
            console.log(`Cita ${reservaId} movida a las ${nuevaFechaHora}`);
            // No hace falta recargar, el calendario ya se ha actualizado visualmente
        } catch (error) {
            alert("Error al actualizar la hora de la cita. Se revertirá el cambio.");
            info.revert(); // Si falla el backend, la cita vuelve a su sitio original en la pantalla
        }
    };

    // === MODIFICADO: Capturamos el reservaId y lo enviamos a NuevaCita2 ===
    const handleClicCita = (info) => {
        const dniPaciente = info.event.extendedProps.dni;
        const reservaId = info.event.id; // Capturamos el ID de la cita del calendario
        
        // Enviamos tanto el DNI como el ID de la reserva por el 'state'
        navigate(`${basePath}/nueva-cita`, { 
            state: { 
                dni: dniPaciente,
                reservaId: reservaId 
            } 
        });
    };
    // ===================================================================================

    const handleSearch = () => {
        navigate(`${basePath}/nueva-cita`, { state: { dni: dniSearch } });
    };

    const handleNuevaCita = () => {
        navigate(`${basePath}/nueva-cita`);
    };

    const handleLogout = () => {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('medicoUser'); 
        window.location.href = '/'; 
    };

    const styles = {
        container: { fontFamily: 'Arial, sans-serif', background: '#f8f9fa', minHeight: '100vh' },
        header: { backgroundColor: '#009640', padding: '10px 30px', color: 'white', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' },
        logoArea: { display: 'flex', alignItems: 'center', gap: '15px' },
        userBar: { background: 'white', padding: '10px 30px', borderBottom: '1px solid #ddd', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
        userInfo: { color: '#333', fontSize: '14px' },
        
        mainContent: { maxWidth: '1200px', margin: '30px auto', padding: '0 20px', display: 'flex', gap: '20px', alignItems: 'flex-start' },
        columnaCalendario: { flex: 2, background: 'white', padding: '20px', borderRadius: '4px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', borderTop: '3px solid #005c27' },
        columnaManual: { flex: 1, background: 'white', padding: '20px', borderRadius: '4px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', borderTop: '3px solid #009640' },
        
        searchBox: { display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '15px' },
        input: { padding: '10px', border: '1px solid #ccc', fontSize: '16px', width: '100%', boxSizing: 'border-box' },
        btnPrimary: { background: '#005c27', color: 'white', border: 'none', padding: '10px 20px', cursor: 'pointer', fontSize: '15px', fontWeight: 'bold' },
        btnSecondary: { background: '#007a33', color: 'white', border: 'none', padding: '10px 20px', cursor: 'pointer', fontSize: '15px' },
        btnLogout: { background: 'transparent', border: '1px solid #d32f2f', color: '#d32f2f', padding: '5px 15px', cursor: 'pointer', fontSize: '13px', borderRadius: '3px', marginLeft: '10px' },
        logo2: { height: '60px', width: 'auto', objectFit: 'contain', marginRight: '20px' },
        btnAdmin: { background: '#f44336', color: 'white', border: 'none', padding: '5px 15px', cursor: 'pointer', fontSize: '13px', borderRadius: '3px', fontWeight: 'bold' }
    };

    return (
        <div style={styles.container}>
            <header style={styles.header}>
                <div style={styles.logoArea}>
                     <img src={logoUJA} alt="Logo UJA" style={styles.logo2} />
                    <span style={{fontSize: '18px', fontWeight: '300', borderLeft: '1px solid rgba(255,255,255,0.3)', paddingLeft: '15px'}}>
                        Estación Clínica
                    </span>
                </div>
            </header>

            <div style={styles.userBar}>
                <div style={styles.userInfo}>
                    <strong>Facultativo:</strong> Dr/a. {medicoUser} | <strong>Centro:</strong> Hospital Universitario
                </div>
                <div>
                    {isAdmin && (
                        <button onClick={() => navigate('/admin')} style={styles.btnAdmin}>
                            ⚙️ PANEL ADMIN
                        </button>
                    )}
                    <button onClick={handleLogout} style={styles.btnLogout}>
                        Desconectar
                    </button>
                </div>
            </div>
                    
            <div style={styles.mainContent}>
                
                {/* PANEL IZQUIERDO (CALENDARIO) */}
                {!isAdmin && (
                    <div style={styles.columnaCalendario}>
                        <h2 style={{ marginTop: 0, color: '#333', borderBottom: '2px solid #eee', paddingBottom: '10px' }}>📅 Agenda del Día</h2>
                        
                        {/* Componente de Calendario de FullCalendar */}
                        <div style={{ marginTop: '20px', height: '500px' }}>
                            <FullCalendar
                                plugins={[ timeGridPlugin, interactionPlugin ]}
                                initialView="timeGridDay"    
                                locale={esLocale}            
                                allDaySlot={false}           
                                slotMinTime="08:00:00"       
                                slotMaxTime="15:00:00"       
                                editable={true}              
                                events={eventosAgenda}       
                                eventDrop={handleMoverCita}  
                                eventClick={handleClicCita}  
                                headerToolbar={false}        
                                height="100%"
                                // El color base se define ahora dinámicamente en cargarAgenda()
                            />
                        </div>
                    </div>
                )}

                {/* PANEL DERECHO (URGENCIAS Y DNIE) */}
                {!isAdmin ? (
                    <div style={styles.columnaManual}>
                        <h3 style={{ marginTop: 0, color: '#333' }}>Admisión sin Cita / Urgencias</h3>
                        <p style={{ color: '#666', fontSize: '14px' }}>
                            Inserte el DNIe en el lector o introduzca el DNI manualmente para crear una entrada clínica instantánea.
                        </p>
                        
                        <div style={styles.searchBox}>
                            <input 
                                type="text" 
                                placeholder="Escribir DNI..." 
                                value={dniSearch} 
                                onChange={(e) => setDniSearch(e.target.value)} 
                                style={styles.input} 
                            />
                            <button onClick={handleSearch} style={styles.btnPrimary}>🔍 Abrir Ficha</button>
                            <hr style={{width: '100%', border: '0', borderTop: '1px solid #eee', margin: '10px 0'}}/>
                            <button onClick={handleNuevaCita} style={styles.btnSecondary}>+ Cita en Blanco</button>
                        </div>
                    </div>
                ) : (
                    <div style={styles.columnaCalendario}>
                        <h2 style={{ marginTop: 0, color: '#333' }}>Panel de Control Administrativo</h2>
                        <p style={{ color: '#666' }}>Bienvenido al sistema de gestión. Como administrador, puede gestionar el personal facultativo desde el botón superior.</p>
                    </div>
                )}

            </div>
        </div>
    );
};

export default Dashboard;