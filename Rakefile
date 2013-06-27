# Builds Hangman using JRuby Rake's interface to Ant.
#
# See build.sh for a convenient way to invoke this.
# eg './build.sh test' runs the tests.
#
# Debugging advice:
#   - Set loglevel in the ant.record task
#   - Get Ant properties by: ant.properties['property.name']
#   - Get Ant path-like objects by: ant.project.getReference('my.classpath')

require 'ant'
require 'rake/clean'

task :default => :jar

BUILD_DIR = "target"
MAIN_SRC_DIR = "src/main/java"
TEST_SRC_DIR = "src/test/java"
JUNIT_JAR = "src/test/lib/junit-4.10.jar"
BUILD_LOG = "build.log"

CLEAN.include BUILD_DIR, BUILD_LOG, "hangman.el"

file BUILD_DIR do |task_arg|
  mkdir_p task_arg.name
end

task :setup => BUILD_DIR do
  ant.record :name => BUILD_LOG, :loglevel => "verbose", :action => "start"
  ant.path :id => "hangman.classpath" do
    pathelement :location => "target"
  end
  ant.path :id => "hangman.test.classpath" do
    pathelement :location => JUNIT_JAR
  end
end

task :compile => :setup do
  ant.javac :destdir => BUILD_DIR, :includeAntRuntime => false do
    classpath :refid => "hangman.classpath"
    src { pathelement :location => MAIN_SRC_DIR }
  end
end

task :jar => :compile do
  ant.jar :destfile => "hangman.jar", :basedir => BUILD_DIR
end

task :compile_test => :setup do
  ant.javac :destdir => BUILD_DIR, :includeAntRuntime => false do
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

