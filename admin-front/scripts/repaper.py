"""把已合成的图片重新映射到新的纸色。

原理：用四边中位数估出图内纸色 -> 按 Lab 距离生成 alpha -> 合成到目标纸色。
只改底色不改笔触，适合整体换纸色时批量处理。
用法：python repaper.py <目标十六进制色>
"""
import glob
import os
import shutil
import sys

import cv2
import numpy as np

DIR = r"C:\Users\叶壮壮\Desktop\aimin\admin-front\src\assets\watercolor\processed"
TARGET = sys.argv[1] if len(sys.argv) > 1 else "#e9eee8"
T = tuple(int(TARGET[i:i + 2], 16) for i in (1, 3, 5))  # RGB
T_BGR = np.array(T[::-1], dtype=np.float32)


def read(path):
    return cv2.imdecode(np.fromfile(path, dtype=np.uint8), cv2.IMREAD_COLOR)


def write(path, img):
    ok, buf = cv2.imencode(".webp", img, [cv2.IMWRITE_WEBP_QUALITY, 90])
    buf.tofile(path)


def repaper(path):
    img = read(path)
    h, w = img.shape[:2]
    ring = 8
    border = np.concatenate([
        img[0:ring, :, :].reshape(-1, 3),
        img[-ring:, :, :].reshape(-1, 3),
        img[:, 0:ring, :].reshape(-1, 3),
        img[:, -ring:, :].reshape(-1, 3),
    ], axis=0)
    src_paper = np.median(border, axis=0)
    src_lab = cv2.cvtColor(np.uint8([[src_paper]]), cv2.COLOR_BGR2LAB)[0, 0].astype(np.float32)
    lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB).astype(np.float32)
    dist = np.sqrt(((lab - src_lab) ** 2).sum(axis=2))
    alpha = np.clip((dist - 3.0) / (12.0 - 3.0), 0, 1)
    alpha = cv2.GaussianBlur(alpha, (3, 3), 0)[..., None]
    out = img.astype(np.float32) * alpha + T_BGR * (1 - alpha)
    out = np.clip(out, 0, 255).astype(np.uint8)
    shutil.copyfile(path, path + ".bak")
    write(path, out)
    print("repaper", os.path.basename(path), "->", TARGET)


for f in ["hero.webp", "hero-bust.webp"]:
    p = os.path.join(DIR, f)
    if os.path.exists(p):
        repaper(p)
