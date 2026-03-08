import urllib.request, json
import ssl
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request("https://eyecarehubdbb-production.up.railway.app/api/v1/categories", headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req, context=ctx) as response:
        print("API:", response.read().decode('utf-8'))
except Exception as e:
    print(e)
