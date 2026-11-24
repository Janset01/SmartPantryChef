#!/usr/bin/env python3
"""Download and verify dish-specific Commons photos for bundling in Android APK.
Never replace a dish photo with an unrelated category image.
A failed download is explicitly reported; it does not silently use the wrong photo.
"""
import io,json,os,re,sys,time,unicodedata
from concurrent.futures import ThreadPoolExecutor,as_completed
from pathlib import Path
from urllib.parse import quote, urlencode, unquote
from urllib.request import Request, urlopen
from PIL import Image, ImageOps

ROOT=Path(__file__).resolve().parents[1]
CATALOG=ROOT/"app/src/main/assets/demo_recipes.json"
OUTPUT=ROOT/"app/src/main/assets/demo_photos"
OUTPUT.mkdir(parents=True,exist_ok=True)
UA="SmartPantryChefDemoPhotoAudit/1.0 (https://github.com/Janset01/SmartPantryChef; contact via GitHub issues)"
recipes=json.loads(CATALOG.read_text(encoding="utf-8"))
def fetch(url,timeout=14):
    req=Request(url,headers={"User-Agent":UA,"Accept":"image/jpeg,image/png,image/webp,application/json;q=0.9,*/*;q=0.5"})
    with urlopen(req,timeout=timeout) as response:
        return response.read(8_000_000), response.headers.get("Content-Type","")
def normalize(s):
    s=unicodedata.normalize("NFKD",s).casefold()
    s="".join(c for c in s if not unicodedata.combining(c))
    return re.sub(r"[^a-z0-9 ]+"," ",s).strip()
def commons_url(filename):
    query=urlencode({"action":"query","format":"json","titles":"File:"+filename,"prop":"imageinfo","iiprop":"url|mime","iiurlwidth":"800"})
    raw,_=fetch("https://commons.wikimedia.org/w/api.php?"+query)
    data=json.loads(raw)
    for page in data.get("query",{}).get("pages",{}).values():
        if "missing" not in page and "imageinfo" in page:
            x=page["imageinfo"][0]
            if x.get("mime","").startswith("image/"):
                return x.get("thumburl") or x.get("url")
    return None
def find_by_search(title):
    query=urlencode({"action":"query","format":"json","generator":"search","gsrsearch":title+" filetype:bitmap","gsrnamespace":"6","gsrlimit":"10","prop":"imageinfo","iiprop":"url|mime","iiurlwidth":"800"})
    raw,_=fetch("https://commons.wikimedia.org/w/api.php?"+query)
    data=json.loads(raw)
    want=set(normalize(title).split())
    candidates=[]
    for page in data.get("query",{}).get("pages",{}).values():
        filename=page.get("title","").removeprefix("File:")
        words=set(normalize(filename.replace("_"," ")).split())
        score=len(words & want)/max(1,len(want))
        extra=len(words-want)
        if score>=0.85 and page.get("imageinfo") and page["imageinfo"][0].get("mime","").startswith("image/"):
            candidates.append((score,-extra,page["imageinfo"][0].get("thumburl") or page["imageinfo"][0].get("url")))
    candidates.sort(reverse=True)
    return candidates[0][2] if candidates else None
def get_picture(index, recipe):
    path=OUTPUT/f"{index:03}.jpg"
    if path.exists() and path.stat().st_size>3500:
        return (index,recipe["name"],"cached")
    source=recipe.get("image","")
    candidates=[]
    if "Special:FilePath/" in source:
        filename=unquote(source.split("Special:FilePath/",1)[1].split("?",1)[0])
        candidates.append(("commons:"+filename,lambda:commons_url(filename)))
    elif source.startswith("https://images.unsplash.com/"):
        # Old Unsplash entries are unverified; do not bundle category-level placeholders.
        pass
    # Search Commons for a filename that actually names this precise dish.
    candidates.append(("commons_search",lambda:find_by_search(recipe["name"])))
    for origin,provider in candidates:
        for attempt in range(2):
            try:
                url=provider()
                if not url:break
                data,mime=fetch(url)
                if len(data)<4000:break
                with Image.open(io.BytesIO(data)) as im:
                    im=ImageOps.exif_transpose(im).convert("RGB")
                    if im.width<140 or im.height<140:break
                    im.thumbnail((950,950))
                    im.save(path,"JPEG",quality=78,optimize=True)
                if path.stat().st_size>3500:return (index,recipe["name"],origin)
            except Exception as e:
                if attempt==1: print("WARN",recipe["name"],origin,str(e)[:130],flush=True)
                time.sleep(0.7)
    return (index,recipe["name"],"MISSING")
if __name__=="__main__":
    results=[]
    with ThreadPoolExecutor(max_workers=5) as executor:
        jobs=[executor.submit(get_picture,i,recipe) for i,recipe in enumerate(recipes)]
        for future in as_completed(jobs):
            result=future.result();results.append(result)
            print(result[0],result[1],result[2],flush=True)
    ok=[r for r in results if r[2]!="MISSING"]
    missing=[r for r in results if r[2]=="MISSING"]
    report={"total":len(recipes),"bundled":len(ok),"missing":[r[1] for r in missing]}
    (OUTPUT/"report.json").write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding="utf-8")
    print("PHOTO_AUDIT",json.dumps(report,ensure_ascii=False),flush=True)
