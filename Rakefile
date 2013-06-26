# Builds Hangman using JRuby Rake's interface to Ant.

require 'ant'

task :default => :jar

BUILD_DIR = "target"
REPORT_DIR = "report"
MAIN_SRC_DIR = "src/main/java"
TEST_SRC_DIR = "src/test/java"
JUNIT_JAR = "src/test/lib/junit.jar"

# Define build target for each directory to create
[BUILD_DIR, REPORT_DIR].each do |dir|
  file dir do |task_arg|
    mkdir_p task_arg.name
  end
end

task :setup => BUILD_DIR do
  ant.record :name => "build.log", :loglevel => "verbose", :action => "start"
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
  #ant.jar :destfile => "hangman-test.jar", :basedir => BUILD_DIR
end

task :compile_test => :setup do
  ant.javac :destdir => BUILD_DIR, :includeAntRuntime => true do
    classpath do
      path :refid => "hangman.classpath"
      path :refid => "hangman.test.classpath"
    end
    src { pathelement :location => TEST_SRC_DIR }
  end
end

task :test => [:compile, :compile_test, REPORT_DIR] do
  ant.junit :printsummary => "withOutAndErr" do
    classpath do
      path :refid => "hangman.classpath"
      path :refid => "hangman.test.classpath"
    end
    batchtest :todir => REPORT_DIR do
      fileset :dir => TEST_SRC_DIR, :includes => '**/*Test.java'
    end
  end
end

