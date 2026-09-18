#!/usr/bin/env bash
# Prints the path of a java @argfile for the TornadoVM install in $TORNADOVM_HOME.
#
# $TORNADOVM_HOME/tornado-argfile hardcodes paths under .../tornadovm/current/, i.e. the
# SDKMAN *default* install rather than $TORNADOVM_HOME. When `sdk use` and `sdk default`
# name different versions, the demos silently run on the wrong TornadoVM. Expanding the
# shipped template instead makes TORNADOVM_HOME authoritative.
set -euo pipefail
: "${TORNADOVM_HOME:?TORNADOVM_HOME is not set - install TornadoVM via SDKMAN}"

template="$TORNADOVM_HOME/tornado-argfile.template"
if [[ -f "$template" ]]; then
    argfile="${TMPDIR:-/tmp}/tornado-argfile.$(basename "$TORNADOVM_HOME").args"
    sed "s|\${TORNADOVM_HOME}|$TORNADOVM_HOME|g" "$template" > "$argfile"
    printf '%s\n' "$argfile"
else
    # TornadoVM < 5.0 ships no template; its argfile is already fully resolved.
    printf '%s\n' "$TORNADOVM_HOME/tornado-argfile"
fi
