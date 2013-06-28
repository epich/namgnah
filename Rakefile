# Builds Hangman using JRuby's Rake library.
#
# JRuby Rake has an interface to Ant which is used here to compile
# Java. The dependency graph engine comes from Rake however, as does
# the superior scripting facility.
#
# This also compiles Clojure code, using Rake's dependency engine and
# invoking clojure.lang.Compile directly.
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

#CLOJURE_JAR = "/psd15/linux/boreilly/sw/clojure-1.5.1/clojure-1.5.1.jar"
CLOJURE_JAR = "/home/epich/s/sw/clojure-1.5.1/target/clojure-1.5.1.jar"

MAIN_SRC_DIR = "src/main/java"
TEST_SRC_DIR = "src/test/java"
JUNIT_JAR = "src/test/lib/junit-4.10.jar"

CLEAN.include "target", "build.log", "hangman.el"

# Make non version control directories up front.
#
# We don't make directory creation a Rake file task because a
# directory's modification time is updated when files are copied to
# it. This leads to undesired rebuilds for would-be dependents.
mkdir_p ["target/classes", "target/test-classes"]

### Basic Ant setup
ant.record :name => "build.log", :loglevel => "verbose", :action => "start"
ant.path :id => "hangman.classpath" do
  pathelement :location => CLOJURE_JAR
  pathelement :location => "target/classes"
end
ant.path :id => "hangman.test.classpath" do
  path :refid => "hangman.classpath"
  pathelement :location => JUNIT_JAR
  pathelement :location => "target/test-classes"
end

### Create Rake file tasks of Clojure source, to not recompile if uptodate.
#
# A given foo.clj is a dependency of several .class files. For
# simplicity, we create only the :compile => foo.class => foo.clj Rake
# dependency chain, because the other generated .class files from
# foo.clj will generally be as uptodate as the foo.class file.
def create_clj_tasks(src_dir, dest_dir, dependent_target, classpath_arg)
  `find #{src_dir} -name "*.clj"`.split(/\s+/).each do |src_file|
    path_fragment = src_file.sub(/#{src_dir}\/(.*)\.clj/, '\1' )
    obj_file = "#{dest_dir}/#{path_fragment}.class"
    package_name = path_fragment.gsub("/",".")

    # Create the Rake file task
    file obj_file => src_file do
      # The downside of this is starting a JVM for each source
      # file. Not a big deal for a small project like this, but would
      # be good to improve if there were more .clj files to
      # compile.
      system("java -Dclojure.compile.path=#{dest_dir} -cp :#{classpath_arg}:#{src_dir} clojure.lang.Compile #{package_name}")
    end

    # Add to the inputted Rake target
    task dependent_target => obj_file
  end
end
# TODO: Use Clojure?
# create_clj_tasks("src/main/clj",
#                  "target/classes",
#                  :compile,
#                  ant.project.getReference('hangman.classpath'))
# create_clj_tasks("src/test/clj",
#                  "target/test-classes",
#                  :compile_test,
#                  ant.project.getReference('hangman.test.classpath'))

task :compile_main_java do
  ant.javac :srcdir => MAIN_SRC_DIR,
            :destdir => "target/classes",
            :classpathref => "hangman.classpath",
            :includeAntRuntime => false
end

# Compile Java and Clojure
#
# NB: the create_clj_tasks function adds Clojure deps
task :compile => :compile_main_java

task :compile_test_java do
  ant.javac :srcdir => TEST_SRC_DIR,
            :destdir => "target/test-classes",
            :classpathref => "hangman.test.classpath",
            :includeAntRuntime => false
end

# Compile Java and Clojure test code
#
# NB: the create_clj_tasks function adds Clojure deps
task :compile_test => :compile_test_java

task :test => [:jar, :jar_test] do
  ant.junit do
    classpath :refid => "hangman.test.classpath"
    batchtest do
      formatter :type => "plain", :usefile => "false"
      fileset :dir => TEST_SRC_DIR, :includes => '**/*Test.java'
    end
  end

  # TODO
  # Now the Clojure based test
  # require 'target/hangman.jar'
  # require 'target/hangman-test.jar'
  # require CLOJURE_JAR
  # # Call from JRuby straight into Clojure
  # Java::hangman::test::StrategyTest.runTest
end

def choose_random_word
  printf("Choosing random word from words.txt to play with.\n")
  File.readlines("words.txt").sample.strip
end

GUESSES_DEFAULT = 5
desc "Play Hangman with the provided strategy implementation. Optionally
specify WORD (default: random) and GUESSES (default: #{GUESSES_DEFAULT})
in the environment. eg: 'rake play WORD=food GUESSES=6'"
task :play => :jar do
  # Note: We're relying on the implementation to error check that word is in the words.txt.
  # Unless WORD is unspecified, in which case the user wants a random word.
  the_word = ENV.key?('WORD') ? ENV['WORD'] : choose_random_word
  # Only checking for undefined. If user passes something silly, let it fail Integer conversion.
  num_guesses = ENV.key?('GUESSES') ? Integer(ENV['GUESSES']) : GUESSES_DEFAULT
  printf( "Playing Hangman with WORD=%s and allowed GUESSES=%s\n", the_word, num_guesses )
  require 'target/hangman.jar'
  printf( "Score: %s\n",
          Java::hangman::Play.run(Java::hangman::HangmanGame.new(the_word, num_guesses),
                                  Java::hangman::StrategyImpl.new) )
end

# Export Elisp for importing into Emacs.
#
# Primarily for the classpath, of which the Rakefile is the primary source
file "hangman.el" => "Rakefile" do |task_arg|
  classpath_jvmStyle = ant.project.getReference('hangman.classpath').to_s+":"+ant.project.getReference('hangman.test.classpath').to_s
  classpath_lispStyle = '(defvar hangman-classpath (list "'+classpath_jvmStyle.gsub(":", '" "')+'"))'
  File.open("hangman.el", 'w') { |f| f.write(classpath_lispStyle+"\n") }
end

task :jar => :compile do
  ant.jar :destfile => "target/hangman.jar", :basedir => "target/classes"
end
task :jar_test => :compile_test do
  ant.jar :destfile => "target/hangman-test.jar", :basedir => "target/test-classes"
end

task :default => :jar

