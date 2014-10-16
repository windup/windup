
Building Windup
===============

For your convenience, you can use scripts and settings.xml in this directory to
skip configuring Maven yourself.

Due to a known bug (WINDUP-322), you need to run a priming build first, otherwise
the tests would fail finding their dependencies.

The linux (Bash) script will determine the need for a priming build automatically.

For Windows, you need to run a priming build yourself:

    mvn install -DskipTests

