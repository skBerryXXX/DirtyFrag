#!/system/bin/sh
MODEL=$(getprop ro.product.model)
BRAND=$(getprop ro.product.brand)
PATCH=$(getprop ro.build.version.security_patch)
KVER=$(uname -r)
ANDROID=$(getprop ro.build.version.release)
ONEUI=$(getprop ro.build.version.oneui)
KM=$(echo "$KVER" | cut -d. -f1,2)
KMI=""
case "$KM" in
  5.10) KMI="android${ANDROID}-5.10";;
  5.15) KMI="android${ANDROID}-5.15";;
  6.1)  KMI="android14-6.1";;
  6.6)  KMI="android15-6.6";;
  6.12) KMI="android16-6.12";;
  6.18) KMI="android17-6.18";;
esac
SUPPORTED="android12-5.10 android13-5.10 android13-5.15 android14-5.15 android14-6.1 android15-6.6 android16-6.12 android17-6.18"
echo "model=$MODEL brand=$BRAND oneui=$ONEUI"
echo "patch=$PATCH kernel=$KVER"
echo "detected KMI=$KMI"
case " $SUPPORTED " in *" $KMI "*) ;; *) echo "VERDICT: unsupported KMI"; exit 1;; esac
if [ "$BRAND" != "samsung" ]; then echo "VERDICT: not Samsung"; exit 1; fi
if [ "$PATCH" \> "2026-10-31" ]; then echo "VERDICT: security patch closes CVE-2026-43284"; exit 1; fi
echo "VERDICT: likely vulnerable, KMI=$KMI"
