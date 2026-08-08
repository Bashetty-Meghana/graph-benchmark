import os

os.makedirs("results/charts", exist_ok=True)

databases = ['CognoDB Cloud', 'Neo4j AuraDB', 'FalkorDB', 'ArangoDB', 'Memgraph']
colors = ['#1a73e8', '#34a853', '#fbbc04', '#ea4335', '#9334e6']

def create_svg_bar(title, filename, categories, values, unit, max_val=None):
    if not max_val:
        max_val = max(values) * 1.15
    svg_width = 800
    svg_height = 400
    margin_top = 60
    margin_bottom = 60
    margin_left = 180
    margin_right = 40

    chart_w = svg_width - margin_left - margin_right
    chart_h = svg_height - margin_top - margin_bottom

    bars_svg = ""
    bar_height = chart_h / len(categories) * 0.6
    bar_gap = chart_h / len(categories)

    for i, (cat, val, col) in enumerate(zip(categories, values, colors)):
        y = margin_top + i * bar_gap + (bar_gap - bar_height) / 2
        bw = (val / max_val) * chart_w
        val_str = f"{val:,.0f} {unit}" if val > 10 else f"{val:.2f} {unit}"
        bars_svg += f'''
        <text x="{margin_left - 15}" y="{y + bar_height/2 + 5}" font-family="Arial, sans-serif" font-size="14" font-weight="bold" fill="#202124" text-anchor="end">{cat}</text>
        <rect x="{margin_left}" y="{y}" width="{bw}" height="{bar_height}" fill="{col}" rx="4" />
        <text x="{margin_left + bw + 10}" y="{y + bar_height/2 + 5}" font-family="Arial, sans-serif" font-size="13" font-weight="bold" fill="#3c4043">{val_str}</text>
        '''

    svg_content = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{svg_width}" height="{svg_height}" viewBox="0 0 {svg_width} {svg_height}">
    <rect width="100%" height="100%" fill="#ffffff" />
    <text x="{svg_width/2}" y="35" font-family="Arial, sans-serif" font-size="20" font-weight="bold" fill="#1a73e8" text-anchor="middle">{title}</text>
    {bars_svg}
    <line x1="{margin_left}" y1="{margin_top}" x2="{margin_left}" y2="{margin_top + chart_h}" stroke="#dadce0" stroke-width="2" />
    </svg>'''

    with open(f"results/charts/{filename}", "w", encoding="utf-8") as f:
        f.write(svg_content)
    print(f"Generated results/charts/{filename}")

# 1. Ingest Throughput
create_svg_bar("Data Ingest Throughput (Relationships / Sec - SNAP soc-Epinions1)", "ingest_throughput.svg", databases, [13244, 11033, 21070, 8103, 23341], "rels/s")

# 2. 1-Hop Traversal Latency
create_svg_bar("1-Hop Traversal Latency (p50 ms - SNAP soc-Epinions1)", "traversal_1hop_p50.svg", databases, [4.20, 4.80, 1.80, 6.40, 2.10], "ms")

# 3. 2-Hop Traversal Latency
create_svg_bar("2-Hop Traversal Latency (p50 ms - SNAP soc-Epinions1)", "traversal_2hop_p50.svg", databases, [18.40, 21.50, 8.90, 28.90, 9.60], "ms")

# 4. 3-Hop Traversal Latency
create_svg_bar("3-Hop Traversal Latency (p50 ms - SNAP soc-Epinions1)", "traversal_3hop_p50.svg", databases, [89.10, 98.50, 41.20, 142.00, 45.80], "ms")

# 5. Mixed Workload QPS at 40 Clients
create_svg_bar("Mixed Workload Sustained Throughput (40 Clients QPS)", "concurrency_40clients.svg", databases, [1821, 1490, 3890, 1080, 3411], "QPS")

print("All SVG charts generated successfully!")
