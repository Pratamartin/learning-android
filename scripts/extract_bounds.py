import os
import xml.etree.ElementTree as ET
import json

# ✅ 1️⃣ Detecta o primeiro device conectado via adb
def get_first_device():
    result = os.popen("adb devices").read()
    lines = result.strip().splitlines()
    for line in lines[1:]:
        if line.strip() and not line.startswith("*"):
            return line.split()[0]
    return None

device_serial = get_first_device()
if not device_serial:
    print("Nenhum dispositivo conectado via adb!")
    exit(1)

print(f"📱 Usando o dispositivo: {device_serial}")

# ✅ 2️⃣ Dump da UI (uiautomator)
os.system(f"adb -s {device_serial} shell uiautomator dump /sdcard/dump.xml")
os.system(f"adb -s {device_serial} pull /sdcard/dump.xml dump.xml")

# ✅ 3️⃣ Parseia o XML
tree = ET.parse("dump.xml")
root = tree.getroot()

def get_bounds(node):
    bounds_str = node.attrib.get("bounds", "")
    if bounds_str:
        left_top, right_bottom = bounds_str.strip("[]").split("][")
        left, top = map(int, left_top.split(","))
        right, bottom = map(int, right_bottom.split(","))
        return {"left": left, "top": top, "right": right, "bottom": bottom}
    return {}

hotseat_bounds = None
page_indicator_bounds = None
workspace_bounds = None

# ✅ 4️⃣ Itera e extrai os bounds reais
for node in root.iter("node"):
    res_id = node.attrib.get("resource-id", "")
    if "launcher:id/hotseat" in res_id or "launcher:id/hotseat_container" in res_id:
        hotseat_bounds = get_bounds(node)
    elif "launcher:id/page_indicator" in res_id or "launcher:id/workspace_page_indicator" in res_id:
        page_indicator_bounds = get_bounds(node)
    elif "launcher:id/workspace" in res_id:
        workspace_bounds = get_bounds(node)

# ✅ 5️⃣ Salva em JSON
data = {
    "hotseat_bounds": hotseat_bounds,
    "page_indicator_bounds": page_indicator_bounds,
    "workspace_bounds": workspace_bounds
}

with open("device_bounds.json", "w") as f:
    json.dump(data, f, indent=4)

print("\n🎉 JSON gerado: device_bounds.json")
print(json.dumps(data, indent=4))

# ✅ 6️⃣ Envia pro device
os.system(f"adb -s {device_serial} push device_bounds.json /sdcard/device_bounds.json")
print("📦 JSON enviado para /sdcard/device_bounds.json")

# ✅ 7️⃣ CHAMA O APP Android diretamente!
os.system(f"adb -s {device_serial} shell am start -n com.martinho.wallpapereditor/.MainActivity")
print("🚀 App iniciado automaticamente!")
