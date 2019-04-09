storage "postgresql" {
  connection_url = "postgres://postgres:@commondb:5432/inet_vault_1557993817?sslmode=disable"
}

listener "tcp" {
 address     = "0.0.0.0:8200"
 tls_disable = 1
}

disable_mlock = true
ui = true
