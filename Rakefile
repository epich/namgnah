# Builds Hangman using JRuby's Rake library.
#
# JRuby Rake has an interface to Ant which is used here to compile
# Java. The dependency graph engine comes from Rake however, as does
# the superior scripting facility.
#
# See build.sh for a convenient way to invoke this.
# eg './build.sh test' runs the tests.
#
# Debugging notes:
#   - Set loglevel in the ant.record task
#   - Get Ant properties by: ant.properties['property.name']
#   - Get Ant path-like objects by: ant.project.getReference('my.classpath')

require 'ant'
require 'rake/clean'

CLOJURE_JAR = "/psd15/linux/boreilly/sw/clojure-1.5.1/clojure-1.5.1.jar"
#CLOJURE_JAR = "/home/epich/s/sw/clojure-1.5.1/target/clojure-1.5.1.jar"

MAIN_SRC_DIR = "src/main/java"
TEST_SRC_DIR = "src/test/java"
JUNIT_JAR = "src/test/lib/junit-4.10.jar"

CLEAN.include "target", "build.log", "hangman.el"

task :default => :jar

# Dirs not under version control, which the build system will create on demand
NON_VCS_DIRS = ["target/classes", "target/test-classes"]
NON_VCS_DIRS.each do |dir|
  file dir do |task_arg|
    mkdir_p task_arg.name
  end
end

task :setup => NON_VCS_DIRS do
  ant.record :name => "build.log", :loglevel => "verbose", :action => "start"
  ant.path :id => "hangman.classpath" do
    pathelement :location => "target/classes"
    pathelement :location => CLOJURE_JAR
  end
  ant.path :id => "hangman.test.classpath" do
    pathelement :location => "target/test-classes"
    pathelement :location => JUNIT_JAR
  end
end

task :compile => :setup do
  # Compile Java
  ant.javac :destdir => "target/classes", :includeAntRuntime => false do
    classpath :refid => "hangman.classpath"
    src { pathelement :location => MAIN_SRC_DIR }
  end

  # Compile Clojure
  system("java -Dclojure.compile.path=target/classes -cp #{CLOJURE_JAR}:target/classes:src/main/clj clojure.lang.Compile hangman.guessing_strategy")
end

task :jar => :compile do
  ant.jar :destfile => "target/hangman.jar", :basedir => "target/classes"
end

task :compile_test => :setup do
  ant.javac :destdir => "target/test-classes", :includeAntRuntime => false do
    classpath do
      path :refid => "hangman.classpath"
      path :refid => "hangman.test.classpath"
    end
    src { pathelement :location => TEST_SRC_DIR }
  end
end

task :test => [:compile, :compile_test] do
  ant.junit :printsummary => true do
    classpath do
      path :refid => "hangman.classpath"
      path :refid => "hangman.test.classpath"
    end
    batchtest do
      fileset :dir => TEST_SRC_DIR, :includes => '**/*Test.java'
    end
  end
end

# Export Elisp for importing into Emacs.
#
# Primarily for the classpath, of which the Rakefile is the primary source
file "hangman.el" => ["Rakefile", :setup] do |task_arg|
  classpath_jvmStyle = ant.project.getReference('hangman.classpath').to_s+":"+ant.project.getReference('hangman.test.classpath').to_s
  classpath_lispStyle = '(defvar hangman-classpath "'+classpath_jvmStyle.gsub(":", '" "')+'")'
  File.open("hangman.el", 'w') { |f| f.write(classpath_lispStyle+"\n") }
end

