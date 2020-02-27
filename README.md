# A descriptive solution for Cucumber tests

Proposal:
```
variable baseUrl {
    description = "Base URL of the rest services to test"
}

provider "http-rest" {
    version = ">1.0.0"
    baseUrl = ${var.baseUrl}
}

step "login" {
    expression = "I'm logged-in as {string:username}"
    definition = <<EOF
        Given I make a GET request "login" to "/login?user=${var.username}&password=xxx"
        Then the response for "login" matches:
        | jsonPath(${body}, '$.status') == 'OK' |
        | before(isoDateTime(jsonPath(${body}, '$.timestamp')), now()) |
        
    EOF
}

feature "abc" {
    steps = <<EOF
        Given I'm logged-in as "admin"
    
    EOF
}
```

dssd
fdfd