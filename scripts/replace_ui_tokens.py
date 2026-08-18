import re
import os

files = [
    r"app\src\main\res\layout\activity_help_center.xml",
    r"app\src\main\res\layout\activity_product_detail.xml",
    r"app\src\main\res\layout\activity_manual_location.xml",
    r"app\src\main\res\layout\activity_add_occasion.xml"
]

color_map = {
    "@color/home_background": "?attr/colorBackground",
    "@color/home_primary": "?attr/colorPrimary",
    "@color/home_on_surface": "?attr/colorOnSurface",
    "@color/home_on_surface_variant": "?attr/colorOnSurfaceVariant",
    "@color/cart_surface": "?attr/colorSurface",
    "@color/cart_surface_container": "?attr/colorSurfaceVariant",
    "@color/cart_on_surface": "?attr/colorOnSurface",
    "@color/cart_on_surface_variant": "?attr/colorOnSurfaceVariant",
    "@color/cart_primary": "?attr/colorPrimary",
    "@color/cart_outline": "?attr/colorOutline",
    "@color/cart_outline_variant": "?attr/colorOutlineVariant",
    "@color/cart_background": "?attr/colorBackground",
    "@color/bloom_background": "?attr/colorBackground",
    "@color/bloom_primary": "?attr/colorPrimary",
    "@color/bloom_secondary": "?attr/colorSecondary",
    "@color/search_surface": "?attr/colorSurface",
    "@color/search_on_surface": "?attr/colorOnSurface",
    "@color/search_primary": "?attr/colorPrimary",
    "@color/search_outline": "?attr/colorOutline",
    "@color/search_outline_variant": "?attr/colorOutlineVariant",
    "@color/search_on_surface_variant": "?attr/colorOnSurfaceVariant",
    "#FFF8F7": "?attr/colorBackground",
    "#FEE9EE": "?attr/colorSurface",
    "#AF314A": "?attr/colorPrimary",
    "#FFFFFF": "?attr/colorSurface",
    "#333333": "?attr/colorOnSurface",
    "#999999": "?attr/colorOnSurfaceVariant",
    "@color/home_lux_dark": "?attr/colorPrimary",
    "@color/white": "?attr/colorSurface",
    "@android:color/white": "?attr/colorSurface"
}

dimen_map = {
    "2dp": "@dimen/spacing_xxs",
    "4dp": "@dimen/spacing_xs",
    "8dp": "@dimen/spacing_s",
    "12dp": "@dimen/spacing_s", 
    "16dp": "@dimen/spacing_m",
    "20dp": "@dimen/spacing_m",
    "24dp": "@dimen/spacing_l",
    "32dp": "@dimen/spacing_xl",
    "48dp": "@dimen/spacing_xxl",
    "56dp": "@dimen/button_height",
    "64dp": "@dimen/spacing_xxxl",
    "@dimen/_8sdp": "@dimen/spacing_s",
    "@dimen/_12sdp": "@dimen/spacing_s",
    "@dimen/_16sdp": "@dimen/spacing_m",
    "@dimen/_24sdp": "@dimen/spacing_l",
    "@dimen/_32sdp": "@dimen/spacing_xl",
    "44dp": "@dimen/button_height_small",
}

def replace_dimens(content):
    for old, new in dimen_map.items():
        content = re.sub(f'"{old}"', f'"{new}"', content)
    return content

def replace_colors(content):
    sorted_keys = sorted(color_map.keys(), key=len, reverse=True)
    for old in sorted_keys:
        new = color_map[old]
        content = content.replace(old, new)
    return content

for file_path in files:
    full_path = os.path.join(r"C:\Users\lenovo\AndroidStudioProjects\bloom-test", file_path)
    if not os.path.exists(full_path):
        print(f"Skipping {file_path}")
        continue
        
    with open(full_path, "r", encoding="utf-8") as f:
        content = f.read()
        
    content = replace_colors(content)
    content = replace_dimens(content)
    
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)
        
    print(f"Processed {file_path}")
