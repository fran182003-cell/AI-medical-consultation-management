import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig'; 
import { useLocation } from 'react-router-dom';
import dataTerminos from '../datos_medicos.json';

const NuevaCita = () => {
    const location = useLocation();
    
    // --- ESTADOS DEL FORMULARIO ---
    const [dni, setDni] = useState(location.state?.dni || '');
    
    // === NUEVO: Recuperamos el ID de la reserva enviado desde el calendario ===
    const reservaId = location.state?.reservaId || null;
    // =========================================================================

    const [paciente, setPaciente] = useState(null);
    const [fecha, setFecha] = useState(new Date().toISOString().slice(0, 16));
    
    // CAMPOS DE TEXTO
    const [anamesis, setAnamnesis] = useState('');
    const [tratamiento, setTratamiento] = useState('');
    const [diagnostico, setDiagnostico] = useState('');
    
    // CAMPOS IA, VALORACIÓN Y COMENTARIOS
    const [respuestaIA, setRespuestaIA] = useState('');
    const [valoracion, setValoracion] = useState(0); 
    const [comenmed, setcomenmed] = useState(""); 
    const [cargandoIA, setCargandoIA] = useState(false);

    // --- ESTADO DE ERROR GENERAL (POLÍTICA SILENCIOSA) ---
    const [errorGeneral, setErrorGeneral] = useState('');

    // --- ESTADOS PARA SUGERENCIAS (AUTOCOMPLETADO) ---
    const [sugerenciaAnamnesis, setSugerenciaAnamnesis] = useState('');
    const [sugerenciaDiagnostico, setSugerenciaDiagnostico] = useState('');
    const [sugerenciaTratamiento, setSugerenciaTratamiento] = useState(''); 
    
    // Palabras clave (Tags)
    const [palabrasClave, setPalabrasClave] = useState([]);

    // Historial y Similares
    const [casosSimilares, setCasosSimilares] = useState([]);
    const [mostrarSugerencias, setMostrarSugerencias] = useState(false);
    const [diccionario, setDiccionario] = useState([]);
    const [historial, setHistorial] = useState([]);
    const [mostrarHistorial, setMostrarHistorial] = useState(false);
    
    useEffect(() => {
        const todosLosTerminos = Object.values(dataTerminos).flat();
        const terminosUnicos = [...new Set(todosLosTerminos)];
        setDiccionario(terminosUnicos);
    }, []);

    // Lógica de coincidencia (diccionario)
    const obtenerCoincidencia = (valorCompleto) => {
        const palabras = valorCompleto.split(/[\s\n]+/);
        const ultimaPalabra = palabras[palabras.length - 1];

        if (ultimaPalabra.length >= 3) {
            const matches = diccionario.filter(term => 
                term.toLowerCase().startsWith(ultimaPalabra.toLowerCase())
            );
            if (matches.length > 0) {
                return matches.sort((a, b) => a.length - b.length)[0];
            }
        }
        return null;
    };

    // Manejo de tecla TAB para autocompletar
    const handleKeyDown = (e, setter, value, sugerenciaActual, setterSugerencia) => {
        if (e.key === 'Tab' && sugerenciaActual) {
            e.preventDefault();
            const palabras = value.split(/[\s\n]+/);
            const ultimaPalabra = palabras[palabras.length - 1];
            
            const inicioTexto = value.slice(0, -ultimaPalabra.length);
            const palabraCompleta = inicioTexto + sugerenciaActual;
            
            setter(palabraCompleta);
            
            setPalabrasClave(prev => {
                if(!prev.includes(sugerenciaActual)) return [...prev, sugerenciaActual];
                return prev;
            });

            setterSugerencia(''); 
        }
    };

    const eliminarPalabraClave = (index) => {
        setPalabrasClave(prev => prev.filter((_, i) => i !== index));
    };
    
    useEffect(() => {
        // Si hay un DNI precargado (desde el Dashboard) y aún no hay datos del paciente
        if (dni && !paciente) {
            buscarPaciente();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [dni]);
    
    // --- LLAMADAS API ---

    const buscarPaciente = async () => {
        if (!dni) return;
        try {
            const res = await api.get(`/pacientes/${dni}`);
            setPaciente(res.data);
            setErrorGeneral(''); // Limpiamos error general si encuentra paciente
        } catch (error) {
            setPaciente(null);
            alert("Paciente no encontrado. Verifique el DNI.");
        }
    };

    const buscarCasosSimilares = async () => {
        if (palabrasClave.length === 0) {
            alert("Primero añade términos médicos usando la tecla Tab en los campos de texto");
            return;
        }
        try {
            const res = await api.get('/citas/buscar-similares', { 
                params: { terminos: palabrasClave.join(',') } 
            });
            setCasosSimilares(res.data);
            setMostrarSugerencias(true);
            setMostrarHistorial(false);
        } catch (error) {
            console.error("Error al buscar casos:", error);
            alert("Error al conectar con el servidor.");
        }
    };

    const verHistorial = async () => {
        if (!paciente) return;
        try {
            const res = await api.get(`/citas/historial/${dni}`);
            setHistorial(res.data);
            setMostrarHistorial(true);
            setMostrarSugerencias(false); 
        } catch (error) {
            alert("Error cargando historial");
        }
    };

   const generarRespuestaIA = async () => {
    if (!anamesis && !diagnostico) {
        alert("Por favor, rellena al menos la anamesis o el Diagnóstico.");
        return;
    }

    setCargandoIA(true);
    try {
        const textoPalabrasClave = palabrasClave.length > 0 
            ? palabrasClave.join(', ') 
            : 'Ninguna';
        const promptContexto = `anamesis: ${anamesis}. Diagnóstico: ${diagnostico}. Palabras clave detectadas: ${textoPalabrasClave}`;
        const res = await api.post('/ia/generar-respuesta', { prompt: promptContexto });
        
        console.log("Datos brutos recibidos del backend:", res.data); // luego lo quitaremos

        const textoFinal = res.data && res.data.respuesta ? res.data.respuesta : res.data;
        setRespuestaIA(textoFinal); 
        
    } catch (error) {
        console.error("Error en la petición:", error);
        alert("Error al generar respuesta de IA");
    } finally {
        setCargandoIA(false);
    }
};

    // --- GUARDAR CITA ---
    const guardarCita = async (e) => {
        e.preventDefault();
        setErrorGeneral(''); 

        if (!paciente || !paciente.id) {
            setErrorGeneral("Debe seleccionar un paciente válido antes de guardar.");
            return;
        }

        const datosParaBackend = {
            // === NUEVO: Añadimos el reservaId al JSON que va al backend ===
            reservaId: reservaId,
            // ==============================================================
            fechaHora: fecha + ":00",
            tratamiento: tratamiento,
            diagnostico: diagnostico,
            anamesis: anamesis,
            Respuesta_ia: respuestaIA,
            valoracion: valoracion ? parseInt(valoracion) : 0, 
            comenmed: comenmed, 
            palabrasClave: palabrasClave.join(","),
            paciente: { 
                id: paciente.id 
            }
        };

        try {
            await api.post('/citas', datosParaBackend);
            alert("¡Cita guardada correctamente!");
            
            // Limpieza del formulario
            setPalabrasClave([]); 
            setDiagnostico('');
            setTratamiento('');
            setAnamnesis('');
            setRespuestaIA('');
            setValoracion(0);
            setcomenmed('');
            setErrorGeneral('');
            
        } catch (error) {
            // CAPTURA SILENCIOSA
            if (error.response && error.response.status === 400) {
                setErrorGeneral("Error al guardar cita");
            } else if(error.response && error.response.status === 403) {
                alert("Error 403: No tienes permisos para realizar esta acción.");
            } else {
                setErrorGeneral("Fallo de conexión con el servidor o error inesperado.");
            }
        }
    };

    const renderEstrellas = () => {
        return (
            <div style={{ display: 'flex', gap: '5px', alignItems: 'center', marginTop: '5px' }}>
                <span style={{ fontSize: '0.9em', color: '#555' }}>Valorar respuesta IA:</span>
                {[1, 2, 3, 4, 5].map((star) => (
                    <span
                        key={star}
                        onClick={() => setValoracion(star)}
                        style={{
                            cursor: 'pointer',
                            fontSize: '24px',
                            color: star <= valoracion ? '#ffc107' : '#e4e5e9',
                            transition: 'color 0.2s'
                        }}
                    >
                        ★
                    </span>
                ))}
            </div>
        );
    };

    return (
        <div style={{ display: 'flex', height: '100vh', padding: '20px', gap: '20px', backgroundColor: '#FDFBF7', fontFamily: 'Arial, sans-serif' }}>
            
            {/* PANEL IZQUIERDO: Info Paciente */}
            <div style={{ flex: 1, background: '#FFFFFF', padding: '20px', borderRadius: '8px', borderTop: '4px solid #00853B', boxShadow: '0 2px 5px rgba(0,0,0,0.05)', overflowY: 'auto' }}>
                <h3 style={{ color: '#00853B', borderBottom: '2px solid #E8F5E9', paddingBottom: '10px', marginTop: 0 }}>Información del Paciente</h3>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ fontWeight: 'bold', color: '#333' }}>Buscar DNI:</label>
                    <input 
                        type="text" 
                        value={dni} 
                        onChange={(e) => setDni(e.target.value)} 
                        onBlur={buscarPaciente}
                        placeholder="Escriba DNI y pulse Tab"
                        style={{ width: '100%', padding: '10px', marginTop: '5px', border: '1px solid #CCC', borderRadius: '4px' }}
                    />
                    <button onClick={buscarPaciente} style={{ marginTop: '10px', width: '100%', padding: '10px', backgroundColor: '#F0F0F0', color: '#333', border: '1px solid #CCC', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                        🔍 Buscar Paciente
                    </button>
                </div>
                {paciente ? (
                    <div style={{ background: '#F9F9F9', padding: '15px', borderRadius: '6px', borderLeft: '4px solid #00853B' }}>
                        <p style={{ margin: '0 0 5px 0', fontSize: '1.2em', color: '#00853B' }}><strong>{paciente.nombre}</strong></p>
                        <p style={{ margin: '0 0 15px 0', color: '#555', fontSize: '0.95em' }}><strong>DNI:</strong> {paciente.dni}</p>
                        
                        {/* SECCIÓN: DATOS BIOMÉTRICOS */}
                        <div style={{ background: 'white', padding: '15px', borderRadius: '4px', border: '1px solid #E8F5E9', boxShadow: '0 1px 3px rgba(0,0,0,0.02)' }}>
                            <h4 style={{ marginTop: 0, marginBottom: '12px', color: '#005928', borderBottom: '1px solid #EEE', paddingBottom: '5px', fontSize: '0.95em', textTransform: 'uppercase' }}>
                                Datos Biométricos
                            </h4>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '0.9em', color: '#444' }}>
                                <div>
                                    <strong style={{ color: '#666' }}>Edad:</strong><br/>
                                    {paciente.edad ? `${paciente.edad} años` : '-'}
                                </div>
                                <div>
                                    <strong style={{ color: '#666' }}>Sexo:</strong><br/>
                                    {paciente.sexo ? paciente.sexo : '-'}
                                </div>
                                <div>
                                    <strong style={{ color: '#666' }}>Altura:</strong><br/>
                                    {paciente.altura ? `${paciente.altura} m` : '-'}
                                </div>
                                <div>
                                    <strong style={{ color: '#666' }}>Peso:</strong><br/>
                                    {paciente.peso ? `${paciente.peso} kg` : '-'}
                                </div>
                            </div>
                        </div>
                    </div>
                ) : (
                    <p style={{ color: '#888', fontStyle: 'italic', textAlign: 'center', marginTop: '30px', padding: '20px', background: '#F9F9F9', borderRadius: '4px', border: '1px dashed #CCC' }}>
                        Introduzca un DNI para cargar la ficha del paciente.
                    </p>
                )}
            </div>

            {/* PANEL DERECHO: Formulario de Cita */}
            <div style={{ flex: 2, padding: '25px', background: '#FFFFFF', borderRadius: '8px', borderTop: '4px solid #00853B', boxShadow: '0 2px 5px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', overflowY: 'auto' }}>
                <h2 style={{ color: '#00853B', marginTop: 0, marginBottom: '20px' }}>Nueva Entrada Clínica</h2>
                <form onSubmit={guardarCita} style={{ flex: 1 }}>
                    
                    {/* FECHA */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ fontWeight: 'bold', color: '#444' }}>Fecha y Hora:</label><br/>
                        <input 
                            type="datetime-local" 
                            value={fecha} 
                            onChange={(e) => setFecha(e.target.value)}
                            required 
                            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #CCC', marginTop: '5px' }}
                        />
                    </div>

                    {/* ANAMNESIS */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ fontWeight: 'bold', color: '#444' }}>Anamnesis (Motivo de consulta y síntomas):</label>
                        <div style={{ position: 'relative', width: '100%', minHeight: '100px', marginTop: '5px' }}>
                            <div style={styles.ghostLayer}>
                                <span style={{ color: 'transparent' }}>{anamesis}</span>
                                {sugerenciaAnamnesis.toLowerCase().startsWith(anamesis.split(/[\s\n]+/).pop().toLowerCase()) 
                                    ? sugerenciaAnamnesis.slice(anamesis.split(/[\s\n]+/).pop().length) 
                                    : ''}
                            </div>
                            <textarea 
                                value={anamesis} 
                                onChange={(e) => {
                                    const val = e.target.value;
                                    setAnamnesis(val);
                                    const coincidencia = obtenerCoincidencia(val);
                                    setSugerenciaAnamnesis(coincidencia || '');
                                }}
                                onKeyDown={(e) => handleKeyDown(e, setAnamnesis, anamesis, sugerenciaAnamnesis, setSugerenciaAnamnesis)}
                                rows="3" 
                                style={styles.textArea}
                                placeholder="Describa los síntomas del paciente..."
                            />
                        </div>
                    </div>

                    {/* DIAGNÓSTICO Y TRATAMIENTO */}
                    <div style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
                        <div style={{ flex: 1 }}>
                            <label style={{ fontWeight: 'bold', color: '#444' }}>Diagnóstico:</label>
                            <div style={{ position: 'relative', minHeight: '120px', marginTop: '5px' }}>
                                <div style={styles.ghostLayer}>
                                    <span style={{ color: 'transparent' }}>{diagnostico}</span>
                                    {sugerenciaDiagnostico.toLowerCase().startsWith(diagnostico.split(/[\s\n]+/).pop().toLowerCase()) 
                                        ? sugerenciaDiagnostico.slice(diagnostico.split(/[\s\n]+/).pop().length) 
                                        : ''}
                                </div>
                                <textarea 
                                    value={diagnostico} 
                                    onChange={(e) => {
                                        const val = e.target.value;
                                        setDiagnostico(val);
                                        const coincidencia = obtenerCoincidencia(val);
                                        setSugerenciaDiagnostico(coincidencia || '');
                                    }}
                                    onKeyDown={(e) => handleKeyDown(e, setDiagnostico, diagnostico, sugerenciaDiagnostico, setSugerenciaDiagnostico)}
                                    rows="6" 
                                    style={styles.textArea}
                                    required
                                />
                            </div>
                        </div>

                        <div style={{ flex: 1 }}>
                            <label style={{ fontWeight: 'bold', color: '#444' }}>Tratamiento:</label>
                            <div style={{ position: 'relative', minHeight: '120px', marginTop: '5px' }}>
                                <div style={styles.ghostLayer}>
                                    <span style={{ color: 'transparent' }}>{tratamiento}</span>
                                    {sugerenciaTratamiento.toLowerCase().startsWith(tratamiento.split(/[\s\n]+/).pop().toLowerCase()) 
                                        ? sugerenciaTratamiento.slice(tratamiento.split(/[\s\n]+/).pop().length) 
                                        : ''}
                                </div>
                                <textarea 
                                    value={tratamiento} 
                                    onChange={(e) => {
                                        const val = e.target.value;
                                        setTratamiento(val);
                                        const coincidencia = obtenerCoincidencia(val);
                                        setSugerenciaTratamiento(coincidencia || '');
                                    }}
                                    onKeyDown={(e) => handleKeyDown(e, setTratamiento, tratamiento, sugerenciaTratamiento, setSugerenciaTratamiento)}
                                    rows="6" 
                                    style={styles.textArea}
                                />
                            </div>
                        </div>
                    </div>

                    {/* ZONA DE TAGS */}
                    <div style={{ marginBottom: '20px', padding: '15px', background: '#F4F6F4', borderRadius: '5px', border: '1px solid #E8F5E9' }}>
                        <label style={{ fontSize: '0.9em', color: '#555', fontWeight: 'bold' }}>Términos médicos detectados:</label>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '10px' }}>
                            {palabrasClave.map((palabra, index) => (
                                <div key={index} style={{ background: '#E8F5E9', color: '#1B5E20', padding: '6px 12px', borderRadius: '20px', display: 'flex', alignItems: 'center', fontSize: '14px', border: '1px solid #A5D6A7' }}>
                                    {palabra}
                                    <button type="button" onClick={() => eliminarPalabraClave(index)} style={{ marginLeft: '8px', background: 'transparent', border: 'none', cursor: 'pointer', color: '#C62828', fontWeight: 'bold' }}>&times;</button>
                                </div>
                            ))}
                            {palabrasClave.length === 0 && <span style={{fontSize:'0.85em', color:'#888'}}>Use la tecla Tab al escribir para añadir términos automáticamente.</span>}
                        </div>
                    </div>

                    {/* SECCIÓN ASISTENTE IA Y COMENTARIO MÉDICO */}
                    <div style={{ marginBottom: '25px', padding: '20px', background: '#F1F8E9', borderRadius: '8px', border: '1px solid #AED581' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                            <label style={{ fontWeight: 'bold', color: '#33691E', fontSize: '1.1em' }}>🤖 Asistente IA (Segunda Opinión)</label>
                            <button 
                                type="button" 
                                onClick={generarRespuestaIA}
                                disabled={cargandoIA}
                                style={{ background: cargandoIA ? '#C5E1A5' : '#00853B', color: 'white', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', transition: 'background 0.3s' }}
                            >
                                {cargandoIA ? 'Generando consulta...' : '✨ Consultar IA'}
                            </button>
                        </div>
                        
                        <textarea
                            value={respuestaIA}
                            readOnly
                            rows="4"
                            placeholder="La evaluación clínica de la IA aparecerá aquí tras generarla..."
                            style={{ width: '100%', padding: '12px', borderRadius: '4px', border: '1px solid #C5E1A5', marginBottom: '10px', background: '#FFFFFF', color: '#333', resize: 'vertical' }}
                        />
                        
                        {respuestaIA && (
                            <div style={{ background: '#FAFAFA', padding: '15px', borderRadius: '6px', border: '1px solid #E0E0E0' }}>
                                {renderEstrellas()}
                                <div style={{ marginTop: '15px' }}>
                                    <label style={{ fontSize: '0.9em', color: '#444', fontWeight: 'bold' }}>Observaciones médicas sobre el dictamen de la IA:</label>
                                    <textarea
                                        value={comenmed}
                                        onChange={(e) => setcomenmed(e.target.value)}
                                        rows="2"
                                        placeholder="Escriba aquí sus anotaciones sobre la fiabilidad o utilidad de la respuesta..."
                                        style={{ width: '100%', padding: '10px', marginTop: '8px', borderRadius: '4px', border: '1px solid #CCC', resize: 'vertical' }}
                                    />
                                </div>
                            </div>
                        )}
                    </div>

                    {/* ALERTA DE ERROR GENERAL DE VALIDACIÓN */}
                    {errorGeneral && (
                        <div style={{color: '#d32f2f', background: '#ffebee', padding: '10px', marginBottom: '15px', borderRadius: '4px', border: '1px solid #d32f2f', fontSize: '14px', fontWeight: 'bold', textAlign: 'center'}}>
                            ⚠️ {errorGeneral}
                        </div>
                    )}

                    {/* BOTONES DE ACCIÓN */}
                    <div style={{ display: 'flex', gap: '15px', paddingTop: '10px', borderTop: '1px solid #E0E0E0' }}>
                        <button type="submit" style={{ padding: '12px 24px', background: '#00853B', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '16px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>💾 Guardar Cita</button>
                        <button type="button" onClick={verHistorial} style={{ padding: '12px 24px', background: '#F4F6F4', color: '#00853B', border: '1px solid #00853B', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>📋 Ver Histórico</button>
                        <button type="button" onClick={buscarCasosSimilares} style={{ padding: '12px 24px', background: '#005928', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>💡 Buscar Casos Similares</button>
                    </div>
                </form>

                {/* SUGERENCIAS (CASOS SIMILARES) */}
                {mostrarSugerencias && (
                    <div style={{ marginTop: '25px', borderTop: '3px solid #005928', paddingTop: '15px', background: '#F9F9F9', padding: '15px', borderRadius: '0 0 8px 8px' }}>
                        <h4 style={{ color: '#005928', marginTop: 0 }}>🔍 Casos Clínicos Similares Encontrados</h4>
                        <ul style={{ maxHeight: '200px', overflowY: 'auto', listStyle: 'none', padding: 0 }}>
                            {casosSimilares.map((c, index) => (
                                <li key={index} style={{ marginBottom: '10px', padding: '12px', background: '#FFFFFF', borderRadius: '4px', borderLeft: '4px solid #00853B', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                                    <div style={{ marginBottom: '5px' }}><strong>Diagnóstico:</strong> {c.diagnostico}</div>
                                    <div><strong>Tratamiento:</strong> {c.tratamiento}</div>
                                </li>
                            ))}
                            {casosSimilares.length === 0 && <p style={{ color: '#666' }}>No se encontraron coincidencias previas.</p>}
                        </ul>
                        <button onClick={() => setMostrarSugerencias(false)} style={{ padding: '8px 16px', background: '#CCC', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Cerrar panel</button>
                    </div>
                )}

                {/* HISTORIAL */}
                {mostrarHistorial && (
                    <div style={{ marginTop: '25px', borderTop: '3px solid #CCC', paddingTop: '15px', background: '#F9F9F9', padding: '15px', borderRadius: '0 0 8px 8px' }}>
                        <h4 style={{ color: '#444', marginTop: 0 }}>📋 Historial Clínico del Paciente</h4>
                        <ul style={{ maxHeight: '200px', overflowY: 'auto', listStyle: 'none', padding: 0 }}>
                            {historial.map(c => (
                                <li key={c.id} style={{ marginBottom: '15px', padding: '12px', background: '#FFFFFF', borderRadius: '4px', border: '1px solid #EEE' }}>
                                    <div style={{ fontSize: '0.85em', color: '#888', marginBottom: '5px' }}>📅 {c.fechaHora}</div>
                                    <div><strong>Diagnóstico:</strong> {c.diagnostico}</div>
                                </li>
                            ))}
                            {historial.length === 0 && <p style={{ color: '#666' }}>El paciente no tiene citas anteriores.</p>}
                        </ul>
                        <button onClick={() => setMostrarHistorial(false)} style={{ padding: '8px 16px', background: '#CCC', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Cerrar panel</button>
                    </div>
                )}
            </div>
        </div>
    );
};

// ESTILOS DE AUTOCOMPLETADO
const styles = {
    ghostLayer: {
        position: 'absolute',
        top: 0, left: 0, right: 0, bottom: 0,
        padding: '8px', 
        border: '1px solid transparent', 
        color: '#A0A0A0', 
        whiteSpace: 'pre-wrap',
        wordWrap: 'break-word',
        pointerEvents: 'none',
        fontFamily: 'Arial, sans-serif', 
        fontSize: '14px',
        lineHeight: '1.5',
        overflow: 'hidden',
        zIndex: 1
    },
    textArea: {
        width: '100%',
        padding: '8px',
        background: 'transparent',
        position: 'relative',
        zIndex: 2,
        fontFamily: 'Arial, sans-serif', 
        fontSize: '14px',
        lineHeight: '1.5',
        border: '1px solid #CCC',
        borderRadius: '4px',
        boxSizing: 'border-box'
    }
};

export default NuevaCita;