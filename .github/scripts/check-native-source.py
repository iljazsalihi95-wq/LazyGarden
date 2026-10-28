"""Fail early if an editor publishes a placeholder instead of Kotlin source."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[2]
source = root / 'app/src/main/java'
for path in source.rglob('*.kt'):
    if path.read_text().strip() in {'', '...', '…'}:
        raise SystemExit(f'Invalid Kotlin placeholder: {path.relative_to(root)}')

build = (root / 'app/build.gradle.kts').read_text()
namespace = re.search(r'namespace\s*=\s*"([^"]+)"', build).group(1)
manifest = ET.parse(root / 'app/src/main/AndroidManifest.xml')
android = '{http://schemas.android.com/apk/res/android}'
for activity in manifest.findall('./application/activity'):
    if not any(item.get(android + 'name') == 'android.intent.action.MAIN'
               for item in activity.findall('./intent-filter/action')):
        continue
    name = activity.get(android + 'name')
    qualified = namespace + name if name.startswith('.') else name
    path = source / (qualified.replace('.', '/') + '.kt')
    if not path.is_file():
        raise SystemExit(f'Missing native launcher source: {qualified}')
print('Native launcher source and Kotlin source integrity: OK')
