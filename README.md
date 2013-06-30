Hangman
=======

Implements Hangman game and strategy.

Author: Barry O'Reilly

Setup
-----

- JRuby
  - Install JRuby 1.7.4 or above
    - A sufficiently new version is important. I found older JRuby
      were incompatible with the Ant 1.9 series because of
      https://github.com/jruby/jruby/issues/601 bug.
  - Add the JRuby bin directory to PATH
  - jruby -S gem install rake

- Ant
  - Install Ant
    - I used 1.9.1
  - Set ANT_HOME to Ant directory
    - eg: export ANT_HOME=/home/boreilly/sw/apache-ant-1.9.1

- JUnit
  - NB: JUnit 4.10 is bundled with this distribution
  - NB: JUnit 4.11 was too buggy to use

Build and run
-------------

Use JRuby Rake to build. Check `which rake` to verify it comes from
the right JRuby install. Alternatively, verify `which jruby` and use
'jruby -S rake'. build.sh wraps 'jruby -S rake' for convenience.

Some common build tasks:
  - General Rake help:
      rake -h
  - See task documentation:
      rake -D
  - Run tests:
      rake test
  - Play a game of hangman:
      rake play [WORD=word_choice] [GUESSES=number_wrong_guesses]
  - Play many games with score averaging:
      rake brute [STEP_SIZE=step_size_through_dictionary] [GUESSES=number_wrong_guesses]

See the Rakefile for more information.

Strategy decription
-------------------

See the documentation for the StrategyImpl class.


