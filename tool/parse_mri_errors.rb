#!/usr/bin/env ruby
## Experimental MRI test tagging script from a test run log
## The input is read from stdin or the first argument

# Usage:
#     tool/parse_mri_errors.rb output.txt
# or
#     jt test mri test/mri/tests/rdoc/test_rdoc_token_stream.rb | tool/parse_mri_errors.rb
require 'etc'
require 'fileutils'

REASON = ENV['REASON']
SLOW_TEST_THRESHOLD = 30
VERY_SLOW_TEST_THRESHOLD = 60
REPO_ROOT = File.expand_path("../..", __FILE__)
EXCLUDES_DIR = "#{REPO_ROOT}/test/mri/excludes"

contents = ARGF.read.scrub.gsub("[ruby] WARNING StackOverflowError\n", '')

# Usually the first line in the error message gives us enough context to quickly identify what caused the failure.
# Sometimes, the first line isn't helpful and we need to look further down. This filter helps us discard unhelpful data.
def should_skip_error?(message)
  Regexp.union(
    /\[ruby-\w+:\d+\]/i,   # MRI bug IDs.
    /\[Bug #?\d+\]/i,      # MRI bug IDs.
    /\[bug:\d+\]/i,        # MRI bug IDs.
    /pid \d+ exit \d/i,    # PID and exit status upon failure.
  ).match(message)
end

def platform_info
  if RUBY_PLATFORM.include?('darwin')
    cpu_model = `sysctl -n machdep.cpu.brand_string`.strip
  elsif RUBY_PLATFORM.include?('linux')
    cpu_model = `cat /proc/cpuinfo | grep 'model name' | uniq`.split(':').last.strip
  else
    cpu_model = 'unknown'
  end

  "#{cpu_model}: ( #{Etc.nprocessors} vCPUs)"
end

def exclude_test!(class_name, test_method, error_display, platform = nil)
  file = EXCLUDES_DIR + "/" + class_name.split("::").join('/') + ".rb"
  prefix = "exclude #{test_method.strip.to_sym.inspect},"

  if test_method =~ /(linux|darwin)/
    platform = $1
  end

  platform_guard = platform ? " if RUBY_PLATFORM.include?('#{platform}')" : ''
  new_line = "#{prefix} #{(REASON || error_display).inspect}#{platform_guard}\n"

  FileUtils.mkdir_p(File.dirname(file))
  lines = File.exist?(file) ? File.readlines(file) : []

  # we need the ',' to handle a case when one test name is a substring of another test name
  if i = lines.index { |line| line.start_with?(prefix) }
    puts "already excluded: #{class_name}##{test_method}"
    lines[i] = new_line
  else
    puts "adding exclude: #{class_name}##{test_method}"
    lines << new_line
  end

  # The test method name in the tag may have characters that can't appear in a symbol literal without quoting. However,
  # the presence of the quotation marks causes the sorting to look a little funny. Such tags will appear at the front
  # of the list because of lexicographic ordering. However, we'd prefer to have an alphabetic ordering based on the
  # name of the method. So, we extract the name from each symbol and sort them that way.
  lines.sort_by! { |line| line.match(/^exclude :"?(.*?)"?,/)[1] }

  File.write(file, lines.join)
end

# If we have an exception escape the interpreter, it will have caused the TruffleRuby process to abort. The test
# results will likely be truncated. We'll attempt to figure out what test was running when the exception occurred
# and tag it. Then we'll let the caller know that they should retry tagging by exiting with a particular status code.

# Sample:
#
# [101/125] TestM17N#test_string_inspect_encoding
# truffleruby: an internal exception escaped out of the interpreter,
# please report it to https://github.com/oracle/truffleruby/issues
#
# ```
# <no message> (java.lang.AssertionError)
# 	from org.truffleruby.core.string.StringNodes$CharacterPrintablePrimitiveNode.isCharacterPrintable(StringNodes.java:3102)
ESCAPED_EXCEPTION = /^\[\s*\d+\/\d+\] ((?:\w+::)*\w+)#(\w+)\n.*?```\n(.*?\n.*?)\n/m

contents.scan(ESCAPED_EXCEPTION) do |class_name, test_method, exception_message|
  exclude_test!(class_name, test_method, exception_message.sub(/\n\s+/, ' '))

  exit 2
end

# In rare cases a bug in TruffleRuby or GraalVM can result in the process crashing. We'll attempt to parse out the
# signal type and the problematic frame to write the exclusion message, and then retry tagging so any other tests
# have the opportunity to run.
JVM_CRASH = /^\[\s*\d+\/\d+\] ((?:\w+::)*\w+)#(\w+)\s*#\n# A fatal error has been detected.*?(?:\n#)+\s+(SIG\w+).*?Problematic frame:\n#\s+(.+?)\n/m
contents.scan(JVM_CRASH) do |class_name, test_method, error_type, source|
  exclude_test!(class_name, test_method, "JVM crash; #{error_type}: #{source}")

  exit 2
end

# If we're running a test that relies on a C symbol we haven't implemented, the process will exit informing us that
# the symbol was undefined. We want to parse that out, tag the associated test, and then retry tagging so any other
# tests have the opportunity to run.


# Samples:
# [19/21] TestSH#test_strftimetest/mri/tests/runner.rb: TestSH#test_strftime: symbol lookup error: /home/nirvdrum/dev/workspaces/truffleruby-ws/truffleruby/mxbuild/truffleruby-jvm-ce/lib/mri/date_core.so: undefined symbol: rb_str_format
# [1/3] TestBignum_Big2str#test_big2str_generictest/mri/tests/runner.rb: TestBignum_Big2str#test_big2str_generic: symbol lookup error: /home/nirvdrum/dev/workspaces/truffleruby-ws/truffleruby/.ext/c/bignum.so: undefined symbol: rb_big2str_generic
MISSING_SYMBOL = / ((?:\w+::)*\w+)#(\w+): symbol lookup error.*? undefined symbol: (\w+)/

contents.scan(MISSING_SYMBOL) do |class_name, test_method, missing_symbol|
  exclude_test!(class_name, test_method, "undefined symbol: #{missing_symbol}")

  exit 2
end

# In some cases the output format is a little different when an undefined symbol is encountered. We treat this as a
# separate case to keep the regular expression simpler. Since this could overlap with the other symbol lookup error
# case, this one must appear second. Otherwise, the extracted method could be incorrect.
#
# In this case, the interpreter path is printed instead of the path to the runner. And it is attached to the test
# method name with no space between them. Additionally, the method name isn't printed twice as it is when the runner
# path is printed, so we're forced to have to extract the method name that's joined to the interpreter path3.
# Fortunately, the path is absolute so we do have a delimiter character we can use.

# Sample:  [1/4] TestThreadInstrumentation#test_join_counters/home/nirvdrum/dev/workspaces/truffleruby-ws/truffleruby/mxbuild/truffleruby-jvm-ce/bin/ruby: symbol lookup error: /home/nirvdrum/dev/workspaces/truffleruby-ws/truffleruby/.ext/c/thread/instrumentation.so: undefined symbol: rb_internal_thread_add_event_hook
SYMBOL_LOOKUP_ERROR = / ((?:\w+::)*\w+)#(\w+?)(?:\/.*?)?: symbol lookup error.*? undefined symbol: (\w+)/

contents.scan(SYMBOL_LOOKUP_ERROR) do |class_name, test_method, missing_symbol|
  exclude_test!(class_name, test_method, "undefined symbol: #{missing_symbol}")

  exit 2
end

# We've observed on macOS that encountering undefined symbols presents yet another format to parse. Unfortunately, in
# this situation the message may not include what the symbol is. But, we can still extract the error message and tag
# the test for reprocessing.

# Sample: [ 35/123] TestFileExhaustive#test_expand_path_hfsdyld[32447]: missing symbol called
DYLD_MISSING_SYMBOL = / ((?:\w+::)*\w+)#(\w+?)dyld\[\d+\]: (.*)/

contents.scan(DYLD_MISSING_SYMBOL) do |class_name, test_method, dyld_message|
  exclude_test!(class_name, test_method, "dyld: #{dyld_message}", 'darwin')

  exit 2
end

TEST_FAILURE = /^\s+\d+\) (Error|Failure|Timeout):\n((?:\w+::)*\w+)#(.+?)(?:\s*\[(?:[^\]])+\])?:?\n(.*?)\n$/m
contents.scan(TEST_FAILURE) do |error_type, class_name, test_method, error|
  if error_type == 'Timeout'
    exclude_test!(class_name, test_method, "retain-on-retag; test timed out")
    next
  end

  error_lines = error.split("\n")
  index = 0

  while should_skip_error?(error_lines[index])
    index += 1
  end

  error_display = error_lines[index]

  # Mismatched expectations span two lines. It's much more useful if they're combined into one message.
  if error_display =~ /expected but was/
    index += 1
    error_display << ' ' + error_lines[index]

  # Mismatched exception messages span three lines. It's much more useful if they're combined into one message.
  elsif error_display =~ /but the message doesn't match/
    error_display << ' ' + error_lines[index + 1..].join(' ')
    index = error_lines.size

  # Handle exceptions by reading the message up until the backtrace.
  elsif error_display =~ /Exception raised:/
    while (line = error_lines[index + 1]) !~ /Backtrace:/
      index += 1
      error_display << ' ' << line.strip
    end

  # Assertion errors are more useful with the first line of the backtrace.
  elsif error_display&.include?('java.lang.AssertionError')
    index += 1
    error_display << ' ' << error_lines[index ]

  # As a catch-all, any message ending with a colon likely has more context on the next line.
  elsif error_display&.end_with?(':')
    index += 1
    error_display << ' ' << error_lines[index]
  end

  # Generated Markdown code blocks span multiple lines. It's much more useful if they're combined into one message.
  if error_display =~ /```/
    index += 1

    begin
      until (line = error_lines[index]) =~ /```/
        error_display << line << "\n"
        index += 1
      end

      error_display << line
    rescue
      error_display = "needs investigation; multi-line code block"
    end
  end

  if error_display.to_s.strip.empty?
    error_display = "needs investigation"
  end

  exclude_test!(class_name, test_method, error_display)
end


# Tag slow tests.

test_ruby_version = contents.match(/ruby -v: (.*)/)[1].strip

# Sample: [ 6/39] TestSocket_UNIXSocket#test_addr = 0.02 s
TEST_EXECUTION_TIME = /^\[\s*\d+\/\d+\] ((?:\w+::)*\w+)#(.+?) = (\d+\.\d+) s/

contents.scan(TEST_EXECUTION_TIME) do |class_name, test_method, execution_time|
  if execution_time.to_f > SLOW_TEST_THRESHOLD
    prefix =  execution_time.to_f > VERY_SLOW_TEST_THRESHOLD ? "very slow" : "slow"
    message = "#{prefix}: #{execution_time}s on #{test_ruby_version} with #{platform_info}"

    exclude_test!(class_name, test_method, message)
  end
end
