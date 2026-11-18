#!/system/bin/sh
SUPPORTED="android12-5.10 android13-5.10 android13-5.15 android14-5.15 android14-6.1 android15-6.6 android16-6.12 android17-6.18"
MODEL=$(getprop ro.product.model)
BRAND=$(getprop ro.product.brand)
PATCH=$(getprop ro.build.version.security_patch)
KVER=$(uname -r)
ONEUI=$(getprop ro.build.version.oneui)
KMI=""
for k in $SUPPORTED; do
  case "$KVER" in *"$k"*) KMI=$k;; esac
done
echo "model=$MODEL brand=$BRAND oneui=$ONEUI"
echo "patch=$PATCH kernel=$KVER"
if [ "$BRAND" != "samsung" ]; then echo "VERDICT: not Samsung"; exit 1; fi
if [ -z "$KMI" ]; then echo "VERDICT: unsupported KMI"; exit 1; fi
if [ "$PATCH" \> "2026-10-31" ]; then echo "VERDICT: security patch closes CVE-2026-43284"; exit 1; fi
echo "VERDICT: likely vulnerable, KMI=$KMI"
