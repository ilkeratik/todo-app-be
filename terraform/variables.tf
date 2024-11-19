variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "vpc_prefix" {
  description = "vpc name prefix"
  type        = string
  default     = "es-project"
}

variable "vpc_ip_prefix" {
  description = "vpc ip cidr prefix"
  type        = string
  default     = "es-project"
}

variable "cluster_name" {
  description = "Cluster name"
  type        = string
  default     = "production"
}
variable "docker_image" {
  description = "Docker image to deploy"
  type        = string
}

variable "role_for_tasks" {
  description = "role_for_tasks"
  type        = string
}

variable "task_family" {
  description = "task_family"
  type        = string
}

variable "service_name" {
  description = "service_name for app"
  type        = string
}

variable "container_name" {
  description = "Container name"
  type        = string
}

variable "app_port_to_open_traffic" {
  description = "the port will be used in security groups to allow traffic"
  type        = number
  default     = 8080
}

variable "container_port" {
  description = "Container port"
  type        = number
  default     = 8080
}

variable "app_count" {
  description = "Number of docker containers to run"
  type        = number
  default     = 1
}

variable "fargate_cpu" {
  description = "Fargate instance CPU units"
  type        = number
  default     = 256
}

variable "fargate_memory" {
  description = "Fargate instance memory"
  type        = number
  default     = 512
}


variable "db_identifier" {
  description = "db_identifier"
  type        = string
}

variable "db_username" {
  description = "db password"
  type        = string
}

variable "db_name" {
  description = "db_name"
  type        = string
}

variable "db_password" {
  description = "db password"
  type        = string
  sensitive   = true
}

variable "jasypt_password" {
  description = "Jasypt encryptor password"
  type        = string
  sensitive   = true
}

variable "apigw_api_id" {
  description = "API apigw_api_id"
  type        = string
}

variable "apigw_stage_name" {
  description = "API apigw_stage_name"
  type        = string
}

variable "dockerhub_username" {
  description = "dockerhub_username"
  type        = string
  default     = "testuser"
}

variable "dockerhub_password" {
  description = "dockerhub_password"
  type        = string
  sensitive   = true
  default     = "xxxxxxxx"
}

variable "cognito_domain" {
  description = "Cognito domain"
  type        = string
  default     = "es-iap3.auth.us-east-1.amazoncognito.com"
}

variable "cognito_user_pool_client_name" {
  description = "Cognito user pool client name"
  type        = string
  default     = "clientname"
}

variable "cognito_user_pool_name" {
  description = "Cognito User Pool name"
  type        = string
}

variable "callback_urls" {
  description = "List of allowed callback URLs"
  type        = list(string)
}


variable "cors_allowed_origin" {
  description = "Allowed origin for CORS"
  type        = string
}

variable "oauth_scopes" {
  description = "List of allowed OAuth scopes"
  type        = list(string)
  default = [
    "openid"
  ]
}


variable "github_ui_repository_url" {
  description = "Vercel API token"
  type        = string
  sensitive   = true
}

variable "github_access_token" {
  description = "github_access_token"
  type        = string
  sensitive   = true
}

variable "vercel_api_token" {
  description = "Vercel API token"
  type        = string
  sensitive   = true
}

variable "vercel_project_id" {
  description = "Vercel project id"
  type        = string
  sensitive   = true
}

