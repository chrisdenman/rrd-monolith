# [Refuse Recycling Dates - Monolith](https://github.com/chrisdenman/rrd-monolith)

This is a 'toy' application I have been developing to better learn:
1.  Kotlin.
1.  Functional Programming (particularly Monads).
1.  Clean Architecture.
1.  Migration strategies from: monolith- to μService-, architectures.
1.  OSAScript (JavaScript) to automate iCal on OSX.

For usage information, please see the [docs](https://chrisdenman.github.io/rrd-monolith/dokka/html/rrd-monolith/index.html).


## Function (There's a single Use-Case/Story)

1.  Attempt to acquire the next service collection date (and type) using all available input gateways. If this succeeds then, send a notification to the event producers (that decide to whether to notify or not).


## Design

1.  Clean Architecture. Functional first. All state in gateways. 
1.  Scripting iCal using Apple OSAScript (JavaScript & AppleScript).
1.  Apache POI for integrating with .xlsx the file format.
1.  Arrow-kt library for FP helpers.
1.  Selenium (ChromeDriver) for browser automation.
1.  Everything is configured by system properties.


## Technologies

1.  Written in Kotlin.
1.  Built with Gradle.
1.  Developed in IntelliJ Idea.
1.  Linted with klint.
1.  Tested with JUnit 5, using only functional tests.
1.  CI on Jenkins.
1.  Nexus artifact repository.    
1.  Housed on GitHub.
1.  Apache POI for .xlsx integration.
1.  Java Mail (com.sun.mail) for mail integration.
1.  Functional support from Arrow kt.
1.  Browser automation with Selenium WebDriver.
1.  Apple Calendar automation with OSAScript (JavaScript & AppleScript).
1.  JRE.


## System Requirements

1.  Apple OSX with iCal (Calendar) installed.
1.  JDK 11.
1.  Chrome (version 85).
1.  Apple.
1.  SMTP mailbox (network connectivity).
1.  Chrome 87


## Lessons Learnt

1.  Mocking frameworks can't cope with Kotlin.
1.  Lists are only good for homogenous types.
1.  Constructors don't return wrapped types, it's better to use constructor functions, and return `Either`s if they can fail.
1.  Apple's backwards compatibility support is woeful. 


## Todo

1.  Collecting all validation failures and reporting them at once.
1.  Can we use a ForTuple in createConfiguration?
1.  Logging (in particular the failures) (with a Monad).
1.  Use TypeClass DI.
1.  Input/Output gateway factories. 


## Licensing

[The Unlicense](LICENSE)
