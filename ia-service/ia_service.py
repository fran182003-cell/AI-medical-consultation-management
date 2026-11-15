# ==========================================
# 1. INSTALACIÓN DE LIBRERÍAS NECESARIAS
# ==========================================
# En Google Colab, ejecutar antes en una celda:
#   !pip install flask pyngrok flask-cors nest_asyncio

import os
import threading
from flask import Flask, request, jsonify
from flask_cors import CORS
from pyngrok import ngrok
import transformers
import torch
from huggingface_hub import login


def obtener_secreto(nombre):
    """Lee un secreto de los 'Secrets' de Google Colab o, si no, de una variable de entorno."""
    try:
        from google.colab import userdata
        valor = userdata.get(nombre)
        if valor:
            return valor
    except Exception:
        pass
    valor = os.environ.get(nombre)
    if not valor:
        raise RuntimeError(
            f"Falta el secreto {nombre}. Añádelo en Colab (icono de la llave) "
            f"o como variable de entorno."
        )
    return valor


# ==========================================
# 2. CONFIGURACIÓN DEL MODELO (SE CARGA UNA SOLA VEZ)
# ==========================================

# El token de Hugging Face NO se escribe en el código: se lee de los secretos
HF_TOKEN = obtener_secreto("HF_TOKEN")
login(token=HF_TOKEN)

model_id = "ContactDoctor/Bio-Medical-Llama-3-2-1B-CoT-012025"

print("Cargando modelo... esto puede tardar unos minutos...")

# Cargamos el pipeline GLOBALMENTE para no recargarlo en cada petición
pipeline = transformers.pipeline(
    "text-generation",
    model=model_id,
    model_kwargs={"torch_dtype": torch.bfloat16},
    device_map="auto",
)

terminators = [
    pipeline.tokenizer.eos_token_id,
    pipeline.tokenizer.convert_tokens_to_ids("<|eot_id|>")
]

print("✅ Modelo cargado y listo para recibir peticiones.")

# ==========================================
# 3. CREACIÓN DEL SERVIDOR FLASK
# ==========================================

app = Flask(__name__)
CORS(app) # Permite peticiones cruzadas si fuera necesario

@app.route('/ia/generar-respuesta', methods=['POST'])
def generar_respuesta():
    try:
        # 1. Recibir datos del Java Backend
        data = request.json
        input_usuario = data.get('prompt', '')

        print(f"📩 Recibida consulta: {input_usuario[:50]}...")

        # 2. Construir el Prompt con tu estructura CoT
        messages = [
            {
                "role": "system",
                "content": (
                    "Analiza este caso pensando en inglés para mayor precisión clínica, pero dame el resultado traducido al español médico formal con 300/400 palabras. "
                    "Eres un asistente médico experto diseñado para apoyar el diagnóstico clínico. "
                    "Utiliza una metodología de Cadena de Pensamiento (Chain of Thought): "
                    "1. Analiza primero los síntomas clave. "
                    "2. Concluye si se puede tratar de un caso de cancer o no. "
                    "IMPORTANTE: Sé conciso y profesional."
                )
            },
            {
                "role": "user",
                "content": f"""
                Analiza el siguiente caso clínico y proporciona un diagnóstico diferencial:

                {input_usuario}

                Piensa paso a paso antes de dar la conclusión final.
                """
            }
        ]

        # 3. Preparar el prompt para el modelo
        prompt_formateado = pipeline.tokenizer.apply_chat_template(
            messages,
            tokenize=False,
            add_generation_prompt=True
        )

        # 4. Ejecutar la inferencia (Generar texto)
        outputs = pipeline(
            prompt_formateado,
            max_new_tokens=1200,
            eos_token_id=terminators,
            do_sample=True,
            temperature=0.1,
            top_p=0.9,
        )

        # 5. Limpiar la salida (Quitar el prompt original)
        texto_generado = outputs[0]["generated_text"][len(prompt_formateado):]

        # Opcional: Limpiar marcas de sistema si quedan
        texto_limpio = texto_generado.strip()

        print("📤 Respuesta enviada.")

        # 6. Devolver JSON a Java
        return jsonify({"respuesta": texto_limpio})

    except Exception as e:
        print(f"❌ Error: {e}")
        return jsonify({"error": str(e)}), 500

# ==========================================
# 4. ABRIR EL TÚNEL NGROK Y EJECUTAR
# ==========================================

# Authtoken de Ngrok, leído de los secretos (NGROK_AUTHTOKEN)
# Se obtiene gratis en https://dashboard.ngrok.com/get-started/your-authtoken
ngrok.set_auth_token(obtener_secreto("NGROK_AUTHTOKEN"))

# Cerrar túneles previos si existen
ngrok.kill()

# Abrir túnel en el puerto 5000
public_url = ngrok.connect(5000).public_url

print("\n" + "="*50)
print(f"🚀 SERVIDOR ACTIVO. COPIA ESTA URL EN TU SPRING BOOT:")
print(f"{public_url}/ia/generar-respuesta")
print("="*50 + "\n")

app.run(port=5000)