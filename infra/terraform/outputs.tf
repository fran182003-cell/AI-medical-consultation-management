output "public_ip" {
  description = "IP pública de la instancia."
  value       = aws_eip.app.public_ip
}

output "app_url" {
  description = "URL de la aplicación."
  value       = "http://${aws_eip.app.public_ip}:8081"
}

output "instance_id" {
  description = "ID de la instancia, para conectarse con: aws ssm start-session --target <id>"
  value       = aws_instance.app.id
}
