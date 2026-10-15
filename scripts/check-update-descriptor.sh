#!/usr/bin/env bash
# Checks that updatePlugins.xml points to the version declared in gradle.properties.
set -euo pipefail

cd "$(dirname "$0")/.."

prop() {
    grep -E "^$1=" gradle.properties | cut -d= -f2-
}

version="$(prop pluginVersion)"
since_build="$(prop pluginSinceBuild)"
expected_url="https://github.com/RakambdaOrg/RakambdaToolbox/releases/download/v${version}/RakambdaToolbox-${version}.zip"

status=0
grep -q "version=\"${version}\"" updatePlugins.xml || { echo "updatePlugins.xml: version is not ${version}"; status=1; }
grep -q "url=\"${expected_url}\"" updatePlugins.xml || { echo "updatePlugins.xml: url is not ${expected_url}"; status=1; }
grep -q "since-build=\"${since_build}\"" updatePlugins.xml || { echo "updatePlugins.xml: since-build is not ${since_build}"; status=1; }

if [[ $status -eq 0 ]]; then
    echo "updatePlugins.xml is consistent with gradle.properties (version ${version})"
fi
exit $status
