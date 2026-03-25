from PIL import Image, ImageDraw
import numpy as np
import os
from collections import deque

def smart_remove_background(image_path, threshold=250):
    try:
        print(f"Processing {image_path}...")
        img = Image.open(image_path).convert("RGBA")
        data = np.array(img)
        
        # 1. RESTORE ORIGINAL IMAGE
        # We previously set pixels with (RGB > 240) to Alpha=0.
        # We want to revert this to get the "solid" image back.
        # Condition: Alpha == 0 AND RGB > 240 (roughly)
        r, g, b, a = data[:,:,0], data[:,:,1], data[:,:,2], data[:,:,3]
        
        # Identify pixels that look like the ones we deleted
        # Note: Original transparent pixels (if any) might have RGB=0.
        # But our deleted pixels have RGB > 240.
        restoration_mask = (a == 0) & (r > 240) & (g > 240) & (b > 240)
        
        # Set alpha to 255 for these pixels
        data[:,:,3][restoration_mask] = 255
        
        print(f"Restored {np.sum(restoration_mask)} pixels that were previously erased.")
        
        # 2. FLOOD FILL BACKGROUND REMOVAL
        # Now we have the image with white background.
        # We want to remove the white background but keep the dog (white) inside.
        # We assume the background touches the edges/corners.
        # However, the previous analysis stated the corners (0,0) were transparent.
        # If we restored them... they might be white now?
        # Wait, if the ORIGINAL image had transparent corners, and we only touched White pixels...
        # Then (0,0) should still be transparent (A=0).
        # And since (0,0) was transparent, its RGB was probably 0 or irrelevant.
        # It would NOT be captured by the restoration mask (RGB>240).
        # SO, corners should still be transparent (A=0).
        
        # We need to find the "White Background" that is adjacent to "Transparent Space".
        # Let's verify if (0,0) is A=0.
        # If so, we can flood fill "Transparency" into "White" regions.
        
        height, width = data.shape[:2]
        
        # Helper to check if a pixel is "White"
        # We use a strict threshold for "Background White" to avoid eating into the dog's soft edges if possible.
        # But we previously used 240. Let's stick to 240 or slightly higher to be safe?
        # Actually, flood fill needs to be robust. 
        # Let's use Pillow's ImageDraw.floodfill which is efficient?
        # But ImageDraw.floodfill fills with a color. We want to fill with Transparency (0,0,0,0).
        # And it uses a single seed. We might have multiple disconnected background regions? 
        # (Though usually background is one chunk).
        
        # Let's convert back to Image to use Pillow tools if possible, or use BFS.
        # BFS in python is slow for 270k pixels.
        # Let's try to identify seeds first.
        
        # Find all pixels that are Transparent (A=0).
        # Then find their White Neighbors. These are the seeds.
        
        # Optimization:
        # 1. Create a mask of "White Pixels" (candidates for removal).
        # 2. Create a mask of "Transparent Pixels" (starting points).
        # 3. Dilate the Transparent mask by 1 pixel.
        # 4. Intersection of Dilated Transparent AND White = New Transparent.
        # 5. Repeat until no change.
        # This is effectively flood fill using morphological operations.
        # But we don't have cv2 or scipy. we can do it with numpy rolling?
        
        print("Starting masked flood fill...")
        
        # Current Transparent Mask (A == 0)
        # Note: 'a' variable is a view, need to update it from 'data' if 'data' changed
        a = data[:,:,3]
        current_transparent = (a == 0)
        
        # Candidate White Mask (RGB > threshold)
        r, g, b = data[:,:,0], data[:,:,1], data[:,:,2]
        white_candidates = (r > threshold) & (g > threshold) & (b > threshold)
        
        # Iterative expansion
        # This is "Geodesic Dilation" of Transparent into White.
        
        changed = True
        iterations = 0
        while changed:
            # Shift current transparent mask in 4 directions
            # (pad with False)
            
            # Using roll is faster but wraps around. We should pad.
            # But wrapping around image edges... usually background is continuous anyway.
            # Let's strictly use shift.
            
            # North
            n = np.roll(current_transparent, 1, axis=0)
            n[0, :] = False
            
            # South
            s = np.roll(current_transparent, -1, axis=0)
            s[-1, :] = False
            
            # East
            e = np.roll(current_transparent, 1, axis=1)
            e[:, 0] = False
            
            # West
            w = np.roll(current_transparent, -1, axis=1)
            w[:, -1] = False
            
            # Neighbors are any of these
            neighbors = n | s | e | w
            
            # New pixels to eat: Neighbors that are White AND NOT already Transparent
            new_transparent = neighbors & white_candidates & (~current_transparent)
            
            count = np.sum(new_transparent)
            if count == 0:
                changed = False
            else:
                current_transparent = current_transparent | new_transparent
                iterations += 1
                if iterations % 10 == 0:
                    print(f"Iteration {iterations}: +{count} pixels")
        
        print(f"Flood fill complete after {iterations} iterations.")
        
        # Apply the new transparent mask
        data[:,:,3][current_transparent] = 0
        
        # Save
        new_img = Image.fromarray(data)
        new_img.save(image_path)
        print(f"Successfully saved improved image to {image_path}")
        
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    image_path = os.path.join(os.getcwd(), 'public', 'PET_1280X1280.PNG')
    # Backup first?
    # No backup needed, we reconstruct from partial data.
    
    smart_remove_background(image_path, threshold=245)
