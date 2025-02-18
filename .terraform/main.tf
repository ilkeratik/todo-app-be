# Provider configuration
provider "aws" {
  region = var.aws_region
}

# --------------------------------- VPC and subnets ----------------------------------
resource "aws_vpc" "main" {
  cidr_block           = "${var.vpc_ip_prefix}.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.vpc_prefix}-vpc"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.vpc_prefix}-igw"
  }
}

# Public Subnets
resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = count.index == 0 ? "${var.vpc_ip_prefix}.1.0/24" : "${var.vpc_ip_prefix}.2.0/24"
  availability_zone       = count.index == 0 ? "us-east-1a" : "us-east-1b"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.vpc_prefix}-public-subnet-${count.index + 1}"
  }
}

# Private Subnets
resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = count.index == 0 ? "${var.vpc_ip_prefix}.3.0/24" : "${var.vpc_ip_prefix}.4.0/24"
  availability_zone = count.index == 0 ? "us-east-1a" : "us-east-1b"

  tags = {
    Name = "${var.vpc_prefix}-private-subnet-${count.index + 1}"
  }
}

# EIP for NAT Gateway
resource "aws_eip" "nat" {
  domain = "vpc"

  depends_on = [aws_internet_gateway.main]
}

# NAT Gateway
resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id

  depends_on = [aws_internet_gateway.main]

  tags = {
    Name = "${var.vpc_prefix}-nat-gateway"
  }
}

# Public Route Table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.vpc_prefix}-public-rt"
  }
}

# Private Route Table
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }
  depends_on = [aws_nat_gateway.main]

  tags = {
    Name = "${var.vpc_prefix}-private-rt"
  }
}

# Route Table Associations
resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  count          = 2
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

# --------------------------------- RDS DB ---------------------------------
resource "aws_security_group" "rds" {
  name        = "${var.vpc_prefix}-rds-sg"
  description = "RDS Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.vpc_prefix}-rds-sg"
  }
  depends_on = [aws_security_group.ecs_tasks]
}

# RDS Subnet Group
resource "aws_db_subnet_group" "rds" {
  name       = "${var.vpc_prefix}-rds-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.vpc_prefix}-rds-subnet-group"
  }
}

# RDS Instance
resource "aws_db_instance" "mysql" {
  identifier        = var.db_identifier
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  publicly_accessible = false
  skip_final_snapshot = true
  multi_az            = false
  deletion_protection = false
  tags = {
    Name = "${var.vpc_prefix}-rds"
  }
}

# --------------------------------- Backend APP ---------------------------------
#ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = var.cluster_name
}

# ECS Task SG
resource "aws_security_group" "ecs_tasks" {
  name        = "${var.vpc_prefix}-tasks-sg"
  description = "Allow inbound traffic for ECS"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Allow inbound traffic on container port"
    from_port   = var.app_port_to_open_traffic
    to_port     = var.app_port_to_open_traffic
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }


  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.vpc_prefix}-ecs-sg"
  }
}

# Login to docker hub, if not authenticated the pull rate limit is 100 pulls per 6 hours
resource "aws_secretsmanager_secret" "docker_hub" {
  name                    = "docker-hub-credentials"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "docker_hub" {
  secret_id = aws_secretsmanager_secret.docker_hub.id
  secret_string = jsonencode({
    username = var.dockerhub_username,
    password = var.dockerhub_password
  })
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/${var.task_family}"
  retention_in_days = 1
  tags = {
    Name = "/ecs/${var.task_family}"
  }
}

# ECS Task Definition BE application
resource "aws_ecs_task_definition" "app" {
  family                   = var.task_family
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = var.role_for_tasks
  task_role_arn            = var.role_for_tasks

  container_definitions = jsonencode([
    # Init container for database setup
    {
      name      = "${var.container_name}-init"
      image     = "mysql:8.0"
      essential = false

      command = [
        "sh",
        "-c",
        <<EOF
        until mysql -h${aws_db_instance.mysql.address} -u${var.db_username} -p${var.db_password} ${var.db_name} -e 'SELECT 1'; do 
          echo waiting for database; 
          sleep 5; 
        done;
        echo '${file("${path.module}/init.sql")}' | mysql -h${aws_db_instance.mysql.address} -u${var.db_username} -p${var.db_password} ${var.db_name}
      EOF
      ]

      environment = [
        {
          name  = "MYSQL_PWD"
          value = var.db_password
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/${var.task_family}"
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "init"
        }
      }
    },
    # Main application container
    {
      name      = var.container_name
      image     = var.docker_image
      essential = true

      dependsOn = [
        {
          containerName = "${var.container_name}-init"
          condition     = "SUCCESS"
        }
      ]

      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]
      memory            = 512
      memoryReservation = 256
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:${var.container_port}/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
      environment = [
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:mysql://${aws_db_instance.mysql.endpoint}/${var.db_name}"
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.db_username
        },
        {
          name  = "SPRING_DATASOURCE_PASSWORD"
          value = var.db_password
        },
        {
          name  = "JASYPT_ENCRYPTOR_PASSWORD"
          value = var.jasypt_password
        },
        {
          name  = "COGNITO_CLIENT_ID"
          value = aws_cognito_user_pool_client.app_client.id
        },
        {
          name  = "COGNITO_CLIENT_SECRET"
          value = aws_cognito_user_pool_client.app_client.client_secret
        },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/${var.task_family}"
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
  lifecycle {
    create_before_destroy = true
  }

  depends_on = [aws_db_instance.mysql]
}

# ECS Service BE application
resource "aws_ecs_service" "app" {
  name            = "${var.vpc_prefix}-app-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = var.container_name
    container_port   = var.container_port
  }
  lifecycle {
    create_before_destroy = true
    ignore_changes        = [desired_count]
  }
  depends_on = [
    aws_lb_listener.http,
    aws_ecs_task_definition.app,
    aws_lb_target_group.app
  ]
}

# Target Group BE application
resource "aws_lb_target_group" "app" {
  name        = "${var.vpc_prefix}-tg"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    healthy_threshold   = 2
    unhealthy_threshold = 10
    timeout             = 30
    interval            = 60
    path                = "/actuator/health"
    port                = var.container_port
  }

  tags = {
    Name = "${var.vpc_prefix}-target-group"
  }
}

# --------------------------------- API Gateway ---------------------------------
# Cognito Authorizer
resource "aws_api_gateway_authorizer" "cognito" {
  name          = "cognito-authorizer"
  type          = "COGNITO_USER_POOLS"
  rest_api_id   = aws_api_gateway_rest_api.main.id
  provider_arns = [data.aws_cognito_user_pools.existing.arns[0]]
}
# API Definition
resource "aws_api_gateway_rest_api" "main" {
  name = "Todo Application API Terra"

  body = jsonencode({
    openapi = "3.0.1"
    info = {
      title   = "Todo Application API"
      version = "1.0"
    }
    components = {
      securitySchemes = {
        cognito = {
          type                         = "apiKey"
          name                         = "Authorization"
          in                           = "header"
          x-amazon-apigateway-authtype = "cognito_user_pools"
          x-amazon-apigateway-authorizer = {
            type         = "cognito_user_pools"
            providerARNs = [data.aws_cognito_user_pools.existing.arns[0]]
          }
        }
      }
    }
    paths = {
      "/api/v1/todo" = {
        get = {
          security = [{
            cognito = ["api/api"]
          }]
          responses = {
            "200" = {
              description = "Success"
              headers = {
                "Access-Control-Allow-Origin" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type            = "HTTP_PROXY"
            uri             = "http://${aws_lb.app.dns_name}/api/v1/todo"
            httpMethod      = "GET"
            timeoutInMillis = 29000
          }
        }
        post = {
          security = [{
            cognito = ["api/api"]
          }]
          responses = {
            "200" = {
              description = "Success"
              headers = {
                "Access-Control-Allow-Origin" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type            = "HTTP_PROXY"
            uri             = "http://${aws_lb.app.dns_name}/api/v1/todo"
            httpMethod      = "POST"
            timeoutInMillis = 29000
          }
        }
        options = {
          responses = {
            "200" = {
              description = "CORS support"
              headers = {
                "Access-Control-Allow-Headers"     = { schema = { type = "string" } }
                "Access-Control-Allow-Methods"     = { schema = { type = "string" } }
                "Access-Control-Allow-Origin"      = { schema = { type = "string" } }
                "Access-Control-Allow-Credentials" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type = "MOCK"
            requestTemplates = {
              "application/json" = "{\"statusCode\": 200}"
            }
            responses = {
              default = {
                statusCode = "200"
                responseParameters = {
                  "method.response.header.Access-Control-Allow-Headers"     = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,access-control-allow-origin'"
                  "method.response.header.Access-Control-Allow-Methods"     = "'OPTIONS,GET,POST'"
                  "method.response.header.Access-Control-Allow-Origin"      = "'${var.cors_allowed_origin}'"
                  "method.response.header.Access-Control-Allow-Credentials" = "'true'"
                }
              }
            }
          }
        }
      },
      "/api/v1/todo/{id}" = {
        get = {
          security = [{
            cognito = ["api/api"]
          }]
          parameters = [{
            name     = "id"
            in       = "path"
            required = true
            schema   = { type = "string" }
          }]
          responses = {
            "200" = {
              description = "Success"
              headers = {
                "Access-Control-Allow-Origin" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type            = "HTTP_PROXY"
            uri             = "http://${aws_lb.app.dns_name}/api/v1/todo/{id}"
            httpMethod      = "GET"
            timeoutInMillis = 29000
            requestParameters = {
              "integration.request.path.id" = "method.request.path.id"
            }
          }
        }
        delete = {
          security = [{
            cognito = ["api/api"]
          }]
          parameters = [{
            name     = "id"
            in       = "path"
            required = true
            schema   = { type = "string" }
          }]
          responses = {
            "200" = {
              description = "Success"
              headers = {
                "Access-Control-Allow-Origin" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type            = "HTTP_PROXY"
            uri             = "http://${aws_lb.app.dns_name}/api/v1/todo/{id}"
            httpMethod      = "DELETE"
            timeoutInMillis = 29000
            requestParameters = {
              "integration.request.path.id" = "method.request.path.id"
            }
          }
        }
        patch = {
          security = [{
            cognito = ["api/api"]
          }]
          parameters = [{
            name     = "id"
            in       = "path"
            required = true
            schema   = { type = "string" }
          }]
          responses = {
            "200" = {
              description = "Success"
              headers = {
                "Access-Control-Allow-Origin" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type            = "HTTP_PROXY"
            uri             = "http://${aws_lb.app.dns_name}/api/v1/todo/{id}"
            httpMethod      = "PATCH"
            timeoutInMillis = 29000
            requestParameters = {
              "integration.request.path.id" = "method.request.path.id"
            }
          }
        }
        options = {
          parameters = [{
            name     = "id"
            in       = "path"
            required = true
            schema   = { type = "string" }
          }]
          responses = {
            "200" = {
              description = "CORS support"
              headers = {
                "Access-Control-Allow-Headers"     = { schema = { type = "string" } }
                "Access-Control-Allow-Methods"     = { schema = { type = "string" } }
                "Access-Control-Allow-Origin"      = { schema = { type = "string" } }
                "Access-Control-Allow-Credentials" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type = "MOCK"
            requestTemplates = {
              "application/json" = "{\"statusCode\": 200}"
            }
            responses = {
              default = {
                statusCode = "200"
                responseParameters = {
                  "method.response.header.Access-Control-Allow-Headers"     = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,access-control-allow-origin'"
                  "method.response.header.Access-Control-Allow-Methods"     = "'OPTIONS,GET,DELETE,PATCH'"
                  "method.response.header.Access-Control-Allow-Origin"      = "'${var.cors_allowed_origin}'"
                  "method.response.header.Access-Control-Allow-Credentials" = "'true'"
                }
              }
            }
          }
        }
      },
      "/api/v1/auth/token" = {
        post = {
          responses = {
            "200" = {
              description = "Success"
              headers = {
                "Access-Control-Allow-Origin" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type            = "HTTP_PROXY"
            uri             = "http://${aws_lb.app.dns_name}/api/v1/auth/token"
            httpMethod      = "POST"
            timeoutInMillis = 29000
          }
        }
        options = {
          responses = {
            "200" = {
              description = "CORS support"
              headers = {
                "Access-Control-Allow-Headers"     = { schema = { type = "string" } }
                "Access-Control-Allow-Methods"     = { schema = { type = "string" } }
                "Access-Control-Allow-Origin"      = { schema = { type = "string" } }
                "Access-Control-Allow-Credentials" = { schema = { type = "string" } }
              }
            }
          }
          x-amazon-apigateway-integration = {
            type = "MOCK"
            requestTemplates = {
              "application/json" = "{\"statusCode\": 200}"
            }
            responses = {
              default = {
                statusCode = "200"
                responseParameters = {
                  "method.response.header.Access-Control-Allow-Headers"     = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,access-control-allow-origin'"
                  "method.response.header.Access-Control-Allow-Methods"     = "'OPTIONS,POST'"
                  "method.response.header.Access-Control-Allow-Origin"      = "'${var.cors_allowed_origin}'"
                  "method.response.header.Access-Control-Allow-Credentials" = "'true'"
                }
              }
            }
          }
        }
      }
    }
  })

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

# Create deployment
resource "aws_api_gateway_deployment" "main" {
  rest_api_id = aws_api_gateway_rest_api.main.id

  triggers = {
    redeployment = sha1(jsonencode(aws_api_gateway_rest_api.main.body))
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Create stage
resource "aws_api_gateway_stage" "main" {
  deployment_id = aws_api_gateway_deployment.main.id
  rest_api_id   = aws_api_gateway_rest_api.main.id
  stage_name    = var.apigw_stage_name
  variables = {
    "cognitoAuthorizerArn" = aws_api_gateway_authorizer.cognito.id
  }
}

# --------------------------------- UI APP on EC2 ---------------------------------
# Security Group for EC2 instance
resource "aws_security_group" "web_sg" {
  name        = "web-sg"
  description = "Allow HTTP traffic"
  vpc_id      = aws_vpc.main.id # Replace with your VPC ID
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Allow HTTP from anywhere
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Allow SSH from anywhere (optional)
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# EC2 Instance to host the UI app
resource "aws_instance" "web_server" {
  ami           = "ami-04505e74c0741db8d"
  instance_type = "t2.micro"
  key_name      = "vockey"

  vpc_security_group_ids = [aws_security_group.web_sg.id]
  subnet_id              = aws_subnet.public[0].id

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y
              sudo apt-get install -y nginx git curl

              # Create a swap file - this is needed for small instances
              sudo fallocate -l 1G /swapfile
              sudo chmod 600 /swapfile
              sudo mkswap /swapfile
              sudo swapon /swapfile

              # Install Node.js using NVM (Node Version Manager)
              curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash

              # Source NVM properly before using it in this session
              export NVM_DIR="$HOME/.nvm"
              [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

              nvm install --lts

              # Clone your UI app repository from GitHub (replace with your own repo)
              git clone https://github.com/ilkeratik/todo-app-ui.git /home/ubuntu/ui-app

              # Set environment variables for React app (replace with actual values)
              export REACT_APP_API_URL=${aws_api_gateway_stage.main.invoke_url}
              export REACT_APP_COGNITO_CLIENT_ID=${aws_cognito_user_pool_client.app_client.id}

              sudo chown -R ubuntu:ubuntu /home/ubuntu/ui-app

              cd /home/ubuntu/ui-app || exit 1

              npm install || exit 1
              npm run build || exit 1

              # Remove default Nginx files and copy build files to Nginx directory
              sudo rm -rf /var/www/html/*
              sudo cp -r /home/ubuntu/ui-app/build/* /var/www/html/

              # Update Nginx configuration to handle client-side routing for React Router
              sudo bash -c 'cat > /etc/nginx/sites-available/default <<EOL
              server {
                  listen 80 default_server;
                  listen [::]:80 default_server;

                  root /var/www/html;
                  index index.html;

                  server_name _;

                  location / {
                      try_files \$uri /index.html;
                  }
              }
              EOL'

              # Restart Nginx to apply the new configuration
              sudo systemctl restart nginx

              echo "UI App deployed successfully!"
            EOF

  tags = {
    Name = "UI-App-Server"
  }
}

# Target Group for EC2 Instance
resource "aws_lb_target_group" "ui_tg" {
  name        = "ui-tg"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "instance"

  health_check {
    path                = "/"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
    matcher             = "200-399"
  }

}

# Register the EC2 instance to the Target Group
resource "aws_lb_target_group_attachment" "tg_attachment" {
  target_group_arn = aws_lb_target_group.ui_tg.arn
  target_id        = aws_instance.web_server.id
  port             = "80"
}

# --------------------------------- Cognito Client / ELB Callback Binding ---------------------------------
data "aws_cognito_user_pools" "existing" {
  name = var.cognito_user_pool_name
}

resource "aws_cognito_resource_server" "resource" {
  identifier   = "api"
  name         = "api"
  user_pool_id = data.aws_cognito_user_pools.existing.ids[0]

  scope {
    scope_name        = "api"
    scope_description = "API access"
  }
}

resource "aws_cognito_user_pool_client" "app_client" {
  name                                 = var.cognito_user_pool_client_name
  user_pool_id                         = data.aws_cognito_user_pools.existing.ids[0]
  generate_secret                      = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes                 = var.oauth_scopes
  supported_identity_providers         = ["COGNITO"]
  explicit_auth_flows = [
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_SRP_AUTH"
  ]
  callback_urls = concat(var.callback_urls, [
    "https://${aws_lb.app.dns_name}/auth/callback",
    "https://${aws_lb.app.dns_name}/login-callback",
    "${var.cors_allowed_origin}/login-callback"
  ])

  refresh_token_validity = 30
  access_token_validity  = 60
  id_token_validity      = 60
  token_validity_units {
    refresh_token = "days"
    access_token  = "minutes"
    id_token      = "minutes"
  }

  prevent_user_existence_errors = "ENABLED"
  enable_token_revocation       = true

  depends_on = [aws_cognito_resource_server.resource]
}

# --------------------------------- ALB Configuration ---------------------------------
# Application Load Balancer
resource "aws_lb" "app" {
  name               = "${var.vpc_prefix}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  tags = {
    Name = "${var.vpc_prefix}-alb"
  }
  lifecycle {
    create_before_destroy = true
  }
}

# ALB Security Group
resource "aws_security_group" "alb" {
  name        = "${var.vpc_prefix}-alb-sg"
  description = "ALB Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.vpc_prefix}-alb-sg"
  }
}

# HTTPS Listener -- For UI
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.app.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = "arn:aws:acm:us-east-1:834198886233:certificate/68c0426f-790f-4186-95fd-3da963a0fd4a"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.ui_tg.arn
  }

  depends_on = [aws_lb_target_group.ui_tg]
}

# HTTP Listener -- For API
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  depends_on = [aws_lb_target_group.app]
}

# --------------------------------- Outputs ---------------------------------

output "apigw_dns_name" {
  description = "The DNS name of the API Gateway"
  value       = aws_api_gateway_stage.main.invoke_url
}

output "alb_dns_name" {
  description = "The DNS name of the load balancer"
  value       = aws_lb.app.dns_name
}

output "target_group_arn" {
  description = "The ARN of the Target Group"
  value       = aws_lb_target_group.app.arn
}

output "rds_endpoint" {
  value     = aws_db_instance.mysql.endpoint
  sensitive = false
}

output "rds_database_name" {
  value     = aws_db_instance.mysql.db_name
  sensitive = false
}

output "rds_username" {
  value     = aws_db_instance.mysql.username
  sensitive = true
}

output "cognito_client_id" {
  value = aws_cognito_user_pool_client.app_client.id
}

locals {
  common_tags = {
    Environment = var.environment
    Project     = var.vpc_prefix
    ManagedBy   = "Terraform"
  }
}


