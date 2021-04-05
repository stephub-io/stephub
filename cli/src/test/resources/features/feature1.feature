# Comment of feature
Feature: This is the long feature name in this section

  # Comment of scenario
  Scenario: My first scenario
    Given a value { "hello": "6" } - assigned to ${my}
    And wait for 3 seconds
    # Comment of first step
    Then assert that ${var.var1} equals ${my.hello}
    # Comment #1 of 2nd step
    # Comment #2 of 2nd step
    When wait for 10 seconds
    | Abc | Def |
    | 134 | ..  |
    Then assert that 2 equals 1
    """
      Hello from doc-string
      Next line
    """