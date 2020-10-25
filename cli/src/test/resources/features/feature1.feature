# Comment of feature
Feature: Abc

  # Comment of scenario
  Scenario: My first scenario
    Given value { "hello": "6" } - assigned to ${my}
    # Comment of first step
    Given assert that ${var.var1} equals ${my.hello}
    # Comment #1 of 2nd step
    # Comment #2 of 2nd step
    When wait for 60 seconds
    | Abc | Def |
    | 134 | ..  |
    Then assert that 2 equals 1
    """
      Hello from doc-string
      Next line
    """