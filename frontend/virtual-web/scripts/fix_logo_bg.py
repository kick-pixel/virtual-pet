from PIL import Image
import numpy as np
import os

def remove_white_background(image_path, threshold=240):
    try:
        print(f"Processing {image_path}...")
        if not os.path.exists(image_path):
            print(f"Error: File not found at {image_path}")
            return

        img = Image.open(image_path).convert("RGBA")
        data = np.array(img)

        # Get RGB channels
        r, g, b, a = data[:, :, 0], data[:, :, 1], data[:, :, 2], data[:, :, 3]

        # Identify white or near-white pixels
        # Condition: R > threshold AND G > threshold AND B > threshold
        mask = (r > threshold) & (g > threshold) & (b > threshold)

        # Count pixels to be modified
        pixels_to_change = np.sum(mask)
        print(f"Found {pixels_to_change} pixels to make transparent.")

        # Set alpha to 0 for these pixels
        data[:, :, 3][mask] = 0

        # Create new image
        new_img = Image.fromarray(data)
        new_img.save(image_path)
        print(f"Successfully processed and saved {image_path}")
        
    except Exception as e:
        print(f"Error processing image: {e}")

if __name__ == "__main__":
    # Use absolute path to be safe
    image_path = os.path.join(os.getcwd(), 'public', 'PET_1280X1280.PNG')
    # If script is run from scripts folder, adjust path
    if not os.path.exists(image_path):
         image_path = os.path.join(os.getcwd(), '..', 'public', 'PET_1280X1280.PNG')

    print(f"Target image path: {image_path}")
    remove_white_background(image_path)
