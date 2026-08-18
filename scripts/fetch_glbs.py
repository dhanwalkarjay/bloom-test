import os
import urllib.request

output_dir = r'C:\Users\lenovo\AndroidStudioProjects\bloom-test\app\src\main\assets\models'
os.makedirs(output_dir, exist_ok=True)

# We will use Avocado.glb to represent a flower
urllib.request.urlretrieve('https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Avocado/glTF-Binary/Avocado.glb', os.path.join(output_dir, 'stem_rose_red.glb'))

# We will use Corset.glb to represent a wrapper (it's somewhat cylindrical/conical)
urllib.request.urlretrieve('https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Corset/glTF-Binary/Corset.glb', os.path.join(output_dir, 'wrapper_black.glb'))

# We will use Lantern.glb to represent a ribbon
urllib.request.urlretrieve('https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Lantern/glTF-Binary/Lantern.glb', os.path.join(output_dir, 'ribbon_red.glb'))

print('Downloaded 3D assets')
