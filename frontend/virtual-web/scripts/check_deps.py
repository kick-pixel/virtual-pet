import sys
try:
    import numpy as np
    from PIL import Image, ImageDraw
    print("PIL and numpy imported successfully")
except ImportError as e:
    print(f"Import failed: {e}")
