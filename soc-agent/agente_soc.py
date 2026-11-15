from langgraph.checkpoint.memory import MemorySaver

import os
import time
import json
import requests
from langchain_core.tools import tool
from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.prebuilt import create_react_agent
from O365 import Account
from langchain_core.messages import SystemMessage, HumanMessage
# ==========================================
# 1. CONFIGURACIÓN Y CREDENCIALES
# ==========================================
# ==========================================
# 1. CONFIGURACIÓN Y CREDENCIALES
# ==========================================

# Diccionario para almacenar la configuración
config = {}

# Leer el archivo rutas.txt
# Asegúrate de que "rutas.txt" está en la misma carpeta que este script, 
# si no, pon la ruta completa (ej: "C:\\ruta\\al\\archivo\\rutas.txt")
try:
    with open("Rutas.txt", "r", encoding="utf-8") as f:
        for linea in f:
            # Ignorar líneas vacías o comentarios
            if "=" in linea and not linea.strip().startswith("#"):
                # Separar solo por el primer '='
                clave, valor = linea.strip().split("=", 1)
                config[clave] = valor
except FileNotFoundError:
    print(" ERROR: No se encontró el archivo 'Rutas.txt'. Cópialo a partir de 'Rutas.example.txt'.")
    exit()

# Asignar las variables leídas desde el diccionario
os.environ["GOOGLE_API_KEY"] = config.get("GOOGLE_API_KEY", "")

# Extraer las credenciales de O365
client_id = config.get("O365_CLIENT_ID", "")
client_secret = config.get("O365_CLIENT_SECRET", "")
credenciales = (client_id, client_secret)

# Rutas de los archivos
RUTA_LOGS = config.get("RUTA_LOGS", "")
RUTA_JSON = config.get("RUTA_JSON", "")

# Destinatario de las alertas críticas
EMAIL_ALERTAS = config.get("EMAIL_ALERTAS", "")

cuenta = Account(credenciales, tenant_id='common')

# ==========================================
# 2. HERRAMIENTAS DEL AGENTE
# ==========================================

@tool
def enviar_correo(destinatario: str, asunto: str, mensaje: str) -> str:
    """Útil para enviar un correo electrónico de alerta."""
    print(f"\n[ EJECUTANDO HERRAMIENTA] Enviando correo real a {destinatario}...")
    try:
        m = cuenta.new_message()
        m.to.add(destinatario)
        m.subject = f" ALERTA SOC: {asunto}"
        m.body = mensaje
        m.send()
        print(f"[ ÉXITO] Outlook ha confirmado el envío del correo.")
        return f"Alerta enviada exitosamente a {destinatario}."
    except Exception as e:
        print(f"\n[ ERROR DE OUTLOOK] Ocurrió este fallo técnico: {e}")
        return f"Error al enviar la alerta: {e}"

@tool
def registrar_evento_siem(json_data: str) -> str:
    """Útil para guardar el log estructurado en el archivo eventos_siem.json. 
    DEBES pasarle un string con formato JSON válido."""
    try:
        datos = []
        # Si el archivo ya existe, cargamos los datos anteriores
        if os.path.exists(RUTA_JSON):
            with open(RUTA_JSON, "r", encoding="utf-8") as f:
                try:
                    datos = json.load(f)
                except json.JSONDecodeError:
                    datos = []
        
        # Convertimos el string que nos da el LLM a diccionario
        nuevo_evento = json.loads(json_data)
        datos.append(nuevo_evento)
        
        # Guardamos todo de nuevo
        with open(RUTA_JSON, "w", encoding="utf-8") as f:
            json.dump(datos, f, indent=4)
            
        return "Evento guardado correctamente en el SIEM (JSON)."
    except Exception as e:
        return f"Error al guardar el JSON: {e}"

herramientas = [enviar_correo, registrar_evento_siem]

# ==========================================
# 3. CEREBRO Y AGENTE
# ==========================================
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash", temperature=0)
# Creamos la memoria RAM del agente
memoria_soc = MemorySaver()
instrucciones_soc = f"""
Eres un Analista de Ciberseguridad Autónomo (SOC).

REGLA DE ORO: No puedes simplemente escribir "he enviado el correo" o "he guardado el log". ¡DEBES ejecutar las herramientas (tools) disponibles para que la acción sea real! Si no invocas la herramienta, el sistema fallará.

POR CADA LOG QUE RECIBAS:
1. Analiza el evento.
2. EJECUTA LA HERRAMIENTA 'registrar_evento_siem' pasándole el JSON válido.
3. SI detectas ataques recurrentes (ej. se repite 2 veces el mismo fallo seguido en los logs), la severidad es CRITICAL. 
4. Si es CRITICAL, EJECUTA OBLIGATORIAMENTE LA HERRAMIENTA 'enviar_correo' (destinatario: {EMAIL_ALERTAS}, asunto: 'Critico', mensaje: 'error critico detectado').

No simules las acciones. Ejecuta las herramientas.
"""
agente = create_react_agent(
    llm, 
    tools=herramientas, 
    checkpointer=memoria_soc, 
  
)




# ==========================================
# 4. BUCLE DE MONITORIZACIÓN EN TIEMPO REAL
# ==========================================

print("==================================================")
print(" Iniciando Agente SOC ...")
print(f" Monitorizando: {RUTA_LOGS}")
print("==================================================\n")

if not cuenta.is_authenticated:
    print(" Primero debes iniciar sesión en Outlook para las alertas por correo.")
    cuenta.authenticate(scopes=['basic', 'message_all'], redirect_uri='http://localhost:8000')

# Nos aseguramos de que el archivo de logs exista antes de leerlo
if not os.path.exists(RUTA_LOGS):
    with open(RUTA_LOGS, "w", encoding="utf-8") as f:
        pass

# Abrimos el archivo en modo lectura
with open(RUTA_LOGS, "r", encoding="utf-8") as archivo_logs:
    # Opcional: Descomenta la siguiente línea si quieres que el agente ignore los logs 
    # antiguos y solo lea los nuevos que se generen a partir de ahora:
    archivo_logs.seek(0, 2)
    
    while True:
        linea = archivo_logs.readline()
        
        if not linea:
            # Si no hay líneas nuevas, el agente "duerme" 2 segundos y vuelve a comprobar
            time.sleep(2)
            continue
            
        linea = linea.strip()
        if linea:
            print(f"\n[!] NUEVO LOG DETECTADO: {linea}")

            configuracion = {"configurable": {"thread_id": "auditoria_continua_1"}}
            mensaje_usuario = f"Analiza este nuevo log y ejecuta tu protocolo: {linea}"
            mensajes_para_agente = [
            SystemMessage(content=instrucciones_soc),
            HumanMessage(content=mensaje_usuario)
            ]
            
            
            try:
                # El agente piensa, guarda el JSON y decide si envía el correo
                print(" Agente analizando...")
                
                # Invocamos solo con el mensaje del usuario. El agente ya sabe quién es.
                resultado = agente.invoke({"messages": mensajes_para_agente}, config=configuracion)
                respuesta_agente = resultado["messages"][-1]
                
                print(f" Resolución del Agente: {respuesta_agente.content}")
                print("-" * 50)
                
            except Exception as e:
                print(f"\n Error del agente al procesar el log: {e}\n")