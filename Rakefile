# Builds Hangman using JRuby's Rake library.
#
# JRuby Rake has an interface to Ant which is used here to compile
# Java. The dependency graph engine comes from Rake however, as does
# the superior scripting facility.
#
# This also compiles Clojure code, using Rake's dependency engine and
# invoking clojure.lang.Compile directly.
#
# See build.sh for a convenient way to invoke this. eg
# './build.sh test' runs the tests.
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

# Make non version control directories up front.
#
# We don't make directory creation a Rake file task because a
# directory's modification time is updated when files are copied to
# it. This leads to undesired rebuilds for would-be dependents.
mkdir_p ["target/classes", "target/test-classes"]

### Basic Ant setup
ant.record :name => "build.log", :loglevel => "verbose", :action => "start"
ant.path :id => "hangman.classpath" do
  pathelement :location => "target/classes"
  pathelement :location => CLOJURE_JAR
end
ant.path :id => "hangman.test.classpath" do
  pathelement :location => "target/test-classes"
  pathelement :location => JUNIT_JAR
end

### Create Rake file tasks of Clojure source, to not recompile if uptodate.
#
# A given foo.clj is a dependency of several .class files. For
# simplicity, we create only the :compile => foo.class => foo.clj Rake
# dependency chain, because the other generated .class files from
# foo.clj will generally be as uptodate as the foo.class file.
def create_clj_tasks(src_dir, dest_dir, dependent_target)
  `find #{src_dir} -name "*.clj"`.split(/\s+/).each do |src_file|
    path_fragment = src_file.sub(/#{src_dir}\/(.*)\.clj/, '\1' )
    obj_file = "#{dest_dir}/#{path_fragment}.class"
    package_name = path_fragment.gsub("/",".")

    # Create the Rake file task
    file obj_file => src_file do
      system("java -Dclojure.compile.path=#{dest_dir} -cp #{CLOJURE_JAR}:#{dest_dir}:#{src_dir} clojure.lang.Compile #{package_name}")
    end

    # Add to the inputted Rake target
    task dependent_target => obj_file
  end
end
create_clj_tasks("src/main/clj", "target/classes", :compile)
create_clj_tasks("src/test/clj", "target/test-classes", :compile_test)

task :compile_main_java do
  # Compile Java
  ant.javac :destdir => "target/classes", :includeAntRuntime => false do
    classpath :refid => "hangman.classpath"
    src { pathelement :location => MAIN_SRC_DIR }
  end
end

# Compile Java and Clojure
#
# NB: the create_clj_tasks function adds Clojure deps
task :compile => :compile_main_java

task :compile_test_java do
  ant.javac :destdir => "target/test-classes", :includeAntRuntime => false do
    classpath do
      path :refid => "hangman.classpath"
      path :refid => "hangman.test.classpath"
    end
    src { pathelement :location => TEST_SRC_DIR }
  end
end

# Compile Java and Clojure test code
#
# NB: the create_clj_tasks function adds Clojure deps
task :compile_test => :compile_test_java

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
file "hangman.el" => "Rakefile" do |task_arg|
  classpath_jvmStyle = ant.project.getReference('hangman.classpath').to_s+":"+ant.project.getReference('hangman.test.classpath').to_s
  classpath_lispStyle = '(defvar hangman-classpath (list "'+classpath_jvmStyle.gsub(":", '" "')+'"))'
  File.open("hangman.el", 'w') { |f| f.write(classpath_lispStyle+"\n") }
end

task :jar => :compile do
  ant.jar :destfile => "target/hangman.jar", :basedir => "target/classes"
end

task :default => :jar

