Hangman
=======

JRuby
  - Install JRuby 1.7.4 or above
    - A sufficiently new version is important. I found older JRuby were incompatible with the Ant 1.9 series because of [1].
  - Add the JRuby bin directory to PATH
  - jruby -S gem install rake

Ant
  - Install Ant
    - I used 1.9.1
  - Set ANT_HOME to Ant directory
    - eg: export ANT_HOME=/home/boreilly/sw/apache-ant-1.9.1

JUnit
  - JUnit 4.10 is bundled with this distribution
  - JUnit 4.11 was too buggy to use

[1] https://github.com/jruby/jruby/issues/601
