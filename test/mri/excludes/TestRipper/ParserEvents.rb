exclude :test_endless_def, "<{:on_def=>true, :on_parse_error=>true}> expected but was <{:on_def=>true}>."
exclude :test_endless_defs, "<{:on_defs=>true, :on_parse_error=>true}> expected but was <{:on_defs=>true}>."
exclude :test_magic_comment, "dyld: missing symbol called"
exclude :test_warning_ignored_magic_comment, "dyld: missing symbol called"
exclude :test_warning_invalid_magic_comment, "dyld: missing symbol called"
