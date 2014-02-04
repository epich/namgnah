# Builds Hangman using JRuby's Rake library.
#
# JRuby Rake has an interface to Ant which is used here to compile
# Java. The dependency graph engine comes from Rake however, as does
# the superior scripting facility.
#
# build.sh provides an alternative way to call Rake, particularly if
# your PATH rake is from a C Ruby install. JRuby's is required.
#
# Some example usage:
#   - Run the tests: rake test
#   - Play the game: rake play WORD=FACTUAL GUESSES=4
#   - Play by brute force: rake brute [STEP_SIZE=100]
# See "desc" documentation later in this file for details on
# these. Also see 'rake -h' help for general usage of Rake.
#
# Debugging notes:
#   - Set loglevel in the ant.record task
#   - Get Ant properties by: ant.properties['property.name']
#   - Get Ant path-like objects by: ant.project.getReference('my.classpath')

require 'ant'
require 'rake/clean'

JUNIT_JAR = "src/test/lib/junit-4.10.jar"

CLEAN.include "target", "build.log", "hangman.el"

### Basic Ant setup
ant.record :name => "build.log", :loglevel => "verbose", :action => "start"
ant.path :id => "hangman.classpath" do
  pathelement :location => "target/main"
end
ant.path :id => "hangman.test.classpath" do
  path :refid => "hangman.classpath"
  pathelement :location => JUNIT_JAR
  pathelement :location => "target/test"
end

# Rake "directory" tasks to create directories as needed
["target/main", "target/test"].each do |non_vcs_dir|
  directory non_vcs_dir
end

task :resources => "target/main" do
  # Copy resources including words.txt
  ant.copy :todir => "target/main" do
    fileset :dir => "src/main/resources"
  end
end

task :compile => "target/main" do
  ant.javac :srcdir => "src/main/java",
            :destdir => "target/main",
            :classpathref => "hangman.classpath",
            :includeAntRuntime => false,
            :debug => true
end
task :compile_test => "target/test" do
  ant.javac :srcdir => "src/test/java",
            :destdir => "target/test",
            :classpathref => "hangman.test.classpath",
            :includeAntRuntime => false,
            :debug => true
end

desc "Build the primary .jar file for Hangman."
task :jar => [:compile, :resources] do
  ant.jar :destfile => "target/hangman.jar", :basedir => "target/main"
end
task :jar_test => :compile_test do
  ant.jar :destfile => "target/hangman-test.jar", :basedir => "target/test"
end

desc "Run the tests."
task :test => [:jar, :jar_test] do
  # Fork starts a new JVM so as we don't get the huge JRuby stack trace when tests fail.
  ant.junit :fork => true, :forkmode => "perBatch" do
    classpath :refid => "hangman.test.classpath"
    batchtest do
      formatter :type => "plain", :usefile => "false"
      fileset :dir => "src/test/java", :includes => '**/*Test.java'
    end
  end
end

def choose_random_word(dict)
  printf("Choosing random word from #{dict} to play with.\n")
  File.readlines(dict).sample.strip
end

# NB: To observe the DRY principle, I define DEFAULT_NUM_GUESSES
# in the Play Java class. However, in a clean source tree, the
# hangman.jar does not exist yet and therefore JRuby cannot get the
# DEFAULT_NUM_GUESSES value to display as part of the task's desc.
# Of course it can in the body since Rake will have built the
# dependency .jar file.
desc "Play Hangman with the provided strategy implementation.

Optionally specify WORD (default: random from words.txt) and GUESSES
(default: hangman.Play.DEFAULT_NUM_GUESSES) in the environment.
eg: 'rake play WORD=food GUESSES=6'"
task :play => :jar do
  require 'java'
  require 'target/hangman.jar'

  the_word = ENV.key?('WORD') ? ENV['WORD'] : choose_random_word("src/main/resources/words.txt")
  # Only checking for undefined. If user passes something silly, let it fail Integer conversion.
  num_guesses = ENV.key?('GUESSES') ? Integer(ENV['GUESSES']) : Java::hangman::Play::DEFAULT_NUM_GUESSES
  printf( "Playing Hangman with WORD=%s and allowed GUESSES=%s\n", the_word, num_guesses )
  game = Java::hangman::HangmanGame.new(the_word, num_guesses)
  Java::hangman::Play.run(game,
                          Java::hangman::StrategyImpl.new(the_word.length,
                                                          # Find words.txt from hangman.jar
                                                          game.getClass().getClassLoader().getResourceAsStream("words.txt")))
end

desc "Play Hangman, brute forcing words from the words.txt dictionary.

Displays game scores and the average score over games played.

Optionally specify STEP_SIZE (default: 1) in the environment in order
to use every STEP_SIZEth word instead. For reference, STEP_SIZE=100
runs in just under 2min on one computer.

Also, optionally specify GUESSES (default:
hangman.Play.DEFAULT_NUM_GUESSES) in the environment."
task :brute => [:jar, :jar_test] do
  require 'target/hangman.jar'
  require 'target/hangman-test.jar'
  step_size = ENV.key?('STEP_SIZE') ? Integer(ENV['STEP_SIZE']) : 1
  num_guesses = ENV.key?('GUESSES') ? Integer(ENV['GUESSES']) : Java::hangman::Play::DEFAULT_NUM_GUESSES
  Java::hangman::test::HangmanTest.runBrute(step_size, num_guesses)
end

desc "Export Elisp for importing into Emacs.

Primarily for the classpath, of which the Rakefile is the primary source."
file "hangman.el" => "Rakefile" do |task_arg|
  classpath_jvmStyle = ant.project.getReference('hangman.classpath').to_s+":"+ant.project.getReference('hangman.test.classpath').to_s
  classpath_lispStyle = '(defvar hangman-classpath (list "'+classpath_jvmStyle.gsub(":", '" "')+'"))'
  File.open("hangman.el", 'w') { |f| f.write(classpath_lispStyle+"\n") }
end

task :default => :jar

 
