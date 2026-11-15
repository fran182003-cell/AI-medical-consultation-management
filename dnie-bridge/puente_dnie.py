import asyncio
import websockets
import json
import os
import re
from PyKCS11 import PyKCS11, PyKCS11Lib

RUTA_DLL = r"C:\Windows\System32\DNIe_P11_x64.dll"

# --- 1. INICIALIZACIÓN GLOBAL ---
# Cargamos la librería UNA SOLA VEZ para evitar colapsar la memoria de Windows
pkcs11 = PyKCS11Lib()

if os.path.exists(RUTA_DLL):
    try:
        pkcs11.load(RUTA_DLL)
        print(" Librería DNIe cargada en memoria con éxito.")
    except Exception as e:
        print(f" Error al cargar la DLL: {e}")
else:
    print(" ERROR CRÍTICO: No se encuentra la DLL en la ruta especificada.")
    exit(1)

# --- 2. LÓGICA DE LECTURA ---
async def leer_dni_con_dll():
    try:
        # 1. ¿Detecta el lector físico?
        slots = pkcs11.getSlotList(tokenPresent=True)
        if not slots:
            # Si entras aquí, es que el lector está vacío o el PC no ve la tarjeta
            return None

        print(f"---  Tarjeta detectada en slot {slots[0]} ---")
        slot = slots[0]
        session = pkcs11.openSession(slot)

        objetos = session.findObjects([(PyKCS11.CKA_CLASS, PyKCS11.CKO_CERTIFICATE)])

        if not objetos:
            print(" No se encuentran certificados. Probando método alternativo...")
            # Método B: Leer la etiqueta del token directamente
            info = pkcs11.getTokenInfo(slot)
            print(f" Etiqueta del DNI: {info.label}")
            session.closeSession()
            return info.label # A veces aquí viene el nombre

        dni_encontrado = None
        for obj in objetos:
            subject = session.getAttributeValue(obj, [PyKCS11.CKA_SUBJECT])[0]
            texto_datos = "".join(chr(c) for c in subject if 31 < c < 127)

            print(f" Datos en bruto: {texto_datos}") # Para ver qué está leyendo

            match = re.search(r'\d{8}[A-Z]', texto_datos)
            if match:
                dni_encontrado = match.group(0)
                break 

        session.closeSession()
        return dni_encontrado

    except Exception as e:
        print(f" Error leyendo chip: {e}")
        return None
        
    except Exception as e:
        # Si sacan la tarjeta de golpe o pide PIN, capturamos el error silenciosamente
        return None

# --- 3. SERVIDOR WEBSOCKET ---
async def handler(websocket):
    
    print(" ¡CONEXIÓN ESTABLECIDA! React ha saludado a Python.") # <-- Añade esto
    ultimo_enviado = None

    while True:
        dni = await leer_dni_con_dll()
        if dni:
            print(f" Buscando datos en el chip... Encontrado: {dni}")
        try:
            dni = await leer_dni_con_dll()
            
            if dni and dni != ultimo_enviado:
                print(f" DNI LEÍDO DESDE DLL: {dni}")
                await websocket.send(json.dumps({"dni": dni}))
                ultimo_enviado = dni
            elif not dni:
                ultimo_enviado = None
                
            await asyncio.sleep(2) # Pausa de 2 segundos para no saturar el lector
            
        except websockets.exceptions.ConnectionClosed:
            print(" React se ha desconectado.")
            break
        except Exception as e:
            print(f" Error en el bucle del socket: {e}")
            await asyncio.sleep(2)

async def main():
    print("==================================================")
    print(" MODO OFICIAL: PUENTE WEBSOCKET ACTIVADO")
    print(f" DLL: {RUTA_DLL}")
    print("==================================================")
    
    async with websockets.serve(handler, "127.0.0.1", 8765):
        print(" Esperando conexión desde React y la inserción del DNI...")
        await asyncio.Future()  # Mantiene el servidor corriendo infinitamente

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n Servidor apagado manualmente.")