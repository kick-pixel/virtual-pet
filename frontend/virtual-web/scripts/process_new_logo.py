from PIL import Image, ImageDraw
import numpy as np
import os

def process_new_logo(input_path, output_path, threshold=240):
    try:
        print(f"Loading {input_path}...")
        img = Image.open(input_path).convert("RGBA")
        data = np.array(img)
        
        # 1. FLOOD FILL BACKGROUND REMOVAL
        # We assume the background (0,0) is white and we want it gone.
        
        # Current Transparent Mask (Starts empty)
        current_transparent = np.zeros(data.shape[:2], dtype=bool)
        
        # Candidate White Mask (RGB > threshold)
        r, g, b = data[:,:,0], data[:,:,1], data[:,:,2]
        white_candidates = (r > threshold) & (g > threshold) & (b > threshold)
        
        # Check if corners are white (seeds)
        seeds = []
        h, w = data.shape[:2]
        corners = [(0,0), (0, w-1), (h-1, 0), (h-1, w-1)]
        
        # Initialize queue for BFS/Flood Fill
        stack = []
        visited = np.zeros(data.shape[:2], dtype=bool)
        
        for r_idx, c_idx in corners:
            if white_candidates[r_idx, c_idx]:
                stack.append((r_idx, c_idx))
                visited[r_idx, c_idx] = True
                
        print(f"Found {len(stack)} corner seeds for flood fill.")
        
        # Perform Flood Fill (Custom BFS)
        # Using numpy shifting is faster for python
        
        # Mask of "Pending Transparent" starts with seeds
        current_transparent[tuple(zip(*stack))] = True if stack else False
        
        if not stack:
            print("Warning: Corners are not white? Checking average color...")
            avg_color = np.mean(data[0,0])
            print(f"Pixel (0,0): {data[0,0]}")
            # If corners aren't detected as white > 240, maybe lower threshold?
            if data[0,0,0] > 200:
                print("Lowering threshold for corners...")
                white_candidates = (r > 200) & (g > 200) & (b > 200)
                # Retry seeds
                for r_idx, c_idx in corners:
                    if white_candidates[r_idx, c_idx]:
                        stack.append((r_idx, c_idx))
                        current_transparent[r_idx, c_idx] = True
        
        if not stack:
             print("Error: Could not find white background starting from corners.")
             return

        # Iterative expansion (Morphological Dilation constrained to White Mask)
        print("Starting flood fill...")
        iterations = 0
        changed = True
        
        while changed:
            # Shift current transparent mask in 4 directions
            n = np.roll(current_transparent, 1, axis=0); n[0, :] = False
            s = np.roll(current_transparent, -1, axis=0); s[-1, :] = False
            e = np.roll(current_transparent, 1, axis=1); e[:, 0] = False
            w = np.roll(current_transparent, -1, axis=1); w[:, -1] = False
            
            neighbors = n | s | e | w
            
            # New pixels: Neighbors that are White AND NOT already processed
            new_transparent = neighbors & white_candidates & (~current_transparent)
            
            count = np.sum(new_transparent)
            if count == 0:
                changed = False
            else:
                current_transparent = current_transparent | new_transparent
                iterations += 1
                if iterations % 10 == 0:
                    print(f"Iteration {iterations}: +{count} pixels")
        
        print(f"Flood fill complete. Removed {np.sum(current_transparent)} pixels.")
        
        # Apply mask
        data[:,:,3][current_transparent] = 0
        
        # Save
        new_img = Image.fromarray(data)
        new_img.save(output_path)
        print(f"Saved processed image to {output_path}")

    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    base_dir = os.getcwd()
    # Check if we are in 'scripts' or 'virtual-web'
    if base_dir.endswith('scripts'):
         base_dir = os.path.dirname(base_dir)
         
    input_path = os.path.join(base_dir, 'public', 'image.png')
    output_path = os.path.join(base_dir, 'public', 'PET_1280X1280.PNG')
     
    process_new_logo(input_path, output_path)
