#!/usr/bin/env bash
#
# Friendly wrapper around build tool.
#
# You can use 'rake' instead, but first verify 'which rake' shows it's
# from your JRuby install (rather than another C Ruby install).
jruby -S rake $@

