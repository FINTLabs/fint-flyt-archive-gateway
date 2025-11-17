#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE_DIR="$ROOT/kustomize/templates"
DEFAULT_TEMPLATE="$TEMPLATE_DIR/overlay.yaml.tpl"
APPLICATION_NAME="fint-flyt-archive-gateway"
FINT_CLIENT_NAME="fint-flyt-archive-fint-client"

USER_ROLE="USER"
DEVELOPER_ROLE="DEVELOPER"

extra_user_orgs_for_namespace() {
  local namespace="$1"
  case "$namespace" in
    afk-no|bfk-no|ofk-no)
      printf 'frid-iks.no'
      ;;
    *)
      printf ''
      ;;
  esac
}

render_metrics_patch_block() {
  local metrics_path="$1"
  printf '\n      - op: replace\n'
  printf '        path: "/spec/observability/metrics/path"\n'
  printf '        value: "%s"\n' "$metrics_path"
}

render_authorized_role_pairs() {
  local org_id="$1"
  shift

  local entries=("\"${org_id}\":[\"${USER_ROLE}\"]")
  for extra_org in "$@"; do
    entries+=("\"${extra_org}\":[\"${USER_ROLE}\"]")
  done
  entries+=("\"vigo.no\":[\"${DEVELOPER_ROLE}\", \"${USER_ROLE}\"]")
  entries+=("\"novari.no\":[\"${DEVELOPER_ROLE}\", \"${USER_ROLE}\"]")

  local total="${#entries[@]}"
  printf '            {\n'
  for idx in "${!entries[@]}"; do
    local comma=","
    if [[ "$idx" == "$((total - 1))" ]]; then
      comma=""
    fi
    printf '              %s%s\n' "${entries[$idx]}" "$comma"
  done
  printf '            }\n'
}

choose_template() {
  local env_path="$1"
  if [[ -z "$env_path" ]]; then
    printf '%s' "$DEFAULT_TEMPLATE"
    return
  fi

  local candidate="overlay-${env_path//\//-}.yaml.tpl"
  local candidate_path="$TEMPLATE_DIR/$candidate"

  if [[ -f "$candidate_path" ]]; then
    printf '%s' "$candidate_path"
  else
    printf '%s' "$DEFAULT_TEMPLATE"
  fi
}

while IFS= read -r file; do
  rel="${file#"$ROOT/kustomize/overlays/"}"
  dir="$(dirname "$rel")"

  namespace="${dir%%/*}"
  env_path="${dir#*/}"
  if [[ "$env_path" == "$namespace" ]]; then
    env_path=""
  fi

  ns_suffix="${namespace//-/_}"
  path_prefix="/$namespace"
  if [[ -n "$env_path" && "$env_path" != "api" ]]; then
    path_prefix="/${env_path}/$namespace"
  fi

  base_url="https://api.felleskomponent.no"
  onepassword_vault="aks-api-vault"
  if [[ "$env_path" == "beta" ]]; then
    base_url="https://beta.felleskomponent.no"
    onepassword_vault="aks-beta-vault"
  fi

  declare -a additional_user_orgs=()
  extra_orgs="$(extra_user_orgs_for_namespace "$namespace")"
  if [[ -n "$extra_orgs" ]]; then
    for extra_org in $extra_orgs; do
      additional_user_orgs+=("$extra_org")
    done
  fi

  export APPLICATION_NAME
  export APPLICATION_PATCH_LABEL="fint-archive-gateway_${ns_suffix}"
  export NAMESPACE="$namespace"
  export ORG_ID="${namespace//-/.}"
  export APP_INSTANCE_LABEL="${APPLICATION_NAME}_${ns_suffix}"
  export KAFKA_TOPIC="${namespace}.flyt.*"
  export URL_BASE_PATH="$path_prefix"
  export INGRESS_BASE_PATH="${path_prefix}/api/intern/arkiv"
  export ARCHIVE_BASE_URL="$base_url"
  export ONEPASSWORD_ITEM_PATH="vaults/${onepassword_vault}/items/fint-flyt-v1-slack-webhook"
  export READINESS_PATH="${path_prefix}/actuator/health"
  export METRICS_PATH="${path_prefix}/actuator/prometheus"
  metrics_patch_before_ingress=""
  if [[ "$namespace" != "afk-no" ]]; then
    metrics_patch_before_ingress="$(render_metrics_patch_block "$METRICS_PATH")"
  fi
  export METRICS_PATCH_BEFORE_INGRESS="$metrics_patch_before_ingress"
  if ((${#additional_user_orgs[@]})); then
    AUTHORIZED_ORG_ROLE_PAIRS="$(render_authorized_role_pairs "$ORG_ID" "${additional_user_orgs[@]}")"
  else
    AUTHORIZED_ORG_ROLE_PAIRS="$(render_authorized_role_pairs "$ORG_ID")"
  fi
  export AUTHORIZED_ORG_ROLE_PAIRS
  export FINT_CLIENT_NAME
  export FINT_CLIENT_INSTANCE_LABEL="${FINT_CLIENT_NAME}_${ns_suffix}"
  export NOVARI_KAFKA_TOPIC_ORGID="$namespace"

  template="$(choose_template "$env_path")"
  target_dir="$ROOT/kustomize/overlays/$dir"

  tmp="$(mktemp "$target_dir/.kustomization.yaml.XXXXXX")"
  envsubst '$APPLICATION_NAME $APPLICATION_PATCH_LABEL $NAMESPACE $APP_INSTANCE_LABEL $ORG_ID $KAFKA_TOPIC $URL_BASE_PATH $METRICS_PATCH_BEFORE_INGRESS $INGRESS_BASE_PATH $ARCHIVE_BASE_URL $AUTHORIZED_ORG_ROLE_PAIRS $ONEPASSWORD_ITEM_PATH $READINESS_PATH $METRICS_PATH $FINT_CLIENT_NAME $FINT_CLIENT_INSTANCE_LABEL $NOVARI_KAFKA_TOPIC_ORGID' \
    < "$template" > "$tmp"
  mv "$tmp" "$target_dir/kustomization.yaml"
done < <(find "$ROOT/kustomize/overlays" -name kustomization.yaml -print | sort)
