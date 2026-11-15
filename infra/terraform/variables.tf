variable "aws_region" {
  description = "Región de AWS. Se usa una región de la UE para mantener los datos clínicos dentro del espacio del RGPD."
  type        = string
  default     = "eu-west-3"
}

variable "project_name" {
  description = "Prefijo para nombrar los recursos."
  type        = string
  default     = "tfg-medical-platform"
}

variable "instance_type" {
  description = "Tipo de instancia EC2 (t3.medium según el presupuesto de la memoria)."
  type        = string
  default     = "t3.medium"
}

variable "allowed_cidr" {
  description = "Rango de IPs con acceso a la aplicación (por ejemplo, la IP pública del centro, 203.0.113.10/32)."
  type        = string

  validation {
    condition     = can(cidrhost(var.allowed_cidr, 0)) && var.allowed_cidr != "0.0.0.0/0"
    error_message = "Indica un CIDR válido y restringido; no se permite abrir la aplicación a todo Internet."
  }
}

variable "repo_url" {
  description = "Repositorio desde el que la instancia descarga y construye la aplicación."
  type        = string
  default     = "https://github.com/fran182003-cell/AI-medical-consultation-management.git"
}

variable "repo_branch" {
  description = "Rama del repositorio a desplegar."
  type        = string
  default     = "main"
}

variable "ia_url" {
  description = "Endpoint del servicio de IA (URL de ngrok)."
  type        = string
  default     = "https://xxxx.ngrok-free.dev/ia/generar-respuesta"
}

variable "ssm_parameter_prefix" {
  description = "Prefijo de los parámetros de SSM donde se guardan las claves (creados fuera de Terraform)."
  type        = string
  default     = "/tfg-medical"
}

variable "root_volume_size" {
  description = "Tamaño del disco raíz en GB."
  type        = number
  default     = 20
}
