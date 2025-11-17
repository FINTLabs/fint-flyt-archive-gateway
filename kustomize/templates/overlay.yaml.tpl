apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: $NAMESPACE

resources:
  - ../../../base

labels:
  - pairs:
      app.kubernetes.io/instance: $APP_INSTANCE_LABEL
      fintlabs.no/org-id: $ORG_ID

patches:
  - patch: |-
      - op: replace
        path: "/metadata/labels/app.kubernetes.io~1instance"
        value: "$APPLICATION_PATCH_LABEL"
      - op: replace
        path: "/spec/kafka/acls/0/topic"
        value: "$KAFKA_TOPIC"
      - op: replace
        path: "/spec/orgId"
        value: "$ORG_ID"
      - op: replace
        path: "/spec/url/basePath"
        value: "$URL_BASE_PATH"$METRICS_PATCH_BEFORE_INGRESS
      - op: replace
        path: "/spec/ingress/basePath"
        value: "$INGRESS_BASE_PATH"
      - op: add
        path: "/spec/env/-"
        value:
          name: "novari.flyt.archive.gateway.client.fint-archive.base-url"
          value: "$ARCHIVE_BASE_URL"
      - op: add
        path: "/spec/env/-"
        value:
          name: "novari.flyt.resource-server.security.api.internal.authorized-org-id-role-pairs-json"
          value: |
$AUTHORIZED_ORG_ROLE_PAIRS
      - op: add
        path: "/spec/env/-"
        value:
          name: "novari.kafka.topic.org-id"
          value: "$NOVARI_KAFKA_TOPIC_ORGID"
      - op: replace
        path: "/spec/onePassword/itemPath"
        value: "$ONEPASSWORD_ITEM_PATH"
      - op: replace
        path: "/spec/probes/readiness/path"
        value: "$READINESS_PATH"
      - op: replace
        path: "/spec/observability/metrics/path"
        value: "$METRICS_PATH"
    target:
      kind: Application
      name: $APPLICATION_NAME

  - patch: |-
      - op: replace
        path: "/metadata/labels/app.kubernetes.io~1instance"
        value: "$FINT_CLIENT_INSTANCE_LABEL"
      - op: replace
        path: "/spec/orgId"
        value: "$ORG_ID"
    target:
      kind: FintClient
      name: $FINT_CLIENT_NAME
