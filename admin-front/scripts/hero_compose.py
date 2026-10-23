"""把全身图谱合成到页面纸色上，输出 hero.webp。

做法与 watercolor.py 的 alpha 抠图不同：这里不做透明通道，而是把纸底直接
替换成页面的 --paper 色，这样图片叠上去不会出现色差方块；边缘再交给 CSS 羽化。
"""
import os
import numpy as np
import cv2

SRC = r"C:\Users\叶壮壮\Desktop\aimin\admin-front\src\assets\watercolor\Watercolor_anatomical_atlas_of_2026-10-23T12-34-25.png"
OUT = r"C:\Users\叶壮壮\Desktop\aimin\admin-front\src\assets\watercolor\processed\hero.webp"

PAPER_RGB = (236, 226, 207)  # --paper #ece2cf


def imread_unicode(path):
    return cv2.imdecode(np.fromfile(path, dtype=np.uint8), cv2.IMREAD_COLOR)


def main():
    img = imread_unicode(SRC)
    h, w = img.shape[:2]
    img = img[0 : h - 34, 0 : w - 96]  # 裁掉右下角水印

    # 轻微提对比与饱和，避免在深纸色上发灰
    f = img.astype(np.float32) / 255.0
    f = np.clip((f - 0.5) * 1.06 + 0.5, 0, 1)
    hsv = cv2.cvtColor((f * 255).astype(np.uint8), cv2.COLOR_BGR2HSV).astype(np.float32)
    hsv[..., 1] = np.clip(hsv[..., 1] * 1.08, 0, 255)
    img = cv2.cvtColor(hsv.astype(np.uint8), cv2.COLOR_HSV2BGR)

    target = np.array(PAPER_RGB[::-1], dtype=np.float32)  # BGR

    # 抠图基准必须取「原图纸色」，而不是目标纸色；用四边中位数估计
    ring = 10
    borders = np.concatenate([
        img[0:ring, :, :].reshape(-1, 3),
        img[-ring:, :, :].reshape(-1, 3),
        img[:, 0:ring, :].reshape(-1, 3),
        img[:, -ring:, :].reshape(-1, 3),
    ], axis=0)
    src_paper = np.median(borders, axis=0)

    lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB).astype(np.float32)
    src_lab = cv2.cvtColor(np.uint8([[src_paper]]), cv2.COLOR_BGR2LAB)[0, 0].astype(np.float32)
    dist = np.sqrt(((lab - src_lab) ** 2).sum(axis=2))
    alpha = np.clip((dist - 3.0) / (13.0 - 3.0), 0, 1)
    alpha = cv2.GaussianBlur(alpha, (3, 3), 0)[..., None]

    out = img.astype(np.float32) * alpha + target * (1 - alpha)
    out = np.clip(out, 0, 255).astype(np.uint8)

    hh, ww = out.shape[:2]
    scale = min(1.0, 980 / ww)
    if scale < 1:
        out = cv2.resize(out, (int(ww * scale), int(hh * scale)), interpolation=cv2.INTER_AREA)

    ok, buf = cv2.imencode(".webp", out, [cv2.IMWRITE_WEBP_QUALITY, 88])
    buf.tofile(OUT)
    print("saved", OUT, out.shape[1], "x", out.shape[0], round(len(buf) / 1024), "KB")


if __name__ == "__main__":
    main()
