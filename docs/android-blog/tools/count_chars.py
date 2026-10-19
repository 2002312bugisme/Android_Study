#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android 博客章节字数统计

用法：
    python count_chars.py chapters/07-drawable.md
    python count_chars.py chapters/                 # 统计目录下全部 .md
    python count_chars.py chapters/*.md --min 6000 --max 7000
    python count_chars.py chapters/07-drawable.md --detail   # 显示口径说明

统计口径（与 docs/android-blog/README.md 一致）：

    A. 汉字数         仅 CJK 汉字（\\u4e00-\\u9fff、\\u3400-\\u4dbf）
    B. 正文可见字符   汉字 + 中英文可见字符；
                      排除代码围栏、行内代码、Markdown 标记、空白
                      ← 主验收口径
    C. 全文可见字符   在 B 的基础上，包含真实代码块内的可见字符

说明：
    * 标题、表格、列表中的可见文字计入 A/B/C。
    * 链接只计锚文本，不计 URL；图片的替代文本不计入。
    * mermaid 代码块按代码块处理（不计入 B）。
"""

import argparse
import os
import re
import sys
from typing import Dict, List, Tuple

# Windows 控制台默认可能是 GBK，强制 UTF-8 输出，避免中文乱码
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")  # type: ignore[attr-defined]
    except Exception:
        pass

# ---------------------------------------------------------------- 正则定义

# ```lang ... ``` 代码围栏（含 mermaid），非贪婪跨行
FENCED_CODE = re.compile(r"^[ \t]*(```|~~~)[^\n]*\n.*?^[ \t]*\1[ \t]*$",
                         re.MULTILINE | re.DOTALL)

# 行内代码 `xxx`
INLINE_CODE = re.compile(r"`+[^`\n]*`+")

# HTML 标签 / 注释
HTML_TAG = re.compile(r"<!--.*?-->", re.DOTALL)
HTML_ANY = re.compile(r"<[^>\n]+>")

# 图片：整体丢弃（替代文本不计入）
IMAGE = re.compile(r"!\[[^\]]*\]\([^)]*\)")

# 链接：保留锚文本
LINK = re.compile(r"\[([^\]]*)\]\([^)]*\)")

# 引用式链接定义  [id]: url
LINK_DEF = re.compile(r"^\s*\[[^\]]+\]:\s*\S+.*$", re.MULTILINE)

# 表格分隔行 |---|---|
# 注意：这里必须用 [ \t] 而不是 \s。
# 在 MULTILINE|DOTALL 下，\s 能匹配换行，会导致正则跨行吞掉后续正文。
TABLE_SEP = re.compile(r"^[ \t]*\|?[ \t:|-]+\|[ \t:|-]*$", re.MULTILINE)

# 水平分隔线
HRULE = re.compile(r"^[ \t]*([-*_])([ \t]*\1){2,}[ \t]*$", re.MULTILINE)

# 行首结构标记：标题 #、引用 >、列表 - * + 、有序列表 1.
LEAD_MARKER = re.compile(r"^[ \t]*(#{1,6}[ \t]*|>[ \t]*|[-*+][ \t]+|\d+\.[ \t]+)",
                         re.MULTILINE)

# 强调标记 ** __ * _ ~~
EMPHASIS = re.compile(r"(\*{1,3}|_{1,3}|~~)")

# 表格竖线
PIPE = re.compile(r"\|")

# 汉字
CJK = re.compile(r"[\u4e00-\u9fff\u3400-\u4dbf]")


def strip_ws(text: str) -> str:
    """去掉全部空白字符"""
    return re.sub(r"\s+", "", text)


def cut_code_blocks(text: str) -> Tuple[str, str]:
    """返回 (去掉代码块的正文, 拼接起来的代码块内容)"""
    blocks: List[str] = []

    def _grab(m: "re.Match[str]") -> str:
        body = m.group(0)
        # 去掉首尾的围栏行
        lines = body.split("\n")
        if len(lines) >= 2:
            body = "\n".join(lines[1:-1])
        blocks.append(body)
        return "\n"  # 用换行占位，避免前后文粘连

    prose = FENCED_CODE.sub(_grab, text)
    return prose, "\n".join(blocks)


def clean_markdown(prose: str) -> str:
    """把 Markdown 正文清理成"人类实际会读到的文字" """
    s = prose
    s = HTML_TAG.sub("", s)          # HTML 注释
    s = IMAGE.sub("", s)             # 图片（连替代文本一起丢弃）
    s = LINK.sub(r"\1", s)           # 链接只留锚文本
    s = LINK_DEF.sub("", s)          # 引用式链接定义
    s = TABLE_SEP.sub("", s)         # 表格分隔行
    s = HRULE.sub("", s)             # 水平线
    s = INLINE_CODE.sub("", s)       # 行内代码
    s = LEAD_MARKER.sub("", s)       # 行首 # > - * 1.
    s = EMPHASIS.sub("", s)          # ** __ * _ ~~
    s = PIPE.sub(" ", s)             # 表格竖线变成空格（分隔单元格用）
    s = HTML_ANY.sub("", s)          # 残留 HTML 标签
    return s


def analyze(path: str) -> Dict[str, int]:
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        raw = f.read()

    prose_raw, code_raw = cut_code_blocks(raw)

    prose_clean = strip_ws(clean_markdown(prose_raw))
    code_clean = strip_ws(clean_markdown(code_raw))

    hanzi = len(CJK.findall(prose_clean))

    return {
        "hanzi": hanzi,                       # 口径 A
        "body_visible": len(prose_clean),     # 口径 B（主口径）
        "all_visible": len(prose_clean) + len(code_clean),  # 口径 C
        "code_visible": len(code_clean),
        "body_bytes": len(raw.encode("utf-8")),
    }


def evaluate(value: int, lo: int, hi: int) -> str:
    if value < lo:
        gap = lo - value
        return f"⚠️  偏少 {gap}"
    if value > hi:
        over = value - hi
        return f"⚠️  偏多 {over}"
    return "✅ 达标"


def visual_width(s: str) -> int:
    """中文按 2 列宽估算，用于粗略对齐表格"""
    w = 0
    for ch in s:
        w += 2 if ord(ch) > 0x2E7F else 1
    return w


def pad(s: str, width: int, align: str = "left") -> str:
    gap = max(0, width - visual_width(s))
    if align == "right":
        return " " * gap + s
    return s + " " * gap


def collect(targets: List[str]) -> List[str]:
    files: List[str] = []
    for t in targets:
        if os.path.isdir(t):
            for name in sorted(os.listdir(t)):
                if name.lower().endswith((".md", ".markdown")):
                    files.append(os.path.join(t, name))
        elif os.path.isfile(t):
            files.append(t)
        else:
            print(f"[跳过] 找不到：{t}", file=sys.stderr)
    return files


def main() -> int:
    ap = argparse.ArgumentParser(
        description="Android 博客章节字数统计（口径：正文可见字符）",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    ap.add_argument("paths", nargs="+", help="Markdown 文件或目录")
    ap.add_argument("--min", type=int, default=6000, help="目标下限（默认 6000）")
    ap.add_argument("--max", type=int, default=7000, help="目标上限（默认 7000）")
    ap.add_argument("--detail", action="store_true", help="显示口径说明")
    args = ap.parse_args()

    files = collect(args.paths)
    if not files:
        print("没有找到可统计的 Markdown 文件。", file=sys.stderr)
        return 1

    if args.detail:
        print("口径 A = 仅汉字")
        print("口径 B = 正文可见字符（排除代码块/行内代码/Markdown 标记/空白）← 主验收口径")
        print("口径 C = 全文可见字符（含代码块）")
        print()

    header = [
        ("文件", 34, "left"),
        ("A 汉字", 9, "right"),
        ("B 正文", 9, "right"),
        ("C 全文", 9, "right"),
        ("代码", 8, "right"),
        ("状态", 12, "left"),
    ]
    line = "  ".join(pad(h, w, a) for h, w, a in header)
    print(line)
    print("-" * visual_width(line))

    total = {"hanzi": 0, "body_visible": 0, "all_visible": 0, "code_visible": 0}
    rows = []

    for path in files:
        r = analyze(path)
        rows.append((path, r))
        for k in total:
            total[k] += r[k]

    name_width = max(34, max((visual_width(os.path.basename(p)) + 2 for p, _ in rows), default=34))

    for path, r in rows:
        name = os.path.basename(path)
        status = evaluate(r["body_visible"], args.min, args.max)
        print("  ".join([
            pad(name, name_width, "left"),
            pad(str(r["hanzi"]), 9, "right"),
            pad(str(r["body_visible"]), 9, "right"),
            pad(str(r["all_visible"]), 9, "right"),
            pad(str(r["code_visible"]), 8, "right"),
            status,
        ]))

    if len(rows) > 1:
        print("-" * visual_width(line))
        print("  ".join([
            pad(f"合计（{len(rows)} 个文件）", name_width, "left"),
            pad(str(total["hanzi"]), 9, "right"),
            pad(str(total["body_visible"]), 9, "right"),
            pad(str(total["all_visible"]), 9, "right"),
            pad(str(total["code_visible"]), 8, "right"),
            "",
        ]))

    print()
    print(f"目标区间：{args.min} ~ {args.max}（口径 B 正文可见字符）")
    if len(rows) == 1:
        r = rows[0][1]
        ratio = r["all_visible"] / r["body_visible"] if r["body_visible"] else 0
        print(f"该章：正文 {r['body_visible']} / 汉字 {r['hanzi']} / "
              f"含代码 {r['all_visible']}（代码是正文的 {ratio:.2f} 倍）")

    return 0


if __name__ == "__main__":
    sys.exit(main())
