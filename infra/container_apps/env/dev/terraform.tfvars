env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-ms-user-group"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 0
  max_replicas = 1
  scale_rules  = []
  cpu          = 0.5
  memory       = "1Gi"
}

app_settings = [
  {
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar"
  },
  {
    name  = "APPLICATIONINSIGHTS_INSTRUMENTATION_LOGGING_LEVEL"
    value = "OFF"
  },
  {
    name  = "MS_USER_GROUP_LOG_LEVEL"
    value = "DEBUG"
  },
  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "ms-user-group"
  }
]

secrets_names = {
    "APPLICATIONINSIGHTS_CONNECTION_STRING"           = "appinsights-connection-string"
    "MONGODB_CONNECTION_URI"                          = "mongodb-connection-string"
}
