# Reproducible debug APK signing

`lazygarden-debug.keystore` is a public, test-only Android debug key, with the standard Android debug alias/password. It is deliberately not a release credential. All CI and local debug APKs from 0.10.0 onward use this same certificate, so later debug updates can retain application data. Earlier workflows generated a fresh ephemeral key each run; this new key cannot sign as those lost keys. Production publishing must use a separate privately managed release key.
