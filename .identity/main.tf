terraform {
  required_version = ">= 1.6.0"

  backend "local" {
    
  }
}

provider "azurerm" {
  features {}
}

module "repo" {
  source = "github.com/pagopa/selfcare-commons//terraform/azure_github_federation?ref=EC-162-modulo-identity-repo-ms"

  github = {
    repository = "selfcare-ms-user-group"
  }
}