"""水彩素材后处理：裁水印 -> 估纸色 -> 生成 alpha -> 降采样 -> 导出 PNG。

纸色用四边中位数估计，按 Lab 距离做软阈值抠图；水彩的浅色晕染会保留半透明，
这样叠到任何纸色背景上都不会出现白底方块。
"""
import glob
import os
import numpy as np
import cv2

SRC = r"C:\Users\叶壮壮\Desktop\aimin\admin-front\src\assets\watercolor"
OUT = os.path.join(SRC, "processed")
os.makedirs(OUT, exist_ok=True)


def imread_unicode(path):
    data = np.fromfile(path, dtype=np.uint8)
    return cv2.imdecode(data, cv2.IMREAD_COLOR)


def imwrite_unicode(path, img):
    ext = os.path.splitext(path)[1]
    ok, buf = cv2.imencode(ext, img)
    if not ok:
        raise IOError("encode failed: " + path)
    buf.tofile(path)


def paper_color(img, ring=12):
    h, w = img.shape[:2]
    patches = [
        img[0:ring, :, :].reshape(-1, 3),
        img[h - ring:h, :, :].reshape(-1, 3),
        img[:, 0:ring, :].reshape(-1, 3),
        img[:, w - ring:w, :].reshape(-1, 3),
    ]
    px = np.concatenate(patches, axis=0)
    return np.median(px, axis=0)


def cut_out(img, lo=5.0, hi=16.0):
    lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB).astype(np.float32)
    paper = cv2.cvtColor(np.uint8([[paper_color(img)]]), cv2.COLOR_BGR2LAB)[0, 0].astype(np.float32)
    dist = np.sqrt(((lab - paper) ** 2).sum(axis=2))
    alpha = np.clip((dist - lo) / (hi - lo), 0, 1)
    alpha = cv2.GaussianBlur(alpha, (3, 3), 0)
    alpha = np.clip(alpha * 1.06, 0, 1)
    bgra = cv2.cvtColor(img, cv2.COLOR_BGR2BGRA)
    bgra[:, :, 3] = (alpha * 255).astype(np.uint8)
    return bgra, float(alpha.mean())


def trim_watermark(img, px=34, right=96):
    """生成图右下角有 AI 生成水印，直接裁掉底边与右侧窄条。"""
    h, w = img.shape[:2]
    return img[0:h - px, 0:w - right]


def resize_max(img, max_w):
    h, w = img.shape[:2]
    if w <= max_w:
        return img
    scale = max_w / w
    return cv2.resize(img, (int(w * scale), int(h * scale)), interpolation=cv2.INTER_AREA)


def main():
    files = sorted(glob.glob(os.path.join(SRC, "*.png")))
    for path in files:
        name = os.path.basename(path)
        img = imread_unicode(path)
        if img is None:
            print("skip", name)
            continue
        img = trim_watermark(img)
        max_w = 1500 if img.shape[1] > 1200 else 1000
        img = resize_max(img, max_w)
        out, cover = cut_out(img)
        dst = os.path.join(OUT, name.replace(".png", "-cut.png"))
        imwrite_unicode(dst, out)
        print(f"{name} -> {out.shape[1]}x{out.shape[0]} alpha={cover:.2%}")


if __name__ == "__main__":
    main()
