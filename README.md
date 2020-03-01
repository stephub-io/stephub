# A declarative solution for BDD tests

The idea of this solution is to compose BDD tests in Gherkin/Cucumber
of pre-built steps which encapsulate the logic for testing typical application
types like browser apps, RESTful interfaces etc.

The
declarative expression of tests and encapsulation in custom high-level steps
frees from writing code usually required for translating the BDD expressions
to instructions and assertions.

The concept of using variables and functions in declarations enables flexibility
in instrumenting and asserting the behaviour of your application.

## Proposal

### Definition of variables
```
variable baseUrl {
    description = "Base URL of the rest services to test"
}
```

### Importing providers with pre-built steps

E.g. a provider for consuming HTTP Restful services:

```
provider "http-rest" {
    version = ">1.0.0"
    baseUrl = ${var.baseUrl}
}
```

### Definition of features
Using pre-built steps from imported providers.

```
feature "login" {
    steps = <<EOF
        Feature: Login feature
        
        Scenario: Positive login
            Given I make a GET request "login" to "/login?user=${var.username}&password=${var.password}"
            Then the response for "login" matches:
            | ${login.status} == 200                 | Expected status OK |
            | ${login.headers['x-session-id']} != '' | Expected session id in the response header |
              
        Scenario: Bad login
            Given I make a GET request "login" to "/login?user=unknown&password=invalid"
            Then the response for "login" matches:
            | ${login.status} == 401 | Expected status unauthorized error |
    EOF
}
```

### Definition of custom high-level steps
```
step "simple-login" {
    expression = "I'm logged-in as {string:username} using password {string:password}"
    definition = <<EOF
        Given I make a GET request "login" to "/login?user=${arg.username}&password=${arg.password}"
        Then the response for "login" matches:
        | ${login.status} == 200 | Expected status OK |        
    EOF
}

feature "login-using-high-level-steps" {
    steps = <<EOF
        Given I'm logged-in as "admin" using password "secret"
    EOF
}
```

### Using functions

```
feature "shopping-cart" {
    steps = <<EOF
        Feature: Shopping cart verification
        
        Scenario: Cart should contain two items
            Given I'm logged-in as "admin" using password "secret"
            When I make a GET request "cart" to "/cart"
            Then the response for "cart" equals:
            | ${login.status}                                  | 200      |        
            | jsonPath(${login.body}, '$.positions.length())'  | 2        |        
            | jsonPath(${login.body}, '$.positions[0].title))' | 'Rocky'  |        
            | jsonPath(${login.body}, '$.positions[1].title))' | 'Wall-E' |        
    EOF
}

```
