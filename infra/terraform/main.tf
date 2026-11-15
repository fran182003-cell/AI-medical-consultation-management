# Infraestructura mínima para desplegar el backend (con el frontend integrado) en AWS:
# una instancia EC2 con Docker en la VPC por defecto. Sin SSH: el acceso de
# administración se hace con AWS Systems Manager Session Manager.

data "aws_caller_identity" "current" {}

data "aws_vpc" "default" {
  default = true
}

# Última AMI de Amazon Linux 2023
data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

# ---------- Red ----------

resource "aws_security_group" "app" {
  name        = "${var.project_name}-sg"
  description = "Acceso a la aplicacion solo desde el rango autorizado"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Aplicacion web (Spring Boot + React)"
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
    cidr_blocks = [var.allowed_cidr]
  }

  # Salida limitada a HTTP/HTTPS: paquetes, GitHub, Maven Central, Docker Hub,
  # AWS SSM y el servicio de IA (ngrok)
  egress {
    description = "HTTPS saliente"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "HTTP saliente (repositorios de paquetes)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ---------- Permisos de la instancia ----------

data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "app" {
  name               = "${var.project_name}-ec2-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
}

# Administración por Session Manager (sustituye a SSH)
resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Lectura de las claves criptográficas, limitada al prefijo del proyecto
data "aws_iam_policy_document" "read_keys" {
  statement {
    actions = ["ssm:GetParameter"]
    resources = [
      "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_parameter_prefix}/*"
    ]
  }
}

resource "aws_iam_role_policy" "read_keys" {
  name   = "${var.project_name}-read-keys"
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.read_keys.json
}

resource "aws_iam_instance_profile" "app" {
  name = "${var.project_name}-profile"
  role = aws_iam_role.app.name
}

# ---------- Instancia ----------

resource "aws_instance" "app" {
  # checkov:skip=CKV_AWS_126:La monitorización detallada tiene coste adicional y no es necesaria para el prototipo
  ami                    = data.aws_ssm_parameter.al2023_ami.value
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.app.name
  ebs_optimized          = true

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    aws_region  = var.aws_region
    repo_url    = var.repo_url
    repo_branch = var.repo_branch
    ia_url      = var.ia_url
    ssm_prefix  = var.ssm_parameter_prefix
  })
  user_data_replace_on_change = true

  # IMDSv2 obligatorio
  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  # Disco cifrado: la base de datos H2 se guarda aquí
  root_block_device {
    volume_type = "gp3"
    volume_size = var.root_volume_size
    encrypted   = true
  }

  tags = {
    Name = var.project_name
  }
}

# IP pública fija
resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"
}
