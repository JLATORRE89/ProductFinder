import requests

#192.95.12.100
# https://blog.scrapinghub.com/python-requests-proxy
proxies = {
    "https": "https://192.95.12.100"
}

r = requests.get("http://yahoo.com", proxies=proxies)
print(r.content)