import os
import glob

fixes = {
    "?attr/colorOnSurface_variant": "?attr/colorOnSurfaceVariant",
    "?attr/colorSurface_container": "?attr/colorSurfaceVariant",
    "?attr/colorOutline_variant": "?attr/colorOutlineVariant",
    "?attr/colorBackground": "?android:attr/colorBackground"
}

layout_dir = r"C:\Users\lenovo\AndroidStudioProjects\bloom-test\app\src\main\res\layout"
for filepath in glob.glob(os.path.join(layout_dir, "*.xml")):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
        
    original = content
    for old, new in fixes.items():
        content = content.replace(old, new)
        
    if content != original:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Fixed {os.path.basename(filepath)}")
