import os
from PIL import Image

artifacts_dir = r'C:\Users\lenovo\.gemini\antigravity-ide\brain\72daa1be-b9da-4cd2-8861-74cd5497b799'
output_dir = r'C:\Users\lenovo\AndroidStudioProjects\bloom-test\app\src\main\res\drawable-nodpi'

def remove_white_bg(input_path, output_path):
    print(f'Processing {input_path}...')
    img = Image.open(input_path).convert("RGBA")
    datas = img.getdata()
    
    newData = []
    # Tolerance for 'white' (255,255,255). We will consider anything > 240 as background.
    for item in datas:
        if item[0] > 240 and item[1] > 240 and item[2] > 240:
            newData.append((255, 255, 255, 0)) # transparent
        else:
            newData.append(item)
            
    img.putdata(newData)
    
    # Optional: crop to bounding box
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    # Resize slightly so they aren't huge (e.g. max 512x512)
    img.thumbnail((512, 512), Image.Resampling.LANCZOS)
    
    img.save(output_path, "PNG")
    print(f'Saved {output_path}')

# Find the generated images
import glob
wrappers = glob.glob(os.path.join(artifacts_dir, 'wrapper_black_*.png'))
ribbons = glob.glob(os.path.join(artifacts_dir, 'ribbon_red_*.png'))

if wrappers:
    remove_white_bg(wrappers[0], os.path.join(output_dir, 'wrapper_black.png'))
if ribbons:
    remove_white_bg(ribbons[0], os.path.join(output_dir, 'ribbon_red.png'))

print('Done.')
