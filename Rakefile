# Builds Hangman using JRuby's Rake library.
#
# JRuby Rake has an interface to Ant which is used here to compile
# Java. The dependency graph engine comes from Rake however, as does
# the superior scripting facility.
#
# Example usage:
#   - Run the tests: rake test
#   - Play the game: rake play WORD=word GUESSES=5
#       - Default WORD is randomly chosen from words.txt
#       - Default GUESSES is 5
#
# See build.sh provides an alternative way to call Rake assuming jruby
# is available.
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

["target/main", "target/test"].each do |non_vcs_dir|
  # Create Rake task to create dir if it doesn't exist
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
            :includeAntRuntime => false
end
task :compile_test => "target/test" do
  ant.javac :srcdir => "src/test/java",
            :destdir => "target/test",
            :classpathref => "hangman.test.classpath",
            :includeAntRuntime => false
end

task :jar => [:compile, :resources] do
  ant.jar :destfile => "target/hangman.jar", :basedir => "target/main"
end
task :jar_test => :compile_test do
  ant.jar :destfile => "target/hangman-test.jar", :basedir => "target/test"
end

task :test => [:jar, :jar_test] do
  ant.junit do
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

GUESSES_DEFAULT = 5
desc "Play Hangman with the provided strategy implementation. Optionally
specify WORD (default: random) and GUESSES (default: #{GUESSES_DEFAULT})
in the environment. eg: 'rake play WORD=food GUESSES=6'"
task :play => :jar do
  # Note: We're relying on the implementation to error check that word is in the words.txt.
  # Unless WORD is unspecified, in which case the user wants a random word.
  the_word = ENV.key?('WORD') ? ENV['WORD'] : choose_random_word("src/main/resources/words.txt")
  # Only checking for undefined. If user passes something silly, let it fail Integer conversion.
  num_guesses = ENV.key?('GUESSES') ? Integer(ENV['GUESSES']) : GUESSES_DEFAULT
  printf( "Playing Hangman with WORD=%s and allowed GUESSES=%s\n", the_word, num_guesses )
  require 'java'
  require 'target/hangman.jar'
  game = Java::hangman::HangmanGame.new(the_word, num_guesses)
  Java::hangman::Play.run(game,
                          Java::hangman::StrategyImpl.new(the_word.length,
                                                          # Find words.txt from hangman.jar
                                                          game.getClass().getClassLoader().getResourceAsStream("words.txt")))
end

# Export Elisp for importing into Emacs.
#
# Primarily for the classpath, of which the Rakefile is the primary source
file "hangman.el" => "Rakefile" do |task_arg|
  classpath_jvmStyle = ant.project.getReference('hangman.classpath').to_s+":"+ant.project.getReference('hangman.test.classpath').to_s
  classpath_lispStyle = '(defvar hangman-classpath (list "'+classpath_jvmStyle.gsub(":", '" "')+'"))'
  File.open("hangman.el", 'w') { |f| f.write(classpath_lispStyle+"\n") }
end

task :default => :jar

