# Builds Hangman using JRuby Rake's interface to Ant.
#
# See build.sh for a convenient way to invoke this.
# eg './build.sh test' runs the tests.

require 'ant'
require 'rake/clean'

task :default => :jar

BUILD_DIR = "target"
MAIN_SRC_DIR = "src/main/java"
TEST_SRC_DIR = "src/test/java"
JUNIT_JAR = "src/test/lib/junit-4.10.jar"
BUILD_LOG = "build.log"

CLEAN.include BUILD_DIR, BUILD_LOG

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
