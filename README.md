# A declarative solution for BDD tests

The idea of this solution is to compose BDD tests in Gherkin/Cucumber
of pre-built steps for instrumenting and asserting typical application
types like browser apps, RESTful services etc.

The
declarative expression using control flow steps, variables, functions
and the encapsulation of tests in custom high-level steps
frees from writing code usually required for translating the BDD expressions
to instructions and assertions.

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
            | ${cart.status}                                  | 200      |        
            | jsonPath(${cart.body}, '$.positions.length()')  | 2        |        
            | jsonPath(${cart.body}, '$.positions[0].title')  | 'Rocky'  |        
            | jsonPath(${cart.body}, '$.positions[1].title')  | 'Wall-E' |        
    EOF
}

```

### Control flow: Iteration

```
after "delete-cart-items" {
    steps = <<EOF
        Given I make a GET request "cart" to "/cart"
        And I store the values from response "cart" to:
        | positions | jsonPath(${cart.body}, '$.positions') |
        And I start iteration for "position" in "${positions}"
            And I make a DELETE request to "/cart/${position.id}"
        Then I end iteration for "position"
    EOF
}

```

### Control flow: Conditions

```
after "delete-cart-items" {
    steps = <<EOF
        Given I make a GET request "cart" to "/cart"
        For "position" in "jsonPath(${cart.body}, '$.positions')"
            If "${position.type} == 'DVD'"
                Then I make a DELETE request to "/cart/dvd/${position.id}"
            ElseIf "${position.type} == 'CD'"
                Then I make a DELETE request to "/cart/cd/${position.id}"
            Else
                Then I make a DELETE request to "/cart/cd/${position.id}"
            End
        End
    EOF
}

```
