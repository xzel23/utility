#!/usr/bin/env bash
# Prepare and, after explicit confirmation, push a release plan.

set -euo pipefail

usage() {
    cat <<'EOF'
Usage: scripts/prepare-release.sh --type <patch|minor|major> [options]

Options:
  --type <type>                 Required release type: patch, minor, or major.
  --version <major.minor.patch> Optional explicit release version.
  --additional-modules <list>   Comma-separated modules to include in a patch release.
  -h, --help                    Show this help text.

The script first displays a dry-run release plan. On confirmation, it writes and
commits gradle/prepared-release.toml. A second confirmation pushes that commit to
the current branch's upstream, which starts the protected GitHub release workflow.
EOF
}

confirm() {
    local prompt="$1"
    local answer

    read -r -p "$prompt [y/N] " answer
    case "$answer" in
        [Yy]|[Yy][Ee][Ss]) return 0 ;;
        *) return 1 ;;
    esac
}

release_type=""
release_version=""
additional_modules=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --type)
            [[ $# -ge 2 ]] || { echo "--type requires a value" >&2; exit 2; }
            release_type="$2"
            shift 2
            ;;
        --version)
            [[ $# -ge 2 ]] || { echo "--version requires a value" >&2; exit 2; }
            release_version="$2"
            shift 2
            ;;
        --additional-modules)
            [[ $# -ge 2 ]] || { echo "--additional-modules requires a value" >&2; exit 2; }
            additional_modules="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            usage >&2
            exit 2
            ;;
    esac
done

case "$release_type" in
    patch|minor|major)
        ;;
    *)
        echo "Supply --type patch, --type minor, or --type major." >&2
        usage >&2
        exit 2
        ;;
esac

if [[ -n "$additional_modules" && "$release_type" != "patch" ]]; then
    echo "--additional-modules is only valid for a patch release." >&2
    exit 2
fi

script_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repository_directory="$(cd "$script_directory/.." && pwd)"
cd "$repository_directory"

if [[ -n "$(git status --porcelain)" ]]; then
    echo "The Git working tree must be clean before preparing a release." >&2
    exit 1
fi

if [[ -e gradle/prepared-release.toml ]]; then
    echo "gradle/prepared-release.toml already exists; publish, finalize, or resolve the existing release first." >&2
    exit 1
fi

branch="$(git branch --show-current)"
if [[ -z "$branch" ]]; then
    echo "Preparing a release requires a checked-out branch, not a detached HEAD." >&2
    exit 1
fi

if ! git rev-parse --abbrev-ref --symbolic-full-name '@{upstream}' >/dev/null 2>&1; then
    echo "The current branch '$branch' must have an upstream before preparing a release." >&2
    exit 1
fi

git fetch --quiet
if [[ "$(git rev-parse HEAD)" != "$(git rev-parse '@{upstream}')" ]]; then
    echo "The current branch '$branch' must match its upstream before preparing a release; pull or push first." >&2
    exit 1
fi

release_arguments=("-PreleaseType=$release_type")
if [[ -n "$release_version" ]]; then
    release_arguments+=("-PreleaseVersion=$release_version")
fi
if [[ -n "$additional_modules" ]]; then
    release_arguments+=("-PadditionalReleaseModules=$additional_modules")
fi

echo "Generating the dry-run release plan..."
./gradlew prepareRelease "${release_arguments[@]}"

if ! confirm "Create and commit this prepared release plan?"; then
    echo "Release preparation cancelled; no files were changed."
    exit 0
fi

./gradlew prepareRelease "${release_arguments[@]}" -PconfirmRelease=true

prepared_version="$(awk -F '"' '/^bomVersion = "/ { print $2; exit }' gradle/prepared-release.toml)"
if [[ -z "$prepared_version" ]]; then
    echo "Could not determine the prepared release version." >&2
    exit 1
fi

git add -- gradle/prepared-release.toml
git commit -m "Prepare release $prepared_version"

echo "Prepared release $prepared_version in $(git rev-parse --short HEAD)."
if ! confirm "Push the prepared plan and start the protected GitHub release workflow?"; then
    echo "The prepared release commit remains local and has not started a release."
    exit 0
fi

git push
echo "Pushed prepared release $prepared_version from '$branch'. Monitor the 'Publish prepared release' workflow in GitHub Actions."
