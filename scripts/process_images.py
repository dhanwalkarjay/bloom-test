import os
from PIL import Image

artifacts_dir = r'C:\Users\lenovo\.gemini\antigravity-ide\brain\72daa1be-b9da-4cd2-8861-74cd5497b799'
output_dir = r'C:\Users\lenovo\AndroidStudioProjects\bloom-test\app\src\main\res\drawable-nodpi'
os.makedirs(output_dir, exist_ok=True)

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
roses = glob.glob(os.path.join(artifacts_dir, 'rose_generated_*.png'))
peonies = glob.glob(os.path.join(artifacts_dir, 'peony_generated_*.png'))
eucs = glob.glob(os.path.join(artifacts_dir, 'eucalyptus_generated_*.png'))

if roses:
    remove_white_bg(roses[0], os.path.join(output_dir, 'stem_rose_red.png'))
if peonies:
    remove_white_bg(peonies[0], os.path.join(output_dir, 'stem_peony_white.png'))
if eucs:
    remove_white_bg(eucs[0], os.path.join(output_dir, 'filler_eucalyptus.png'))

print('Done.')
